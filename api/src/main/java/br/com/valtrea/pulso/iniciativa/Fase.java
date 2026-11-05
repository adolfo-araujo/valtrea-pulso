package br.com.valtrea.pulso.iniciativa;

/** Em que pé está a iniciativa. Quem muda é o autor. */
public enum Fase {
	ABERTA, EM_ANDAMENTO, RESOLVIDA, ARQUIVADA;

	/** Aberta e em andamento aparecem por padrão na lista do bairro. */
	public boolean ativa() {
		return this == ABERTA || this == EM_ANDAMENTO;
	}
}
