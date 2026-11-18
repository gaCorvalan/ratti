package com.gacorvalan.ratti.ui.tarjeta

import com.gacorvalan.ratti.datos.modelo.ConsumoTarjeta
import com.gacorvalan.ratti.datos.modelo.Cuota
import com.gacorvalan.ratti.datos.modelo.ModeloFinanzas
import com.gacorvalan.ratti.datos.modelo.Tarjeta
import com.gacorvalan.ratti.ui.inicio.ColorConsumo
import com.gacorvalan.ratti.ui.inicio.ColorTarjeta
import com.gacorvalan.ratti.ui.inicio.idTarjeta
import com.gacorvalan.ratti.ui.inicio.pesosEnteros
import java.time.LocalDate
import java.util.Locale

/**
 * Lo que pinta el detalle de una tarjeta.
 * Sale del modelo que Inicio ya cargó. [ciclo] es "Noviembre, cierra el 26/11".
 */
internal data class DetalleTarjeta(
    val titulo: String,
    val pastilla: String,
    val total: Long,
    val vencimiento: String,
    val ciclo: String,
    val color: ColorTarjeta,
    val consumos: List<ConsumoDetalle>,
    val cuotas: List<CuotaDetalle>,
)

internal data class ConsumoDetalle(
    val id: String,
    val comercio: String,
    val meta: String,
    val monto: Long,
    val inicial: String,
    val color: ColorConsumo,
)

internal data class CuotaDetalle(
    val id: String,
    val descripcion: String,
    val monto: Long,
    /** "2 DE 6". */
    val avance: String,
    val restan: Long,
    /** Cuotas ya contadas sobre el total. Entre 0 y 1. */
    val fraccion: Float,
)

/** Consumos del ciclo, de todas las tarjetas, para VER TODOS. */
internal data class ConsumosCiclo(
    val ciclo: String,
    val consumos: List<ConsumoDetalle>,
)

internal fun detalleDe(finanzas: ModeloFinanzas, id: String, hoy: LocalDate): DetalleTarjeta? {
    val indice = finanzas.tarjetas.indices.firstOrNull { i ->
        idTarjeta(i, finanzas.tarjetas[i].marca) == id
    } ?: return null
    return finanzas.tarjetas[indice].aDetalle(indice, finanzas, hoy)
}

internal fun consumosDelCiclo(finanzas: ModeloFinanzas, hoy: LocalDate): ConsumosCiclo {
    val consumos = finanzas.tarjetas
        .flatMap { it.consumos }
        .sortedWith(compareByDescending { it.fecha ?: LocalDate.MIN })
        .mapIndexed { indice, consumo -> consumo.aDetalle(indice, hoy) }
    return ConsumosCiclo(ciclo = finanzas.ciclo.etiqueta, consumos = consumos)
}

private fun Tarjeta.aDetalle(indice: Int, finanzas: ModeloFinanzas, hoy: LocalDate): DetalleTarjeta {
    val cierre = finanzas.ciclo.cierre
    return DetalleTarjeta(
        titulo = marca.ifBlank { "Tarjeta" },
        pastilla = marca.ifBlank { "Tarjeta" }.uppercase(localeAr),
        total = pesosEnteros(total),
        vencimiento = fechaCorta(cierre.dayOfMonth, cierre.monthValue),
        ciclo = finanzas.ciclo.etiqueta,
        color = colorDe(marca, indice),
        consumos = consumos.mapIndexed { i, consumo -> consumo.aDetalle(i, hoy) },
        cuotas = cuotas.mapIndexed { i, cuota -> cuota.aDetalle(i) },
    )
}

private fun ConsumoTarjeta.aDetalle(indice: Int, hoy: LocalDate) = ConsumoDetalle(
    id = "$indice-${comercio.trim()}-$monto",
    comercio = comercio.trim().ifBlank { "Sin comercio" },
    meta = metaConsumo(categoria, fecha, hoy),
    monto = pesosEnteros(monto),
    inicial = inicialDe(comercio),
    color = COLORES[indice % COLORES.size],
)

private fun Cuota.aDetalle(indice: Int): CuotaDetalle {
    val tope = total.coerceAtLeast(1)
    val hechas = actual.coerceIn(0, tope)
    val quedan = (total - actual).coerceAtLeast(0)
    return CuotaDetalle(
        id = "$indice-${descripcion.trim()}",
        descripcion = descripcion.trim().ifBlank { "Cuota" },
        monto = pesosEnteros(monto),
        avance = "$actual DE $total",
        restan = pesosEnteros(monto * quedan),
        fraccion = hechas.toFloat() / tope.toFloat(),
    )
}

internal fun metaConsumo(categoria: String, fecha: LocalDate?, hoy: LocalDate): String {
    val rubro = categoria.trim().uppercase(localeAr).ifBlank { "SIN CATEGORÍA" }
    val cuando = cuando(fecha, hoy) ?: return rubro
    return "$rubro · $cuando"
}

private fun cuando(fecha: LocalDate?, hoy: LocalDate): String? = when (fecha) {
    null -> null
    hoy -> "HOY"
    hoy.minusDays(1) -> "AYER"
    else -> "${fecha.dayOfMonth} ${MESES[fecha.monthValue - 1]}"
}

private fun inicialDe(comercio: String): String {
    val letra = comercio.trim().firstOrNull { it.isLetterOrDigit() } ?: return "·"
    return letra.uppercase(localeAr)
}

private fun fechaCorta(dia: Int, mes: Int): String {
    val d = dia.toString().padStart(2, '0')
    val m = mes.toString().padStart(2, '0')
    return "$d/$m"
}

private fun colorDe(marca: String, indice: Int): ColorTarjeta = when (marca.lowercase(Locale.ROOT)) {
    "visa" -> ColorTarjeta.Rosa
    "amex" -> ColorTarjeta.Naranja
    else -> if (indice % 2 == 0) ColorTarjeta.Rosa else ColorTarjeta.Naranja
}

private val localeAr: Locale = Locale.forLanguageTag("es-AR")

private val MESES = listOf(
    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC",
)

private val COLORES = listOf(
    ColorConsumo.Teal,
    ColorConsumo.Rosa,
    ColorConsumo.Naranja,
    ColorConsumo.Lima,
    ColorConsumo.Cobalto,
)
