package br.com.valtrea.pulso.admin;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import br.com.valtrea.pulso.erro.NegocioException;
import br.com.valtrea.pulso.usuario.Papel;
import br.com.valtrea.pulso.usuario.Usuario;
import br.com.valtrea.pulso.usuario.UsuarioRepository;

/**
 * Quem pode ver o painel de administração: contas com papel ADMINISTRADOR no banco ou com o e-mail listado em
 * VALTREA_ADMIN_EMAILS (separados por vírgula), como no Valtrea Finance e no Pay. A conta precisa ter o e-mail confirmado.
 */
@Component
public class Administradores {

	private final Set<String> emails;

	public Administradores(@Value("${valtrea.admin-emails:}") String lista) {
		this.emails = Arrays.stream(lista.split(","))
				.map(e -> e.strip().toLowerCase(Locale.ROOT))
				.filter(e -> !e.isEmpty())
				.collect(Collectors.toUnmodifiableSet());
	}

	public boolean ehAdmin(Usuario usuario) {
		if (usuario == null || !usuario.isEmailConfirmado()) {
			return false;
		}
		return usuario.getPapel() == Papel.ADMINISTRADOR
				|| emails.contains(usuario.getEmail().toLowerCase(Locale.ROOT));
	}

	/** Para as rotas da administração: 403 para quem não é. */
	public Usuario exigir(UsuarioRepository usuarios, Jwt jwt) {
		return usuarios.findById(UUID.fromString(jwt.getSubject())).filter(this::ehAdmin)
				.orElseThrow(() -> new NegocioException(HttpStatus.FORBIDDEN, "Acesso restrito à administração."));
	}

}
