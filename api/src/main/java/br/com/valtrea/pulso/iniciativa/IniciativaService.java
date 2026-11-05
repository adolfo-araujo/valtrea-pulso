package br.com.valtrea.pulso.iniciativa;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.valtrea.pulso.bairro.BairroService;
import br.com.valtrea.pulso.config.ValtreaProperties;
import br.com.valtrea.pulso.email.Email;
import br.com.valtrea.pulso.erro.NegocioException;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.AtualizacaoRequest;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.AtualizacaoResponse;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaRequest;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaResponse;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaResumo;
import br.com.valtrea.pulso.usuario.Usuario;
import br.com.valtrea.pulso.usuario.UsuarioRepository;

/**
 * Iniciativas, apoios, atualizações e denúncias. O id de quem pede vem sempre do token. Uma iniciativa oculta responde
 * 404 para todo mundo, menos para o autor, como se não existisse.
 */
@Service
public class IniciativaService {

	/** Com 3 denúncias de pessoas diferentes, a iniciativa some até a administração revisar. */
	static final int DENUNCIAS_PARA_OCULTAR = 3;
	static final int MAX_POR_DIA = 3;
	static final int MAX_ATIVAS_POR_PESSOA = 20;
	static final int MAX_ATUALIZACOES = 50;
	private static final int TAMANHO_LISTA = 200;

	private final IniciativaRepository iniciativas;
	private final ApoioRepository apoios;
	private final AtualizacaoRepository atualizacoes;
	private final DenunciaRepository denuncias;
	private final UsuarioRepository usuarios;
	private final BairroService bairros;
	private final ApplicationEventPublisher eventos;
	private final ValtreaProperties.Site site;

	public IniciativaService(IniciativaRepository iniciativas, ApoioRepository apoios, AtualizacaoRepository atualizacoes,
			DenunciaRepository denuncias, UsuarioRepository usuarios, BairroService bairros,
			ApplicationEventPublisher eventos, ValtreaProperties propriedades) {
		this.iniciativas = iniciativas;
		this.apoios = apoios;
		this.atualizacoes = atualizacoes;
		this.denuncias = denuncias;
		this.usuarios = usuarios;
		this.bairros = bairros;
		this.eventos = eventos;
		this.site = propriedades.site();
	}

	// ---------- leitura (aberta a todos; quem pede pode ser nulo) ----------

	/**
	 * @param fase   nula: abertas e em andamento
	 * @param ordem  "apoios" (mais apoiadas primeiro) ou qualquer outra coisa (mais recentes)
	 */
	@Transactional(readOnly = true)
	public List<IniciativaResumo> doBairro(UUID bairroId, Fase fase, String ordem, UUID quemPede) {
		bairros.existente(bairroId);
		Set<Fase> fases = fase == null ? EnumSet.of(Fase.ABERTA, Fase.EM_ANDAMENTO) : EnumSet.of(fase);
		List<Iniciativa> lista = iniciativas.visiveisDoBairro(bairroId, fases, Limit.of(TAMANHO_LISTA));
		List<IniciativaResumo> resumos = resumos(lista, quemPede);
		if ("apoios".equals(ordem)) {
			return resumos.stream()
					.sorted(Comparator.comparingLong(IniciativaResumo::apoios).reversed()
							.thenComparing(IniciativaResumo::criadoEm, Comparator.reverseOrder()))
					.toList();
		}
		return resumos;
	}

	@Transactional(readOnly = true)
	public IniciativaResponse detalhe(UUID id, UUID quemPede) {
		Iniciativa i = visivelPara(id, quemPede);
		boolean apoiei = quemPede != null && apoios.existsById(new Apoio.Chave(id, quemPede));
		boolean denunciei = quemPede != null && denuncias.existsById(new Denuncia.Chave(id, quemPede));
		List<AtualizacaoResponse> historico = atualizacoes.findByIniciativaIdOrderByCriadoEmAsc(id).stream()
				.map(a -> new AtualizacaoResponse(a.getId(), a.getTexto(), a.getFase(), a.getCriadoEm()))
				.toList();
		return new IniciativaResponse(i.getId(), bairros.resposta(i.getBairro()), i.getTitulo(), i.getDescricao(),
				i.getCategoria(), i.getFase(), i.getAutor().getPrimeiroNome(), apoios.countByIdIniciativaId(id),
				i.getCriadoEm(), i.getAtualizadoEm(), historico, apoiei, denunciei,
				quemPede != null && i.ehDo(quemPede), i.isOculta());
	}

	@Transactional(readOnly = true)
	public List<IniciativaResumo> minhas(UUID autorId) {
		return resumos(iniciativas.findByAutorIdOrderByCriadoEmDesc(autorId), autorId);
	}

	// ---------- publicar ----------

