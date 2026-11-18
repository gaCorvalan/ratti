package com.gacorvalan.ratti.ui.inicio

import androidx.compose.ui.graphics.Color
import com.gacorvalan.ratti.ui.theme.RattiColor

/**
 * Lo que pinta Inicio.
 * [teQuedan] es el neto de la planilla. Si viene null, se calcula como tope menos usado
 * (así los previews de ejemplo siguen funcionando).
 * [pasadoDeTope] cubre un neto negativo que, redondeado a pesos, da cero.
 * [ejemplo] solo lo prenden los previews: en datos reales no se muestra el cartel.
 */
data class EstadoInicio(
    val saludo: String,
    val inicialAvatar: String,
    /** Nombre del ciclo, por ejemplo "Noviembre". Nunca un AAAA-MM. */
    val mes: String,
    /** Día y mes de cierre, por ejemplo "26/11". */
    val cierre: String,
    val usado: Long,
    val tope: Long,
    val porDia: Long,
    val tarjetas: List<TarjetaInicio>,
    val consumos: List<ConsumoInicio>,
    val teQuedan: Long? = null,
    val pasadoDeTope: Boolean = false,
    val ejemplo: Boolean = false,
) {
    /** Forma de producto: "Noviembre, cierra el 26/11". */
    val ciclo: String get() = "$mes, cierra el $cierre"

    val queda: Long get() = teQuedan ?: (tope - usado)

    val excedido: Boolean get() = pasadoDeTope || queda < 0L

    val fraccionUsada: Float
        get() = when {
            excedido -> 1f
            tope <= 0L -> 0f
            else -> (usado.toFloat() / tope.toFloat()).coerceIn(0f, 1f)
        }

    /** Porcentaje que falta, o el que se pasó, siempre en positivo. */
    val porcentajeMarca: Double
        get() = if (tope <= 0L) 0.0 else kotlin.math.abs(queda).toDouble() / tope.toDouble() * 100.0

    val colorSemaforo: Color
        get() = if (excedido) RattiColor.Danger else RattiColor.Lime
}

data class TarjetaInicio(
    val id: String,
    val marca: String,
    val banco: String,
    val monto: Long,
    val venceEl: String,
    val color: ColorTarjeta,
)

enum class ColorTarjeta { Rosa, Naranja }

data class ConsumoInicio(
    val id: String,
    val comercio: String,
    val categoria: String,
    val monto: Long,
    val inicial: String,
    val color: ColorConsumo,
)

enum class ColorConsumo { Teal, Rosa, Naranja, Lima, Cobalto }
