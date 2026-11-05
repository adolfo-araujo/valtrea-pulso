package br.com.valtrea.pulso.email;

/** E-mail pronto para envio. Publicado como evento e enviado só depois que a transação do banco é gravada. */
public record Email(String para, String assunto, String texto) {

	public static Email confirmarEmail(String para, String nome, String codigo, long minutos) {
		return new Email(para, "Seu código Valtrea: " + codigo, """
				Olá, %s!

				Use este código no Valtrea Pulso para confirmar seu e-mail:

				    %s

				Ele vale por %d minutos. Se você não criou uma conta na Valtrea, ignore este e-mail.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), codigo, minutos));
	}

	public static Email redefinirSenha(String para, String nome, String codigo, long minutos) {
		return new Email(para, "Código para redefinir sua senha Valtrea", """
				Olá, %s!

				Recebemos um pedido para redefinir a senha da sua conta no Valtrea Pulso. Use este código:

				    %s

				Ele vale por %d minutos. Se não foi você, ignore este e-mail: sua senha continua a mesma.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), codigo, minutos));
	}

	public static Email contaBloqueada(String para, String nome, long minutos) {
		return new Email(para, "Tentativas de acesso à sua conta Valtrea", """
				Olá, %s!

				Houve várias tentativas de entrar na sua conta com a senha errada, então bloqueamos o login por %d minutos.

				Se foi você, é só esperar e tentar de novo, ou usar "Esqueci minha senha" no app.
				Se não foi você, recomendamos redefinir sua senha.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), minutos));
	}

	public static Email senhaAlterada(String para, String nome) {
		return new Email(para, "Sua senha Valtrea foi alterada", """
				Olá, %s!

				A senha da sua conta acabou de ser alterada e todos os aparelhos foram desconectados.

				Se não foi você, fale com a gente imediatamente: contato@valtrea.com.br

				— Equipe Valtrea
				""".formatted(primeiroNome(nome)));
	}

	public static Email contaExcluida(String para, String nome) {
		return new Email(para, "Sua conta Valtrea Pulso foi excluída", """
				Olá, %s!

				Sua conta no Valtrea Pulso foi excluída, como você pediu. Apagamos seus dados, as iniciativas que
				você publicou, os seus apoios e as suas denúncias.

				Se não foi você, fale com a gente imediatamente: contato@valtrea.com.br

				— Equipe Valtrea
				""".formatted(primeiroNome(nome)));
	}

	public static Email iniciativaOculta(String para, String nome, String titulo, String urlMinhas) {
		return new Email(para, "Sua iniciativa foi ocultada para revisão", """
				Olá, %s!

				A sua iniciativa "%s" recebeu denúncias de vizinhos e ficou oculta até a nossa equipe revisar.
				Se ela seguir os Termos de uso, volta a aparecer. Enquanto isso, só você a vê: %s

				Lembre: nada de xingamentos, de dados de outras pessoas (nome, endereço, telefone, foto) nem de
				propaganda ou pedido de voto.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), titulo, urlMinhas));
	}

	public static Email iniciativaRemovida(String para, String nome, String titulo) {
		return new Email(para, "Sua iniciativa foi removida", """
				Olá, %s!

				Depois de denúncias de vizinhos, revisamos a sua iniciativa "%s" e ela foi removida por não seguir os
				Termos de uso do Valtrea Pulso.

				Se você acha que foi um engano, responda para contato@valtrea.com.br.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), titulo));
	}

	private static String primeiroNome(String nome) {
		return nome.strip().split("\\s+")[0];
	}

}
