package com.gacorvalan.ratti.ui.ahorros

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.inicio.CargaInicio
import com.gacorvalan.ratti.ui.inicio.InicioViewModel
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTabMetrics
import com.gacorvalan.ratti.ui.theme.RattiType

private enum class LugarAhorro { Resumen, Colchon, Casa, Dolares }

/**
 * Lee el [InicioViewModel] que arma [com.gacorvalan.ratti.RattiApp].
 * Cambiar a esta pestaña no vuelve a pedir la planilla.
 */
@Composable
internal fun AhorrosTab(
    inicio: InicioViewModel,
    email: String?,
) {
    val context = LocalContext.current
    var lugarNombre by rememberSaveable { mutableStateOf(LugarAhorro.Resumen.name) }
    LaunchedEffect(email) {
        if (email.isNullOrBlank()) {
            lugarNombre = LugarAhorro.Resumen.name
        }
    }
    val lugar = runCatching { LugarAhorro.valueOf(lugarNombre) }.getOrDefault(LugarAhorro.Resumen)
    val carga = inicio.carga
    val enDetalle = carga is CargaInicio.Listo && lugar != LugarAhorro.Resumen
    BackHandler(enabled = enDetalle) {
        lugarNombre = LugarAhorro.Resumen.name
    }

    when (carga) {
        CargaInicio.SinSesion -> MarcoAhorros {
            TextoEstado(stringResource(R.string.ahorros_entrar))
        }
        CargaInicio.Cargando -> MarcoAhorros {
            Cargando()
        }
        is CargaInicio.Error -> MarcoAhorros {
            ErrorAhorros(carga.mensaje, onReintentar = { inicio.refrescar(context) })
        }
        is CargaInicio.Listo -> AhorrosListo(
            carga = carga,
            lugar = lugar,
            onLugar = { lugarNombre = it.name },
            onVolver = { lugarNombre = LugarAhorro.Resumen.name },
        )
    }
}

@Composable
private fun AhorrosListo(
    carga: CargaInicio.Listo,
    lugar: LugarAhorro,
    onLugar: (LugarAhorro) -> Unit,
    onVolver: () -> Unit,
) {
    val resumen = resumenDe(carga.finanzas.ahorros, carga.finanzas.tipoCambio)
    when (lugar) {
        LugarAhorro.Resumen -> AhorrosScreen(
            resumen = resumen,
            onColchon = { onLugar(LugarAhorro.Colchon) },
            onCasa = { onLugar(LugarAhorro.Casa) },
            onDolares = { onLugar(LugarAhorro.Dolares) },
            aviso = carga.aviso,
        )
        LugarAhorro.Colchon -> DetalleColchon(resumen.colchon, onVolver)
        LugarAhorro.Casa -> DetalleCasa(resumen.casa, onVolver)
        LugarAhorro.Dolares -> DetalleDolares(resumen.dolares, resumen.tipoCambio, onVolver)
    }
}

@Composable
private fun Cargando() {
    Column(verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner)) {
        CircularProgressIndicator(
            modifier = Modifier.size(RattiTabMetrics.icon),
            color = RattiColor.Cobalt,
            strokeWidth = RattiRadius.track,
        )
        TextoEstado(stringResource(R.string.leyendo_planilla))
    }
}

@Composable
private fun ErrorAhorros(
    mensaje: String,
    onReintentar: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner)) {
        TextoEstado(mensaje)
        Text(
            text = stringResource(R.string.reintentar),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onReintentar,
            ),
            style = RattiType.link,
            color = RattiColor.Cobalt,
        )
    }
}

@Composable
private fun TextoEstado(texto: String) {
    Text(
        text = texto,
        style = RattiType.pocketCaption,
        color = RattiColor.Ink,
    )
}
