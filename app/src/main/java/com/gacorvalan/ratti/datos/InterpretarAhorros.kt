package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.Ahorros
import com.gacorvalan.ratti.datos.modelo.AsignacionUsdc
import java.time.LocalDate

internal val FONDOS_USDC = listOf("MUELAS", "ANTEOJOS", "FOCUS", "ELECTRO", "GUSTOS")

internal const val SIN_ASIGNAR = "SIN_ASIGNAR"

internal fun interpretarAhorros(fondos: Grilla): Ahorros {
    val bolsillos = fondos.tabla("bolsillo", "saldo")
    val objetivos = fondos.tabla(
        "id",
        "objetivo",
        desdeFila = bolsillos?.let { filaSiguiente(fondos, "bolsillo", "saldo") } ?: 1,
    )
    val colchon = bolsillo(bolsillos, "ars")
    val usdc = bolsillo(bolsillos, "usdc")
    val voog = bolsillo(bolsillos, "voog")
    val asignados = FONDOS_USDC.associateWith { id ->
        asignadoUsdc(objetivos, id)
    }
    val totalUsdc = usdc?.saldo ?: 0.0
    val sinAsignar = totalUsdc - asignados.values.sum()
    return Ahorros(
        colchonPesos = colchon?.saldo ?: 0.0,
        metaColchon = metaDe(objetivos, "COLCHON"),
        voogCuotapartes = voog?.saldo ?: 0.0,
        voogMercadoUsd = voog?.mercadoUsd ?: 0.0,
        voogCostoUsd = voog?.costoUsd ?: 0.0,
        usdcTotal = totalUsdc,
        asignacionUsdc = FONDOS_USDC.map { id ->
            AsignacionUsdc(id, asignados.getValue(id), metaUsdDe(objetivos, id))
        } + AsignacionUsdc(SIN_ASIGNAR, sinAsignar),
        dondeColchon = colchon?.donde,
        dondeUsdc = usdc?.donde,
        dondeVoog = voog?.donde,
        fechaPrecioVoog = voog?.fechaPrecio,
        metaCasaUsd = metaUsdDe(objetivos, "CASA"),
    )
}

private data class Bolsillo(
    val saldo: Double,
    val costoUsd: Double,
    val mercadoUsd: Double,
    val donde: String?,
    val fechaPrecio: LocalDate?,
)

private fun bolsillo(tabla: Tabla?, id: String): Bolsillo? {
    if (tabla == null) return null
    val colBolsillo = tabla.columna("bolsillo")
    val colSaldo = tabla.columna("saldo")
    val colCosto = tabla.columnaCon("valor", "costo")
    val colMercado = tabla.columnaCon("valor", "mercado", "usd")
    val colDonde = tabla.columna("donde esta") ?: tabla.columnaCon("donde")
    val colFecha = tabla.columna("fecha precio mercado")
        ?: tabla.columnaCon("fecha", "precio")
        ?: tabla.columnaCon("fecha", "mercado")
    return tabla.filasDatos.mapNotNull { fila ->
        val nombre = tabla.texto(fila, colBolsillo) ?: return@mapNotNull null
        if (normalizar(nombre) != id) return@mapNotNull null
        Bolsillo(
            saldo = tabla.numero(fila, colSaldo) ?: 0.0,
            costoUsd = tabla.numero(fila, colCosto) ?: 0.0,
            mercadoUsd = tabla.numero(fila, colMercado) ?: 0.0,
            donde = tabla.texto(fila, colDonde),
            fechaPrecio = tabla.fecha(fila, colFecha),
        )
    }.reduceOrNull { acumulado, item ->
        Bolsillo(
            saldo = acumulado.saldo + item.saldo,
            costoUsd = acumulado.costoUsd + item.costoUsd,
            mercadoUsd = acumulado.mercadoUsd + item.mercadoUsd,
            donde = acumulado.donde ?: item.donde,
            fechaPrecio = acumulado.fechaPrecio ?: item.fechaPrecio,
        )
    }
}

private fun metaDe(tabla: Tabla?, id: String): Double {
    val fila = filaObjetivo(tabla, id) ?: return 0.0
    val colMeta = tabla?.columna("meta") ?: return 0.0
    return tabla.numero(fila, colMeta) ?: 0.0
}

/** Meta en dólares. Si la moneda no es USD, o la celda está vacía, no hay meta. */
private fun metaUsdDe(tabla: Tabla?, id: String): Double? {
    val fila = filaObjetivo(tabla, id) ?: return null
    val colMoneda = tabla?.columna("moneda meta")
        ?: tabla?.columnaCon("moneda", "meta")
        ?: tabla?.columna("moneda")
    val moneda = tabla?.texto(fila, colMoneda).orEmpty()
    if (!esMonedaUsd(moneda)) return null
    val colMeta = tabla?.columna("meta") ?: return null
    return tabla.numero(fila, colMeta)
}

private fun esMonedaUsd(moneda: String): Boolean {
    val texto = normalizar(moneda)
    return texto == "usd" || texto == "usdc" || "usd" in texto
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
        val mapa = encabezadosDe(grilla.filas.getOrNull(fila - 1).orEmpty())
        if (cubreEncabezados(mapa, necesarios)) return fila + 1
    }
    return 1
}
