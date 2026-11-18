package com.gacorvalan.ratti.ui.ahorros

import com.gacorvalan.ratti.datos.modelo.Ahorros
import com.gacorvalan.ratti.ui.inicio.pesos
import com.gacorvalan.ratti.ui.inicio.pesosEnteros
import com.gacorvalan.ratti.ui.inicio.porcentaje
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Los pesos son el colchón. El VOOG es Casa, a mercado y a costo.
 * El USDC se reparte por monto entre los cinco objetivos. Sin asignar
 * es el resto, para que la suma cierre siempre. Gustos es un fondo, no una meta.
 * Lo que la planilla no trae (banco, lotes, metas en dólares, fecha del precio)
 * queda afuera: no se rellena con números de ejemplo.
 */
internal fun resumenDe(ahorros: Ahorros, tipoCambio: Double): ResumenAhorros {
    val usdMercado = suma(ahorros.voogMercadoUsd, ahorros.usdcTotal)
    val usdCosto = suma(ahorros.voogCostoUsd, ahorros.usdcTotal)
    val tc = tipoCambio.takeIf { it.isFinite() && it > 0.0 }
    val totalPesos = when {
        usdMercado == null -> null
        usdMercado == 0.0 -> pesosSiFinito(ahorros.colchonPesos)
        tc == null -> null
        else -> pesosSiFinito(ahorros.colchonPesos + usdMercado * tc)
    }
    return ResumenAhorros(
        totalPesos = totalPesos,
        usdMercado = usdMercado,
        usdCosto = usdCosto,
        tipoCambio = tc,
        colchon = colchonDe(ahorros),
        casa = casaDe(ahorros),
        dolares = dolaresDe(ahorros),
    )
}

internal data class ResumenAhorros(
    /** Nulo cuando hace falta un tipo de cambio y la planilla no lo trae. */
    val totalPesos: Long?,
    val usdMercado: Double?,
    val usdCosto: Double?,
    val tipoCambio: Double?,
    val colchon: ColchonUi,
    val casa: CasaUi,
    val dolares: DolaresUi,
)

internal data class ColchonUi(
    val pesos: Long?,
    val meta: Long?,
    /** Positivo si falta, negativo si se pasó, cero si llegó justo. Nulo sin meta. */
    val diferencia: Long?,
    val fraccion: Float?,
    val porcentaje: Double?,
)

internal data class CasaUi(
    val mercado: Double?,
    val costo: Double?,
    val resultado: Double?,
    /** Nulo si no hay costo contra el cual medir. */
    val porcentaje: Double?,
    val cuotapartes: Double?,
    /** Mercado dividido las cuotapartes. Nulo si no se puede. */
    val precio: Double?,
)

internal data class DolaresUi(
    val total: Double,
    val sinAsignar: Double,
    val objetivos: List<ObjetivoUi>,
)

internal data class ObjetivoUi(
    val id: String,
    val nombre: String,
    val monto: Double,
    /** Gustos no tiene barra ni porcentaje: es un fondo. */
    val fondo: Boolean,
)

private data class Definicion(val id: String, val nombre: String, val fondo: Boolean)

private val OBJETIVOS = listOf(
    Definicion("MUELAS", "Muelas", fondo = false),
    Definicion("ANTEOJOS", "Anteojos", fondo = false),
    Definicion("FOCUS", "Focus", fondo = false),
    Definicion("ELECTRO", "Electrodomésticos", fondo = false),
    Definicion("GUSTOS", "Gustos", fondo = true),
)

private fun colchonDe(ahorros: Ahorros): ColchonUi {
    val pesos = pesosSiFinito(ahorros.colchonPesos)
    val meta = pesosSiFinito(ahorros.metaColchon)?.takeIf { it > 0L }
    if (pesos == null || meta == null) {
        return ColchonUi(pesos, meta, diferencia = null, fraccion = null, porcentaje = null)
    }
    val diferencia = meta - pesos
    val fraccion = (pesos.toFloat() / meta.toFloat()).coerceIn(0f, 1f)
    val porcentaje = pesos.toDouble() / meta.toDouble() * 100.0
    return ColchonUi(pesos, meta, diferencia, fraccion, porcentaje)
}

