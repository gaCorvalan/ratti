package com.gacorvalan.ratti.ui.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun TarjetaSaldoInicio(
    estado: EstadoInicio,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = RattiSpace.screenMargin)
                .fillMaxWidth()
                .clip(RoundedCornerShape(RattiRadius.hero))
                .background(RattiColor.Cobalt),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_orbe_turquesa),
                contentDescription = null,
                modifier = Modifier
                    .size(116.dp)
                    .offset(x = 246.dp, y = (-58).dp),
            )
            Box(
                modifier = Modifier
                    .offset(x = 230.dp, y = 36.dp)
                    .size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_destello_lima),
                    contentDescription = null,
                    modifier = Modifier.size(width = 38.0423.dp, height = 40.dp),
                )
            }
            Column(
                modifier = Modifier.padding(RattiSpace.cardPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val banda = estado.bandaSaldo
                val suave = banda.suave
                Text(
                    text = stringResource(banda.pastilla),
                    modifier = Modifier
                        .clip(RoundedCornerShape(RattiRadius.full))
                        .background(banda.fondo)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    style = RattiType.pill,
                    color = banda.tinta,
                )
                MontoQueEntra(
                    texto = pesos(estado.queda),
                    estilo = RattiType.amountHero,
                    color = RattiColor.White,
                    maximo = 34.sp,
                    minimo = 20.sp,
                )
                Box(
                    modifier = Modifier
                        .width(196.dp)
                        .height(4.dp)
                        .background(suave),
                )
                Text(
                    text = captionSaldo(estado),
                    style = RattiType.heroSubtitle,
                    color = RattiColor.White,
                )
                SemaforoInicio(estado)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 28.dp)
                .background(RattiColor.Lime),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = textoCinta(estado),
                modifier = Modifier.fillMaxWidth(),
                style = RattiType.overline,
                color = RattiColor.Ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
internal fun MontoQueEntra(
    texto: String,
    estilo: TextStyle,
    color: Color,
    maximo: TextUnit,
    minimo: TextUnit,
    modifier: Modifier = Modifier,
) {
    var tamano by remember(texto, maximo, minimo) { mutableStateOf(maximo) }
    val alto = estilo.lineHeight.value / estilo.fontSize.value
    Text(
        text = texto,
        modifier = modifier.fillMaxWidth(),
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        style = estilo.copy(fontSize = tamano, lineHeight = tamano * alto),
        onTextLayout = { resultado ->
            if (resultado.hasVisualOverflow && tamano > minimo) {
                val siguiente = (tamano.value * 0.92f).coerceAtLeast(minimo.value)
                tamano = siguiente.sp
            }
        },
    )
}

@Composable
private fun SemaforoInicio(estado: EstadoInicio) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(RattiRadius.full))
                .background(RattiColor.Black28),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(estado.fraccionRestante)
                    .clip(RoundedCornerShape(RattiRadius.full))
                    .background(estado.bandaSaldo.suave),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = textoUsaste(estado),
                style = RattiType.heroSubtitle,
                color = RattiColor.White,
            )
            Text(
                text = textoEstadoSaldo(estado),
                style = RattiType.saldoEstado,
                color = RattiColor.White,
            )
        }
    }
}

private val BandaSaldo.pastilla: Int
    get() = when (this) {
        BandaSaldo.Verde -> R.string.te_quedan_disponible
        BandaSaldo.Naranja -> R.string.te_quedan_atencion
        BandaSaldo.Rojo -> R.string.te_quedan_bajo
    }

private val BandaSaldo.fondo: Color
    get() = when (this) {
        BandaSaldo.Verde -> RattiColor.SaldoVerde
        BandaSaldo.Naranja -> RattiColor.SaldoAmbar
        BandaSaldo.Rojo -> RattiColor.SaldoRojo
    }

private val BandaSaldo.tinta: Color
    get() = if (this == BandaSaldo.Rojo) RattiColor.White else RattiColor.Ink

private val BandaSaldo.suave: Color
    get() = when (this) {
        BandaSaldo.Verde -> RattiColor.SaldoVerdeSuave
        BandaSaldo.Naranja -> RattiColor.SaldoAmbarSuave
        BandaSaldo.Rojo -> RattiColor.SaldoRojoSuave
    }
