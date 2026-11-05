package br.com.valtrea.pulso.ui.bairro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.valtrea.pulso.data.Bairro
import br.com.valtrea.pulso.data.BairroRequest
import br.com.valtrea.pulso.data.MudarBairroRequest
import br.com.valtrea.pulso.data.ValtreaApi
import br.com.valtrea.pulso.data.mensagemAmigavel
import br.com.valtrea.pulso.ui.componentes.BotaoVoltar
import br.com.valtrea.pulso.ui.valtreaViewModel
import kotlinx.coroutines.launch

val UFS = listOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE",
    "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")

class EscolherBairroViewModel(private val api: ValtreaApi) : ViewModel() {
    var resultados by mutableStateOf<List<Bairro>?>(null)
        private set
    var ocupado by mutableStateOf(false)
        private set
    var erro by mutableStateOf<String?>(null)
        private set

    fun buscar(uf: String, busca: String) = tarefa {
        resultados = api.buscarBairros(uf.ifBlank { null }, busca.trim().ifBlank { null })
    }

    fun morarEm(bairro: Bairro, aoConcluir: () -> Unit) = tarefa {
        api.mudarBairro(MudarBairroRequest(bairro.id))
        aoConcluir()
    }

    fun criar(nome: String, cidade: String, uf: String, aoConcluir: () -> Unit) = tarefa {
        val bairro = api.encontrarOuCriarBairro(BairroRequest(nome.trim(), cidade.trim(), uf))
        api.mudarBairro(MudarBairroRequest(bairro.id))
        aoConcluir()
    }

    private fun tarefa(bloco: suspend () -> Unit) {
        if (ocupado) return
        ocupado = true
        erro = null
        viewModelScope.launch {
            runCatching { bloco() }.onFailure { erro = it.mensagemAmigavel() }
            ocupado = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorUf(rotulo: String, valor: String, permiteTodos: Boolean, aoMudar: (String) -> Unit, modifier: Modifier = Modifier) {
    var aberto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = aberto, onExpandedChange = { aberto = it }, modifier = modifier) {
        OutlinedTextField(
            value = valor.ifBlank { "Todos" },
            onValueChange = {},
            readOnly = true,
            label = { Text(rotulo) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            if (permiteTodos) DropdownMenuItem(text = { Text("Todos") }, onClick = { aoMudar(""); aberto = false })
            UFS.forEach { uf -> DropdownMenuItem(text = { Text(uf) }, onClick = { aoMudar(uf); aberto = false }) }
        }
    }
}

@Composable
fun EscolherBairroScreen(aoConcluir: () -> Unit, aoVoltar: () -> Unit, contentPadding: PaddingValues) {
    val vm = valtreaViewModel { EscolherBairroViewModel(it.api) }
    var ufBusca by remember { mutableStateOf("") }
    var busca by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var uf by remember { mutableStateOf("SP") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BotaoVoltar(aoVoltar, Modifier.offset(x = (-12).dp))
        Text("Escolha o seu bairro", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        vm.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Text("Procure", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SeletorUf("Estado", ufBusca, permiteTodos = true, aoMudar = { ufBusca = it }, modifier = Modifier.width(120.dp))
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Bairro ou cidade") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.buscar(ufBusca, busca) }),
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedButton(
            onClick = { vm.buscar(ufBusca, busca) },
            enabled = !vm.ocupado,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Procurar") }
        vm.resultados?.let { lista ->
            if (lista.isEmpty()) Text("Nenhum bairro encontrado com esse nome.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            lista.forEach { b ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(b.nomeCompleto, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (b.iniciativas == 1L) "1 iniciativa aberta" else "${b.iniciativas} iniciativas abertas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(onClick = { vm.morarEm(b, aoConcluir) }, enabled = !vm.ocupado) { Text("Moro aqui") }
                }
            }
        }

        Text("Não achou? Cadastre", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
        Text(
            "Se alguém já tiver cadastrado com o mesmo nome, você entra no mesmo bairro.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = nome,
            onValueChange = { if (it.length <= 80) nome = it },
            label = { Text("Bairro") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cidade,
                onValueChange = { if (it.length <= 80) cidade = it },
                label = { Text("Cidade") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            SeletorUf("Estado", uf, permiteTodos = false, aoMudar = { uf = it }, modifier = Modifier.width(120.dp))
        }
        Button(
            onClick = { vm.criar(nome, cidade, uf, aoConcluir) },
            enabled = !vm.ocupado && nome.trim().length >= 2 && cidade.trim().length >= 2,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text("Cadastrar e morar aqui") }
    }
}
