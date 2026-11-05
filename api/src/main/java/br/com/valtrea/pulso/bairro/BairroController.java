package br.com.valtrea.pulso.bairro;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.pulso.bairro.BairroDtos.BairroRequest;
import br.com.valtrea.pulso.bairro.BairroDtos.BairroResponse;
import br.com.valtrea.pulso.erro.NegocioException;
import br.com.valtrea.pulso.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/bairros")
@Tag(name = "Bairros")
public class BairroController {

	private final BairroService bairros;
	private final UsuarioRepository usuarios;

	public BairroController(BairroService bairros, UsuarioRepository usuarios) {
		this.bairros = bairros;
		this.usuarios = usuarios;
	}

	@PostMapping
	@Operation(summary = "Acha o bairro pelo nome ou, se ainda não existir, cria (e-mail confirmado)")
	public BairroResponse encontrarOuCriar(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid BairroRequest pedido) {
		boolean confirmado = usuarios.findById(UUID.fromString(jwt.getSubject()))
				.map(u -> u.isEmailConfirmado()).orElse(false);
		if (!confirmado) {
			throw new NegocioException(HttpStatus.FORBIDDEN, "Confirme seu e-mail antes de criar um bairro.");
		}
		return bairros.resposta(bairros.encontrarOuCriar(pedido));
	}

}
