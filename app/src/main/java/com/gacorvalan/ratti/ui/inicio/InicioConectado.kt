package com.gacorvalan.ratti.ui.inicio

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.auth.TokenPlanillasException
import com.gacorvalan.ratti.auth.tokenParaLeerPlanillas
import com.gacorvalan.ratti.datos.ErrorPlanilla
import com.gacorvalan.ratti.datos.PlanillaRepositorio
import com.gacorvalan.ratti.datos.modelo.ErrorModelo
import com.gacorvalan.ratti.ui.InicioSesion
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTabMetrics
import com.gacorvalan.ratti.ui.theme.RattiType
import kotlin.coroutines.cancellation.CancellationException

internal sealed interface CargaInicio {
    data object SinSesion : CargaInicio
    data object Cargando : CargaInicio
    data class Listo(
        val modelo: EstadoInicio,
        val actualizando: Boolean = false,
    ) : CargaInicio
    data class Error(val mensaje: String) : CargaInicio
}

@Composable
internal fun InicioConectado(
    email: String?,
    errorSesion: String?,
    ocupado: Boolean,
    onEntrar: () -> Unit,
    onSalir: () -> Unit,
    onDatosCrudos: (() -> Unit)?,
) {
    val context = LocalContext.current
    val repositorio = remember { PlanillaRepositorio() }
    var carga by remember { mutableStateOf<CargaInicio>(CargaInicio.SinSesion) }
    var pedido by remember { mutableIntStateOf(0) }

    LaunchedEffect(email, pedido) {
        if (email.isNullOrBlank()) {
            carga = CargaInicio.SinSesion
            return@LaunchedEffect
        }
        val previo = carga
        carga = if (previo is CargaInicio.Listo) {
            previo.copy(actualizando = true)
        } else {
            CargaInicio.Cargando
        }
        carga = leerInicio(context, repositorio)
    }

    val recargar = {
        val actual = carga
        val enCurso = actual is CargaInicio.Cargando ||
            (actual is CargaInicio.Listo && actual.actualizando)
        if (!email.isNullOrBlank() && !enCurso) pedido += 1
    }

    when (val estado = carga) {
        CargaInicio.SinSesion -> MarcoSimple {
            BloqueSesion(email, errorSesion, ocupado, onEntrar, onSalir, onDatosCrudos, null)
        }
        CargaInicio.Cargando -> MarcoSimple {
            Leyendo()
            BloqueSesion(email, errorSesion, ocupado, onEntrar, onSalir, onDatosCrudos, null)
        }
        is CargaInicio.Error -> MarcoSimple {
            MensajeError(estado.mensaje, onReintentar = recargar)
            BloqueSesion(email, errorSesion, ocupado, onEntrar, onSalir, onDatosCrudos, null)
        }
        is CargaInicio.Listo -> InicioListo(
            estado = estado,
            onRefresh = recargar,
            email = email,
            errorSesion = errorSesion,
            ocupado = ocupado,
            onEntrar = onEntrar,
            onSalir = onSalir,
            onDatosCrudos = onDatosCrudos,
        )
    }
}

@Composable
private fun InicioListo(
    estado: CargaInicio.Listo,
    onRefresh: () -> Unit,
    email: String?,
    errorSesion: String?,
    ocupado: Boolean,
    onEntrar: () -> Unit,
    onSalir: () -> Unit,
    onDatosCrudos: (() -> Unit)?,
) {
    val pull = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = estado.actualizando,
        onRefresh = onRefresh,
        state = pull,
        modifier = Modifier.fillMaxSize(),
        indicator = { IndicadorRefresh(pull, estado.actualizando) },
    ) {
        InicioScreen(
            estado = estado.modelo,
            email = email,
            errorSesion = errorSesion,
            ocupado = ocupado,
            onEntrar = onEntrar,
            onSalir = onSalir,
            onDatosCrudos = onDatosCrudos,
            onActualizar = onRefresh,
        )
    }
}

