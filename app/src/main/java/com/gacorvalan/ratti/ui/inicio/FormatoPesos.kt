package com.gacorvalan.ratti.ui.inicio

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val simbolosAr = DecimalFormatSymbols(Locale.forLanguageTag("es-AR")).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

private val pesosFmt = DecimalFormat("#,##0", simbolosAr)

private val porcentajeFmt = DecimalFormat("#,##0.0", simbolosAr).apply {
    roundingMode = RoundingMode.HALF_UP
}

/** "$279.251", sin decimales y sin espacio después del peso. El signo queda adelante. */
internal fun pesos(valor: Long): String {
    val cuerpo = pesosFmt.format(kotlin.math.abs(valor))
    return if (valor < 0) "−$$cuerpo" else "$$cuerpo"
}

/** Pesos enteros, redondeo half-up. Un valor que no es finito cuenta como cero. */
internal fun pesosEnteros(valor: Double): Long {
    if (!valor.isFinite()) return 0L
    return BigDecimal.valueOf(valor).setScale(0, RoundingMode.HALF_UP).longValueExact()
}

/** "21,5" sin el símbolo de porcentaje. */
internal fun porcentaje(valor: Double): String = porcentajeFmt.format(valor)

internal fun captionSaldo(estado: EstadoInicio): String {
    val monto = pesos(kotlin.math.abs(estado.queda))
    val cabeza = if (estado.excedido) "Te pasaste $monto" else "Te quedan $monto"
    if (estado.porDia == 0L) return cabeza
    val porDia = pesos(estado.porDia)
    return "$cabeza · ~$porDia\npor día hasta el ${estado.cierre}"
}

internal fun marcaSemaforo(estado: EstadoInicio): String {
    val pct = porcentaje(estado.porcentajeMarca)
    return if (estado.excedido) "TE PASASTE $pct%" else "QUEDA $pct%"
}

internal fun textoCinta(estado: EstadoInicio): String {
    val marca = marcaSemaforo(estado)
    if (estado.porDia == 0L) return "●   $marca"
    val porDia = pesos(estado.porDia)
    return "●   $porDia POR DÍA HASTA EL ${estado.cierre}   ✦   $marca"
}

internal fun textoUsaste(estado: EstadoInicio): String =
    "Usaste ${pesos(estado.usado)} de ${pesos(estado.tope)}"
