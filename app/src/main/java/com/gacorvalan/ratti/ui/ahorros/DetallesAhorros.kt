package com.gacorvalan.ratti.ui.ahorros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTheme
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun DetalleColchon(
    colchon: ColchonUi,
    onVolver: () -> Unit,
) {
    MarcoAhorros(encabezado = {
        NavAhorros(stringResource(R.string.ahorros_nav_colchon), onVolver)
    }) {
        TarjetaClara {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Overline(stringResource(R.string.ahorros_colchon))
                colchon.porcentaje?.let { Overline(porcentajeFirmado(it)) }
            }
            MontoGrande(colchon.pesos?.let(::pesosDe) ?: stringResource(R.string.ahorros_no_disponible))
            Caption(captionMeta(colchon))
            colchon.fraccion?.let { BarraColchon(it) }
            Caption(stringResource(R.string.ahorros_cien_pesos))
        }
        TarjetaDonde(colchon.donde)
        Column(verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner)) {
            Overline(stringResource(R.string.ahorros_movimientos))
            FilaClara {
                Text(
                    text = stringResource(R.string.ahorros_sin_movimientos),
                    style = RattiType.pocketCaption,
                    color = RattiColor.Ink72,
                )
            }
        }
    }
}

@Composable
internal fun DetalleCasa(
    casa: CasaUi,
    onVolver: () -> Unit,
) {
    val enRojo = (casa.resultado ?: 0.0) < 0.0 && casa.porcentaje != null
    val colorResultado = if (enRojo) RattiColor.DangerDark else RattiColor.Ink
    MarcoAhorros(encabezado = {
        NavAhorros(stringResource(R.string.ahorros_nav_casa), onVolver)
    }) {
        TarjetaClara {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Overline(stringResource(R.string.ahorros_casa))
                casa.porcentaje?.let { Overline(porcentajeFirmado(it), colorResultado) }
            }
            MontoGrande(casa.mercado?.let(::dolares) ?: stringResource(R.string.ahorros_no_disponible))
            Caption(precioCasa(casa))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Overline(stringResource(R.string.ahorros_costo))
                    Text(
                        text = casa.costo?.let(::dolares) ?: stringResource(R.string.ahorros_no_disponible),
                        style = RattiType.rowTitle,
                        color = RattiColor.Ink,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Overline(stringResource(R.string.ahorros_resultado), colorResultado)
                    Text(
                        text = casa.resultado?.let(::dolares) ?: stringResource(R.string.ahorros_no_disponible),
                        style = RattiType.rowTitle,
                        color = colorResultado,
                        textAlign = TextAlign.End,
                    )
                }
            }
            Caption(
                casa.cuotapartes?.let { papeles ->
                    stringResource(R.string.ahorros_papeles, papeles(papeles))
                } ?: stringResource(R.string.ahorros_sin_papeles),
            )
            Caption(
                casa.metaUsd?.let { meta ->
                    stringResource(R.string.ahorros_meta_casa, dolares(meta))
                } ?: stringResource(R.string.ahorros_sin_meta_fija),
            )
            Text(
                text = stringResource(R.string.ahorros_sin_lotes),
                style = RattiType.lotNote,
                color = RattiColor.Ink72,
            )
        }
        casa.donde?.let { TarjetaDonde(it) }
    }
}

@Composable
internal fun DetalleDolares(
    dolares: DolaresUi,
    tipoCambio: Double?,
    onVolver: () -> Unit,
) {
    MarcoAhorros(
        gap = RattiSpace.sectionInner,
        encabezado = { NavAhorros(stringResource(R.string.ahorros_nav_dolares), onVolver) },
    ) {
        TarjetaClara(fondo = RattiColor.Lime) {
            Overline(stringResource(R.string.ahorros_dolares))
            MontoGrande(usdc(dolares.sinAsignar))
            Caption(captionSinAsignar(dolares))
        }
        dolares.donde?.let { TarjetaDonde(it) }
        dolares.objetivos.forEach { objetivo ->
            FilaObjetivo(objetivo)
        }
        Text(
            text = if (tipoCambio != null) {
                stringResource(R.string.ahorros_tc, tipoCambioTexto(tipoCambio))
            } else {
                stringResource(R.string.ahorros_sin_tc)
            },
            modifier = Modifier.padding(horizontal = 4.dp),
            style = RattiType.lotNote,
            color = RattiColor.Ink72,
        )
    }
}