	@Transactional
	public IniciativaResponse criar(UUID autorId, IniciativaRequest pedido) {
		Usuario autor = confirmado(autorId, "publicar");
		if (autor.getBairro() == null) {
			throw new NegocioException(HttpStatus.CONFLICT, "Escolha o seu bairro antes de publicar.");
		}
		if (iniciativas.countByAutorIdAndCriadoEmAfter(autorId, Instant.now().minus(Duration.ofDays(1))) >= MAX_POR_DIA) {
			throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS,
					"Você já publicou " + MAX_POR_DIA + " iniciativas hoje. Tente de novo amanhã.");
		}
		if (iniciativas.contarAtivasDoAutor(autorId) >= MAX_ATIVAS_POR_PESSOA) {
			throw new NegocioException(HttpStatus.CONFLICT, "Você tem " + MAX_ATIVAS_POR_PESSOA
					+ " iniciativas abertas. Marque alguma como resolvida ou arquivada antes de publicar outra.");
		}
		Iniciativa i = iniciativas.save(new Iniciativa(autor.getBairro(), autor, limpar(pedido.titulo()),
				limpar(pedido.descricao()), pedido.categoria()));
		return detalhe(i.getId(), autorId);
	}

	@Transactional
	public IniciativaResponse editar(UUID autorId, UUID id, IniciativaRequest pedido) {
		Iniciativa i = doAutor(autorId, id);
		i.editar(limpar(pedido.titulo()), limpar(pedido.descricao()), pedido.categoria());
		return detalhe(id, autorId);
	}

	@Transactional
	public void excluir(UUID autorId, UUID id) {
		iniciativas.delete(doAutor(autorId, id));
	}

	@Transactional
	public IniciativaResponse atualizar(UUID autorId, UUID id, AtualizacaoRequest pedido) {
		Iniciativa i = doAutor(autorId, id);
		if (atualizacoes.countByIniciativaId(id) >= MAX_ATUALIZACOES) {
			throw new NegocioException(HttpStatus.CONFLICT, "Esta iniciativa chegou ao limite de atualizações.");
		}
		Fase nova = pedido.fase() != null && pedido.fase() != i.getFase() ? pedido.fase() : null;
		if (nova != null) {
			i.mudarFase(nova);
		}
		atualizacoes.save(new Atualizacao(i, limpar(pedido.texto()), nova));
		return detalhe(id, autorId);
	}

	// ---------- apoiar ----------

	@Transactional
	public IniciativaResponse apoiar(UUID usuarioId, UUID id) {
		confirmado(usuarioId, "apoiar");
		Iniciativa i = visivelPara(id, usuarioId);
		if (i.ehDo(usuarioId)) {
			throw new NegocioException(HttpStatus.CONFLICT, "A iniciativa é sua: o seu apoio já está nela.");
		}
		if (!i.getFase().ativa()) {
			throw new NegocioException(HttpStatus.CONFLICT, "Esta iniciativa já foi resolvida ou arquivada.");
		}
		Apoio.Chave chave = new Apoio.Chave(id, usuarioId);
		if (!apoios.existsById(chave)) {
			apoios.save(new Apoio(id, usuarioId));
		}
		return detalhe(id, usuarioId);
	}

	@Transactional
	public IniciativaResponse retirarApoio(UUID usuarioId, UUID id) {
		visivelPara(id, usuarioId);
		apoios.deleteById(new Apoio.Chave(id, usuarioId));
		return detalhe(id, usuarioId);
	}

	// ---------- denunciar ----------

	@Transactional
	public void denunciar(UUID usuarioId, UUID id, MotivoDenuncia motivo) {
		confirmado(usuarioId, "denunciar");
		Iniciativa i = visivelPara(id, usuarioId);
		if (i.ehDo(usuarioId)) {
			throw new NegocioException(HttpStatus.CONFLICT, "Você não pode denunciar a sua própria iniciativa.");
		}
		Denuncia.Chave chave = new Denuncia.Chave(id, usuarioId);
		if (denuncias.existsById(chave)) {
			return;
		}
		denuncias.saveAndFlush(new Denuncia(id, usuarioId, motivo));
		if (!i.isOculta() && !i.isRevisada() && denuncias.countByIdIniciativaId(id) >= DENUNCIAS_PARA_OCULTAR) {
			i.ocultar();
			Usuario autor = i.getAutor();
			eventos.publishEvent(Email.iniciativaOculta(autor.getEmail(), autor.getNome(), i.getTitulo(),
					site.link("/minhas")));
		}
	}

	// ---------- auxiliares ----------

	private List<IniciativaResumo> resumos(List<Iniciativa> lista, UUID quemPede) {
		if (lista.isEmpty()) {
			return List.of();
		}
		List<UUID> ids = lista.stream().map(Iniciativa::getId).toList();
		Map<UUID, Long> contagem = new HashMap<>();
		for (Object[] linha : apoios.contar(ids)) {
			contagem.put((UUID) linha[0], (Long) linha[1]);
		}
		Set<UUID> apoiadas = quemPede == null ? Set.of() : new HashSet<>(apoios.apoiadasPor(quemPede, ids));
		return lista.stream()
				.map(i -> new IniciativaResumo(i.getId(), i.getBairro().getId(), i.getTitulo(), i.getCategoria(),
						i.getFase(), i.getAutor().getPrimeiroNome(), contagem.getOrDefault(i.getId(), 0L),
						i.getCriadoEm(), i.getAtualizadoEm(), apoiadas.contains(i.getId()),
						quemPede != null && i.ehDo(quemPede), i.isOculta()))
				.toList();
	}

	private Iniciativa visivelPara(UUID id, UUID quemPede) {
		return iniciativas.findById(id)
				.filter(i -> !i.isOculta() || (quemPede != null && i.ehDo(quemPede)))
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Iniciativa não encontrada."));
	}

	private Iniciativa doAutor(UUID autorId, UUID id) {
		return iniciativas.findById(id)
				.filter(i -> i.ehDo(autorId))
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Iniciativa não encontrada."));
	}

	private Usuario confirmado(UUID usuarioId, String acao) {
		Usuario u = usuarios.findById(usuarioId)
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
		if (!u.isEmailConfirmado()) {
			throw new NegocioException(HttpStatus.FORBIDDEN, "Confirme seu e-mail antes de " + acao + ".");
		}
		return u;
	}

	private static String limpar(String texto) {
		return texto.strip().replaceAll("[\\p{Cc}&&[^\\n]]", "").replaceAll("\\n{3,}", "\n\n");
	}

}
