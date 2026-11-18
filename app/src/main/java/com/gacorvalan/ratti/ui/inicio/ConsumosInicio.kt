package com.gacorvalan.ratti.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun ConsumosInicio(
    consumos: List<ConsumoInicio>,
    onVerTodos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RattiSpace.screenMargin),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.ultimos_consumos),
                modifier = Modifier.weight(1f),
                style = RattiType.sectionTitle,
                color = RattiColor.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.ver_todos),
                modifier = Modifier
                    .padding(4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onVerTodos,
                    ),
                style = RattiType.link,
                color = RattiColor.Cobalt,
                textAlign = TextAlign.End,
            )
        }
        if (consumos.isEmpty()) {
            Text(
                text = stringResource(R.string.sin_consumos),
                style = RattiType.meta,
                color = RattiColor.Ink72,
            )
        } else {
            consumos.take(3).forEach { consumo ->
                FilaConsumo(consumo)
            }
        }
    }
}

@Composable
private fun FilaConsumo(consumo: ConsumoInicio) {
    val fondo = when (consumo.color) {
        ColorConsumo.Teal -> RattiColor.Teal
        ColorConsumo.Rosa -> RattiColor.Pink
        ColorConsumo.Naranja -> RattiColor.Tangerine
        ColorConsumo.Lima -> RattiColor.Lime
        ColorConsumo.Cobalto -> RattiColor.Cobalt
    }
    val tinta = if (consumo.color == ColorConsumo.Cobalto) RattiColor.White else RattiColor.Ink
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(71.dp)
            .clip(RoundedCornerShape(RattiRadius.row))
            .background(RattiColor.White)
            .padding(horizontal = RattiSpace.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(RattiSpace.rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoxInicial(inicial = consumo.inicial, fondo = fondo, tinta = tinta)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = consumo.comercio,
                style = RattiType.merchant,
                color = RattiColor.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = consumo.categoria,
                style = RattiType.txMeta,
                color = RattiColor.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = pesos(consumo.monto),
            style = RattiType.txAmount,
            color = RattiColor.Ink,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun BoxInicial(
    inicial: String,
    fondo: Color,
    tinta: Color,
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .background(fondo, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = inicial,
            style = RattiType.badgeInitial,
            color = tinta,
            textAlign = TextAlign.Center,
        )
    }
}