@Composable
private fun FilaObjetivo(objetivo: ObjetivoUi) {
    FilaClara {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = objetivo.nombre,
                modifier = Modifier.weight(1f),
                style = RattiType.rowTitle,
                color = RattiColor.Ink,
            )
            Overline(
                if (objetivo.fondo) {
                    stringResource(R.string.ahorros_fondo)
                } else {
                    stringResource(R.string.ahorros_sin_meta_etiqueta)
                },
            )
        }
        Caption(captionObjetivo(objetivo))
    }
}

@Composable
private fun TarjetaDonde(lugar: String?) {
    TarjetaClara {
        Overline(stringResource(R.string.ahorros_donde))
        if (lugar.isNullOrBlank()) {
            Caption(stringResource(R.string.ahorros_donde_vacio))
        } else {
            Text(
                text = lugar,
                style = RattiType.bankName,
                color = RattiColor.Ink,
            )
        }
    }
}

@Composable
private fun captionObjetivo(objetivo: ObjetivoUi): String {
    val monto = dolares(objetivo.monto)
    val meta = objetivo.metaUsd?.let(::dolares)
    return when {
        objetivo.fondo && meta != null -> stringResource(R.string.ahorros_fondo_meta, monto, meta)
        objetivo.fondo -> stringResource(R.string.ahorros_fondo_caption, monto)
        meta != null -> stringResource(R.string.ahorros_meta_usd, monto, meta)
        else -> stringResource(R.string.ahorros_meta_no_disponible, monto)
    }
}

@Composable
private fun captionMeta(colchon: ColchonUi): String {
    val meta = colchon.meta ?: return stringResource(R.string.ahorros_sin_meta)
    val diferencia = colchon.diferencia ?: return stringResource(R.string.ahorros_de_meta, pesosDe(meta))
    val metaTexto = pesosDe(meta)
    return when {
        diferencia > 0L -> stringResource(R.string.ahorros_faltan, metaTexto, pesosDe(diferencia))
        diferencia < 0L -> stringResource(R.string.ahorros_pasaste_meta, metaTexto, pesosDe(-diferencia))
        else -> stringResource(R.string.ahorros_llegaste, metaTexto)
    }
}

@Composable
private fun precioCasa(casa: CasaUi): String {
    val precio = casa.precio ?: return stringResource(R.string.ahorros_sin_precio)
    val fecha = casa.fechaPrecio?.let(::fechaCorta)
    return if (fecha != null) {
        stringResource(R.string.ahorros_precio_fecha, dolares(precio), fecha)
    } else {
        stringResource(R.string.ahorros_precio, dolares(precio))
    }
}

@Composable
private fun captionSinAsignar(dolares: DolaresUi): String {
    val total = usdc(dolares.total)
    return when {
        dolares.sinAsignar < 0.0 -> stringResource(R.string.ahorros_sin_asignar_pasa, total)
        dolares.sinAsignar == dolares.total -> stringResource(R.string.ahorros_sin_asignar_listo)
        else -> stringResource(R.string.ahorros_sin_asignar_cierra, total)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun ColchonPreview() {
    RattiTheme {
        DetalleColchon(ejemploConTipoDeCambio().colchon, onVolver = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun CasaPreview() {
    RattiTheme {
        DetalleCasa(ejemploConTipoDeCambio().casa, onVolver = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun DolaresPreview() {
    val resumen = ejemploConTipoDeCambio()
    RattiTheme {
        DetalleDolares(resumen.dolares, resumen.tipoCambio, onVolver = {})
    }
}
