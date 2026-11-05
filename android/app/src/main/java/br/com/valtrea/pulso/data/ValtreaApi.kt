package br.com.valtrea.pulso.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ValtreaApi {

    @POST("api/v1/auth/cadastro")
    suspend fun cadastrar(@Body pedido: CadastroRequest): TokenResponse

    @POST("api/v1/auth/login")
    suspend fun entrar(@Body pedido: LoginRequest): TokenResponse

    @POST("api/v1/auth/sair")
    suspend fun sair(@Body pedido: RenovarRequest)

    @POST("api/v1/auth/confirmar-email")
    suspend fun confirmarEmail(@Body pedido: CodigoRequest)

    @POST("api/v1/auth/reenviar-confirmacao")
    suspend fun reenviarConfirmacao()

    @POST("api/v1/auth/esqueci-senha")
    suspend fun esqueciSenha(@Body pedido: EsqueciSenhaRequest)

    @POST("api/v1/auth/redefinir-senha")
    suspend fun redefinirSenha(@Body pedido: RedefinirSenhaRequest)

    @GET("api/v1/eu")
    suspend fun perfil(): Perfil

    @PATCH("api/v1/eu")
    suspend fun atualizarPerfil(@Body pedido: AtualizarPerfilRequest): Perfil

    @POST("api/v1/eu/termos")
    suspend fun aceitarTermos(@Body pedido: AceitarTermosRequest): Perfil

    /** "Baixar meus dados": arquivo JSON com cadastro, bairro, iniciativas, apoios e denúncias (LGPD). */
    @GET("api/v1/eu/dados")
    suspend fun meusDados(): okhttp3.ResponseBody

    @HTTP(method = "DELETE", path = "api/v1/eu", hasBody = true)
    suspend fun excluirConta(@Body pedido: ExcluirContaRequest)

    @GET("api/v1/admin/metricas")
    suspend fun metricasAdmin(): MetricasAdmin

    @GET("api/v1/admin/moderacao")
    suspend fun moderacao(): List<ParaRevisar>

    @POST("api/v1/admin/moderacao/{id}/manter")
    suspend fun manter(@Path("id") id: String)

    @DELETE("api/v1/admin/moderacao/{id}")
    suspend fun remover(@Path("id") id: String)

    @GET("api/v1/publico/bairros")
    suspend fun buscarBairros(@Query("uf") uf: String?, @Query("busca") busca: String?): List<Bairro>

    @POST("api/v1/bairros")
    suspend fun encontrarOuCriarBairro(@Body pedido: BairroRequest): Bairro

    @PUT("api/v1/eu/bairro")
    suspend fun mudarBairro(@Body pedido: MudarBairroRequest): Perfil

    @GET("api/v1/bairros/{id}/iniciativas")
    suspend fun iniciativasDoBairro(
        @Path("id") bairroId: String,
        @Query("fase") fase: Fase?,
        @Query("ordem") ordem: String?,
    ): List<IniciativaResumo>

    @GET("api/v1/eu/iniciativas")
    suspend fun minhas(): List<IniciativaResumo>

    @GET("api/v1/iniciativas/{id}")
    suspend fun iniciativa(@Path("id") id: String): Iniciativa

    @POST("api/v1/iniciativas")
    suspend fun publicar(@Body pedido: IniciativaRequest): Iniciativa

    @PUT("api/v1/iniciativas/{id}")
    suspend fun editar(@Path("id") id: String, @Body pedido: IniciativaRequest): Iniciativa

    @DELETE("api/v1/iniciativas/{id}")
    suspend fun apagar(@Path("id") id: String)

    @POST("api/v1/iniciativas/{id}/atualizacoes")
    suspend fun atualizar(@Path("id") id: String, @Body pedido: AtualizacaoRequest): Iniciativa

    @POST("api/v1/iniciativas/{id}/apoio")
    suspend fun apoiar(@Path("id") id: String): Iniciativa

    @DELETE("api/v1/iniciativas/{id}/apoio")
    suspend fun retirarApoio(@Path("id") id: String): Iniciativa

    @POST("api/v1/iniciativas/{id}/denuncia")
    suspend fun denunciar(@Path("id") id: String, @Body pedido: DenunciaRequest)
}
