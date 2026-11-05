package br.com.valtrea.pulso.ui.bairro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.valtrea.pulso.ui.componentes.Links

@Composable
internal fun AvisoConfirmarEmail(aoConfirmar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = aoConfirmar)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.MarkEmailUnread, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(Modifier.width(12.dp))
        Text(
            "Confirme seu e-mail para publicar e apoiar iniciativas.",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Aviso de nova versão dos Termos e da Política, com o novo aceite. */
@Composable
internal fun AvisoNovosTermos(aoAceitar: () -> Unit) {
    val contexto = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "Atualizamos os Termos de uso e a Política de privacidade",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            "Se não concordar, você pode excluir sua conta em Conta.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { Links.abrir(contexto, Links.termos) }) { Text("Termos") }
            TextButton(onClick = { Links.abrir(contexto, Links.privacidade) }) { Text("Privacidade") }
            Spacer(Modifier.weight(1f))
            Button(onClick = aoAceitar) { Text("Li e concordo") }
        }
    }
}
