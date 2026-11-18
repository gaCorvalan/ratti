package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.ErrorModelo
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.time.DateTimeException
import java.time.LocalDate
import java.util.Locale

internal class Grilla(val filas: List<List<ValorCrudo?>>) {
    fun valor(fila: Int, columna: Int): ValorCrudo? {
        if (fila < 1 || columna < 0) return null
        return filas.getOrNull(fila - 1)?.getOrNull(columna)
    }

    fun filasConEtiqueta(columna: Int, cercaDe: Int, coincide: (String) -> Boolean): List<Int> {
        val halladas = mutableListOf<Int>()
        for (fila in 1..filas.size) {
            val texto = textoPlano(valor(fila, columna)) ?: continue
            if (coincide(normalizar(texto))) halladas.add(fila)
        }
        return halladas.sortedBy { kotlin.math.abs(it - cercaDe) }
    }

    /**
     * Busca la etiqueta y lee la columna de al lado.
     * Si ninguna etiqueta tiene un valor que sirva, usa la celda fija.
     */
    fun leer(
        columnaEtiqueta: Int,
        columnaValor: Int,
        filaFija: Int,
        coincide: (String) -> Boolean,
        acepta: (ValorCrudo) -> Boolean,
    ): ValorCrudo? {
        for (fila in filasConEtiqueta(columnaEtiqueta, filaFija, coincide)) {
            val candidato = valor(fila, columnaValor) ?: continue
            if (acepta(candidato)) return candidato
        }
        return valor(filaFija, columnaValor)
    }

    fun tabla(vararg encabezados: String, desdeFila: Int = 1): Tabla? {
        val necesarios = encabezados.map { normalizar(it) }
        for (fila in desdeFila..filas.size) {
            val mapa = encabezadosDe(filas.getOrNull(fila - 1).orEmpty())
            if (!cubreEncabezados(mapa, necesarios)) continue
            val datos = if (fila >= filas.size) emptyList() else filas.drop(fila)
            return Tabla(mapa, datos)
        }
        return null
    }
}

internal class Tabla(
    val columnas: Map<String, Int>,
    val filasDatos: List<List<ValorCrudo?>>,
) {
    fun columna(nombre: String): Int? = columnas[normalizar(nombre)]

    fun columnaCon(vararg tokens: String): Int? {
        val claves = tokens.map { normalizar(it) }
        return columnas.entries.firstOrNull { (header, _) -> claves.all { it in header } }?.value
    }

    fun texto(fila: List<ValorCrudo?>, columna: Int?): String? =
        columna?.let { textoPlano(fila.getOrNull(it)) }

    fun numero(fila: List<ValorCrudo?>, columna: Int?): Double? =
        columna?.let { numeroDe(fila.getOrNull(it)) }

    fun fecha(fila: List<ValorCrudo?>, columna: Int?): LocalDate? =
        columna?.let { fechaDe(fila.getOrNull(it)) }
}

internal fun Map<String, List<List<ValorCrudo?>>>.grilla(nombre: String): Grilla {
    val filas = this[nombre] ?: throw ErrorModelo.Pestana(nombre)
    return Grilla(filas)
}

internal fun normalizar(texto: String): String {
    val descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD)
    return descompuesto
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
        .replace('\u00A0', ' ')
        .trim()
        .replace(Regex("\\s+"), " ")
}

internal fun textoPlano(valor: ValorCrudo?): String? = when (valor) {
    is ValorCrudo.Texto -> valor.valor.trim().ifBlank { null }
    else -> null
}

internal fun esVacio(valor: ValorCrudo?): Boolean = when (valor) {
    null -> true
    is ValorCrudo.Texto -> valor.valor.isBlank()
    is ValorCrudo.Numero -> false
}

internal fun numeroDe(valor: ValorCrudo?): Double? = when (valor) {
    null -> null
    is ValorCrudo.Numero -> valor.valor.takeIf { it.isFinite() }
    is ValorCrudo.Texto -> parsearNumeroArgentino(valor.valor)
}

internal fun fechaDe(valor: ValorCrudo?): LocalDate? = when (valor) {
    null -> null
    is ValorCrudo.Texto -> parsearFecha(valor.valor)
    is ValorCrudo.Numero -> fechaSerial(valor.valor)
}

internal fun esNumero(valor: ValorCrudo): Boolean = numeroDe(valor) != null

internal fun esFecha(valor: ValorCrudo): Boolean = fechaDe(valor) != null

private val CODIGO_CICLO = Regex("(\\d{4})-(\\d{2})")

internal fun codigoCiclo(texto: String): String? {
    val match = CODIGO_CICLO.find(texto.trim()) ?: return null
    val mes = match.groupValues[2].toIntOrNull() ?: return null
    if (mes !in 1..12) return null
    return "${match.groupValues[1]}-${match.groupValues[2]}"
}

internal fun esCodigoCiclo(valor: ValorCrudo): Boolean = codigoCiclo(textoPlano(valor).orEmpty()) != null

