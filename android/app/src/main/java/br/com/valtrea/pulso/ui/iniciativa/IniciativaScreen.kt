package br.com.valtrea.pulso.ui.iniciativa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.valtrea.pulso.data.AtualizacaoRequest
import br.com.valtrea.pulso.data.DenunciaRequest
import br.com.valtrea.pulso.data.Iniciativa
import br.com.valtrea.pulso.data.IniciativaRequest
import br.com.valtrea.pulso.data.MotivoDenuncia
import br.com.valtrea.pulso.data.ValtreaApi
import br.com.valtrea.pulso.data.mensagemAmigavel
import br.com.valtrea.pulso.ui.componentes.BotaoVoltar
import br.com.valtrea.pulso.ui.componentes.dataCurta
import br.com.valtrea.pulso.ui.valtreaViewModel
import kotlinx.coroutines.launch

enum class Dialogo { NENHUM, EDITAR, NOVIDADE, DENUNCIAR, APAGAR }

class IniciativaViewModel(private val api: ValtreaApi, private val id: String) : ViewModel() {
    var iniciativa by mutableStateOf<Iniciativa?>(null)
        private set
    var emailConfirmado by mutableStateOf(false)
        private set
    var ocupado by mutableStateOf(false)
        private set
    var erro by mutableStateOf<String?>(null)
        private set
    var aviso by mutableStateOf<String?>(null)
        private set
    var dialogo by mutableStateOf(Dialogo.NENHUM)

    fun carregar() = tarefa {
        runCatching { api.perfil() }.onSuccess { emailConfirmado = it.emailConfirmado }
        iniciativa = api.iniciativa(id)
    }

    fun alternarApoio() = tarefa {
        val atual = iniciativa ?: return@tarefa
        iniciativa = if (atual.apoiei) api.retirarApoio(id) else api.apoiar(id)
    }

    suspend fun editar(pedido: IniciativaRequest) {
        iniciativa = api.editar(id, pedido)
        dialogo = Dialogo.NENHUM
    }

    suspend fun contarNovidade(pedido: AtualizacaoRequest) {
        iniciativa = api.atualizar(id, pedido)
        dialogo = Dialogo.NENHUM
    }

    suspend fun denunciar(motivo: MotivoDenuncia) {
        api.denunciar(id, DenunciaRequest(motivo))
        iniciativa = iniciativa?.copy(denunciei = true)
        aviso = "Denúncia enviada. Obrigado por avisar."
        dialogo = Dialogo.NENHUM
    }

    fun apagar(aoTerminar: () -> Unit) = tarefa {
        api.apagar(id)
        dialogo = Dialogo.NENHUM
        aoTerminar()
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

@Composable
fun IniciativaScreen(id: String, aoVoltar: () -> Unit, contentPadding: PaddingValues) {
    val vm = valtreaViewModel { IniciativaViewModel(it.api, id) }
    LaunchedEffect(id) { vm.carregar() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BotaoVoltar(aoVoltar, Modifier.offset(x = (-12).dp))
        vm.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        vm.aviso?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        val i = vm.iniciativa ?: return@Column

        Text(
            i.bairro.nomeCompleto,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        if (i.oculta) {
            Text(
                "Esta iniciativa recebeu denúncias e está oculta até a nossa revisão. Só você a vê.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SeloFase(i.fase)
            Text(i.categoria.rotulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(i.titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text(
            "Por ${if (i.minha) "você" else i.autor}, em ${i.criadoEm.dataCurta()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(i.descricao, style = MaterialTheme.typography.bodyLarge)

        // Apoios: o número é lido de novo pelo TalkBack quando muda.
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                apoiosTexto(i.apoios),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp).weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (!i.minha && i.fase.ativa) {
                if (i.apoiei) {
                    OutlinedButton(onClick = vm::alternarApoio, enabled = !vm.ocupado) { Text("Retirar apoio") }
                } else {
                    Button(onClick = vm::alternarApoio, enabled = !vm.ocupado && vm.emailConfirmado) { Text("Apoiar") }
                }
            }
        }
        if (!vm.emailConfirmado && !i.minha) {
            Text(
                "Confirme seu e-mail em Conta para apoiar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text("Acompanhamento", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        if (i.atualizacoes.isEmpty()) {
            Text("Nenhuma novidade ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        i.atualizacoes.forEach { a ->
            Column(Modifier.fillMaxWidth().padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val fase = a.fase?.let { " · passou para ${it.rotulo}" } ?: ""
                Text(
                    "${a.criadoEm.dataCurta()}$fase",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(a.texto)
            }
        }

        if (i.minha) {
            Button(onClick = { vm.dialogo = Dialogo.NOVIDADE }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Contar uma novidade")
            }
            OutlinedButton(onClick = { vm.dialogo = Dialogo.EDITAR }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Editar")
            }
            TextButton(onClick = { vm.dialogo = Dialogo.APAGAR }) {
                Text("Apagar iniciativa", color = MaterialTheme.colorScheme.error)
            }
        } else if (vm.emailConfirmado && !i.denunciei) {
            TextButton(onClick = { vm.dialogo = Dialogo.DENUNCIAR }) {
                Text("Denunciar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    val i = vm.iniciativa ?: return
    when (vm.dialogo) {
        Dialogo.EDITAR -> DialogoIniciativa(i, null, aoCancelar = { vm.dialogo = Dialogo.NENHUM }, aoSalvar = vm::editar)
        Dialogo.NOVIDADE -> DialogoAtualizacao(i.fase, aoCancelar = { vm.dialogo = Dialogo.NENHUM }, aoEnviar = vm::contarNovidade)
        Dialogo.DENUNCIAR -> DialogoDenuncia(aoCancelar = { vm.dialogo = Dialogo.NENHUM }, aoEnviar = vm::denunciar)
        Dialogo.APAGAR -> AlertDialog(
            onDismissRequest = { vm.dialogo = Dialogo.NENHUM },
            title = { Text("Apagar esta iniciativa?") },
            text = { Text("Apaga também os apoios e as novidades. Não dá para desfazer.") },
            confirmButton = {
                TextButton(onClick = { vm.apagar(aoVoltar) }, enabled = !vm.ocupado) {
                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { vm.dialogo = Dialogo.NENHUM }) { Text("Cancelar") } },
        )
        Dialogo.NENHUM -> Unit
    }
}
