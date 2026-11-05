package br.com.valtrea.pulso.bairro;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class BairroDtos {

	private BairroDtos() {
	}

	public record BairroRequest(
			@NotBlank @Size(min = 2, max = 80) String nome,
			@NotBlank @Size(min = 2, max = 80) String cidade,
			@NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "use a sigla do estado, como SP") String uf) {
	}

	/** @param iniciativas quantas iniciativas abertas ou em andamento o bairro tem */
	public record BairroResponse(UUID id, String nome, String cidade, String uf, long iniciativas) {
	}

	public record MudarBairroRequest(UUID bairroId) {
	}

}
