package br.com.valtrea.pulso;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/** O fluxo principal: escolher o bairro, publicar, os vizinhos apoiam, o autor acompanha; e a moderação. */
class PulsoIntegracaoTest extends IntegracaoBase {

	@Autowired
	JdbcTemplate jdbc;

	private List<Resumo> doBairro(UUID bairro, String token, String filtros) {
		var pedido = http.get().uri((token == null ? "/api/v1/publico" : "/api/v1") + "/bairros/" + bairro
				+ "/iniciativas" + filtros);
		if (token != null) {
			pedido = pedido.header("Authorization", "Bearer " + token);
		}
		return pedido.retrieve().body(new ParameterizedTypeReference<List<Resumo>>() {
		});
	}

	@Test
	void bairroComOMesmoNomeNaoRepete() {
		String ana = cadastrarConfirmado("Ana Souza", emailUnico("ana"));
		String nome = "Vila Ásia " + UUID.randomUUID().toString().substring(0, 6);
		Bairro primeiro = http.post().uri("/api/v1/bairros").header("Authorization", "Bearer " + ana)
				.contentType(MediaType.APPLICATION_JSON).body(Map.of("nome", nome, "cidade", "São Paulo", "uf", "sp"))
				.retrieve().body(Bairro.class);
		Bairro mesmo = http.post().uri("/api/v1/bairros").header("Authorization", "Bearer " + ana)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("nome", "  " + nome.toLowerCase().replace("á", "a") + " ", "cidade", "sao paulo", "uf", "SP"))
				.retrieve().body(Bairro.class);
		assertThat(mesmo.id()).isEqualTo(primeiro.id());
		assertThat(primeiro.uf()).isEqualTo("SP");

		// A busca aberta acha sem acento.
		ResponseEntity<String> busca = publico("/bairros?uf=SP&busca=" + nome.toLowerCase().replace("á", "a"));
		assertThat(busca.getBody()).contains(primeiro.id().toString());

