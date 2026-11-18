package com.gacorvalan.ratti.ui.ahorros

import com.gacorvalan.ratti.datos.modelo.Ahorros
import com.gacorvalan.ratti.datos.modelo.AsignacionUsdc
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Montos de ejemplo, los mismos que usa el test de la planilla.
 * No son saldos reales.
 */
class MapaAhorrosTest {
    @Test
    fun elTotalEnPesosUsaElTipoDeCambioDeLaPlanilla() {
        val resumen = resumenDe(ejemplo(), tipoCambio = 1000.0)

        assertEquals(330_000L, resumen.totalPesos)
        assertEquals(280.0, resumen.usdMercado!!, 0.001)
        assertEquals(300.0, resumen.usdCosto!!, 0.001)
        assertEquals(1000.0, resumen.tipoCambio!!, 0.001)
    }

    @Test
    fun sinTipoDeCambioNoInventaElTotalSiHayDolares() {
        val resumen = resumenDe(ejemplo(), tipoCambio = 0.0)

        assertNull(resumen.totalPesos)
        assertNull(resumen.tipoCambio)
        assertEquals(280.0, resumen.usdMercado!!, 0.001)
        assertEquals("US$280,00", dolares(resumen.usdMercado))
    }

    @Test
    fun sinDolaresElTotalEnPesosEsElColchonAunqueFalteElCambio() {
        val resumen = resumenDe(ejemplo().copy(voogMercadoUsd = 0.0, usdcTotal = 0.0), tipoCambio = 0.0)

        assertEquals(50_000L, resumen.totalPesos)
    }

    @Test
    fun losPesosSonElColchonConMetaYProgreso() {
        val colchon = resumenDe(ejemplo(), 1000.0).colchon

        assertEquals(50_000L, colchon.pesos)
        assertEquals(80_000L, colchon.meta)
        assertEquals(30_000L, colchon.diferencia)
        assertEquals(0.625f, colchon.fraccion!!, 0.001f)
        assertEquals(62.5, colchon.porcentaje!!, 0.001)
        assertEquals("62,5%", porcentajeFirmado(colchon.porcentaje))
    }

    @Test
    fun sinMetaElColchonNoInventaUnPorcentaje() {
        val colchon = resumenDe(ejemplo().copy(metaColchon = 0.0), 1000.0).colchon

        assertNull(colchon.meta)
        assertNull(colchon.porcentaje)
        assertNull(colchon.fraccion)
    }

    @Test
    fun elVoogEsCasaAMercadoYACosto() {
        val casa = resumenDe(ejemplo(), 1000.0).casa

        assertEquals(180.0, casa.mercado!!, 0.001)
        assertEquals(200.0, casa.costo!!, 0.001)
        assertEquals(-20.0, casa.resultado!!, 0.001)
        assertEquals(-10.0, casa.porcentaje!!, 0.001)
        assertEquals(2.5, casa.cuotapartes!!, 0.001)
        assertEquals(72.0, casa.precio!!, 0.001)
        assertEquals("−US$20,00", dolares(casa.resultado))
        assertEquals("−10%", porcentajeFirmado(casa.porcentaje))
    }

    @Test
    fun elUsdcSeRepartePorMontoYSinAsignarCierraLaSuma() {
        val dolares = resumenDe(ejemplo(), 1000.0).dolares

        assertEquals(listOf("MUELAS", "ANTEOJOS", "FOCUS", "ELECTRO", "GUSTOS"), dolares.objetivos.map { it.id })
        assertEquals(listOf(30.0, 10.0, 0.0, 20.0, 5.0), dolares.objetivos.map { it.monto })
        assertEquals(35.0, dolares.sinAsignar, 0.001)
        assertEquals(100.0, dolares.objetivos.sumOf { it.monto } + dolares.sinAsignar, 0.001)
        assertTrue(dolares.objetivos.single { it.id == "GUSTOS" }.fondo)
        assertTrue(dolares.objetivos.filter { it.id != "GUSTOS" }.none { it.fondo })
    }