/** "1.234,50", "$ 1.000", "10,5", "2.5" o "6000". Vacío devuelve null. */
internal fun parsearNumeroArgentino(texto: String): Double? {
    var crudo = texto.trim().replace('\u00A0', ' ').replace(" ", "")
    if (crudo.isEmpty() || crudo == "-" || crudo == "—" || crudo == "–") return null
    var negativo = false
    if (crudo.startsWith("(") && crudo.endsWith(")") && crudo.length > 2) {
        negativo = true
        crudo = crudo.substring(1, crudo.lastIndex)
    }
    crudo = crudo.replace(Regex("(?i)u\\\$s|us\\\$|usd|ars|\\$"), "")
    if (crudo.startsWith("+") || crudo.startsWith("-") || crudo.startsWith("−")) {
        negativo = negativo || crudo[0] != '+'
        crudo = crudo.substring(1)
    }
    if (crudo.isEmpty()) return null
    val tieneComa = ',' in crudo
    val tienePunto = '.' in crudo
    val normal = when {
        tieneComa && tienePunto -> crudo.replace(".", "").replace(',', '.')
        tieneComa -> crudo.replace(',', '.')
        tienePunto && crudo.matches(Regex("\\d{1,3}(\\.\\d{3})+")) -> crudo.replace(".", "")
        else -> crudo
    }
    if (!normal.matches(Regex("\\d+(\\.\\d+)?"))) return null
    val valor = normal.toDoubleOrNull() ?: return null
    if (!valor.isFinite()) return null
    return if (negativo) -valor else valor
}

private val FECHA = Regex("(\\d{1,2})/(\\d{1,2})/(\\d{2,4})")

internal fun parsearFecha(texto: String): LocalDate? {
    val match = FECHA.find(texto.trim()) ?: return null
    val dia = match.groupValues[1].toIntOrNull() ?: return null
    val mes = match.groupValues[2].toIntOrNull() ?: return null
    var anio = match.groupValues[3].toIntOrNull() ?: return null
    if (anio < 100) anio += 2000
    return try {
        LocalDate.of(anio, mes, dia)
    } catch (_: DateTimeException) {
        null
    }
}

/** Número de serie de Sheets (días desde el 30/12/1899), si cae en un rango de fecha. */
internal fun fechaSerial(serial: Double): LocalDate? {
    if (serial < 20_000.0 || serial > 80_000.0) return null
    val dias = serial.toLong()
    if (kotlin.math.abs(serial - dias) > 0.001) return null
    return try {
        LocalDate.of(1899, 12, 30).plusDays(dias)
    } catch (_: DateTimeException) {
        null
    }
}

internal val NOMBRES_MES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)

internal fun nombreDeMes(mes: Int): String {
    if (mes !in 1..12) throw ErrorModelo.CicloIlegible
    return NOMBRES_MES[mes - 1]
}

internal fun nombreDeCodigo(codigo: String): String {
    val mes = codigo.substringAfter('-').toIntOrNull() ?: throw ErrorModelo.CicloIlegible
    return nombreDeMes(mes)
}

internal fun pesosArgentino(valor: Double): String {
    val redondeado = BigDecimal.valueOf(valor).setScale(0, RoundingMode.HALF_UP).toLong()
    val simbolos = DecimalFormatSymbols(Locale.forLanguageTag("es-AR")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val cuerpo = DecimalFormat("#,##0", simbolos).format(kotlin.math.abs(redondeado))
    return if (redondeado < 0) "−$$cuerpo" else "$$cuerpo"
}

internal fun montoEnPesos(
    fila: List<ValorCrudo?>,
    colArs: Int?,
    colUsd: Int?,
    colEquivalente: Int?,
    tipoCambio: Double,
): Double {
    if (colEquivalente != null) {
        val equivalente = numeroDe(fila.getOrNull(colEquivalente))
        if (equivalente != null) return equivalente
    }
    val ars = colArs?.let { numeroDe(fila.getOrNull(it)) } ?: 0.0
    val usd = colUsd?.let { numeroDe(fila.getOrNull(it)) } ?: 0.0
    return ars + usd * tipoCambio
}

internal fun encabezadosDe(celdas: List<ValorCrudo?>): Map<String, Int> {
    val mapa = LinkedHashMap<String, Int>()
    for ((columna, valor) in celdas.withIndex()) {
        val texto = textoPlano(valor) ?: continue
        mapa.putIfAbsent(normalizar(texto), columna)
    }
    return mapa
}

/**
 * Cada texto pedido tiene que coincidir con una columna distinta.
 * Primero vale el encabezado igual; si no hay, uno más largo que lo contiene.
 * Un título de una sola celda ("SALDOS POR BOLSILLO") no alcanza para bolsillo y saldo.
 */
internal fun cubreEncabezados(encabezados: Map<String, Int>, necesarios: List<String>): Boolean {
    val usados = HashSet<Int>()
    val sinExacto = ArrayList<String>(necesarios.size)
    for (requerido in necesarios) {
        val columna = encabezados[requerido]
        if (columna != null && usados.add(columna)) continue
        sinExacto.add(requerido)
    }
    for (requerido in sinExacto) {
        if (requerido.length <= 3) return false
        val columna = encabezados.entries.firstOrNull { (header, indice) ->
            indice !in usados && requerido in header
        }?.value ?: return false
        usados.add(columna)
    }
    return true
}

internal fun tiene(vararg palabras: String): (String) -> Boolean {
    val claves = palabras.map { normalizar(it) }
    return { texto -> claves.all { it in texto } }
}
