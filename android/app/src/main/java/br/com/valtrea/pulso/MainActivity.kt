package br.com.valtrea.pulso

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.com.valtrea.pulso.data.EstadoSessao
import br.com.valtrea.pulso.ui.admin.AdminScreen
import br.com.valtrea.pulso.ui.confirmar.ConfirmarEmailScreen
import br.com.valtrea.pulso.ui.conta.ContaScreen
import br.com.valtrea.pulso.ui.entrar.EntrarScreen
import br.com.valtrea.pulso.ui.bairro.BairroScreen
import br.com.valtrea.pulso.ui.bairro.EscolherBairroScreen
import br.com.valtrea.pulso.ui.iniciativa.IniciativaScreen
import br.com.valtrea.pulso.ui.iniciativa.MinhasScreen
import br.com.valtrea.pulso.ui.theme.ValtreaTheme
import kotlinx.serialization.Serializable

@Serializable data object RotaBairro
@Serializable data object RotaEscolherBairro
@Serializable data class RotaIniciativa(val id: String)
@Serializable data object RotaMinhas
@Serializable data object RotaConta
@Serializable data object RotaConfirmarEmail
@Serializable data object RotaAdmin

private data class Aba(val rota: Any, val rotulo: String, val icone: ImageVector)

private val abas = listOf(
    Aba(RotaBairro, "Meu bairro", Icons.Default.Home),
    Aba(RotaMinhas, "Minhas", Icons.Default.Description),
    Aba(RotaConta, "Conta", Icons.Default.Person),
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sessao = (application as ValtreaApp).sessao
        installSplashScreen().setKeepOnScreenCondition { sessao.estado.value == EstadoSessao.Carregando }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pedirAcessoARedeLocalEmDebug()
        setContent {
            ValtreaTheme {
                val estado by sessao.estado.collectAsStateWithLifecycle()
                // O Surface pinta o fundo do tema e define a cor do texto (claro no modo escuro, escuro no claro).
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (estado) {
                        EstadoSessao.Carregando -> Unit
                        EstadoSessao.Deslogado -> EntrarScreen()
                        EstadoSessao.Logado -> AppLogado()
                    }
                }
            }
        }
    }

    /** A API de desenvolvimento roda no computador, que o Android 17+ trata como rede local. */
    private fun pedirAcessoARedeLocalEmDebug() {
        val permissao = "android.permission.ACCESS_LOCAL_NETWORK"
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 37 &&
            checkSelfPermission(permissao) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(permissao), 0)
        }
    }
}

@Composable
private fun AppLogado() {
    val nav = rememberNavController()
    val entradaAtual by nav.currentBackStackEntryAsState()

    fun irPara(rota: Any) {
        nav.navigate(rota) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun voltar() {
        nav.popBackStack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                abas.forEach { aba ->
                    NavigationBarItem(
                        selected = entradaAtual?.destination?.hasRoute(aba.rota::class) == true,
                        onClick = { irPara(aba.rota) },
                        icon = { Icon(aba.icone, contentDescription = null) },
                        label = { Text(aba.rotulo) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = RotaBairro) {
            composable<RotaBairro> {
                BairroScreen(
                    aoAbrir = { id -> nav.navigate(RotaIniciativa(id)) },
                    aoEscolherBairro = { nav.navigate(RotaEscolherBairro) },
                    aoConfirmarEmail = { nav.navigate(RotaConfirmarEmail) },
                    contentPadding = padding,
                )
            }
            composable<RotaEscolherBairro> {
                EscolherBairroScreen(aoConcluir = { voltar() }, aoVoltar = { voltar() }, contentPadding = padding)
            }
            composable<RotaIniciativa> { entrada ->
                IniciativaScreen(entrada.toRoute<RotaIniciativa>().id, aoVoltar = { voltar() }, contentPadding = padding)
            }
            composable<RotaMinhas> {
                MinhasScreen(aoAbrir = { id -> nav.navigate(RotaIniciativa(id)) }, contentPadding = padding)
            }
            composable<RotaConta> {
                ContaScreen(contentPadding = padding, aoAbrirAdmin = { nav.navigate(RotaAdmin) })
            }
            composable<RotaConfirmarEmail> {
                ConfirmarEmailScreen(aoConcluir = { voltar() }, aoVoltar = { voltar() }, contentPadding = padding)
            }
            composable<RotaAdmin> { AdminScreen(aoVoltar = { voltar() }, contentPadding = padding) }
        }
    }
}