    @Test
    fun sinAsignarSeRecalculaAunqueElModeloTraigaOtroResto() {
        val torcido = ejemplo().copy(
            asignacionUsdc = ejemplo().asignacionUsdc.map {
                if (it.fondo == "SIN_ASIGNAR") it.copy(monto = 99.0) else it
            },
        )
        val dolares = resumenDe(torcido, 1000.0).dolares

        assertEquals(35.0, dolares.sinAsignar, 0.001)
        assertEquals(dolares.total, dolares.objetivos.sumOf { it.monto } + dolares.sinAsignar, 0.001)
    }

    @Test
    fun elLugarLaFechaYLaMetaLleganSiLaPlanillaLosTrae() {
        val ahorros = ejemplo().copy(
            dondeColchon = "Caja ejemplo",
            dondeUsdc = "Billetera ejemplo",
            dondeVoog = "Broker ejemplo",
            fechaPrecioVoog = LocalDate.of(2024, 11, 10),
            metaCasaUsd = 50.0,
            asignacionUsdc = ejemplo().asignacionUsdc.map { asignacion ->
                if (asignacion.fondo == "MUELAS") asignacion.copy(metaUsd = 1_500.0) else asignacion
            },
        )
        val resumen = resumenDe(ahorros, 1000.0)

        assertEquals("Caja ejemplo", resumen.colchon.donde)
        assertEquals("Broker ejemplo", resumen.casa.donde)
        assertEquals(LocalDate.of(2024, 11, 10), resumen.casa.fechaPrecio)
        assertEquals("10/11", fechaCorta(resumen.casa.fechaPrecio!!))
        assertEquals(50.0, resumen.casa.metaUsd!!, 0.001)
        assertEquals("Billetera ejemplo", resumen.dolares.donde)
        assertEquals(1_500.0, resumen.dolares.objetivos.first { it.id == "MUELAS" }.metaUsd!!, 0.001)
        assertNull(resumen.dolares.objetivos.first { it.id == "GUSTOS" }.metaUsd)
    }

    @Test
    fun sinLugarNiFechaNiMetaElResumenLosDejaVacios() {
        val resumen = resumenDe(ejemplo(), 1000.0)

        assertNull(resumen.colchon.donde)
        assertNull(resumen.casa.donde)
        assertNull(resumen.casa.fechaPrecio)
        assertNull(resumen.casa.metaUsd)
        assertNull(resumen.dolares.donde)
        assertTrue(resumen.dolares.objetivos.all { it.metaUsd == null })
    }

    @Test
    fun losCentavosDelRepartoCierranConElTotal() {
        val ahorros = ejemplo().copy(
            usdcTotal = 10.0,
            asignacionUsdc = listOf(
                AsignacionUsdc("MUELAS", 0.004),
                AsignacionUsdc("ANTEOJOS", 1.005),
                AsignacionUsdc("FOCUS", 0.0),
                AsignacionUsdc("ELECTRO", 2.0),
                AsignacionUsdc("GUSTOS", 0.1),
            ),
        )
        val dolares = resumenDe(ahorros, 1.0).dolares
        val suma = dolares.objetivos.sumOf { it.monto } + dolares.sinAsignar

        assertEquals(0.0, dolares.objetivos.first { it.id == "MUELAS" }.monto, 0.0001)
        assertEquals(1.01, dolares.objetivos.first { it.id == "ANTEOJOS" }.monto, 0.0001)
        assertEquals(10.0, suma, 0.001)
        assertEquals("USDC 10,00", usdc(dolares.total))
    }

    private fun ejemplo() = Ahorros(
        colchonPesos = 50_000.0,
        metaColchon = 80_000.0,
        voogCuotapartes = 2.5,
        voogMercadoUsd = 180.0,
        voogCostoUsd = 200.0,
        usdcTotal = 100.0,
        asignacionUsdc = listOf(
            AsignacionUsdc("MUELAS", 30.0),
            AsignacionUsdc("ANTEOJOS", 10.0),
            AsignacionUsdc("FOCUS", 0.0),
            AsignacionUsdc("ELECTRO", 20.0),
            AsignacionUsdc("GUSTOS", 5.0),
            AsignacionUsdc("SIN_ASIGNAR", 35.0),
        ),
    )
}
