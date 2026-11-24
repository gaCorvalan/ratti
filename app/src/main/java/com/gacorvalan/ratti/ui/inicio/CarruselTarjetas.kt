package com.gacorvalan.ratti.ui.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun CarruselTarjetas(
    tarjetas: List<TarjetaInicio>,
    onTarjeta: (TarjetaInicio) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
    ) {
        Text(
            text = stringResource(R.string.tus_tarjetas),
            modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
            style = RattiType.sectionTitle,
            color = RattiColor.Ink,
        )
        if (tarjetas.isEmpty()) {
            Text(
                text = stringResource(R.string.sin_tarjetas),
                modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
                style = RattiType.meta,
                color = RattiColor.Ink72,
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = RattiSpace.screenMargin),
                horizontalArrangement = Arrangement.spacedBy(RattiSpace.rowPadding),
            ) {
                items(tarjetas, key = { it.id }) { tarjeta ->
                    TarjetaTile(
                        tarjeta = tarjeta,
                        onClick = { onTarjeta(tarjeta) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaTile(
    tarjeta: TarjetaInicio,
    onClick: () -> Unit,
) {
    val fondo = when (tarjeta.color) {
        ColorTarjeta.Rosa -> RattiColor.Pink
        ColorTarjeta.Naranja -> RattiColor.Tangerine
    }
    Box(
        modifier = Modifier
            .width(252.dp)
            .height(94.dp)
            .clip(RoundedCornerShape(RattiRadius.tile))
            .background(fondo)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_orbe_tile),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .offset(x = 196.dp, y = (-18).dp),
        )
        Column(
            modifier = Modifier.padding(RattiSpace.rowPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = tarjeta.marca,
                    style = RattiType.tileBrand,
                    color = RattiColor.Ink,
                )
                if (tarjeta.banco.isNotEmpty()) {
                    Text(
                        text = tarjeta.banco,
                        modifier = Modifier.weight(1f),
                        style = RattiType.meta,
                        color = RattiColor.Ink70,
                        textAlign = TextAlign.End,
                    )
                }
            }
            MontoQueEntra(
                texto = pesos(tarjeta.monto),
                estilo = RattiType.cardAmount,
                color = RattiColor.Ink,
                maximo = 26.sp,
                minimo = 16.sp,
            )
            Text(
                text = stringResource(R.string.vence_el, tarjeta.venceEl),
                style = RattiType.captionBold,
                color = RattiColor.Ink,
            )
        }
    }
}
