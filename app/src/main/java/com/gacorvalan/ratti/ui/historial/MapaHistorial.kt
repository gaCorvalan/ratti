package com.gacorvalan.ratti.ui.historial

import com.gacorvalan.ratti.datos.modelo.MesHistorial
import com.gacorvalan.ratti.datos.nombreDeMes
import com.gacorvalan.ratti.ui.inicio.pesosEnteros
import java.time.LocalDate

/**
 * Pasa los ciclos ya cargados al estado de Historial.
 * El nombre sale de la fecha de cierre. El código AAAA-MM no se muestra.
 * El gasto es un solo número: el neto, o el bruto si el neto no vino.
 */
internal fun ciclosDe(meses: List<MesHistorial>, codigoActual: String): List<CicloHistorial> =
    meses.sortedByDescending { it.codigo }.map { it.aCiclo(codigoActual) }

private fun MesHistorial.aCiclo(codigoActual: String): CicloHistorial {
    val fecha = cierre
    return CicloHistorial(
        id = codigo,
        mes = if (fecha != null) nombreDeMes(fecha.monthValue) else nombreMes,
        cierre = fecha?.let(::fechaCorta).orEmpty(),
        gasto = pesosEnteros(gasto),
        tope = pesosEnteros(tope),
        actual = codigo == codigoActual,
        categorias = categorias
            .map { CategoriaHistorial(it.categoria, pesosEnteros(it.monto)) }
            .filter { it.monto != 0L },
    )
}

private fun fechaCorta(fecha: LocalDate): String {
    val dia = fecha.dayOfMonth.toString().padStart(2, '0')
    val mes = fecha.monthValue.toString().padStart(2, '0')
    return "$dia/$mes"
}
