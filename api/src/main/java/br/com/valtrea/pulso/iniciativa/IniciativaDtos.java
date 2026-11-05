package br.com.valtrea.pulso.iniciativa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.valtrea.pulso.bairro.BairroDtos.BairroResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class IniciativaDtos {

	private IniciativaDtos() {
	}

	public record IniciativaRequest(
			@NotBlank @Size(min = 8, max = 120, message = "escreva um título entre 8 e 120 caracteres") String titulo,
			@NotBlank @Size(min = 20, max = 2000, message = "explique em 20 a 2000 caracteres") String descricao,
			@NotNull Categoria categoria) {
	}

	public record AtualizacaoRequest(
			@NotBlank @Size(min = 5, max = 1000, message = "escreva entre 5 e 1000 caracteres") String texto,
			Fase fase) {
	}

	public record DenunciaRequest(@NotNull MotivoDenuncia motivo) {
	}

	/**
	 * Resumo para as listas. {@code autor} é só o primeiro nome.
	 *
	 * @param apoiei   se quem pediu (logado) já apoiou; sempre falso para quem não entrou
	 * @param minha    se quem pediu é o autor
	 * @param oculta   só aparece verdadeiro para o autor, nas listas dele
	 */
	public record IniciativaResumo(UUID id, UUID bairroId, String titulo, Categoria categoria, Fase fase, String autor,
			long apoios, Instant criadoEm, Instant atualizadoEm, boolean apoiei, boolean minha, boolean oculta) {
	}

	public record AtualizacaoResponse(UUID id, String texto, Fase fase, Instant criadoEm) {
	}

	public record IniciativaResponse(UUID id, BairroResponse bairro, String titulo, String descricao, Categoria categoria,
			Fase fase, String autor, long apoios, Instant criadoEm, Instant atualizadoEm,
			List<AtualizacaoResponse> atualizacoes, boolean apoiei, boolean denunciei, boolean minha, boolean oculta) {
	}

}
