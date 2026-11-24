package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.Ahorros
import com.gacorvalan.ratti.datos.modelo.AsignacionUsdc

internal val FONDOS_USDC = listOf("MUELAS", "ANTEOJOS", "FOCUS", "ELECTRO", "GUSTOS")

internal const val SIN_ASIGNAR = "SIN_ASIGNAR"

internal fun interpretarAhorros(fondos: Grilla): Ahorros {
    val bolsillos = fondos.tabla("bolsillo", "saldo")
    val objetivos = fondos.tabla(
        "id",
        "objetivo",
        desdeFila = bolsillos?.let { filaSiguiente(fondos, "bolsillo", "saldo") } ?: 1,
    )
    val colchon = saldoBolsillo(bolsillos, "ars")
    val usdc = saldoBolsillo(bolsillos, "usdc")
    val voog = bolsillo(bolsillos, "voog")
    val asignados = FONDOS_USDC.associateWith { id ->
        asignadoUsdc(objetivos, id)
    }
    val sinAsignar = usdc - asignados.values.sum()
    return Ahorros(
        colchonPesos = colchon,
        metaColchon = metaDe(objetivos, "COLCHON"),
        voogCuotapartes = voog?.saldo ?: 0.0,
        voogMercadoUsd = voog?.mercadoUsd ?: 0.0,
        voogCostoUsd = voog?.costoUsd ?: 0.0,
        usdcTotal = usdc,
        asignacionUsdc = FONDOS_USDC.map { AsignacionUsdc(it, asignados.getValue(it)) } +
            AsignacionUsdc(SIN_ASIGNAR, sinAsignar),
    )
}

private data class Bolsillo(
    val saldo: Double,
    val costoUsd: Double,
    val mercadoUsd: Double,
)

private fun saldoBolsillo(tabla: Tabla?, id: String): Double = bolsillo(tabla, id)?.saldo ?: 0.0

private fun bolsillo(tabla: Tabla?, id: String): Bolsillo? {
    if (tabla == null) return null
    val colBolsillo = tabla.columna("bolsillo")
    val colSaldo = tabla.columna("saldo")
    val colCosto = tabla.columnaCon("valor", "costo")
    val colMercado = tabla.columnaCon("valor", "mercado", "usd")
    return tabla.filasDatos.mapNotNull { fila ->
        val nombre = tabla.texto(fila, colBolsillo) ?: return@mapNotNull null
        if (normalizar(nombre) != id) return@mapNotNull null
        Bolsillo(
            saldo = tabla.numero(fila, colSaldo) ?: 0.0,
            costoUsd = tabla.numero(fila, colCosto) ?: 0.0,
            mercadoUsd = tabla.numero(fila, colMercado) ?: 0.0,
        )
    }.reduceOrNull { acumulado, item ->
        Bolsillo(
            saldo = acumulado.saldo + item.saldo,
            costoUsd = acumulado.costoUsd + item.costoUsd,
            mercadoUsd = acumulado.mercadoUsd + item.mercadoUsd,
        )
    }
}

private fun metaDe(tabla: Tabla?, id: String): Double {
    val fila = filaObjetivo(tabla, id) ?: return 0.0
    val colMeta = tabla?.columna("meta") ?: return 0.0
    return tabla.numero(fila, colMeta) ?: 0.0
}

private fun asignadoUsdc(tabla: Tabla?, id: String): Double {
    val fila = filaObjetivo(tabla, id) ?: return 0.0
    val colBolsillo = tabla?.columna("bolsillo")
    val bolsillo = tabla?.texto(fila, colBolsillo).orEmpty()
    if (normalizar(bolsillo) != "usdc" && normalizar(bolsillo) != "usd") return 0.0
    val colAsignado = tabla?.columnaCon("asignado", "unidad") ?: tabla?.columna("asignado")
    return tabla?.numero(fila, colAsignado) ?: 0.0
}

private fun filaObjetivo(tabla: Tabla?, id: String): List<ValorCrudo?>? {
    if (tabla == null) return null
    val colId = tabla.columna("id") ?: return null
    return tabla.filasDatos.firstOrNull { fila ->
        normalizar(tabla.texto(fila, colId).orEmpty()) == normalizar(id)
    }
}

private fun filaSiguiente(grilla: Grilla, vararg encabezados: String): Int {
    val necesarios = encabezados.map { normalizar(it) }
    for (fila in 1..grilla.filas.size) {
        val textos = grilla.filas.getOrNull(fila - 1).orEmpty().mapNotNull { textoPlano(it)?.let(::normalizar) }
        val cubre = necesarios.all { requerido -> textos.any { it == requerido || (requerido.length > 3 && requerido in it) } }
        if (cubre) return fila + 1
    }
    return 1
}
