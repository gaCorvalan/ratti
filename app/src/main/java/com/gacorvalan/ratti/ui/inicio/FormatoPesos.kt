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

/** "21,5" o "35" si el decimal es cero. Sin el símbolo de porcentaje. */
internal fun porcentaje(valor: Double): String {
    val texto = porcentajeFmt.format(kotlin.math.abs(valor))
    return if (texto.endsWith(",0")) texto.dropLast(2) else texto
}

internal fun captionSaldo(estado: EstadoInicio): String {
    val cabeza = "Te quedan ${pesos(estado.queda)}"
    if (estado.porDia == 0L) return cabeza
    val porDia = pesos(estado.porDia)
    return "$cabeza · ~$porDia\npor día hasta el ${estado.cierre}"
}

/** "QUEDA 21,5% · MENOS DEL 25%". Desde el 45 % no agrega la coletilla. */
internal fun textoEstadoSaldo(estado: EstadoInicio): String {
    val numero = porcentaje(estado.porcentajeRestante)
    val conSigno = if (estado.porcentajeRestante < 0.0) "−$numero" else numero
    val queda = "QUEDA $conSigno%"
    val umbral = when (estado.bandaSaldo) {
        BandaSaldo.Verde -> if (estado.porcentajeRestante >= 45.0) null else "45%"
        BandaSaldo.Naranja -> "25%"
        BandaSaldo.Rojo -> "15%"
    }
    return if (umbral == null) queda else "$queda · MENOS DEL $umbral"
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
