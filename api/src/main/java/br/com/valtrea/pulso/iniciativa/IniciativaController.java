package br.com.valtrea.pulso.iniciativa;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.pulso.iniciativa.IniciativaDtos.AtualizacaoRequest;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.DenunciaRequest;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaRequest;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaResponse;
import br.com.valtrea.pulso.iniciativa.IniciativaDtos.IniciativaResumo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** O que precisa de conta. As mesmas leituras, sem conta, ficam em {@link PublicoController}. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Iniciativas")
public class IniciativaController {

	private final IniciativaService servico;

	public IniciativaController(IniciativaService servico) {
		this.servico = servico;
	}

	@GetMapping("/bairros/{bairroId}/iniciativas")
	@Operation(summary = "Iniciativas do bairro, dizendo quais você apoiou")
	public List<IniciativaResumo> doBairro(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID bairroId,
			@RequestParam(required = false) Fase fase, @RequestParam(required = false) String ordem) {
		return servico.doBairro(bairroId, fase, ordem, id(jwt));
	}

	@GetMapping("/eu/iniciativas")
	@Operation(summary = "As iniciativas que você publicou (inclusive as ocultas por denúncia)")
	public List<IniciativaResumo> minhas(@AuthenticationPrincipal Jwt jwt) {
		return servico.minhas(id(jwt));
	}

	@GetMapping("/iniciativas/{id}")
	@Operation(summary = "Uma iniciativa, dizendo se você apoiou ou denunciou")
	public IniciativaResponse detalhe(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return servico.detalhe(id, id(jwt));
	}

	@PostMapping("/iniciativas")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Publica uma iniciativa no seu bairro (e-mail confirmado)")
	public IniciativaResponse criar(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid IniciativaRequest pedido) {
		return servico.criar(id(jwt), pedido);
	}

	@PutMapping("/iniciativas/{id}")
	@Operation(summary = "Edita o título, a descrição e a categoria (só o autor)")
	public IniciativaResponse editar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody @Valid IniciativaRequest pedido) {
		return servico.editar(id(jwt), id, pedido);
	}

	@DeleteMapping("/iniciativas/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Apaga a iniciativa, os apoios e as atualizações (só o autor)")
	public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		servico.excluir(id(jwt), id);
	}

	@PostMapping("/iniciativas/{id}/atualizacoes")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Conta o que aconteceu e, se quiser, muda a fase (só o autor)")
	public IniciativaResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody @Valid AtualizacaoRequest pedido) {
		return servico.atualizar(id(jwt), id, pedido);
	}

	@PostMapping("/iniciativas/{id}/apoio")
	@Operation(summary = "Apoia a iniciativa (um apoio por pessoa)")
	public IniciativaResponse apoiar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return servico.apoiar(id(jwt), id);
	}

	@DeleteMapping("/iniciativas/{id}/apoio")
	@Operation(summary = "Retira o seu apoio")
	public IniciativaResponse retirarApoio(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return servico.retirarApoio(id(jwt), id);
	}

	@PostMapping("/iniciativas/{id}/denuncia")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Denuncia a iniciativa. Com 3 denúncias, ela some até a revisão")
	public void denunciar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody @Valid DenunciaRequest pedido) {
		servico.denunciar(id(jwt), id, pedido.motivo());
	}

	private static UUID id(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}

}
