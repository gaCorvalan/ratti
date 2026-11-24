package com.gacorvalan.ratti.ui.inicio

/**
 * Lo que pinta Inicio.
 * [teQuedan] es el neto de la planilla. Si viene null, se calcula como tope menos usado
 * (así los previews de ejemplo siguen funcionando).
 * [pasadoDeTope] cubre un neto negativo que, redondeado a pesos, da cero.
 * [ejemplo] solo lo prenden los previews: en datos reales no se muestra el cartel.
 */
data class EstadoInicio(
    val saludo: String,
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

    /** Porcentaje de saldo que queda. Negativo si el neto se pasó del tope. */
    val porcentajeRestante: Double
        get() = if (tope <= 0L) 0.0 else queda.toDouble() / tope.toDouble() * 100.0

    /**
     * Bandas de la tarjeta de saldo.
     * Rojo si queda menos del 15 %, naranja si queda menos del 25 %, verde desde el 25 %.
     * Desde el 45 % sigue en verde: el componente no tiene una cuarta variante.
     */
    val bandaSaldo: BandaSaldo
        get() = when {
            porcentajeRestante < 15.0 -> BandaSaldo.Rojo
            porcentajeRestante < 25.0 -> BandaSaldo.Naranja
            else -> BandaSaldo.Verde
        }

    /** Ancho de la barra: es el saldo que queda, no lo gastado. */
    val fraccionRestante: Float
        get() = (porcentajeRestante / 100.0).toFloat().coerceIn(0f, 1f)

    /** Porcentaje que falta, o el que se pasó, siempre en positivo. Lo usa la cinta. */
    val porcentajeMarca: Double
        get() = if (tope <= 0L) 0.0 else kotlin.math.abs(queda).toDouble() / tope.toDouble() * 100.0
}

enum class BandaSaldo { Verde, Naranja, Rojo }

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
