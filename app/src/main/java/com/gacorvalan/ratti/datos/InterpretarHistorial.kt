package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.GastoCategoria
import com.gacorvalan.ratti.datos.modelo.MesHistorial

internal val CATEGORIAS = listOf(
    "Fijos",
    "Suscripciones",
    "Super",
    "Comida fuera",
    "Ocio",
    "Transporte",
    "Mascotas",
    "Casa/hogar",
    "Salud",
    "Extraordinario",
    "Otros",
)

private val COLUMNAS_HISTORIAL = mapOf(
    "ciclo" to 0,
    "inicio" to 1,
    "cierre" to 2,
    "estado" to 3,
    "tope" to 4,
    "gasto bruto" to 7,
    "gasto neto" to 9,
) + CATEGORIAS.mapIndexed { indice, nombre -> normalizar(nombre) to (14 + indice) }

internal fun interpretarHistorial(
    historial: Grilla,
    categoriasResumen: Grilla,
    codigoActual: String,
): List<MesHistorial> {
    val tabla = historial.tabla("ciclo", "tope", "gasto neto")
        ?: Tabla(COLUMNAS_HISTORIAL, historial.filas.drop(4))
    val colCiclo = tabla.columna("ciclo")
    val colCierre = tabla.columna("cierre")
    val colEstado = tabla.columna("estado")
    val colTope = tabla.columna("tope")
    val colNeto = tabla.columna("gasto neto")
    val colBruto = tabla.columna("gasto bruto")
    val columnasCategoria = CATEGORIAS.associateWith { tabla.columna(normalizar(it)) }
    val respaldo = categoriasDelResumen(categoriasResumen)
    return tabla.filasDatos.mapNotNull { fila ->
        val codigo = codigoCiclo(tabla.texto(fila, colCiclo).orEmpty()) ?: return@mapNotNull null
        val categorias = columnasCategoria.map { (nombre, columna) ->
            nombre to tabla.numero(fila, columna)
        }
        val sinDesglose = categorias.all { it.second == null }
        val montos = if (sinDesglose && codigo == codigoActual && respaldo.isNotEmpty()) {
            respaldo
        } else {
            categorias.map { (nombre, monto) -> GastoCategoria(nombre, monto ?: 0.0) }
        }
        val neto = tabla.numero(fila, colNeto)
        val bruto = tabla.numero(fila, colBruto)
        MesHistorial(
            codigo = codigo,
            nombreMes = nombreDeCodigo(codigo),
            cierre = tabla.fecha(fila, colCierre),
            estado = tabla.texto(fila, colEstado).orEmpty(),
            gasto = neto ?: bruto ?: 0.0,
            tope = tabla.numero(fila, colTope) ?: 0.0,
            categorias = montos,
        )
    }.sortedBy { it.codigo }
}

private fun categoriasDelResumen(grilla: Grilla): List<GastoCategoria> {
    val tabla = grilla.tabla("categoria", "total") ?: return emptyList()
    val colCategoria = tabla.columna("categoria")
    val colTotal = tabla.columna("total")
    val conocidas = CATEGORIAS.associateBy { normalizar(it) }
    val porNombre = LinkedHashMap<String, Double>()
    for (fila in tabla.filasDatos) {
        val nombre = tabla.texto(fila, colCategoria) ?: continue
        val canonico = conocidas[normalizar(nombre)] ?: continue
        val total = tabla.numero(fila, colTotal) ?: continue
        porNombre.putIfAbsent(canonico, total)
    }
    if (porNombre.isEmpty()) return emptyList()
    return CATEGORIAS.map { nombre -> GastoCategoria(nombre, porNombre[nombre] ?: 0.0) }
}