		ResponseEntity<String> ufErrada = http.post().uri("/api/v1/bairros").header("Authorization", "Bearer " + ana)
				.contentType(MediaType.APPLICATION_JSON).body(Map.of("nome", "Centro", "cidade", "Lugar", "uf", "XX"))
				.retrieve().toEntity(String.class);
		assertThat(ufErrada.getStatusCode().value()).isEqualTo(400);
	}

	@Test
	void vizinhosApoiamEOAutorAcompanhaSemExporNinguem() {
		String emailAna = emailUnico("ana");
		String ana = cadastrarConfirmado("Ana Maria Souza", emailAna);
		Bairro bairro = bairroNovo(ana);
		morarEm(ana, bairro.id());
		assertThat(perfil(ana).bairro().id()).isEqualTo(bairro.id());

		Iniciativa poste = criarIniciativa(ana, "Trocar a lâmpada do poste da esquina");
		assertThat(poste.fase()).isEqualTo("ABERTA");
		assertThat(poste.autor()).isEqualTo("Ana");
		assertThat(poste.minha()).isTrue();

		String bruno = morador("Bruno Lima", bairro.id());
		String carla = morador("Carla Dias", bairro.id());
		assertThat(apoiar(bruno, poste.id()).getBody().apoios()).isEqualTo(1L);
		assertThat(apoiar(bruno, poste.id()).getBody().apoios()).as("apoiar de novo não conta duas vezes").isEqualTo(1L);
		assertThat(apoiar(carla, poste.id()).getBody().apoios()).isEqualTo(2L);
		assertThat(apoiar(ana, poste.id()).getStatusCode().value()).as("a autora não apoia a própria").isEqualTo(409);

		// Quem não entrou vê a lista e o detalhe, só com o primeiro nome da autora.
		List<Resumo> lista = doBairro(bairro.id(), null, "");
		assertThat(lista).extracting(Resumo::titulo).containsExactly("Trocar a lâmpada do poste da esquina");
		assertThat(lista.getFirst().apoios()).isEqualTo(2L);
		assertThat(lista.getFirst().apoiei()).isFalse();
		ResponseEntity<String> aberto = publico("/iniciativas/" + poste.id());
		assertThat(aberto.getStatusCode().value()).isEqualTo(200);
		assertThat(aberto.getBody()).contains("\"autor\":\"Ana\"").doesNotContain("Souza", emailAna, "Bruno", "Carla");

		// Logado, a lista diz o que a pessoa apoiou.
		assertThat(doBairro(bairro.id(), bruno, "").getFirst().apoiei()).isTrue();

		// Mais apoiadas primeiro.
		Iniciativa lixo = criarIniciativa(bruno, "Coleta de lixo duas vezes por semana");
		assertThat(doBairro(bairro.id(), null, "?ordem=apoios")).extracting(Resumo::id)
				.containsExactly(poste.id(), lixo.id());
		assertThat(doBairro(bairro.id(), null, "")).extracting(Resumo::id).containsExactly(lixo.id(), poste.id());

		// A autora conta o que aconteceu e muda a fase; ninguém mais pode.
		ResponseEntity<String> intrusa = http.post().uri("/api/v1/iniciativas/{id}/atualizacoes", poste.id())
				.header("Authorization", "Bearer " + bruno).contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("texto", "Resolvi sozinho", "fase", "RESOLVIDA")).retrieve().toEntity(String.class);
		assertThat(intrusa.getStatusCode().value()).isEqualTo(404);
		Iniciativa andamento = http.post().uri("/api/v1/iniciativas/{id}/atualizacoes", poste.id())
				.header("Authorization", "Bearer " + ana).contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("texto", "Abri o pedido 123 na prefeitura.", "fase", "EM_ANDAMENTO")).retrieve()
				.body(Iniciativa.class);
		assertThat(andamento.fase()).isEqualTo("EM_ANDAMENTO");
		Iniciativa resolvida = http.post().uri("/api/v1/iniciativas/{id}/atualizacoes", poste.id())
				.header("Authorization", "Bearer " + ana).contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("texto", "Trocaram a lâmpada hoje!", "fase", "RESOLVIDA")).retrieve()
				.body(Iniciativa.class);
		assertThat(resolvida.atualizacoes()).extracting(Atualizacao::fase).containsExactly("EM_ANDAMENTO", "RESOLVIDA");

		// Resolvida sai da lista padrão e aparece no filtro.
		assertThat(doBairro(bairro.id(), null, "")).extracting(Resumo::id).containsExactly(lixo.id());
		assertThat(doBairro(bairro.id(), null, "?fase=RESOLVIDA")).extracting(Resumo::id).containsExactly(poste.id());
		assertThat(apoiar(carla, poste.id()).getStatusCode().value()).isEqualTo(409);

		// Editar e apagar: só a autora.
		ResponseEntity<Void> apagarAlheio = http.method(HttpMethod.DELETE).uri("/api/v1/iniciativas/{id}", poste.id())
				.header("Authorization", "Bearer " + bruno).retrieve().toBodilessEntity();
		assertThat(apagarAlheio.getStatusCode().value()).isEqualTo(404);
		ResponseEntity<Void> apagar = http.method(HttpMethod.DELETE).uri("/api/v1/iniciativas/{id}", poste.id())
				.header("Authorization", "Bearer " + ana).retrieve().toBodilessEntity();
		assertThat(apagar.getStatusCode().value()).isEqualTo(204);
		assertThat(publico("/iniciativas/" + poste.id()).getStatusCode().value()).isEqualTo(404);
	}

	@Test
	void limiteDePublicacoesPorDia() {
		String gil = cadastrarConfirmado("Gil Pressa", emailUnico("gil"));
		morarEm(gil, bairroNovo(gil).id());
		for (int i = 1; i <= 3; i++) {
			criarIniciativa(gil, "Pedido número " + i + " do bairro");
		}
		assertThat(publicar(gil, "Pedido número 4 do bairro").getStatusCode().value()).isEqualTo(429);
	}

	@Test
	void tresDenunciasOcultamAteAAdministracaoDecidir() {
		String emailAutor = emailUnico("davi");
		String davi = cadastrarConfirmado("Davi Autor", emailAutor);
		Bairro bairro = bairroNovo(davi);
		morarEm(davi, bairro.id());
		Iniciativa post = criarIniciativa(davi, "Uma iniciativa que vão denunciar");
		assertThat(denunciar(davi, post.id(), "SPAM").getStatusCode().value()).isEqualTo(409);

		String[] vizinhos = { morador("Eva Um", bairro.id()), morador("Fred Dois", bairro.id()),
				morador("Gabi Tres", bairro.id()) };
		assertThat(denunciar(vizinhos[0], post.id(), "OFENSIVO").getStatusCode().value()).isEqualTo(204);
		assertThat(denunciar(vizinhos[0], post.id(), "OFENSIVO").getStatusCode().value())
				.as("a mesma pessoa não conta duas vezes").isEqualTo(204);
		assertThat(denunciar(vizinhos[1], post.id(), "OFENSIVO").getStatusCode().value()).isEqualTo(204);
		assertThat(publico("/iniciativas/" + post.id()).getStatusCode().value()).isEqualTo(200);
		assertThat(denunciar(vizinhos[2], post.id(), "PROPAGANDA").getStatusCode().value()).isEqualTo(204);

		// Oculta: some para todo mundo, menos para o autor, que recebe um e-mail.
		assertThat(publico("/iniciativas/" + post.id()).getStatusCode().value()).isEqualTo(404);
		assertThat(doBairro(bairro.id(), null, "")).isEmpty();
		assertThat(get("/api/v1/iniciativas/" + post.id(), davi, Iniciativa.class).oculta()).isTrue();
		assertThat(emails.para(emailAutor).getLast().getSubject()).contains("ocultada");

		// A administração vê o texto e os motivos, sem saber quem denunciou.
		String emailAdmin = emailUnico("admin");
		String admin = cadastrarConfirmado("Ana Admin", emailAdmin);
		jdbc.update("UPDATE usuarios SET papel = 'ADMINISTRADOR' WHERE email = ?", emailAdmin);
		assertThat(http.get().uri("/api/v1/admin/moderacao").header("Authorization", "Bearer " + vizinhos[0]).retrieve()
				.toBodilessEntity().getStatusCode().value()).isEqualTo(403);
		ResponseEntity<String> fila = http.get().uri("/api/v1/admin/moderacao").header("Authorization", "Bearer " + admin)
				.retrieve().toEntity(String.class);
		assertThat(fila.getBody()).contains(post.id().toString(), "\"OFENSIVO\":2", "\"PROPAGANDA\":1")
				.doesNotContain("Eva", "Fred", "Gabi", emailAutor);

		// Manter: volta a aparecer e não some mais sozinha com novas denúncias.
		assertThat(post("/api/v1/admin/moderacao/" + post.id() + "/manter", admin, Map.of()).getStatusCode().value())
				.isEqualTo(204);
		assertThat(publico("/iniciativas/" + post.id()).getStatusCode().value()).isEqualTo(200);
		for (String v : vizinhos) {
			denunciar(v, post.id(), "SPAM");
		}
		assertThat(publico("/iniciativas/" + post.id()).getStatusCode().value()).isEqualTo(200);

		// Remover: outra iniciativa denunciada.
		Iniciativa outra = criarIniciativa(davi, "Outra iniciativa denunciada");
		for (String v : vizinhos) {
			denunciar(v, outra.id(), "FALSO");
		}
		ResponseEntity<Void> remover = http.method(HttpMethod.DELETE).uri("/api/v1/admin/moderacao/{id}", outra.id())
				.header("Authorization", "Bearer " + admin).retrieve().toBodilessEntity();
		assertThat(remover.getStatusCode().value()).isEqualTo(204);
		assertThat(get("/api/v1/iniciativas/" + outra.id(), davi, String.class)).contains("não encontrada");
		assertThat(emails.para(emailAutor).getLast().getSubject()).contains("removida");
	}

}
