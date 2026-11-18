package com.gacorvalan.ratti.ui.tarjeta

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.datos.PlanillaRepositorio
import com.gacorvalan.ratti.ui.inicio.CargaInicio
import com.gacorvalan.ratti.ui.inicio.ColorConsumo
import com.gacorvalan.ratti.ui.inicio.ColorTarjeta
import com.gacorvalan.ratti.ui.inicio.InicioViewModel
import com.gacorvalan.ratti.ui.inicio.pesos
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTheme
import com.gacorvalan.ratti.ui.theme.RattiType
import java.time.LocalDate

private enum class Pestana { Consumos, Cuotas }

@Composable
internal fun DetalleTarjetaScreen(
    tarjetaId: String,
    onVolver: () -> Unit,
    inicio: InicioViewModel = viewModel { InicioViewModel(PlanillaRepositorio()) },
) {
    val finanzas = (inicio.carga as? CargaInicio.Listo)?.finanzas
    val detalle = finanzas?.let { detalleDe(it, tarjetaId, LocalDate.now()) }
    var pestanaNombre by rememberSaveable(tarjetaId) { mutableStateOf(Pestana.Consumos.name) }
    val pestana = Pestana.valueOf(pestanaNombre)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        NavEncabezado(
            titulo = detalle?.titulo ?: stringResource(R.string.titulo_tarjeta),
            onVolver = onVolver,
        )
        val aviso = avisoDeCarga(inicio.carga)
        when {
            aviso != null -> Vacio(aviso)
            detalle == null -> Vacio(stringResource(R.string.no_encontre_tarjeta))
            else -> CuerpoDetalle(
                detalle = detalle,
                pestana = pestana,
                onPestana = { pestanaNombre = it.name },
            )
        }
    }
}

@Composable
internal fun ConsumosCicloScreen(
    onVolver: () -> Unit,
    inicio: InicioViewModel = viewModel { InicioViewModel(PlanillaRepositorio()) },
) {
    val finanzas = (inicio.carga as? CargaInicio.Listo)?.finanzas
    val lista = finanzas?.let { consumosDelCiclo(it, LocalDate.now()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        NavEncabezado(
            titulo = stringResource(R.string.titulo_consumos),
            onVolver = onVolver,
        )
        val aviso = avisoDeCarga(inicio.carga)
        when {
            aviso != null -> Vacio(aviso)
            lista == null -> Vacio(stringResource(R.string.sin_consumos_ciclo))
            else -> ColumnaScroll {
                Text(
                    text = lista.ciclo,
                    style = RattiType.pocketCaption,
                    color = RattiColor.Ink,
                )
                if (lista.consumos.isEmpty()) {
                    VacioEnLista(stringResource(R.string.sin_consumos_ciclo))
                } else {
                    lista.consumos.forEach { consumo -> FilaConsumoDetalle(consumo) }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.CuerpoDetalle(
    detalle: DetalleTarjeta,
    pestana: Pestana,
    onPestana: (Pestana) -> Unit,
) {
    ColumnaScroll {
        HeroTarjeta(detalle)
        Segmentos(activa = pestana, onElegir = onPestana)
        when (pestana) {
            Pestana.Consumos -> if (detalle.consumos.isEmpty()) {
                VacioEnLista(stringResource(R.string.sin_consumos_tarjeta))
            } else {
                detalle.consumos.forEach { FilaConsumoDetalle(it) }
            }
            Pestana.Cuotas -> if (detalle.cuotas.isEmpty()) {
                VacioEnLista(stringResource(R.string.sin_cuotas))
            } else {
                detalle.cuotas.forEach { FilaCuota(it) }
            }
        }
    }
}

@Composable
private fun ColumnScope.ColumnaScroll(contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = RattiSpace.screenMargin)
            .padding(bottom = RattiSpace.contentBottom),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
    ) {
        contenido()
    }
}

@Composable
private fun NavEncabezado(
    titulo: String,
    onVolver: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = RattiSpace.cardPadding, vertical = 4.dp),
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
            Icon(
                painter = painterResource(R.drawable.ic_nav_atras),
                contentDescription = stringResource(R.string.volver),
                tint = RattiColor.Ink,
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
        Spacer(Modifier.size(44.dp))
    }
}

@Composable
private fun HeroTarjeta(detalle: DetalleTarjeta) {
    val fondo = when (detalle.color) {
        ColorTarjeta.Rosa -> RattiColor.Pink
        ColorTarjeta.Naranja -> RattiColor.Tangerine
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RattiRadius.hero))
            .background(fondo),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_orbe_detalle),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 16.dp, y = (-28).dp)
                .size(88.dp),
        )
        Column(
            modifier = Modifier.padding(RattiSpace.cardPadding),
            verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        ) {
            Text(
                text = detalle.pastilla,
                modifier = Modifier
                    .clip(RoundedCornerShape(RattiRadius.pill))
                    .background(RattiColor.Ink)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = RattiType.pill,
                color = RattiColor.White,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.total_a_pagar),
                        style = RattiType.heroLabel,
                        color = RattiColor.Ink,
                    )
                    Text(
                        text = pesos(detalle.total),
                        style = RattiType.screenTitle,
                        color = RattiColor.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.vencimiento),
                        style = RattiType.heroLabel,
                        color = RattiColor.Ink,
                    )
                    Text(
                        text = detalle.vencimiento,
                        style = RattiType.dueDate,
                        color = RattiColor.Ink,
                    )
                }
            }
            Text(
                text = detalle.ciclo,
                style = RattiType.meta,
                color = RattiColor.Ink,
            )
        }
    }
}

