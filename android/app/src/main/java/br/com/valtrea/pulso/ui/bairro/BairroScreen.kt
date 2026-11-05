package br.com.valtrea.pulso.ui.bairro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.valtrea.pulso.data.AceitarTermosRequest
import br.com.valtrea.pulso.data.Fase
import br.com.valtrea.pulso.data.IniciativaRequest
import br.com.valtrea.pulso.data.IniciativaResumo
import br.com.valtrea.pulso.data.Perfil
import br.com.valtrea.pulso.data.ValtreaApi
import br.com.valtrea.pulso.data.mensagemAmigavel
import br.com.valtrea.pulso.ui.componentes.Links
import br.com.valtrea.pulso.ui.iniciativa.CartaoIniciativa
import br.com.valtrea.pulso.ui.iniciativa.DialogoIniciativa
import br.com.valtrea.pulso.ui.valtreaViewModel
import kotlinx.coroutines.launch

class BairroViewModel(private val api: ValtreaApi) : ViewModel() {
    var perfil by mutableStateOf<Perfil?>(null)
        private set
    var lista by mutableStateOf<List<IniciativaResumo>?>(null)
        private set
    var erro by mutableStateOf<String?>(null)
        private set
    /** Nula: abertas e em andamento. */
    var fase by mutableStateOf<Fase?>(null)
        private set
    var maisApoiadas by mutableStateOf(false)
        private set
    var publicando by mutableStateOf(false)

    fun carregar() {
        viewModelScope.launch {
            runCatching { api.perfil() }
                .onSuccess { perfil = it }
                .onFailure { erro = it.mensagemAmigavel() }
            carregarLista()
        }
    }

    fun filtrar(fase: Fase?) {
        this.fase = fase
        viewModelScope.launch { carregarLista() }
    }

    fun ordenarPorApoios(sim: Boolean) {
        maisApoiadas = sim
        viewModelScope.launch { carregarLista() }
    }

    private suspend fun carregarLista() {
        val bairro = perfil?.bairro ?: return
        runCatching { api.iniciativasDoBairro(bairro.id, fase, if (maisApoiadas) "apoios" else null) }
            .onSuccess { lista = it; erro = null }
            .onFailure { erro = it.mensagemAmigavel() }
    }

    fun aceitarTermos() {
        viewModelScope.launch {
            runCatching { api.aceitarTermos(AceitarTermosRequest(Links.VERSAO_TERMOS)) }
                .onSuccess { perfil = it }
                .onFailure { erro = it.mensagemAmigavel() }
        }
    }

    suspend fun publicar(pedido: IniciativaRequest): String = api.publicar(pedido).id
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BairroScreen(
    aoAbrir: (String) -> Unit,
    aoEscolherBairro: () -> Unit,
    aoConfirmarEmail: () -> Unit,
    contentPadding: PaddingValues,
) {
    val vm = valtreaViewModel { BairroViewModel(it.api) }
    LaunchedEffect(Unit) { vm.carregar() }
    val perfil = vm.perfil

    LazyColumn(
        Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    perfil?.bairro?.nome ?: "Meu bairro",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                if (perfil?.bairro != null) TextButton(onClick = aoEscolherBairro) { Text("Mudar") }
            }
            perfil?.bairro?.let {
                Text("${it.cidade} - ${it.uf}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        perfil?.let { p ->
            if (!p.termosAceitos) item { AvisoNovosTermos(aoAceitar = vm::aceitarTermos) }
            if (!p.emailConfirmado) item { AvisoConfirmarEmail(aoConfirmarEmail) }
        }
        vm.erro?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }

        if (perfil != null && perfil.bairro == null) {
            item {
                Text(
                    "Escolha o seu bairro para ver o que os vizinhos estão pedindo e publicar as suas iniciativas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Button(onClick = aoEscolherBairro, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Escolher meu bairro") }
            }
            return@LazyColumn
        }
        if (perfil?.bairro == null) return@LazyColumn

        item {
            Button(
                onClick = { vm.publicando = true },
                enabled = perfil?.emailConfirmado == true,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("  Nova iniciativa")
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = vm.fase == null, onClick = { vm.filtrar(null) }, label = { Text("Abertas") })
                FilterChip(selected = vm.fase == Fase.RESOLVIDA, onClick = { vm.filtrar(Fase.RESOLVIDA) }, label = { Text("Resolvidas") })
                FilterChip(selected = vm.fase == Fase.ARQUIVADA, onClick = { vm.filtrar(Fase.ARQUIVADA) }, label = { Text("Arquivadas") })
                FilterChip(selected = vm.maisApoiadas, onClick = { vm.ordenarPorApoios(!vm.maisApoiadas) }, label = { Text("Mais apoiadas") })
            }
        }
        val lista = vm.lista
        if (lista != null && lista.isEmpty()) {
            item {
                Text(
                    "Nenhuma iniciativa aqui ainda. Conte o que o bairro precisa: o poste apagado, a calçada quebrada, a praça sem cuidado.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(lista.orEmpty(), key = { it.id }) { i -> CartaoIniciativa(i) { aoAbrir(i.id) } }
    }

    if (vm.publicando) {
        DialogoIniciativa(
            inicial = null,
            nomeDoBairro = perfil?.bairro?.nome,
            aoCancelar = { vm.publicando = false },
            aoSalvar = { pedido ->
                val id = vm.publicar(pedido)
                vm.publicando = false
                aoAbrir(id)
            },
        )
    }
}
