package com.gacorvalan.ratti.ui.historial

/**
 * Un ciclo de tarjeta en el historial.
 *
 * La lista que entrega el ViewModel va del más nuevo al más viejo.
 * [mes] es el nombre ("Noviembre") y [cierre] el día ("26/11").
 * Nunca se guarda ni se muestra un AAAA-MM.
 */
data class CicloHistorial(
    val id: String,
    val mes: String,
    val cierre: String,
    val gasto: Long,
    val tope: Long,
    val actual: Boolean,
    val categorias: List<CategoriaHistorial>,
) {
    /** "Noviembre, cierra el 26/11". Sin fecha, queda el nombre del mes. */
    val frase: String
        get() = if (cierre.isBlank()) mes else "$mes, cierra el $cierre"

    val queda: Long get() = tope - gasto

    val excedido: Boolean get() = gasto > tope

    val tono: TonoMes
        get() = when {
            excedido -> TonoMes.Excedido
            actual -> TonoMes.Actual
            else -> TonoMes.Normal
        }

    /** Porcentaje del tope, truncado. 78,5% se muestra 78. */
    val porcentajeEntero: Int
        get() = if (tope <= 0L) 0 else ((gasto * 100L) / tope).toInt()
}

data class CategoriaHistorial(
    val nombre: String,
    val monto: Long,
)

/** Cómo se pinta el mes: el que se pasó del tope gana sobre el actual. */
enum class TonoMes { Actual, Normal, Excedido }

/** Alto de la barra o ancho del fill, contra el tope. Lo que se pasa llena el tramo. */
internal fun fraccionDelTope(monto: Long, tope: Long): Float = when {
    monto <= 0L -> 0f
    tope <= 0L -> 1f
    else -> (monto.toFloat() / tope.toFloat()).coerceAtMost(1f)
}

/** Parte que falta, o la que se pasó, siempre en positivo. */
internal fun porcentajeMarca(gasto: Long, tope: Long): Double {
    if (tope <= 0L) return 0.0
    return kotlin.math.abs(tope - gasto).toDouble() / tope.toDouble() * 100.0
}

/** Parte del tope que se llevó una categoría. */
internal fun porcentajeCategoria(monto: Long, tope: Long): Double {
    if (tope <= 0L) return 0.0
    return monto.toDouble() * 100.0 / tope.toDouble()
}

/** El gráfico lee de izquierda a derecha, del más viejo al más nuevo. */
internal fun cronologico(ciclos: List<CicloHistorial>): List<CicloHistorial> = ciclos.asReversed()

/** Tope que se anuncia en el gráfico: el del mes actual, o el más nuevo. */
internal fun topeDelGrafico(ciclos: List<CicloHistorial>): Long? =
    (ciclos.firstOrNull { it.actual } ?: ciclos.firstOrNull())?.tope
