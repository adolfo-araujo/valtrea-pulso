package br.com.valtrea.pulso.ui.admin

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.valtrea.pulso.data.ContasAdmin
import br.com.valtrea.pulso.data.MetricasAdmin
import br.com.valtrea.pulso.data.SemanaAdmin
import br.com.valtrea.pulso.data.UsoAdmin
import br.com.valtrea.pulso.ui.theme.ValtreaTheme
import br.com.valtrea.pulso.ui.verificarAcessibilidade
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PainelAdminTest {

    @get:Rule
    val compose = createComposeRule()

    private val metricas = MetricasAdmin(
        geradoEm = "2026-11-04T12:30:00Z",
        contas = ContasAdmin(total = 12, confirmadas = 9, novas7Dias = 3, novas30Dias = 7, comSessaoAtiva = 8),
        uso = UsoAdmin(
            bairros = 5, bairrosComIniciativas = 3, iniciativas = 20, ativas = 15, resolvidas = 4,
            iniciativas30Dias = 9, apoios = 40, apoios30Dias = 22, ocultas = 1,
        ),
        cadastrosPorSemana = listOf(
            "2026-09-14", "2026-09-21", "2026-09-28", "2026-10-05",
            "2026-10-12", "2026-10-19", "2026-10-26", "2026-11-02",
        ).mapIndexed { i, semana -> SemanaAdmin(semana, i.toLong()) },
    )

    @Test
    fun mostraOsNumerosELeCadaLinhaInteira() {
        var atualizou = 0
        var voltou = 0
        compose.setContent {
            ValtreaTheme(escuro = false) {
                PainelAdmin(metricas, carregando = false, erro = null, aoAtualizar = { atualizou++ },
                    aoVoltar = { voltou++ })
            }
        }
        compose.onNodeWithText("Administração").assertIsDisplayed()
        compose.onNodeWithContentDescription("Total: 12").assertIsDisplayed()
        compose.onNodeWithContentDescription("Apoios em 30 dias: 22").assertIsDisplayed()
        compose.onNodeWithContentDescription("Esperando revisão: 1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Cadastros por semana", substring = true).assertIsDisplayed()

        val titulos = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .fetchSemanticsNodes()
        assertEquals("título da tela + 3 seções", 4, titulos.size)

        compose.onNodeWithText("Atualizar").performClick()
        compose.onNodeWithContentDescription("Voltar").performClick()
        assertEquals(1, atualizou)
        assertEquals(1, voltou)
        compose.verificarAcessibilidade()
    }

    @Test
    fun mostraOErroSemNumeros() {
        compose.setContent {
            ValtreaTheme(escuro = true) {
                PainelAdmin(null, carregando = false, erro = "Acesso restrito à administração.", aoAtualizar = {},
                    aoVoltar = {})
            }
        }
        compose.onNodeWithText("Acesso restrito à administração.").assertIsDisplayed()
        compose.onAllNodesWithText("Total", substring = true).assertCountEquals(0)
        compose.verificarAcessibilidade()
    }
}
