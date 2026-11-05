package br.com.valtrea.pulso.ui.iniciativa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import br.com.valtrea.pulso.data.AtualizacaoRequest
import br.com.valtrea.pulso.data.Categoria
import br.com.valtrea.pulso.data.Fase
import br.com.valtrea.pulso.data.Iniciativa
import br.com.valtrea.pulso.data.IniciativaRequest
import br.com.valtrea.pulso.data.MotivoDenuncia
import br.com.valtrea.pulso.data.mensagemAmigavel
import kotlinx.coroutines.launch

/** Escolha numa lista fechada (categoria, fase), com o rótulo sempre visível. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Escolha(rotulo: String, opcoes: List<T>, atual: T, nome: (T) -> String, aoEscolher: (T) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = aberto, onExpandedChange = { aberto = it }) {
        OutlinedTextField(
            value = nome(atual),
            onValueChange = {},
            readOnly = true,
            label = { Text(rotulo) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(text = { Text(nome(opcao)) }, onClick = { aoEscolher(opcao); aberto = false })
            }
        }
    }
}

/** Botão de confirmar com indicador de envio, e o erro da API logo acima. */
@Composable
private fun DialogoComEnvio(
    titulo: String,
    confirmar: String,
    podeEnviar: Boolean,
    aoCancelar: () -> Unit,
    enviar: suspend () -> Unit,
    conteudo: @Composable () -> Unit,
) {
    var enviando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val escopo = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!enviando) aoCancelar() },
        title = { Text(titulo) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                conteudo()
                erro?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !enviando && podeEnviar,
                onClick = {
                    enviando = true
                    erro = null
                    escopo.launch {
                        runCatching { enviar() }.onFailure { erro = it.mensagemAmigavel() }
                        enviando = false
                    }
                },
            ) {
                if (enviando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text(confirmar)
            }
        },
        dismissButton = { TextButton(onClick = aoCancelar, enabled = !enviando) { Text("Cancelar") } },
    )
}

/** Nova iniciativa ou edição. [inicial] nulo = nova. */
@Composable
fun DialogoIniciativa(
    inicial: Iniciativa?,
    nomeDoBairro: String?,
    aoCancelar: () -> Unit,
    aoSalvar: suspend (IniciativaRequest) -> Unit,
) {
    var titulo by remember { mutableStateOf(inicial?.titulo ?: "") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: Categoria.ILUMINACAO) }
    DialogoComEnvio(
        titulo = if (inicial == null) "Nova iniciativa" else "Editar iniciativa",
        confirmar = if (inicial == null) "Publicar" else "Salvar",
        podeEnviar = titulo.trim().length >= 8 && descricao.trim().length >= 20,
        aoCancelar = aoCancelar,
        enviar = { aoSalvar(IniciativaRequest(titulo.trim(), descricao.trim(), categoria)) },
    ) {
        nomeDoBairro?.let {
            Text(
                "No bairro $it. Aparece para todos com o seu primeiro nome.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = titulo,
            onValueChange = { if (it.length <= 120) titulo = it },
            label = { Text("Título") },
            supportingText = { Text("Pelo menos 8 caracteres.") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Escolha("Categoria", Categoria.entries, categoria, { it.rotulo }) { categoria = it }
        OutlinedTextField(
            value = descricao,
            onValueChange = { if (it.length <= 2000) descricao = it },
            label = { Text("O que precisa e por quê") },
            supportingText = { Text("Onde é, desde quando, quem é afetado. Sem dados de pessoas nem propaganda.") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
        )
    }
}

@Composable
fun DialogoAtualizacao(faseAtual: Fase, aoCancelar: () -> Unit, aoEnviar: suspend (AtualizacaoRequest) -> Unit) {
    var texto by remember { mutableStateOf("") }
    var fase by remember { mutableStateOf(faseAtual) }
    DialogoComEnvio(
        titulo = "Contar uma novidade",
        confirmar = "Publicar",
        podeEnviar = texto.trim().length >= 5,
        aoCancelar = aoCancelar,
        enviar = { aoEnviar(AtualizacaoRequest(texto.trim(), fase)) },
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { if (it.length <= 1000) texto = it },
            label = { Text("O que aconteceu?") },
            supportingText = { Text("Ex.: Abri o pedido 2026-123 no 156.") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
        )
        Escolha("Fase", Fase.entries, fase, { it.rotulo }) { fase = it }
    }
}

@Composable
fun DialogoDenuncia(aoCancelar: () -> Unit, aoEnviar: suspend (MotivoDenuncia) -> Unit) {
    var motivo by remember { mutableStateOf<MotivoDenuncia?>(null) }
    DialogoComEnvio(
        titulo = "Denunciar",
        confirmar = "Enviar denúncia",
        podeEnviar = motivo != null,
        aoCancelar = aoCancelar,
        enviar = { aoEnviar(motivo!!) },
    ) {
        Text(
            "Com 3 denúncias, a iniciativa some até a nossa revisão. Ninguém fica sabendo que foi você.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(Modifier.selectableGroup()) {
            MotivoDenuncia.entries.forEach { m ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .selectable(selected = motivo == m, onClick = { motivo = m }, role = Role.RadioButton),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = motivo == m, onClick = null)
                    Text(m.rotulo, Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
