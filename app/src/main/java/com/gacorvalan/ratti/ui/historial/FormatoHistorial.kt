package com.gacorvalan.ratti.ui.historial

import com.gacorvalan.ratti.ui.inicio.pesos
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val simbolosAr = DecimalFormatSymbols(Locale.forLanguageTag("es-AR")).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

private val porcentajeFmt = DecimalFormat("#,##0.0", simbolosAr).apply {
    roundingMode = RoundingMode.HALF_UP
}

private val localeEs = Locale.forLanguageTag("es-AR")

private val siglas = mapOf(
    "enero" to "ENE",
    "febrero" to "FEB",
    "marzo" to "MAR",
    "abril" to "ABR",
    "mayo" to "MAY",
    "junio" to "JUN",
    "julio" to "JUL",
    "agosto" to "AGO",
    "septiembre" to "SEP",
    "octubre" to "OCT",
    "noviembre" to "NOV",
    "diciembre" to "DIC",
)

private fun porcentaje(valor: Double): String = porcentajeFmt.format(valor)

/** "JUN" para el eje del gráfico. */
internal fun siglaMes(mes: String): String =
    siglas[mes.trim().lowercase(localeEs)]
        ?: mes.trim().take(3).uppercase(localeEs)

/** "NOVIEMBRE · CIERRA 26/11". */
internal fun pastillaCiclo(mes: String, cierre: String): String {
    val titulo = mes.uppercase(localeEs)
    if (cierre.isBlank()) return titulo
    return "$titulo · CIERRA $cierre"
}

/** "78%" contra el tope, sin decimales. */
internal fun textoPorcentajeTope(ciclo: CicloHistorial): String = "${ciclo.porcentajeEntero}%"

/** "15,3%" de la categoría contra el tope del ciclo. */
internal fun textoPorcentajeCategoria(monto: Long, tope: Long): String =
    "${porcentaje(porcentajeCategoria(monto, tope))}%"

/** "de $1.300.000  ·  te quedan $279.251". */
internal fun subtituloHero(ciclo: CicloHistorial): String {
    val verbo = if (ciclo.excedido) "te pasaste" else "te quedan"
    val delta = pesos(kotlin.math.abs(ciclo.queda))
    return "de ${pesos(ciclo.tope)}  ·  $verbo $delta"
}

internal fun textoUsaste(ciclo: CicloHistorial): String =
    "Usaste ${pesos(ciclo.gasto)} de ${pesos(ciclo.tope)}"

internal fun marcaSemaforo(ciclo: CicloHistorial): String {
    val pct = porcentaje(porcentajeMarca(ciclo.gasto, ciclo.tope))
    return if (ciclo.excedido) "TE PASASTE $pct%" else "QUEDA $pct%"
}

/** Lo que lee el lector de pantalla: el ciclo, el gasto y el porcentaje. */
internal fun descripcionMes(ciclo: CicloHistorial): String =
    "${ciclo.frase}. ${pesos(ciclo.gasto)}. ${textoPorcentajeTope(ciclo)}"
