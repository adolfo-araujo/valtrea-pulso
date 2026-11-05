package br.com.valtrea.pulso.ui.iniciativa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.valtrea.pulso.data.IniciativaResumo
import br.com.valtrea.pulso.data.ValtreaApi
import br.com.valtrea.pulso.data.mensagemAmigavel
import br.com.valtrea.pulso.ui.valtreaViewModel
import kotlinx.coroutines.launch

class MinhasViewModel(private val api: ValtreaApi) : ViewModel() {
    var lista by mutableStateOf<List<IniciativaResumo>?>(null)
        private set
    var erro by mutableStateOf<String?>(null)
        private set

    fun carregar() {
        viewModelScope.launch {
            runCatching { api.minhas() }
                .onSuccess { lista = it; erro = null }
                .onFailure { erro = it.mensagemAmigavel() }
        }
    }
}

@Composable
fun MinhasScreen(aoAbrir: (String) -> Unit, contentPadding: PaddingValues) {
    val vm = valtreaViewModel { MinhasViewModel(it.api) }
    LaunchedEffect(Unit) { vm.carregar() }
    LazyColumn(
        Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Minhas iniciativas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        }
        vm.erro?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (vm.lista?.isEmpty() == true) {
            item {
                Text(
                    "Você ainda não publicou nada. Publique pela tela do seu bairro.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(vm.lista.orEmpty(), key = { it.id }) { i -> CartaoIniciativa(i) { aoAbrir(i.id) } }
    }
}
