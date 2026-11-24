package com.gacorvalan.ratti.ui.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun EditorialInicio(
    estado: EstadoInicio,
    modifier: Modifier = Modifier,
    onSalir: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RattiSpace.screenMargin),
        horizontalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        ) {
            Text(
                text = estado.ciclo,
                modifier = Modifier
                    .clip(RoundedCornerShape(RattiRadius.pill))
                    .background(RattiColor.Ink)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = RattiType.editorialPill,
                color = RattiColor.White,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = if (onSalir != null) Modifier.weight(1f) else Modifier,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = estado.saludo,
                        style = RattiType.greeting,
                        color = RattiColor.Ink,
                        maxLines = 2,
                    )
                    if (estado.ejemplo) {
                        Text(
                            text = stringResource(R.string.datos_ejemplo),
                            style = RattiType.txMeta,
                            color = RattiColor.Ink72,
                        )
                    }
                }
                if (onSalir != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = onSalir,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.salir),
                            style = RattiType.link,
                            color = RattiColor.Cobalt,
                        )
                    }
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_sol),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.micro_decorativo),
                style = RattiType.microJp,
                color = RattiColor.Ink,
                textAlign = TextAlign.Center,
            )
        }
    }
}
