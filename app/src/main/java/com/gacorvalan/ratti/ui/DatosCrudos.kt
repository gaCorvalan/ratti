package com.gacorvalan.ratti.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.auth.TokenPlanillasException
import com.gacorvalan.ratti.auth.tokenParaLeerPlanillas
import com.gacorvalan.ratti.datos.ErrorPlanilla
import com.gacorvalan.ratti.datos.PlanillaRepositorio
import com.gacorvalan.ratti.datos.ValorCrudo
import com.gacorvalan.ratti.datos.interpretarPlanilla
import com.gacorvalan.ratti.datos.modelo.ErrorModelo
import com.gacorvalan.ratti.datos.numeroEnTexto
import com.gacorvalan.ratti.datos.textoParaDepurar
import java.time.LocalDate
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTabMetrics
import com.gacorvalan.ratti.ui.theme.RattiTheme
import com.gacorvalan.ratti.ui.theme.RattiType
import kotlin.coroutines.cancellation.CancellationException

private const val FILAS_VISIBLES = 20

private sealed interface EstadoDatosCrudos {
    data object Cargando : EstadoDatosCrudos
    data class Listo(
        val pestanas: Map<String, List<List<ValorCrudo?>>>,
        val resumen: String,
    ) : EstadoDatosCrudos
    data class Fallo(val texto: TextoFallo) : EstadoDatosCrudos
}

private sealed interface TextoFallo {
    data class Fijo(@StringRes val id: Int) : TextoFallo
    data class ConArgumento(@StringRes val id: Int, val argumento: String) : TextoFallo
}

@Composable
internal fun DatosCrudos(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repositorio = remember { PlanillaRepositorio() }
    var intento by remember { mutableIntStateOf(0) }
    var estado by remember { mutableStateOf<EstadoDatosCrudos>(EstadoDatosCrudos.Cargando) }
    var abiertas by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(intento) {
        estado = EstadoDatosCrudos.Cargando
        estado = cargarDatos(context, repositorio)
    }

    DatosCrudosContenido(
        estado = estado,
        abiertas = abiertas,
        onAlternar = { nombre ->
            abiertas = if (nombre in abiertas) abiertas - nombre else abiertas + nombre
        },
        onVolver = onVolver,
        onReintentar = { intento += 1 },
        modifier = modifier,
    )
}

private suspend fun cargarDatos(
    context: Context,
    repositorio: PlanillaRepositorio,
): EstadoDatosCrudos {
    return try {
        val pestanas = repositorio.leer { tokenParaLeerPlanillas(context) }
        val resumen = try {
            textoParaDepurar(interpretarPlanilla(pestanas, LocalDate.now()))
        } catch (e: ErrorModelo) {
            context.getString(R.string.error_modelo, e.message)
        }
        EstadoDatosCrudos.Listo(pestanas, resumen)
    } catch (e: CancellationException) {
        throw e
    } catch (e: TokenPlanillasException) {
        EstadoDatosCrudos.Fallo(
            TextoFallo.Fijo(
                if (e.sinConexion) R.string.error_red else R.string.error_planilla_permiso,
            ),
        )
    } catch (e: ErrorPlanilla) {
        EstadoDatosCrudos.Fallo(e.aTexto())
    } catch (_: Exception) {
        EstadoDatosCrudos.Fallo(TextoFallo.Fijo(R.string.error_planilla_lectura))
    }
}

private fun ErrorPlanilla.aTexto(): TextoFallo = when (this) {
    ErrorPlanilla.SinConexion -> TextoFallo.Fijo(R.string.error_red)
    ErrorPlanilla.Prohibido -> TextoFallo.Fijo(R.string.error_planilla_prohibida)
    ErrorPlanilla.PermisoVencido -> TextoFallo.Fijo(R.string.error_planilla_vencida)
    ErrorPlanilla.Ilegible -> TextoFallo.Fijo(R.string.error_planilla_ilegible)
    ErrorPlanilla.NoEncontrada -> TextoFallo.Fijo(R.string.error_planilla_no_encontrada)
    ErrorPlanilla.Lectura -> TextoFallo.Fijo(R.string.error_planilla_lectura)
    is ErrorPlanilla.PestanaFaltante -> {
        val nombre = nombre
        if (nombre.isNullOrBlank()) {
            TextoFallo.Fijo(R.string.error_planilla_pestana_alguna)
        } else {
            TextoFallo.ConArgumento(R.string.error_planilla_pestana, nombre)
        }
    }
    is ErrorPlanilla.Http -> TextoFallo.ConArgumento(R.string.error_planilla_http, codigo.toString())
}

