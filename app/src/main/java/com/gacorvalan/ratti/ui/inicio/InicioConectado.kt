package com.gacorvalan.ratti.ui.inicio

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.auth.TokenPlanillasException
import com.gacorvalan.ratti.auth.tokenParaLeerPlanillas
import com.gacorvalan.ratti.datos.ErrorPlanilla
import com.gacorvalan.ratti.datos.PlanillaRepositorio
import com.gacorvalan.ratti.datos.modelo.ErrorModelo
import com.gacorvalan.ratti.datos.modelo.ModeloFinanzas
import com.gacorvalan.ratti.ui.InicioSesion
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTabMetrics
import com.gacorvalan.ratti.ui.theme.RattiType
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal sealed interface CargaInicio {
    data object SinSesion : CargaInicio
    data object Cargando : CargaInicio
    data class Listo(
        val modelo: EstadoInicio,
        /** El mismo modelo que armó [modelo]. Inicio, Ahorros y el detalle lo leen sin volver a la planilla. */
        val finanzas: ModeloFinanzas,
        val actualizando: Boolean = false,
        val aviso: String? = null,
    ) : CargaInicio
    data class Error(val mensaje: String) : CargaInicio
}

/**
 * Vive en la actividad. Inicio, Ahorros y el detalle de tarjeta comparten esta instancia:
 * cambiar de pestaña o abrir un detalle no la recrea y no vuelve a leer la planilla.
 * La primera lectura ocurre al entrar. Después, solo el gesto de tirar hacia abajo.
 */
internal class InicioViewModel(
    private val repositorio: PlanillaRepositorio,
) : ViewModel() {
    var carga by mutableStateOf<CargaInicio>(CargaInicio.SinSesion)
        private set

    private var emailCargado: String? = null
    private var trabajo: Job? = null

    fun sincronizar(context: Context, email: String?) {
        if (email.isNullOrBlank()) {
            trabajo?.cancel()
            emailCargado = null
            carga = CargaInicio.SinSesion
            return
        }
        if (emailCargado == email && carga !is CargaInicio.SinSesion) return
        emailCargado = email
        leer(context, previo = null)
    }

    fun refrescar(context: Context) {
        if (emailCargado.isNullOrBlank()) return
        when (val actual = carga) {
            is CargaInicio.Cargando -> return
            is CargaInicio.Listo -> {
                if (actual.actualizando) return
                leer(context, previo = actual)
            }
            else -> leer(context, previo = null)
        }
    }

    private fun leer(context: Context, previo: CargaInicio.Listo?) {
        carga = if (previo != null) {
            previo.copy(actualizando = true, aviso = null)
        } else {
            CargaInicio.Cargando
        }
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            val resultado = leerInicio(context, repositorio)
            if (emailCargado == null) return@launch
            carga = when {
                resultado is CargaInicio.Listo -> resultado
                previo != null && resultado is CargaInicio.Error ->
                    previo.copy(actualizando = false, aviso = resultado.mensaje)
                else -> resultado
            }
        }
    }
}

@Composable
internal fun InicioConectado(
    inicio: InicioViewModel,
    errorSesion: String?,
    ocupado: Boolean,
    onEntrar: () -> Unit,
    onTarjeta: (TarjetaInicio) -> Unit,
    onVerTodos: () -> Unit,
) {
    val context = LocalContext.current
    when (val estado = inicio.carga) {
        CargaInicio.SinSesion -> MarcoSimple {
            InicioSesion(
                error = errorSesion,
                ocupado = ocupado,
                onEntrar = onEntrar,
                modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
            )
        }
        CargaInicio.Cargando -> MarcoSimple {
            Leyendo()
        }
        is CargaInicio.Error -> MarcoSimple {
            MensajeError(estado.mensaje, onReintentar = { inicio.refrescar(context) })
            InicioSesion(
                error = errorSesion,
                ocupado = ocupado,
                onEntrar = onEntrar,
                modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
            )
        }
        is CargaInicio.Listo -> InicioListo(
            estado = estado,
            onRefresh = { inicio.refrescar(context) },
            onTarjeta = onTarjeta,
            onVerTodos = onVerTodos,
        )
    }
}

@Composable
private fun InicioListo(
    estado: CargaInicio.Listo,
    onRefresh: () -> Unit,
    onTarjeta: (TarjetaInicio) -> Unit,
    onVerTodos: () -> Unit,
) {
    val pull = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = estado.actualizando,
        onRefresh = onRefresh,
        state = pull,
        modifier = Modifier.fillMaxSize(),
        indicator = { BarraFina(pull, estado.actualizando) },
    ) {
        InicioScreen(
            estado = estado.modelo,
            aviso = estado.aviso,
            onTarjeta = onTarjeta,
            onVerTodos = onVerTodos,
        )
    }
}

@Composable
private fun BoxScope.BarraFina(
    estado: PullToRefreshState,
    actualizando: Boolean,
) {
    if (!actualizando && estado.distanceFraction <= 0f) return
    LinearProgressIndicator(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .fillMaxWidth()
            .height(3.dp),
        color = RattiColor.Cobalt,
        trackColor = RattiColor.Cream,
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
private fun Leyendo() {
    Column(
        modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
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
        val finanzas = repositorio.leerModelo(pedirToken = { tokenParaLeerPlanillas(context) })
        CargaInicio.Listo(modelo = inicioDe(finanzas), finanzas = finanzas)
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
