package br.com.valtrea.pulso.iniciativa;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** "Eu também quero isso". Um por pessoa em cada iniciativa; ninguém vê quem apoiou, só quantos. */
@Entity
@Table(name = "apoios")
public class Apoio {

	@Embeddable
	public static class Chave implements Serializable {

		@Column(name = "iniciativa_id")
		private UUID iniciativaId;

		@Column(name = "usuario_id")
		private UUID usuarioId;

		protected Chave() {
		}

		public Chave(UUID iniciativaId, UUID usuarioId) {
			this.iniciativaId = iniciativaId;
			this.usuarioId = usuarioId;
		}

		public UUID getIniciativaId() {
			return iniciativaId;
		}

		public UUID getUsuarioId() {
			return usuarioId;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Chave c && Objects.equals(iniciativaId, c.iniciativaId)
					&& Objects.equals(usuarioId, c.usuarioId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(iniciativaId, usuarioId);
		}

	}

	@EmbeddedId
	private Chave id;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	protected Apoio() {
	}

	public Apoio(UUID iniciativaId, UUID usuarioId) {
		this.id = new Chave(iniciativaId, usuarioId);
		this.criadoEm = Instant.now();
	}

	public Chave getId() {
		return id;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
