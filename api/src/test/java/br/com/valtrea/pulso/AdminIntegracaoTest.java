package br.com.valtrea.pulso;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/** Painel de administração: só para administradores e só com números agregados. */
class AdminIntegracaoTest extends IntegracaoBase {

	@Autowired
	JdbcTemplate jdbc;

	record PerfilAdmin(UUID id, boolean admin) {
	}

	private PerfilAdmin perfilAdmin(String token) {
		return http.get().uri("/api/v1/eu").header("Authorization", "Bearer " + token).retrieve()
				.body(PerfilAdmin.class);
	}

	private ResponseEntity<String> metricas(String token) {
		return http.get().uri("/api/v1/admin/metricas").header("Authorization", "Bearer " + token).retrieve()
				.toEntity(String.class);
	}

	@Test
	void pessoaComumNaoVeOPainel() {
		String comum = cadastrarConfirmado("Pessoa Comum", emailUnico("comum"));
		assertThat(perfilAdmin(comum).admin()).isFalse();
		assertThat(metricas(comum).getStatusCode().value()).isEqualTo(403);
		assertThat(http.get().uri("/api/v1/admin/metricas").retrieve().toBodilessEntity().getStatusCode().value())
				.isEqualTo(401);
	}

	@Test
	void emailDaListaVeOPainelDepoisDeConfirmar() {
		String token = cadastrar("Chefe", "chefe-pulso@exemplo.com").accessToken();
		assertThat(perfilAdmin(token).admin()).as("sem e-mail confirmado ainda não é admin").isFalse();
		assertThat(confirmarEmail(token, emails.ultimoCodigo("chefe-pulso@exemplo.com")).getStatusCode().value())
				.isEqualTo(204);
		assertThat(perfilAdmin(token).admin()).isTrue();
		assertThat(metricas(token).getStatusCode().value()).isEqualTo(200);
	}

	@Test
	void papelAdministradorMostraNumerosAgregadosSemDadosPessoais() {
		String emailAdmin = emailUnico("admin");
		String admin = cadastrarConfirmado("Ana Admin", emailAdmin);
		jdbc.update("UPDATE usuarios SET papel = 'ADMINISTRADOR' WHERE email = ?", emailAdmin);

		String bia = cadastrarConfirmado("Bia Moradora", emailUnico("bia"));
		Bairro bairro = bairroNovo(bia);
		morarEm(bia, bairro.id());
		Iniciativa praca = criarIniciativa(bia, "Reforma da praça Azul");
		String caio = morador("Caio Vizinho", bairro.id());
		apoiar(caio, praca.id());

		ResponseEntity<String> resposta = metricas(admin);
		assertThat(resposta.getStatusCode().value()).isEqualTo(200);
		assertThat(resposta.getBody()).contains("\"contas\"", "\"uso\"", "\"cadastrosPorSemana\"");
		assertThat(resposta.getBody()).doesNotContain("Bia", "praça Azul", bairro.nome(), "@exemplo.com");

		MetricasLidas m = http.get().uri("/api/v1/admin/metricas").header("Authorization", "Bearer " + admin)
				.retrieve().body(MetricasLidas.class);
		assertThat(m.contas().confirmadas()).isGreaterThanOrEqualTo(2);
		assertThat(m.uso().iniciativas()).isGreaterThanOrEqualTo(1);
		assertThat(m.uso().apoios30Dias()).isGreaterThanOrEqualTo(1);
		assertThat(m.cadastrosPorSemana()).hasSize(8);
	}

	record MetricasLidas(ContasLidas contas, UsoLido uso, java.util.List<Object> cadastrosPorSemana) {
	}

	record ContasLidas(long total, long confirmadas) {
	}

	record UsoLido(long bairros, long iniciativas, long apoios30Dias, long ocultas) {
	}

}
