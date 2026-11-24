package com.gacorvalan.ratti.datos.modelo

import java.time.LocalDate

/**
 * Foto de la planilla, lista para las pantallas.
 * Los montos del ciclo son netos de aportes: un solo "te quedan" y un solo "por día".
 * [Ciclo.codigo] existe para filtrar; la UI usa [Ciclo.etiqueta], nunca el AAAA-MM.
 */
internal data class ModeloFinanzas(
    val ciclo: Ciclo,
    val resumen: ResumenCiclo,
    val tarjetas: List<Tarjeta>,
    val ultimosMovimientos: List<Movimiento>,
    val historial: List<MesHistorial>,
    val ahorros: Ahorros,
    /**
     * Pesos por dólar, celda de tipo de cambio del semáforo.
     * Cero si la planilla no lo trae: la UI no inventa un total en pesos.
     */
    val tipoCambio: Double = 0.0,
)

internal data class Ciclo(
    val codigo: String,
    val nombreMes: String,
    val cierre: LocalDate,
    val diasHastaCierre: Int,
) {
    /** "Noviembre, cierra el 26/11". */
    val etiqueta: String
        get() {
            val dia = cierre.dayOfMonth.toString().padStart(2, '0')
            val mes = cierre.monthValue.toString().padStart(2, '0')
            return "$nombreMes, cierra el $dia/$mes"
        }
}

internal enum class EstadoSemaforo { Verde, Amarillo, Rojo }

internal data class ResumenCiclo(
    val tope: Double,
    val gastado: Double,
    val teQuedan: Double,
    val porDia: Double,
    val estado: EstadoSemaforo,
    /** Verdadero cuando [teQuedan] es negativo. */
    val pasadoDeTope: Boolean,
)

internal data class Tarjeta(
    val marca: String,
    /** Variable del ciclo más fijos, cuotas y suscripciones, en un solo número. */
    val total: Double,
    val consumos: List<ConsumoTarjeta>,
    val cuotas: List<Cuota>,
)

internal data class ConsumoTarjeta(
    val fecha: LocalDate?,
    val comercio: String,
    val monto: Double,
    val categoria: String,
)

internal data class Cuota(
    val descripcion: String,
    val monto: Double,
    val actual: Int,
    val total: Int,
) {
    val texto: String get() = "$actual de $total"
}

internal data class Movimiento(
    val fecha: LocalDate?,
    val comercio: String,
    val monto: Double,
    val categoria: String,
    val medio: String,
    val ciclo: String,
)

internal data class GastoCategoria(
    val categoria: String,
    val monto: Double,
)

internal data class MesHistorial(
    val codigo: String,
    val nombreMes: String,
    val cierre: LocalDate?,
    val estado: String,
    val gasto: Double,
    val tope: Double,
    val categorias: List<GastoCategoria>,
)

internal data class Ahorros(
    val colchonPesos: Double,
    val metaColchon: Double,
    /** Todo el VOOG es Casa: cuotapartes, valor a mercado y valor a costo. */
    val voogCuotapartes: Double,
    val voogMercadoUsd: Double,
    val voogCostoUsd: Double,
    val usdcTotal: Double,
    /**
     * MUELAS, ANTEOJOS, FOCUS, ELECTRO, GUSTOS y SIN_ASIGNAR.
     * SIN_ASIGNAR es el total menos la suma del resto, así la asignación cierra.
     */
    val asignacionUsdc: List<AsignacionUsdc>,
    /** "Dónde está" de cada bolsillo. Nulo si la celda viene vacía. */
    val dondeColchon: String? = null,
    val dondeUsdc: String? = null,
    val dondeVoog: String? = null,
    /** Fecha del precio a mercado del VOOG. */
    val fechaPrecioVoog: LocalDate? = null,
    /** Meta de Casa en dólares, si el objetivo la trae en USD. */
    val metaCasaUsd: Double? = null,
)

internal data class AsignacionUsdc(
    val fondo: String,
    val monto: Double,
    /** Meta del fondo en dólares. Nula si no está o no es en USD. */
    val metaUsd: Double? = null,
)

internal sealed class ErrorModelo(message: String) : Exception(message) {
    data object SinCierre : ErrorModelo("No encontré la fecha de cierre.")
    data object CierreIlegible : ErrorModelo("No pude leer la fecha de cierre.")
    data object SinCiclo : ErrorModelo("No encontré el ciclo.")
    data object CicloIlegible : ErrorModelo("El ciclo no tiene forma AAAA-MM.")
    data object SinTope : ErrorModelo("No encontré el tope del ciclo.")
    data object TopeIlegible : ErrorModelo("No pude leer el tope del ciclo.")
    data object SinGastado : ErrorModelo("No encontré el gastado del ciclo.")
    data object GastadoIlegible : ErrorModelo("No pude leer el gastado del ciclo.")
    data object SinDisponible : ErrorModelo("No encontré cuánto queda del tope.")
    data object DisponibleIlegible : ErrorModelo("No pude leer cuánto queda del tope.")
    data object PorDiaIlegible : ErrorModelo("No pude leer el monto por día.")
    data class Pestana(val nombre: String) : ErrorModelo("No encontré la pestaña $nombre.")
}
