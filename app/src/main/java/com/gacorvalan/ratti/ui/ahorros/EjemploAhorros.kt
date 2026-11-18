package com.gacorvalan.ratti.ui.ahorros

import com.gacorvalan.ratti.datos.modelo.Ahorros
import com.gacorvalan.ratti.datos.modelo.AsignacionUsdc
import java.time.LocalDate

/**
 * Números chicos del test de la planilla, solo para previews.
 * No son saldos reales.
 */
private fun ahorrosDeEjemplo() = Ahorros(
    colchonPesos = 50_000.0,
    metaColchon = 80_000.0,
    voogCuotapartes = 2.5,
    voogMercadoUsd = 180.0,
    voogCostoUsd = 200.0,
    usdcTotal = 100.0,
    asignacionUsdc = listOf(
        AsignacionUsdc("MUELAS", 30.0, metaUsd = 1000.0),
        AsignacionUsdc("ANTEOJOS", 10.0, metaUsd = 200.0),
        AsignacionUsdc("FOCUS", 0.0),
        AsignacionUsdc("ELECTRO", 20.0, metaUsd = 50.0),
        AsignacionUsdc("GUSTOS", 5.0),
        AsignacionUsdc("SIN_ASIGNAR", 35.0),
    ),
    dondeColchon = "Banco ejemplo",
    dondeUsdc = "Billetera ejemplo",
    dondeVoog = "Broker ejemplo",
    fechaPrecioVoog = LocalDate.of(2024, 4, 1),
)

internal fun ejemploConTipoDeCambio(): ResumenAhorros = resumenDe(ahorrosDeEjemplo(), tipoCambio = 1000.0)

internal fun ejemploSinTipoDeCambio(): ResumenAhorros = resumenDe(ahorrosDeEjemplo(), tipoCambio = 0.0)
