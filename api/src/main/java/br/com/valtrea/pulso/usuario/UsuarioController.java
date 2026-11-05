package br.com.valtrea.pulso.usuario;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.pulso.admin.Administradores;
import br.com.valtrea.pulso.erro.NegocioException;
import br.com.valtrea.pulso.bairro.BairroDtos.BairroResponse;
import br.com.valtrea.pulso.bairro.BairroDtos.MudarBairroRequest;
import br.com.valtrea.pulso.bairro.BairroService;
import br.com.valtrea.pulso.iniciativa.ApoioRepository;
import br.com.valtrea.pulso.iniciativa.DenunciaRepository;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaResponse;
import br.com.valtrea.pulso.iniciativa.IniciativaService;
import br.com.valtrea.pulso.iniciativa.MotivoDenuncia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Usuários")
public class UsuarioController {

	/**
	 * @param termosAceitos falso quando a pessoa ainda não aceitou a versão vigente dos Termos (o app mostra o aviso)
	 * @param admin         se o app deve mostrar o painel de administração
	 * @param bairro        nulo até a pessoa escolher
	 */
	public record PerfilResponse(UUID id, String nome, String email, boolean emailConfirmado, boolean termosAceitos,
			boolean admin, BairroResponse bairro) {
	}

	public record AtualizarPerfilRequest(@NotBlank @Size(max = 120) String nome) {
	}

	public record AceitarTermosRequest(@NotBlank String versao) {
	}

	public record ExcluirContaRequest(@NotBlank String senha) {
	}

	/** "Baixar meus dados" (LGPD art. 18, II e V). Nunca inclui a senha nem códigos. */
	public record MeusDadosResponse(Instant exportadoEm, String servico, DadosConta conta,
			List<IniciativaResponse> iniciativas, List<ApoioDado> apoios, List<DenunciaDado> denuncias) {
	}

	public record DadosConta(UUID id, String nome, String email, boolean emailConfirmado, Instant criadoEm,
			String termosVersao, Instant termosAceitosEm, BairroResponse bairro) {
	}

	public record ApoioDado(UUID iniciativaId, Instant em) {
	}

	public record DenunciaDado(UUID iniciativaId, MotivoDenuncia motivo) {
	}

	private final UsuarioRepository usuarios;
	private final ExclusaoContaService exclusao;
	private final IniciativaService iniciativas;
	private final BairroService bairros;
	private final ApoioRepository apoios;
	private final DenunciaRepository denuncias;
	private final Administradores administradores;

	public UsuarioController(UsuarioRepository usuarios, ExclusaoContaService exclusao, IniciativaService iniciativas,
			BairroService bairros, ApoioRepository apoios, DenunciaRepository denuncias,
			Administradores administradores) {
		this.usuarios = usuarios;
		this.exclusao = exclusao;
		this.iniciativas = iniciativas;
		this.bairros = bairros;
		this.apoios = apoios;
		this.denuncias = denuncias;
		this.administradores = administradores;
	}

	@GetMapping("/eu")
	@Transactional(readOnly = true)
	@Operation(summary = "Perfil de quem está logado")
	public PerfilResponse eu(@AuthenticationPrincipal Jwt jwt) {
		return perfil(usuarioDo(jwt));
	}

	@PatchMapping("/eu")
	@Transactional
	@Operation(summary = "Troca o nome")
	public PerfilResponse atualizar(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid AtualizarPerfilRequest pedido) {
		Usuario usuario = usuarioDo(jwt);
		usuario.atualizarNome(pedido.nome().strip());
		return perfil(usuario);
	}

	@PutMapping("/eu/bairro")
	@Transactional
	@Operation(summary = "Muda o seu bairro (as iniciativas que você já publicou continuam no bairro de antes)")
	public PerfilResponse mudarBairro(@AuthenticationPrincipal Jwt jwt, @RequestBody MudarBairroRequest pedido) {
		Usuario usuario = usuarioDo(jwt);
		usuario.mudarBairro(pedido.bairroId() == null ? null : bairros.existente(pedido.bairroId()));
		return perfil(usuario);
	}

	@GetMapping("/eu/dados")
	@Transactional(readOnly = true)
	@Operation(summary = "Baixar meus dados (LGPD): cadastro, bairro, iniciativas, apoios e denúncias")
	public ResponseEntity<MeusDadosResponse> meusDados(@AuthenticationPrincipal Jwt jwt) {
		Usuario u = usuarioDo(jwt);
		DadosConta conta = new DadosConta(u.getId(), u.getNome(), u.getEmail(), u.isEmailConfirmado(),
				u.getCriadoEm(), u.getTermosVersao(), u.getTermosAceitosEm(),
				u.getBairro() == null ? null : bairros.resposta(u.getBairro()));
		List<IniciativaResponse> lista = iniciativas.minhas(u.getId()).stream()
				.map(r -> iniciativas.detalhe(r.id(), u.getId()))
				.toList();
		List<ApoioDado> meusApoios = apoios.findByIdUsuarioId(u.getId()).stream()
				.map(a -> new ApoioDado(a.getId().getIniciativaId(), a.getCriadoEm()))
				.toList();
		List<DenunciaDado> minhasDenuncias = denuncias.findByIdUsuarioId(u.getId()).stream()
				.map(d -> new DenunciaDado(d.getId().getIniciativaId(), d.getMotivo()))
				.toList();
		Instant agora = Instant.now();
		MeusDadosResponse corpo = new MeusDadosResponse(agora, "Valtrea Pulso (https://pulso.valtrea.com.br)", conta,
				lista, meusApoios, minhasDenuncias);
		String nomeArquivo = "valtrea-pulso-meus-dados-" + agora.toString().substring(0, 10) + ".json";
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
				.body(corpo);
	}

	@PostMapping("/eu/termos")
	@Transactional
	@Operation(summary = "Registra o aceite da versão vigente dos Termos de uso e da Política de privacidade")
	public PerfilResponse aceitarTermos(@AuthenticationPrincipal Jwt jwt,
			@RequestBody @Valid AceitarTermosRequest pedido) {
		if (!Termos.VERSAO.equals(pedido.versao())) {
			throw new NegocioException(HttpStatus.BAD_REQUEST, "Versão dos termos desatualizada. Atualize o app.");
		}
		Usuario usuario = usuarioDo(jwt);
		usuario.aceitarTermos(Termos.VERSAO);
		return perfil(usuario);
	}

	@DeleteMapping("/eu")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exclui a conta e tudo dela (iniciativas, apoios e denúncias). Pede a senha")
	public void excluir(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid ExcluirContaRequest pedido) {
		exclusao.excluir(UUID.fromString(jwt.getSubject()), pedido.senha());
	}

	private Usuario usuarioDo(Jwt jwt) {
		return usuarios.findById(UUID.fromString(jwt.getSubject()))
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
	}

	private PerfilResponse perfil(Usuario u) {
		return new PerfilResponse(u.getId(), u.getNome(), u.getEmail(), u.isEmailConfirmado(),
				Termos.VERSAO.equals(u.getTermosVersao()), administradores.ehAdmin(u),
				u.getBairro() == null ? null : bairros.resposta(u.getBairro()));
	}

}
