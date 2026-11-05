package br.com.valtrea.pulso;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.valtrea.pulso.retencao.RetencaoService;
import br.com.valtrea.pulso.usuario.Termos;

/** Senha forte, "Baixar meus dados", aceite dos Termos, exclusão de conta e rotina de retenção. */
class LgpdIntegracaoTest extends IntegracaoBase {

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	RetencaoService retencao;

	private ResponseEntity<Void> excluir(String token, String senha) {
		return http.method(HttpMethod.DELETE).uri("/api/v1/eu").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).body(Map.of("senha", senha)).retrieve().toBodilessEntity();
	}

	private boolean existe(String tabela, UUID id) {
		return jdbc.queryForObject("SELECT count(*) FROM " + tabela + " WHERE id = ?", Integer.class, id) == 1;
	}

	@Test
	void cadastroPedeSoNomeEmailESenhaComLetrasENumeros() {
		var semNumero = http.post().uri("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("nome", "Senha Fraca", "email", emailUnico("fraca"), "senha", "somenteletras")).retrieve()
				.toBodilessEntity();
		assertThat(semNumero.getStatusCode().value()).isEqualTo(400);

		String email = emailUnico("dupla");
		cadastrar("Primeira", email);
		var repetido = http.post().uri("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("nome", "Segunda", "email", email.toUpperCase(), "senha", "senha-forte-123")).retrieve()
				.toBodilessEntity();
		assertThat(repetido.getStatusCode().value()).isEqualTo(409);
	}

	@Test
	void baixarMeusDadosTrazTudoMenosSenhaECodigos() {
		String email = emailUnico("olga");
		String olga = cadastrarConfirmado("Olga Dados", email);
		Bairro bairro = bairroNovo(olga);
		morarEm(olga, bairro.id());
		Iniciativa minha = criarIniciativa(olga, "Faixa de pedestre na escola");
		String vizinho = morador("Paulo Vizinho", bairro.id());
		Iniciativa dele = criarIniciativa(vizinho, "Lixeiras na praça central");
		apoiar(olga, dele.id());

		ResponseEntity<String> resposta = http.get().uri("/api/v1/eu/dados").header("Authorization", "Bearer " + olga)
				.retrieve().toEntity(String.class);
		assertThat(resposta.getStatusCode().value()).isEqualTo(200);
		assertThat(resposta.getHeaders().getFirst("Content-Disposition")).contains("valtrea-pulso-meus-dados-");
		assertThat(resposta.getBody()).contains(email, bairro.nome(), "Faixa de pedestre na escola", minha.id().toString(),
				dele.id().toString(), "termosVersao");
		assertThat(resposta.getBody()).doesNotContain("senha", "$2a$", "codigo_hash", "token_hash");
	}

	@Test
	void contaNovaJaAceitaOsTermosEAntigaPrecisaAceitar() {
		String token = cadastrar("Quela Termos", emailUnico("quela")).accessToken();
		Perfil perfil = perfil(token);
		assertThat(perfil.termosAceitos()).isTrue();

		jdbc.update("UPDATE usuarios SET termos_versao = NULL, termos_aceitos_em = NULL WHERE id = ?", perfil.id());
		assertThat(perfil(token).termosAceitos()).isFalse();
		assertThat(post("/api/v1/eu/termos", token, Map.of("versao", "2000-01-01")).getStatusCode().value())
				.isEqualTo(400);
		assertThat(post("/api/v1/eu/termos", token, Map.of("versao", Termos.VERSAO)).getStatusCode().value())
				.isEqualTo(200);
		assertThat(perfil(token).termosAceitos()).isTrue();
	}

	@Test
	void excluirContaApagaTudoNaHora() {
		String email = emailUnico("rita");
		String rita = cadastrarConfirmado("Rita Sai", email);
		UUID idRita = perfil(rita).id();
		Bairro bairro = bairroNovo(rita);
		morarEm(rita, bairro.id());
		Iniciativa minha = criarIniciativa(rita, "Poda das árvores da rua");
		String vizinho = morador("Saulo Vizinho", bairro.id());
		Iniciativa dele = criarIniciativa(vizinho, "Semáforo no cruzamento");
		apoiar(rita, dele.id());
		apoiar(vizinho, minha.id());

		assertThat(excluir(rita, "senha-errada-1").getStatusCode().value()).isEqualTo(403);
		assertThat(excluir(rita, "senha-forte-123").getStatusCode().value()).isEqualTo(204);

		assertThat(existe("usuarios", idRita)).isFalse();
		assertThat(existe("iniciativas", minha.id())).isFalse();
		assertThat(jdbc.queryForObject("SELECT count(*) FROM apoios WHERE usuario_id = ? OR iniciativa_id = ?",
				Integer.class, idRita, minha.id())).isZero();
		assertThat(publico("/iniciativas/" + minha.id()).getStatusCode().value()).isEqualTo(404);
		// A iniciativa do vizinho continua, só sem o apoio dela.
		assertThat(get("/api/v1/iniciativas/" + dele.id(), vizinho, Iniciativa.class).apoios()).isZero();
		assertThat(emails.para(email).getLast().getSubject()).contains("excluída");
		assertThat(login(email, "senha-forte-123").getStatusCode().value()).isEqualTo(401);
	}

	@Test
	void retencaoApagaSoOQueNaoPrecisaMaisSerGuardado() {
		UUID parado = perfil(cadastrar("Rui Parado", emailUnico("rui")).accessToken()).id();
		UUID recente = perfil(cadastrar("Tais Recente", emailUnico("tais")).accessToken()).id();
		String vera = cadastrarConfirmado("Vera Antiga", emailUnico("vera"));
		UUID confirmadaAntiga = perfil(vera).id();
		jdbc.update("UPDATE usuarios SET criado_em = now() - interval '31 days' WHERE id IN (?, ?)", parado,
				confirmadaAntiga);

		Bairro bairro = bairroNovo(vera);
		morarEm(vera, bairro.id());
		Iniciativa velha = criarIniciativa(vera, "Buraco na rua de cima");
		Iniciativa nova = criarIniciativa(vera, "Buraco na rua de baixo");
		jdbc.update("UPDATE iniciativas SET oculta = true, oculta_em = now() - interval '61 days' WHERE id = ?",
				velha.id());
		jdbc.update("UPDATE iniciativas SET oculta = true, oculta_em = now() - interval '10 days' WHERE id = ?",
				nova.id());

		Bairro vazio = bairroNovo(vera);
		Bairro vazioRecente = bairroNovo(vera);
		jdbc.update("UPDATE bairros SET criado_em = now() - interval '31 days' WHERE id IN (?, ?)", vazio.id(),
				bairro.id());

		retencao.executar(Instant.now());

		assertThat(existe("usuarios", parado)).isFalse();
		assertThat(existe("usuarios", recente)).isTrue();
		assertThat(existe("usuarios", confirmadaAntiga)).isTrue();
		assertThat(existe("iniciativas", velha.id())).isFalse();
		assertThat(existe("iniciativas", nova.id())).isTrue();
		assertThat(existe("bairros", vazio.id())).isFalse();
		assertThat(existe("bairros", vazioRecente.id())).isTrue();
		assertThat(existe("bairros", bairro.id())).isTrue();
	}

}
