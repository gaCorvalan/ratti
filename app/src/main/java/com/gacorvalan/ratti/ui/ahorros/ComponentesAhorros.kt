package com.gacorvalan.ratti.ui.ahorros

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiType

private val Tarjeta = RoundedCornerShape(RattiRadius.savingsCard)
private val Fila = RoundedCornerShape(RattiRadius.row)
private val Pista = RoundedCornerShape(RattiRadius.track)

@Composable
internal fun MarcoAhorros(
    gap: Dp = RattiSpace.sectionGap,
    encabezado: @Composable () -> Unit = { EncabezadoAhorros() },
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        encabezado()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = RattiSpace.contentBottom)
                .padding(horizontal = RattiSpace.screenMargin)
                .padding(top = RattiSpace.sectionInner),
            verticalArrangement = Arrangement.spacedBy(gap),
            content = contenido,
        )
    }
}

@Composable
internal fun EncabezadoAhorros() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = RattiSpace.screenMargin),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        ) {
            Text(
                text = stringResource(R.string.ahorros_titulo),
                style = RattiType.screenTitle,
                color = RattiColor.Ink,
            )
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_destello_ahorros),
                    contentDescription = null,
                    modifier = Modifier.size(width = 26.6296.dp, height = 28.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RattiSpace.screenMargin)
                .height(2.dp)
                .background(RattiColor.Ink),
        )
    }
}

@Composable
internal fun NavAhorros(
    titulo: String,
    onVolver: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onVolver,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_nav_atras),
                contentDescription = stringResource(R.string.ahorros_volver),
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = titulo,
            modifier = Modifier.weight(1f),
            style = RattiType.navTitle,
            color = RattiColor.Ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(modifier = Modifier.size(44.dp))
    }
}

@Composable
internal fun TarjetaClara(
    modifier: Modifier = Modifier,
    fondo: Color = RattiColor.White,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val toque = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Tarjeta)
            .background(fondo)
            .then(
                if (onClick == null) {
                    Modifier
                } else {
                    Modifier.clickable(
                        interactionSource = toque,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                },
            )
            .padding(RattiSpace.cardPadding),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        content = contenido,
    )
}

@Composable
internal fun FilaClara(
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Fila)
            .background(RattiColor.White)
            .padding(RattiSpace.rowPadding),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        content = contenido,
    )
}

@Composable
internal fun CabezalBolsillo(
    @DrawableRes swatch: Int,
    titulo: String,
    marca: String?,
    marcaEnRojo: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        ) {
            Image(
                painter = painterResource(swatch),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = titulo,
                style = RattiType.overline,
                color = RattiColor.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (marca != null) {
            Text(
                text = marca,
                style = RattiType.overline,
                color = if (marcaEnRojo) RattiColor.DangerDark else RattiColor.Ink,
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_chevron_bolsillo),
            contentDescription = null,
            modifier = Modifier
                .padding(start = RattiSpace.sectionInner)
                .size(20.dp),
        )
    }
}

@Composable
internal fun MontoGrande(texto: String) {
    Text(
        text = texto,
        style = RattiType.screenTitle,
        color = RattiColor.Ink,
    )
}

@Composable
internal fun Caption(
    texto: String,
    enRojo: Boolean = false,
) {
    Text(
        text = texto,
        style = RattiType.pocketCaption,
        color = if (enRojo) RattiColor.DangerDark else RattiColor.Ink72,
    )
}

@Composable
internal fun BarraColchon(fraccion: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(Pista)
            .background(RattiColor.Ink12),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .background(RattiColor.Lime),
        )
    }
}

@Composable
internal fun Overline(texto: String, color: Color = RattiColor.Ink) {
    Text(
        text = texto,
        style = RattiType.overline,
        color = color,
    )
}
