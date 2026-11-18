package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.Ciclo
import com.gacorvalan.ratti.datos.modelo.ErrorModelo
import com.gacorvalan.ratti.datos.modelo.EstadoSemaforo
import com.gacorvalan.ratti.datos.modelo.ResumenCiclo
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val COL_B = 1
private const val COL_C = 2
private const val COL_E = 4
private const val COL_F = 5

/**
 * El código AAAA-MM es el mes en que cierra el resumen.
 * Los días se cuentan en el calendario entre [hoy] y el cierre: pueden ser 0 o negativos.
 * La planilla nunca muestra menos de 1; acá sí, para no ocultar que el ciclo ya cerró.
 */
internal fun interpretarCiclo(semaforo: Grilla, hoy: LocalDate): Ciclo {
    val cierreCrudo = semaforo.leer(COL_B, COL_C, filaFija = 7, coincide = tiene("proximo", "cierre"), acepta = ::esFecha)
    if (esVacio(cierreCrudo)) throw ErrorModelo.SinCierre
    val cierre = fechaDe(cierreCrudo) ?: throw ErrorModelo.CierreIlegible

    val codigoCrudo = semaforo.leer(COL_B, COL_C, filaFija = 11, coincide = ::etiquetaCiclo, acepta = ::esCodigoCiclo)
    val codigo = codigoCiclo(textoPlano(codigoCrudo).orEmpty())
        ?: String.format(Locale.ROOT, "%04d-%02d", cierre.year, cierre.monthValue)
    if (codigoCiclo(codigo) == null) throw ErrorModelo.CicloIlegible

    val dias = ChronoUnit.DAYS.between(hoy, cierre).toInt()
    return Ciclo(
        codigo = codigo,
        nombreMes = nombreDeMes(cierre.monthValue),
        cierre = cierre,
        diasHastaCierre = dias,
    )
}

/**
 * Tope en C13, gastado neto en F31, "te quedan" neto en F32 y "$ por día" neto en F33.
 * El color sale de esos netos: verde si queda al menos el 20 % del tope, amarillo si queda menos, rojo si te pasaste.
 */
internal fun interpretarResumen(semaforo: Grilla, ciclo: Ciclo): ResumenCiclo {
    val tope = numeroObligatorio(
        semaforo.leer(COL_B, COL_C, filaFija = 13, coincide = tiene("presupuesto"), acepta = ::esNumero),
        siFalta = ErrorModelo.SinTope,
        siIlegible = ErrorModelo.TopeIlegible,
    )
    val gastado = numeroObligatorio(
        semaforo.leer(COL_E, COL_F, filaFija = 31, coincide = tiene("gasto", "neto"), acepta = ::esNumero),
        siFalta = ErrorModelo.SinGastado,
        siIlegible = ErrorModelo.GastadoIlegible,
    )
    val teQuedan = numeroObligatorio(
        semaforo.leer(COL_E, COL_F, filaFija = 32, coincide = tiene("disponible", "neto"), acepta = ::esNumero),
        siFalta = ErrorModelo.SinDisponible,
        siIlegible = ErrorModelo.DisponibleIlegible,
    )
    val porDia = porDiaNeto(semaforo, teQuedan, ciclo.diasHastaCierre)
    return ResumenCiclo(
        tope = tope,
        gastado = gastado,
        teQuedan = teQuedan,
        porDia = porDia,
        estado = estadoSemaforo(teQuedan, tope),
        pasadoDeTope = teQuedan < 0.0,
    )
}

internal fun tipoDeCambio(semaforo: Grilla): Double {
    val crudo = semaforo.leer(COL_B, COL_C, filaFija = 10, coincide = tiene("cambio"), acepta = ::esNumero)
    if (esVacio(crudo)) return 0.0
    return numeroDe(crudo) ?: 0.0
}

internal fun estadoSemaforo(teQuedan: Double, tope: Double): EstadoSemaforo {
    if (teQuedan < 0.0) return EstadoSemaforo.Rojo
    if (tope > 0.0 && teQuedan * 5.0 < tope) return EstadoSemaforo.Amarillo
    return EstadoSemaforo.Verde
}

private fun porDiaNeto(semaforo: Grilla, teQuedan: Double, dias: Int): Double {
    val crudo = semaforo.leer(COL_E, COL_F, filaFija = 33, coincide = tiene("dia", "neto"), acepta = ::esNumero)
    if (esVacio(crudo)) {
        if (teQuedan <= 0.0 || dias <= 0) return 0.0
        return teQuedan / dias
    }
    return numeroDe(crudo) ?: throw ErrorModelo.PorDiaIlegible
}

private fun numeroObligatorio(
    valor: ValorCrudo?,
    siFalta: ErrorModelo,
    siIlegible: ErrorModelo,
): Double {
    if (esVacio(valor)) throw siFalta
    return numeroDe(valor) ?: throw siIlegible
}

private fun etiquetaCiclo(texto: String): Boolean = texto == "ciclo" || texto.startsWith("ciclo ")