private fun casaDe(ahorros: Ahorros): CasaUi {
    val mercado = finito(ahorros.voogMercadoUsd)
    val costo = finito(ahorros.voogCostoUsd)
    val papeles = finito(ahorros.voogCuotapartes)?.takeIf { it > 0.0 }
    val resultado = if (mercado != null && costo != null) mercado - costo else null
    val porcentaje = if (costo != null && costo > 0.0 && mercado != null) {
        (mercado - costo) / costo * 100.0
    } else {
        null
    }
    val precio = if (mercado != null && papeles != null) mercado / papeles else null
    return CasaUi(mercado, costo, resultado, porcentaje, papeles, precio)
}

private fun dolaresDe(ahorros: Ahorros): DolaresUi {
    val porId = ahorros.asignacionUsdc.associate { it.fondo to it.monto }
    val total = centavos(ahorros.usdcTotal)
    val objetivos = OBJETIVOS.map { definicion ->
        ObjetivoUi(
            id = definicion.id,
            nombre = definicion.nombre,
            monto = desdeCentavos(centavos(porId[definicion.id] ?: 0.0)),
            fondo = definicion.fondo,
        )
    }
    val asignado = objetivos.sumOf { centavos(it.monto) }
    return DolaresUi(
        total = desdeCentavos(total),
        sinAsignar = desdeCentavos(total - asignado),
        objetivos = objetivos,
    )
}

private val simbolosAr = DecimalFormatSymbols(Locale.forLanguageTag("es-AR")).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

private val usdFmt = DecimalFormat("#,##0.00", simbolosAr).apply {
    roundingMode = RoundingMode.HALF_UP
}

private val papelesFmt = DecimalFormat("#,##0.########", simbolosAr).apply {
    roundingMode = RoundingMode.HALF_UP
}

private val tcFmt = DecimalFormat("#,##0.##", simbolosAr).apply {
    roundingMode = RoundingMode.HALF_UP
}

/** "US$1.204,22". El signo queda adelante. */
internal fun dolares(valor: Double): String = moneda("US$", valor, usdFmt)

/** "USDC 400,00". */
internal fun usdc(valor: Double): String = moneda("USDC ", valor, usdFmt)

/** Cuotapartes, sin inventar decimales de más. */
internal fun papeles(valor: Double): String = papelesFmt.format(valor)

/** "67,3%" o "−3,7%". */
internal fun porcentajeFirmado(valor: Double): String {
    val texto = porcentaje(valor)
    return if (valor < 0.0) "−$texto%" else "$texto%"
}

/** Tipo de cambio en pesos, con centavos solo si los tiene. */
internal fun tipoCambioTexto(valor: Double): String {
    val cuerpo = tcFmt.format(kotlin.math.abs(valor))
    return if (valor < 0.0) "−$$cuerpo" else "$$cuerpo"
}

internal fun pesosDe(valor: Long): String = pesos(valor)

private fun moneda(prefijo: String, valor: Double, formato: DecimalFormat): String {
    if (!valor.isFinite()) return "${prefijo}—"
    val cuerpo = formato.format(kotlin.math.abs(valor))
    return if (valor < 0.0) "−$prefijo$cuerpo" else "$prefijo$cuerpo"
}

private fun finito(valor: Double): Double? = valor.takeIf { it.isFinite() }

private fun suma(a: Double, b: Double): Double? {
    if (!a.isFinite() || !b.isFinite()) return null
    return a + b
}

private fun pesosSiFinito(valor: Double): Long? = if (valor.isFinite()) pesosEnteros(valor) else null

private fun centavos(valor: Double): Long {
    if (!valor.isFinite()) return 0L
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
}

private fun desdeCentavos(centavos: Long): Double = BigDecimal.valueOf(centavos, 2).toDouble()