@Composable
private fun Segmentos(
    activa: Pestana,
    onElegir: (Pestana) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(RattiRadius.tile))
            .background(RattiColor.White)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Pestana.entries.forEach { pestana ->
            val elegido = pestana == activa
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(RattiRadius.segment))
                    .background(if (elegido) RattiColor.Lime else Color.Transparent)
                    .selectable(
                        selected = elegido,
                        onClick = { onElegir(pestana) },
                        role = Role.Tab,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (pestana == Pestana.Consumos) R.string.pestana_consumos else R.string.pestana_cuotas,
                    ),
                    style = RattiType.segment,
                    color = RattiColor.Ink,
                )
            }
        }
    }
}

@Composable
private fun FilaConsumoDetalle(consumo: ConsumoDetalle) {
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
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(fondo, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = consumo.inicial,
                style = RattiType.badgeInitial,
                color = tinta,
                textAlign = TextAlign.Center,
            )
        }
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
                text = consumo.meta,
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
        )
    }
}

@Composable
private fun FilaCuota(cuota: CuotaDetalle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RattiRadius.row))
            .background(RattiColor.White)
            .padding(RattiSpace.rowPadding),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = cuota.descripcion,
                modifier = Modifier.weight(1f),
                style = RattiType.rowTitle,
                color = RattiColor.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pesos(cuota.monto),
                style = RattiType.rowTitle,
                color = RattiColor.Ink,
            )
        }
        Row {
            Text(
                text = cuota.avance,
                modifier = Modifier.weight(1f),
                style = RattiType.meta,
                color = RattiColor.Ink,
            )
            Text(
                text = stringResource(R.string.restan, pesos(cuota.restan)),
                style = RattiType.meta,
                color = RattiColor.Ink,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(RattiRadius.track))
                .background(RattiColor.Ink12),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(cuota.fraccion.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(RattiRadius.track))
                    .background(RattiColor.Pink),
            )
        }
    }
}

@Composable
private fun avisoDeCarga(carga: CargaInicio): String? = when (carga) {
    is CargaInicio.Listo -> null
    CargaInicio.Cargando -> stringResource(R.string.leyendo_planilla)
    CargaInicio.SinSesion -> stringResource(R.string.detalle_sin_sesion)
    is CargaInicio.Error -> carga.mensaje
}

@Composable
private fun Vacio(texto: String) {
    Text(
        text = texto,
        modifier = Modifier.padding(horizontal = RattiSpace.screenMargin, vertical = RattiSpace.sectionGap),
        style = RattiType.pocketCaption,
        color = RattiColor.Ink,
    )
}

@Composable
private fun VacioEnLista(texto: String) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RattiRadius.row))
            .background(RattiColor.White)
            .padding(RattiSpace.cardPadding),
        style = RattiType.pocketCaption,
        color = RattiColor.Ink72,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun DetalleConsumosPreview() {
    RattiTheme {
        Column(Modifier.statusBarsPadding()) {
            NavEncabezado(titulo = "Visa", onVolver = {})
            CuerpoDetalle(detalle = ejemploVisa, pestana = Pestana.Consumos, onPestana = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun DetalleCuotasPreview() {
    RattiTheme {
        Column(Modifier.statusBarsPadding()) {
            NavEncabezado(titulo = "Visa", onVolver = {})
            CuerpoDetalle(detalle = ejemploVisa, pestana = Pestana.Cuotas, onPestana = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun DetalleVacioPreview() {
    RattiTheme {
        Column(Modifier.statusBarsPadding()) {
            NavEncabezado(titulo = "Amex", onVolver = {})
            CuerpoDetalle(
                detalle = ejemploVisa.copy(
                    titulo = "Amex",
                    pastilla = "AMEX",
                    color = ColorTarjeta.Naranja,
                    consumos = emptyList(),
                    cuotas = emptyList(),
                ),
                pestana = Pestana.Consumos,
                onPestana = {},
            )
        }
    }
}

private val ejemploVisa = DetalleTarjeta(
    titulo = "Visa",
    pastilla = "VISA",
    total = 487_320L,
    vencimiento = "12/11",
    ciclo = "Noviembre, cierra el 26/11",
    color = ColorTarjeta.Rosa,
    consumos = listOf(
        ConsumoDetalle("1", "Jumbo", "SUPER · HOY", 48_320L, "J", ColorConsumo.Teal),
        ConsumoDetalle("2", "Rappi", "COMIDA FUERA · HOY", 12_450L, "R", ColorConsumo.Rosa),
        ConsumoDetalle("3", "YPF", "TRANSPORTE · AYER", 32_100L, "Y", ColorConsumo.Naranja),
        ConsumoDetalle("4", "Coto", "SUPER · 8 NOV", 67_890L, "C", ColorConsumo.Lima),
        ConsumoDetalle("5", "Netflix", "SUSCRIPCIONES · 7 NOV", 25_105L, "N", ColorConsumo.Cobalto),
    ),
    cuotas = listOf(
        CuotaDetalle("tv", "TV Samsung", 85_400L, "2 DE 6", 341_600L, 2f / 6f),
        CuotaDetalle("note", "Notebook Lenovo", 62_150L, "4 DE 12", 497_200L, 4f / 12f),
        CuotaDetalle("seguro", "Seguro del auto", 28_900L, "5 DE 12", 202_300L, 5f / 12f),
        CuotaDetalle("ml", "Mercado Libre", 45_200L, "1 DE 3", 90_400L, 1f / 3f),
    ),
)
