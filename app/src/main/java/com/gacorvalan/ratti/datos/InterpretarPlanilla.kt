package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.EstadoSemaforo
import com.gacorvalan.ratti.datos.modelo.ModeloFinanzas
import java.time.LocalDate

/** Arma el modelo entero a partir de las pestañas crudas. No toca la red. */
internal fun interpretarPlanilla(
    pestanas: Map<String, List<List<ValorCrudo?>>>,
    hoy: LocalDate,
): ModeloFinanzas {
    val semaforo = pestanas.grilla("Semáforo")
    val ciclo = interpretarCiclo(semaforo, hoy)
    return ModeloFinanzas(
        ciclo = ciclo,
        resumen = interpretarResumen(semaforo, ciclo),
        tarjetas = interpretarTarjetas(
            proximoMes = pestanas.grilla("Próximo mes"),
            fijos = pestanas.grilla("Fijos y cuotas"),
            movimientos = pestanas.grilla("Movimientos"),
            codigoCiclo = ciclo.codigo,
            tipoCambio = tipoDeCambio(semaforo),
        ),
        ultimosMovimientos = ultimosMovimientos(pestanas.grilla("Movimientos")),
        historial = interpretarHistorial(
            historial = pestanas.grilla("Historial"),
            categoriasResumen = pestanas.grilla("Categorías resumen"),
            codigoActual = ciclo.codigo,
        ),
        ahorros = interpretarAhorros(pestanas.grilla("Fondos")),
    )
}

/** Una línea para la pantalla de depuración. No muestra el código AAAA-MM. */
internal fun textoParaDepurar(modelo: ModeloFinanzas): String {
    val dias = when (val n = modelo.ciclo.diasHastaCierre) {
        0 -> "cierra hoy"
        1 -> "falta 1 día"
        in 2..Int.MAX_VALUE -> "faltan $n días"
        -1 -> "cerró ayer"
        else -> "cerró hace ${-n} días"
    }
    val queda = if (modelo.resumen.pasadoDeTope) {
        "te pasaste ${pesosArgentino(kotlin.math.abs(modelo.resumen.teQuedan))}"
    } else {
        "te quedan ${pesosArgentino(modelo.resumen.teQuedan)}"
    }
    val estado = when (modelo.resumen.estado) {
        EstadoSemaforo.Verde -> "verde"
        EstadoSemaforo.Amarillo -> "amarillo"
        EstadoSemaforo.Rojo -> "rojo"
    }
    val tarjetas = modelo.tarjetas.joinToString(" · ") { "${it.marca} ${pesosArgentino(it.total)}" }
    val gastado = pesosArgentino(modelo.resumen.gastado)
    return "${modelo.ciclo.etiqueta} · $dias · gastado $gastado · $queda · $estado · $tarjetas"
}