@Composable
private fun DatosCrudosContenido(
    estado: EstadoDatosCrudos,
    abiertas: Set<String>,
    onAlternar: (String) -> Unit,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = RattiSpace.screenMargin)
            .padding(top = RattiSpace.sectionGap, bottom = RattiSpace.contentBottom),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.datos_crudos),
                style = RattiType.navTitle,
                color = RattiColor.Ink,
            )
            Enlace(texto = stringResource(R.string.volver), onClick = onVolver)
        }
        when (estado) {
            EstadoDatosCrudos.Cargando -> Row(
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
            is EstadoDatosCrudos.Fallo -> Column(
                verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
            ) {
                Text(
                    text = estado.texto.resolver(),
                    style = RattiType.pocketCaption,
                    color = RattiColor.Ink,
                )
                Enlace(texto = stringResource(R.string.reintentar), onClick = onReintentar)
            }
            is EstadoDatosCrudos.Listo -> {
                Text(
                    text = estado.resumen,
                    style = RattiType.pocketCaption,
                    color = RattiColor.Ink,
                )
                estado.pestanas.forEach { (nombre, filas) ->
                    PestanaCruda(
                        nombre = nombre,
                        filas = filas,
                        abierta = nombre in abiertas,
                        onAlternar = { onAlternar(nombre) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TextoFallo.resolver(): String = when (this) {
    is TextoFallo.Fijo -> stringResource(id)
    is TextoFallo.ConArgumento -> stringResource(id, argumento)
}

@Composable
private fun PestanaCruda(
    nombre: String,
    filas: List<List<ValorCrudo?>>,
    abierta: Boolean,
    onAlternar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RattiRadius.tile))
            .background(RattiColor.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onAlternar,
                )
                .padding(RattiSpace.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = RattiType.rowTitle,
                    color = RattiColor.Ink,
                )
                Spacer(Modifier.height(RattiSpace.sectionInner))
                Text(
                    text = if (filas.isEmpty()) {
                        stringResource(R.string.sin_filas)
                    } else {
                        pluralStringResource(R.plurals.cantidad_filas, filas.size, filas.size)
                    },
                    style = RattiType.heroSubtitle,
                    color = RattiColor.Ink72,
                )
            }
            Enlace(
                texto = stringResource(if (abierta) R.string.ocultar_filas else R.string.ver_filas),
                onClick = onAlternar,
            )
        }
        if (abierta && filas.isNotEmpty()) {
            SelectionContainer {
                Column(
                    modifier = Modifier.padding(
                        start = RattiSpace.cardPadding,
                        end = RattiSpace.cardPadding,
                        bottom = RattiSpace.cardPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
                ) {
                    filas.take(FILAS_VISIBLES).forEachIndexed { indice, fila ->
                        Text(
                            text = stringResource(R.string.fila_cruda, indice + 1, fila.enPantalla()),
                            style = RattiType.pocketCaption,
                            color = RattiColor.Ink,
                        )
                    }
                    val resto = filas.size - FILAS_VISIBLES
                    if (resto > 0) {
                        Text(
                            text = pluralStringResource(R.plurals.filas_mas, resto, resto),
                            style = RattiType.meta,
                            color = RattiColor.Ink72,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Enlace(
    texto: String,
    onClick: () -> Unit,
) {
    Text(
        text = texto,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick,
        ),
        style = RattiType.link,
        color = RattiColor.Cobalt,
    )
}

private fun List<ValorCrudo?>.enPantalla(): String = joinToString(" | ") { celda ->
    when (celda) {
        null -> ""
        is ValorCrudo.Texto -> "\"${celda.valor.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        is ValorCrudo.Numero -> numeroEnTexto(celda.valor)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun DatosCrudosPreview() {
    RattiTheme {
        DatosCrudosContenido(
            estado = EstadoDatosCrudos.Listo(
                pestanas = mapOf(
                    "Semáforo" to listOf(
                        listOf(ValorCrudo.Texto("Tope"), ValorCrudo.Numero(2_000_000.0)),
                        listOf(ValorCrudo.Texto("Usado"), ValorCrudo.Numero(640000.5)),
                    ),
                    "Movimientos" to emptyList(),
                ),
                resumen = "Abril, cierra el 05/04 · faltan 4 días · gastado $40.000 · te quedan $60.000 · verde",
            ),
            abiertas = setOf("Semáforo"),
            onAlternar = {},
            onVolver = {},
            onReintentar = {},
        )
    }
}
