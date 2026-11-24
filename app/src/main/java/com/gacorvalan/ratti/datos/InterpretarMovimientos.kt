package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.ConsumoTarjeta
import com.gacorvalan.ratti.datos.modelo.Cuota
import com.gacorvalan.ratti.datos.modelo.Movimiento
import com.gacorvalan.ratti.datos.modelo.Tarjeta
import java.time.LocalDate

private val MARCAS = listOf("Visa", "Amex")

private val CUOTA_DE = Regex("(\\d+)\\s*de\\s*(\\d+)", RegexOption.IGNORE_CASE)

private val COLUMNAS_MOVIMIENTOS = mapOf(
    "fecha" to 0,
    "comercio" to 1,
    "monto" to 2,
    "categoria" to 3,
    "medio" to 4,
    "notas" to 5,
    "ciclo" to 6,
)

private val COLUMNAS_FIJOS = mapOf(
    "concepto" to 0,
    "categoria" to 1,
    "monto ars" to 2,
    "monto usd" to 3,
    "tarjeta" to 4,
    "tipo" to 5,
    "cuota actual/total" to 6,
    "activo" to 7,
    "notas" to 8,
    "ars equivalente (calc.)" to 9,
)

internal fun interpretarTarjetas(
    proximoMes: Grilla,
    fijos: Grilla,
    movimientos: Grilla,
    codigoCiclo: String,
    tipoCambio: Double,
): List<Tarjeta> {
    val filas = leerMovimientos(movimientos)
    val tablaFijos = tablaFijos(fijos)
    return MARCAS.map { marca ->
        val consumos = filas
            .filter { it.sirveParaGasto && it.ciclo == codigoCiclo && normalizar(it.medio) == normalizar(marca) }
            .sortedWith(ordenPorFecha)
            .map { it.aConsumo() }
        val cuotas = cuotasDe(tablaFijos, marca, tipoCambio)
        val variable = montoProximo(proximoMes, marca, fijos = false, filaFija = if (marca == "Visa") 8 else 9)
            ?: consumos.sumOf { it.monto }
        val comprometido = montoProximo(proximoMes, marca, fijos = true, filaFija = if (marca == "Visa") 10 else 11)
            ?: sumaFijos(tablaFijos, marca, tipoCambio)
        Tarjeta(marca = marca, total = variable + comprometido, consumos = consumos, cuotas = cuotas)
    }
}

internal fun ultimosMovimientos(movimientos: Grilla): List<Movimiento> {
    return leerMovimientos(movimientos)
        .filter { it.sirveParaGasto && !it.ciclo.isNullOrBlank() }
        .sortedWith(ordenPorFecha)
        .map { it.aMovimiento() }
}

private data class FilaMovimiento(
    val fecha: LocalDate?,
    val comercio: String,
    val monto: Double,
    val categoria: String,
    val medio: String,
    val notas: String,
    val ciclo: String?,
    val indice: Int,
) {
    val sirveParaGasto: Boolean
        get() = !esEjemplo(notas) && !esMovimientoDeAhorro(categoria)

    fun aMovimiento() = Movimiento(
        fecha = fecha,
        comercio = comercio,
        monto = monto,
        categoria = categoria,
        medio = medio,
        ciclo = ciclo.orEmpty(),
    )

    fun aConsumo() = ConsumoTarjeta(
        fecha = fecha,
        comercio = comercio,
        monto = monto,
        categoria = categoria,
    )
}

private val ordenPorFecha = compareByDescending<FilaMovimiento> { it.fecha ?: LocalDate.MIN }
    .thenByDescending { it.indice }

private fun leerMovimientos(grilla: Grilla): List<FilaMovimiento> {
    val tabla = grilla.tabla("fecha", "comercio", "monto", "categoria", "medio", "ciclo")
        ?: Tabla(COLUMNAS_MOVIMIENTOS, grilla.filas.drop(4))
    val colFecha = tabla.columna("fecha")
    val colComercio = tabla.columna("comercio")
    val colMonto = tabla.columna("monto")
    val colCategoria = tabla.columna("categoria")
    val colMedio = tabla.columna("medio")
    val colNotas = tabla.columna("notas")
    val colCiclo = tabla.columna("ciclo")
    return tabla.filasDatos.mapIndexedNotNull { indice, fila ->
        val monto = tabla.numero(fila, colMonto) ?: return@mapIndexedNotNull null
        val comercio = tabla.texto(fila, colComercio).orEmpty()
        val categoria = tabla.texto(fila, colCategoria).orEmpty()
        if (comercio.isBlank() && categoria.isBlank() && tabla.fecha(fila, colFecha) == null) {
            return@mapIndexedNotNull null
        }
        FilaMovimiento(
            fecha = tabla.fecha(fila, colFecha),
            comercio = comercio,
            monto = monto,
            categoria = categoria,
            medio = tabla.texto(fila, colMedio).orEmpty(),
            notas = tabla.texto(fila, colNotas).orEmpty(),
            ciclo = codigoCiclo(tabla.texto(fila, colCiclo).orEmpty()),
            indice = indice,
        )
    }
}

