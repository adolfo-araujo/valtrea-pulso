package br.com.valtrea.pulso.iniciativa;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** "Fomos à subprefeitura", "a lâmpada foi trocada": o que o autor conta depois. */
@Entity
@Table(name = "atualizacoes")
public class Atualizacao {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "iniciativa_id")
	private Iniciativa iniciativa;

	@Column(nullable = false, length = 1000)
	private String texto;

	/** A fase nova, quando a atualização mudou a fase. */
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Fase fase;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	protected Atualizacao() {
	}

	public Atualizacao(Iniciativa iniciativa, String texto, Fase fase) {
		this.iniciativa = iniciativa;
		this.texto = texto;
		this.fase = fase;
		this.criadoEm = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getTexto() {
		return texto;
	}

	public Fase getFase() {
		return fase;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
