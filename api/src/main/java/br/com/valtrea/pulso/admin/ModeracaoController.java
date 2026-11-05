package br.com.valtrea.pulso.admin;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.pulso.email.Email;
import br.com.valtrea.pulso.erro.NegocioException;
import br.com.valtrea.pulso.iniciativa.Categoria;
import br.com.valtrea.pulso.iniciativa.Denuncia;
import br.com.valtrea.pulso.iniciativa.DenunciaRepository;
import br.com.valtrea.pulso.iniciativa.Iniciativa;
import br.com.valtrea.pulso.iniciativa.IniciativaRepository;
import br.com.valtrea.pulso.iniciativa.MotivoDenuncia;
import br.com.valtrea.pulso.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Iniciativas ocultas por denúncia, esperando decisão. A administração vê o texto (que já era público), o primeiro nome
 * do autor e quantas denúncias de cada motivo, nunca quem denunciou nem o e-mail do autor.
 */
@RestController
@RequestMapping("/api/v1/admin/moderacao")
@Tag(name = "Administração")
public class ModeracaoController {

	public record ParaRevisar(UUID id, String titulo, String descricao, Categoria categoria, String bairro,
			String autor, Instant criadoEm, Instant ocultaEm, Map<MotivoDenuncia, Long> denuncias) {
	}

	private final IniciativaRepository iniciativas;
	private final DenunciaRepository denuncias;
	private final UsuarioRepository usuarios;
	private final Administradores administradores;
	private final ApplicationEventPublisher eventos;

	public ModeracaoController(IniciativaRepository iniciativas, DenunciaRepository denuncias,
			UsuarioRepository usuarios, Administradores administradores, ApplicationEventPublisher eventos) {
		this.iniciativas = iniciativas;
		this.denuncias = denuncias;
		this.usuarios = usuarios;
		this.administradores = administradores;
		this.eventos = eventos;
	}

	@GetMapping
	@Transactional(readOnly = true)
	@Operation(summary = "Iniciativas ocultas por denúncia, das mais antigas para as mais novas")
	public List<ParaRevisar> listar(@AuthenticationPrincipal Jwt jwt) {
		administradores.exigir(usuarios, jwt);
		return iniciativas.findByOcultaTrueOrderByOcultaEmAsc().stream().map(this::paraRevisar).toList();
	}

	@PostMapping("/{id}/manter")
	@Transactional
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "A iniciativa segue os Termos: volta a aparecer e não some mais sozinha")
	public void manter(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		administradores.exigir(usuarios, jwt);
		Iniciativa i = oculta(id);
		i.manter();
		denuncias.apagarDa(id);
	}

	@DeleteMapping("/{id}")
	@Transactional
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove a iniciativa (avisa o autor por e-mail)")
	public void remover(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		administradores.exigir(usuarios, jwt);
		Iniciativa i = oculta(id);
		Email aviso = Email.iniciativaRemovida(i.getAutor().getEmail(), i.getAutor().getNome(), i.getTitulo());
		iniciativas.delete(i);
		eventos.publishEvent(aviso);
	}

	private Iniciativa oculta(UUID id) {
		return iniciativas.findById(id).filter(Iniciativa::isOculta)
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Nada para revisar com este id."));
	}

	private ParaRevisar paraRevisar(Iniciativa i) {
		Map<MotivoDenuncia, Long> motivos = new EnumMap<>(MotivoDenuncia.class);
		for (Denuncia d : denuncias.findByIdIniciativaId(i.getId())) {
			motivos.merge(d.getMotivo(), 1L, Long::sum);
		}
		String bairro = i.getBairro().getNome() + ", " + i.getBairro().getCidade() + " - " + i.getBairro().getUf();
		return new ParaRevisar(i.getId(), i.getTitulo(), i.getDescricao(), i.getCategoria(), bairro,
				i.getAutor().getPrimeiroNome(), i.getCriadoEm(), i.getOcultaEm(), motivos);
	}

}
