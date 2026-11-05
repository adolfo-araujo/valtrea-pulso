package br.com.valtrea.pulso.bairro;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Um bairro, criado pelo primeiro morador que o escolhe. Não guarda nada de ninguém. */
@Entity
@Table(name = "bairros")
public class Bairro {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 80)
	private String nome;

	@Column(nullable = false, length = 80)
	private String cidade;

	@Column(nullable = false, length = 2, columnDefinition = "char(2)")
	private String uf;

	/** UF + cidade + nome sem acentos, em minúsculas: "Vila Mariana" e "vila  mariana" são o mesmo bairro. */
	@Column(nullable = false, length = 200)
	private String chave;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	protected Bairro() {
	}

	public Bairro(String nome, String cidade, String uf) {
		this.nome = arrumar(nome);
		this.cidade = arrumar(cidade);
		this.uf = uf.strip().toUpperCase(Locale.ROOT);
		this.chave = chave(this.nome, this.cidade, this.uf);
		this.criadoEm = Instant.now();
	}

	public static String chave(String nome, String cidade, String uf) {
		return normalizar(uf) + "|" + normalizar(cidade) + "|" + normalizar(nome);
	}

	static String normalizar(String texto) {
		String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").strip();
	}

	private static String arrumar(String texto) {
		return texto.strip().replaceAll("\\s+", " ");
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getCidade() {
		return cidade;
	}

	public String getUf() {
		return uf;
	}

	public String getChave() {
		return chave;
	}

}