@Composable
private fun BoxScope.IndicadorRefresh(
    estado: PullToRefreshState,
    actualizando: Boolean,
) {
    PullToRefreshDefaults.Indicator(
        state = estado,
        isRefreshing = actualizando,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding(),
        color = RattiColor.Cobalt,
        containerColor = RattiColor.White,
    )
}

@Composable
private fun MarcoSimple(contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = RattiSpace.contentBottom),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionGap),
    ) {
        CabeceraInicio(inicial = "R")
        contenido()
    }
}

@Composable
private fun BloqueSesion(
    email: String?,
    errorSesion: String?,
    ocupado: Boolean,
    onEntrar: () -> Unit,
    onSalir: () -> Unit,
    onDatosCrudos: (() -> Unit)?,
    onActualizar: (() -> Unit)?,
) {
    InicioSesion(
        email = email,
        error = errorSesion,
        ocupado = ocupado,
        onEntrar = onEntrar,
        onSalir = onSalir,
        onDatosCrudos = onDatosCrudos,
        onActualizar = onActualizar,
        modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
    )
}

@Composable
private fun Leyendo() {
    Row(
        modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
        horizontalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(RattiTabMetrics.icon),
            color = RattiColor.Cobalt,
            strokeWidth = RattiRadius.track,
        )
        Text(
            text = stringResource(R.string.leyendo_planilla),
            style = RattiType.pocketCaption,
            color = RattiColor.Ink,
        )
    }
}

@Composable
private fun MensajeError(
    mensaje: String,
    onReintentar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = RattiSpace.screenMargin)
            .fillMaxWidth()
            .clip(RoundedCornerShape(RattiRadius.tile))
            .background(RattiColor.White)
            .padding(RattiSpace.cardPadding),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
    ) {
        Text(
            text = mensaje,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = RattiType.pocketCaption,
            color = RattiColor.Ink,
        )
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

private suspend fun leerInicio(
    context: Context,
    repositorio: PlanillaRepositorio,
): CargaInicio {
    return try {
        val modelo = repositorio.leerModelo(pedirToken = { tokenParaLeerPlanillas(context) })
        CargaInicio.Listo(inicioDe(modelo))
    } catch (e: CancellationException) {
        throw e
    } catch (e: TokenPlanillasException) {
        CargaInicio.Error(
            context.getString(
                if (e.sinConexion) R.string.error_red else R.string.error_planilla_permiso,
            ),
        )
    } catch (e: ErrorPlanilla) {
        CargaInicio.Error(context.getString(e.texto().id, *e.texto().args))
    } catch (e: ErrorModelo) {
        CargaInicio.Error(context.getString(R.string.error_modelo, e.message.orEmpty()))
    } catch (_: Exception) {
        CargaInicio.Error(context.getString(R.string.error_planilla_lectura))
    }
}

private data class TextoError(val id: Int, val args: Array<out Any> = emptyArray())

private fun ErrorPlanilla.texto(): TextoError = when (this) {
    ErrorPlanilla.SinConexion -> TextoError(R.string.error_red)
    ErrorPlanilla.Prohibido -> TextoError(R.string.error_planilla_prohibida)
    ErrorPlanilla.PermisoVencido -> TextoError(R.string.error_planilla_vencida)
    ErrorPlanilla.Ilegible -> TextoError(R.string.error_planilla_ilegible)
    ErrorPlanilla.NoEncontrada -> TextoError(R.string.error_planilla_no_encontrada)
    ErrorPlanilla.Lectura -> TextoError(R.string.error_planilla_lectura)
    is ErrorPlanilla.PestanaFaltante -> {
        val nombre = nombre
        if (nombre.isNullOrBlank()) {
            TextoError(R.string.error_planilla_pestana_alguna)
        } else {
            TextoError(R.string.error_planilla_pestana, arrayOf(nombre))
        }
    }
    is ErrorPlanilla.Http -> TextoError(R.string.error_planilla_http, arrayOf(codigo.toString()))
}
