package br.com.valtrea.pulso.usuario;

import java.time.Instant;
import java.util.UUID;

import br.com.valtrea.pulso.bairro.Bairro;
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
 * Morador. Só nome, e-mail, senha e o bairro (LGPD, só o necessário). Em público aparece apenas o primeiro nome.
 * Excluir a conta apaga a linha e, em cascata no banco, as iniciativas, os apoios, as denúncias, as sessões e os
 * códigos.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private String email;

	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Papel papel;

	@Column(name = "tentativas_login_falhas", nullable = false)
	private int tentativasLoginFalhas;

	@Column(name = "bloqueado_ate")
	private Instant bloqueadoAte;

	@Column(name = "email_confirmado_em")
	private Instant emailConfirmadoEm;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	/** Versão dos Termos e da Política aceita e quando (prova do aceite). */
	@Column(name = "termos_versao", length = 20)
	private String termosVersao;

	@Column(name = "termos_aceitos_em")
	private Instant termosAceitosEm;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bairro_id")
	private Bairro bairro;

	protected Usuario() {
	}

	public Usuario(String nome, String email, String senhaHash) {
		this.nome = nome;
		this.email = email;
		this.senhaHash = senhaHash;
		this.papel = Papel.USUARIO;
		this.criadoEm = Instant.now();
	}

	public void atualizarNome(String nome) {
		this.nome = nome;
	}

	public Bairro getBairro() {
		return bairro;
	}

	public void mudarBairro(Bairro bairro) {
		this.bairro = bairro;
	}

	/** O que aparece em público: só o primeiro nome. */
	public String getPrimeiroNome() {
		String limpo = nome.strip();
		int espaco = limpo.indexOf(' ');
		return espaco > 0 ? limpo.substring(0, espaco) : limpo;
	}

	public void aceitarTermos(String versao) {
		this.termosVersao = versao;
		this.termosAceitosEm = Instant.now();
	}

	public String getTermosVersao() {
		return termosVersao;
	}

	public Instant getTermosAceitosEm() {
		return termosAceitosEm;
	}

	public boolean isBloqueado(Instant agora) {
		return bloqueadoAte != null && bloqueadoAte.isAfter(agora);
	}

	/**
	 * Conta uma senha errada e bloqueia o login até {@code bloquearAte} ao atingir o limite.
	 *
	 * @return {@code true} se esta tentativa causou o bloqueio
	 */
	public boolean registrarFalhaLogin(int tentativasMaximas, Instant bloquearAte) {
		tentativasLoginFalhas++;
		if (tentativasLoginFalhas >= tentativasMaximas) {
			bloqueadoAte = bloquearAte;
			tentativasLoginFalhas = 0;
			return true;
		}
		return false;
	}

	public boolean isEmailConfirmado() {
		return emailConfirmadoEm != null;
	}

	public void confirmarEmail() {
		if (emailConfirmadoEm == null) {
			emailConfirmadoEm = Instant.now();
		}
	}

	/** Nova senha definida pela recuperação: também desfaz o bloqueio, já que a pessoa provou ter o e-mail. */
	public void trocarSenha(String novoHash) {
		senhaHash = novoHash;
		registrarLoginComSucesso();
	}

	public void registrarLoginComSucesso() {
		tentativasLoginFalhas = 0;
		bloqueadoAte = null;
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public Papel getPapel() {
		return papel;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