private fun tablaFijos(grilla: Grilla): Tabla {
    return grilla.tabla("concepto", "tarjeta", "tipo", "activo")
        ?: Tabla(COLUMNAS_FIJOS, grilla.filas.drop(4))
}

private fun cuotasDe(tabla: Tabla, marca: String, tipoCambio: Double): List<Cuota> {
    val colConcepto = tabla.columna("concepto")
    val colTarjeta = tabla.columna("tarjeta")
    val colTipo = tabla.columna("tipo")
    val colCuota = tabla.columna("cuota actual/total") ?: tabla.columnaCon("cuota")
    val colActivo = tabla.columna("activo")
    val colArs = tabla.columna("monto ars")
    val colUsd = tabla.columna("monto usd")
    val colEquivalente = tabla.columnaCon("ars", "equivalente")
    return tabla.filasDatos.mapNotNull { fila ->
        val descripcion = tabla.texto(fila, colConcepto) ?: return@mapNotNull null
        if (normalizar(tabla.texto(fila, colTarjeta).orEmpty()) != normalizar(marca)) return@mapNotNull null
        if (normalizar(tabla.texto(fila, colActivo).orEmpty()) == "no") return@mapNotNull null
        if (normalizar(tabla.texto(fila, colTipo).orEmpty()) != "cuota") return@mapNotNull null
        val partes = CUOTA_DE.find(tabla.texto(fila, colCuota).orEmpty()) ?: return@mapNotNull null
        val actual = partes.groupValues[1].toIntOrNull() ?: return@mapNotNull null
        val total = partes.groupValues[2].toIntOrNull() ?: return@mapNotNull null
        if (total <= 0) return@mapNotNull null
        Cuota(
            descripcion = descripcion,
            monto = montoEnPesos(fila, colArs, colUsd, colEquivalente, tipoCambio),
            actual = actual,
            total = total,
        )
    }
}

private fun sumaFijos(tabla: Tabla, marca: String, tipoCambio: Double): Double {
    val colConcepto = tabla.columna("concepto")
    val colTarjeta = tabla.columna("tarjeta")
    val colActivo = tabla.columna("activo")
    val colArs = tabla.columna("monto ars")
    val colUsd = tabla.columna("monto usd")
    val colEquivalente = tabla.columnaCon("ars", "equivalente")
    return tabla.filasDatos.sumOf { fila ->
        if (tabla.texto(fila, colConcepto).isNullOrBlank()) return@sumOf 0.0
        if (normalizar(tabla.texto(fila, colTarjeta).orEmpty()) != normalizar(marca)) return@sumOf 0.0
        if (normalizar(tabla.texto(fila, colActivo).orEmpty()) == "no") return@sumOf 0.0
        montoEnPesos(fila, colArs, colUsd, colEquivalente, tipoCambio)
    }
}

private fun montoProximo(grilla: Grilla, marca: String, fijos: Boolean, filaFija: Int): Double? {
    val marcaNormal = normalizar(marca)
    val candidatas = mutableListOf<Pair<Int, Double>>()
    for (fila in 1..grilla.filas.size) {
        val textos = (0 until 4).mapNotNull { textoPlano(grilla.valor(fila, it)) }.map { normalizar(it) }
        if (textos.none { marcaNormal in it }) continue
        val esFijo = textos.any { "fijo" in it || "suscrip" in it }
        val esVariable = textos.any { "variable" in it || "consumo" in it }
        val coincide = if (fijos) esFijo && !esVariable else esVariable && !esFijo
        if (!coincide) continue
        val numero = (0 until 4).firstNotNullOfOrNull { numeroDe(grilla.valor(fila, it)) } ?: continue
        candidatas.add(fila to numero)
    }
    if (candidatas.isNotEmpty()) {
        return candidatas.minBy { kotlin.math.abs(it.first - filaFija) }.second
    }
    return numeroDe(grilla.valor(filaFija, 1))
}

private fun esEjemplo(notas: String): Boolean = normalizar(notas) == "ejemplo"

private fun esMovimientoDeAhorro(categoria: String): Boolean =
    normalizar(categoria) == "movimiento de ahorro"
