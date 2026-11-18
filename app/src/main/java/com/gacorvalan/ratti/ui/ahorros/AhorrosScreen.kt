package com.gacorvalan.ratti.ui.ahorros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTheme
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun AhorrosScreen(
    resumen: ResumenAhorros,
    onColchon: () -> Unit,
    onCasa: () -> Unit,
    onDolares: () -> Unit,
    aviso: String? = null,
) {
    MarcoAhorros {
        if (aviso != null) {
            Text(
                text = aviso,
                style = RattiType.meta,
                color = RattiColor.DangerDark,
            )
        }
        HeroTotal(resumen)
        TarjetaColchon(resumen.colchon, onClick = onColchon)
        TarjetaCasa(resumen.casa, onClick = onCasa)
        TarjetaDolares(resumen.dolares, onClick = onDolares)
    }
}

@Composable
private fun HeroTotal(resumen: ResumenAhorros) {
    TarjetaClara {
        Overline(stringResource(R.string.ahorros_total))
        MontoGrande(
            resumen.totalPesos?.let(::pesosDe) ?: stringResource(R.string.ahorros_no_disponible),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Caption(stringResource(R.string.ahorros_a_mercado, usdONo(resumen.usdMercado)))
            Caption(stringResource(R.string.ahorros_a_costo, usdONo(resumen.usdCosto)))
        }
    }
}

@Composable
private fun TarjetaColchon(colchon: ColchonUi, onClick: () -> Unit) {
    val marca = colchon.porcentaje?.let(::porcentajeFirmado)
    TarjetaClara(onClick = onClick) {
        CabezalBolsillo(
            swatch = R.drawable.ic_swatch_teal,
            titulo = stringResource(R.string.ahorros_colchon),
            marca = marca,
            marcaEnRojo = false,
        )
        MontoGrande(colchon.pesos?.let(::pesosDe) ?: stringResource(R.string.ahorros_no_disponible))
        Caption(
            if (colchon.meta != null) {
                stringResource(R.string.ahorros_de_meta, pesosDe(colchon.meta))
            } else {
                stringResource(R.string.ahorros_sin_meta)
            },
        )
        colchon.fraccion?.let { BarraColchon(it) }
    }
}

@Composable
private fun TarjetaCasa(casa: CasaUi, onClick: () -> Unit) {
    val enRojo = (casa.resultado ?: 0.0) < 0.0 && casa.porcentaje != null
    TarjetaClara(onClick = onClick) {
        CabezalBolsillo(
            swatch = R.drawable.ic_swatch_pink,
            titulo = stringResource(R.string.ahorros_casa),
            marca = casa.porcentaje?.let(::porcentajeFirmado),
            marcaEnRojo = enRojo,
        )
        MontoGrande(usdONo(casa.mercado))
        Caption(captionCasa(casa), enRojo = enRojo)
    }
}

@Composable
private fun TarjetaDolares(dolares: DolaresUi, onClick: () -> Unit) {
    TarjetaClara(onClick = onClick) {
        CabezalBolsillo(
            swatch = R.drawable.ic_swatch_lime,
            titulo = stringResource(R.string.ahorros_dolares),
            marca = null,
            marcaEnRojo = false,
        )
        MontoGrande(usdc(dolares.total))
        Caption(
            stringResource(
                R.string.ahorros_sin_asignar_objetivos,
                usdc(dolares.sinAsignar),
                dolares.objetivos.size,
            ),
        )
    }
}

@Composable
private fun captionCasa(casa: CasaUi): String {
    val resultado = casa.resultado
    val costo = casa.costo
    val porcentaje = casa.porcentaje
    if (resultado == null || costo == null || porcentaje == null) {
        return stringResource(R.string.ahorros_sin_costo)
    }
    return stringResource(
        R.string.ahorros_vs_costo,
        dolares(resultado),
        porcentajeFirmado(porcentaje),
        dolares(costo),
    )
}

@Composable
private fun usdONo(valor: Double?): String =
    valor?.let(::dolares) ?: stringResource(R.string.ahorros_no_disponible)

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun AhorrosScreenPreview() {
    RattiTheme {
        AhorrosScreen(
            resumen = ejemploConTipoDeCambio(),
            onColchon = {},
            onCasa = {},
            onDolares = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun AhorrosSinCambioPreview() {
    RattiTheme {
        AhorrosScreen(
            resumen = ejemploSinTipoDeCambio(),
            onColchon = {},
            onCasa = {},
            onDolares = {},
        )
    }
}
