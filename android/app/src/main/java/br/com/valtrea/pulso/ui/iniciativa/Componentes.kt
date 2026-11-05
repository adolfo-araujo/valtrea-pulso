package br.com.valtrea.pulso.ui.iniciativa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.valtrea.pulso.data.Fase
import br.com.valtrea.pulso.data.IniciativaResumo
import br.com.valtrea.pulso.ui.componentes.dataCurta

fun apoiosTexto(n: Long) = if (n == 1L) "1 apoio" else "$n apoios"

@Composable
fun SeloFase(fase: Fase) {
    val cor = when (fase) {
        Fase.ABERTA -> MaterialTheme.colorScheme.primary
        Fase.EM_ANDAMENTO -> MaterialTheme.colorScheme.secondary
        Fase.RESOLVIDA -> MaterialTheme.colorScheme.onSurface
        Fase.ARQUIVADA -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        fase.rotulo,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = cor,
        modifier = Modifier
            .border(1.dp, cor.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartaoIniciativa(iniciativa: IniciativaResumo, aoAbrir: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClickLabel = "Abrir iniciativa", role = Role.Button, onClick = aoAbrir)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.Center) {
            SeloFase(iniciativa.fase)
            Text(
                iniciativa.categoria.rotulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            if (iniciativa.oculta) {
                Text(
                    "oculta para revisão",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
        }
        Text(iniciativa.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val autor = if (iniciativa.minha) "sua" else "por ${iniciativa.autor}"
        val apoio = if (iniciativa.apoiei) " · você apoia" else ""
        Text(
            "${apoiosTexto(iniciativa.apoios)}$apoio · $autor, ${iniciativa.criadoEm.dataCurta()}",
            style = MaterialTheme.typography.bodySmall,
            color = if (iniciativa.apoiei) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

