package br.com.valtrea.pulso.iniciativa;

import java.time.Instant;
import java.util.UUID;

import br.com.valtrea.pulso.bairro.Bairro;
import br.com.valtrea.pulso.usuario.Usuario;
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

/**
 * Algo que o bairro precisa. Pública (só o primeiro nome do autor aparece). Depois de várias denúncias, fica
 * {@code oculta} até a administração revisar; se ela mantiver, fica {@code revisada} e não some mais sozinha.
 */
@Entity
@Table(name = "iniciativas")
public class Iniciativa {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bairro_id")
	private Bairro bairro;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "autor_id")
	private Usuario autor;

	@Column(nullable = false, length = 120)
	private String titulo;

	@Column(nullable = false, length = 2000)
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Categoria categoria;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Fase fase;

	@Column(nullable = false)
	private boolean oculta;

	@Column(name = "oculta_em")
	private Instant ocultaEm;

	@Column(nullable = false)
	private boolean revisada;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private Instant atualizadoEm;

	protected Iniciativa() {
	}

	public Iniciativa(Bairro bairro, Usuario autor, String titulo, String descricao, Categoria categoria) {
		this.bairro = bairro;
		this.autor = autor;
		this.fase = Fase.ABERTA;
		this.criadoEm = Instant.now();
		editar(titulo, descricao, categoria);
	}

	/** Editar o texto tira a marca de revisada: o novo texto pode ser denunciado de novo. */
	public void editar(String titulo, String descricao, Categoria categoria) {
		this.titulo = titulo;
		this.descricao = descricao;
		this.categoria = categoria;
		this.revisada = false;
		this.atualizadoEm = Instant.now();
	}

	public void mudarFase(Fase fase) {
		this.fase = fase;
		this.atualizadoEm = Instant.now();
	}

	public void ocultar() {
		this.oculta = true;
		this.ocultaEm = Instant.now();
	}

	/** A administração olhou e manteve. */
	public void manter() {
		this.oculta = false;
		this.ocultaEm = null;
		this.revisada = true;
	}

	public boolean ehDo(UUID usuarioId) {
		return autor.getId().equals(usuarioId);
	}

	public UUID getId() {
		return id;
	}

	public Bairro getBairro() {
		return bairro;
	}

	public Usuario getAutor() {
		return autor;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getDescricao() {
		return descricao;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public Fase getFase() {
		return fase;
	}

	public boolean isOculta() {
		return oculta;
	}

	public Instant getOcultaEm() {
		return ocultaEm;
	}

	public boolean isRevisada() {
		return revisada;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public Instant getAtualizadoEm() {
		return atualizadoEm;
	}

}
