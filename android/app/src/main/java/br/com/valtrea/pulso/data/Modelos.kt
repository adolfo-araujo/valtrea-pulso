package br.com.valtrea.pulso.data

import kotlinx.serialization.Serializable

// Espelham os DTOs da API (api/src/main/java/br/com/valtrea/pulso).

@Serializable
data class CadastroRequest(val nome: String, val email: String, val senha: String)

@Serializable
data class LoginRequest(val email: String, val senha: String)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val tipo: String,
    val expiraEm: String,
    val refreshToken: String,
)

@Serializable
data class RenovarRequest(val refreshToken: String)

@Serializable
data class CodigoRequest(val codigo: String)

@Serializable
data class EsqueciSenhaRequest(val email: String)

@Serializable
data class RedefinirSenhaRequest(val email: String, val codigo: String, val novaSenha: String)

@Serializable
data class Perfil(
    val id: String,
    val nome: String,
    val email: String,
    val emailConfirmado: Boolean,
    /** Falso quando há Termos novos para aceitar: a tela do bairro mostra o aviso. */
    val termosAceitos: Boolean = true,
    /** Conta de administração: a tela Conta mostra o atalho para o painel. */
    val admin: Boolean = false,
    /** Nulo até a pessoa escolher. */
    val bairro: Bairro? = null,
)

@Serializable
data class AceitarTermosRequest(val versao: String)

@Serializable
data class AtualizarPerfilRequest(val nome: String)

@Serializable
data class ExcluirContaRequest(val senha: String)

@Serializable
data class Bairro(
    val id: String,
    val nome: String,
    val cidade: String,
    val uf: String,
    /** Abertas ou em andamento. */
    val iniciativas: Long = 0,
) {
    val nomeCompleto get() = "$nome, $cidade - $uf"
}

@Serializable
data class BairroRequest(val nome: String, val cidade: String, val uf: String)

@Serializable
data class MudarBairroRequest(val bairroId: String?)

/** Categorias da API, com o nome mostrado nas telas. */
@Serializable
enum class Categoria(val rotulo: String) {
    ILUMINACAO("Iluminação"),
    LIMPEZA("Limpeza e lixo"),
    SEGURANCA("Segurança"),
    TRANSITO("Trânsito"),
    CALCADAS_E_RUAS("Calçadas e ruas"),
    AREAS_VERDES("Praças e áreas verdes"),
    SAUDE("Saúde"),
    EDUCACAO("Educação"),
    LAZER("Esporte e lazer"),
    TRANSPORTE("Transporte público"),
    OUTROS("Outros"),
}

@Serializable
enum class Fase(val rotulo: String) {
    ABERTA("Aberta"),
    EM_ANDAMENTO("Em andamento"),
    RESOLVIDA("Resolvida"),
    ARQUIVADA("Arquivada");

    val ativa get() = this == ABERTA || this == EM_ANDAMENTO
}

@Serializable
enum class MotivoDenuncia(val rotulo: String) {
    OFENSIVO("Xingamento, ataque ou preconceito"),
    DADOS_PESSOAIS("Expõe uma pessoa (nome, endereço, telefone, foto)"),
    PROPAGANDA("Propaganda, venda ou pedido de voto"),
    SPAM("Repetida, sem sentido ou de outro bairro"),
    FALSO("Informação falsa ou golpe"),
    OUTRO("Outro motivo"),
}

/** Resumo das listas. [autor] é só o primeiro nome. */
@Serializable
data class IniciativaResumo(
    val id: String,
    val bairroId: String,
    val titulo: String,
    val categoria: Categoria,
    val fase: Fase,
    val autor: String,
    val apoios: Long = 0,
    val criadoEm: String,
    val atualizadoEm: String,
    val apoiei: Boolean = false,
    val minha: Boolean = false,
    val oculta: Boolean = false,
)

@Serializable
data class Atualizacao(val id: String, val texto: String, val fase: Fase? = null, val criadoEm: String)

@Serializable
data class Iniciativa(
    val id: String,
    val bairro: Bairro,
    val titulo: String,
    val descricao: String,
    val categoria: Categoria,
    val fase: Fase,
    val autor: String,
    val apoios: Long = 0,
    val criadoEm: String,
    val atualizadoEm: String,
    val atualizacoes: List<Atualizacao> = emptyList(),
    val apoiei: Boolean = false,
    val denunciei: Boolean = false,
    val minha: Boolean = false,
    val oculta: Boolean = false,
)

@Serializable
data class IniciativaRequest(val titulo: String, val descricao: String, val categoria: Categoria)

@Serializable
data class AtualizacaoRequest(val texto: String, val fase: Fase? = null)

@Serializable
data class DenunciaRequest(val motivo: MotivoDenuncia)

/** Fila de moderação: o texto, o primeiro nome do autor e quantas denúncias de cada motivo. */
@Serializable
data class ParaRevisar(
    val id: String,
    val titulo: String,
    val descricao: String,
    val categoria: Categoria,
    val bairro: String,
    val autor: String,
    val criadoEm: String,
    val ocultaEm: String? = null,
    val denuncias: Map<MotivoDenuncia, Long> = emptyMap(),
)

/** Painel de administração: só números agregados, nenhum dado de outra pessoa. */
@Serializable
data class MetricasAdmin(
    val geradoEm: String,
    val contas: ContasAdmin,
    val uso: UsoAdmin,
    val cadastrosPorSemana: List<SemanaAdmin>,
)

@Serializable
data class ContasAdmin(
    val total: Long,
    val confirmadas: Long,
    val novas7Dias: Long,
    val novas30Dias: Long,
    val comSessaoAtiva: Long,
)

@Serializable
data class UsoAdmin(
    val bairros: Long,
    val bairrosComIniciativas: Long,
    val iniciativas: Long,
    val ativas: Long,
    val resolvidas: Long,
    val iniciativas30Dias: Long,
    val apoios: Long,
    val apoios30Dias: Long,
    val ocultas: Long,
)

@Serializable
data class SemanaAdmin(val semana: String, val cadastros: Long)

/** Resposta de erro da API no formato ProblemDetail. */
@Serializable
data class Problema(val detail: String? = null, val campos: Map<String, String>? = null)
