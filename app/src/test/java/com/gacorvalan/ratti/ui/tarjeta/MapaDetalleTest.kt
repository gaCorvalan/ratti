package com.gacorvalan.ratti.ui.tarjeta

import com.gacorvalan.ratti.datos.modelo.Ahorros
import com.gacorvalan.ratti.datos.modelo.Ciclo
import com.gacorvalan.ratti.datos.modelo.ConsumoTarjeta
import com.gacorvalan.ratti.datos.modelo.Cuota
import com.gacorvalan.ratti.datos.modelo.EstadoSemaforo
import com.gacorvalan.ratti.datos.modelo.ModeloFinanzas
import com.gacorvalan.ratti.datos.modelo.ResumenCiclo
import com.gacorvalan.ratti.datos.modelo.Tarjeta
import com.gacorvalan.ratti.ui.inicio.ColorConsumo
import com.gacorvalan.ratti.ui.inicio.ColorTarjeta
import com.gacorvalan.ratti.ui.inicio.inicioDe
import com.gacorvalan.ratti.ui.inicio.pesos
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapaDetalleTest {
    private val hoy = LocalDate.of(2026, 11, 18)

    @Test
    fun elToqueDeVisaAbreSuCicloSusConsumosYSusCuotas() {
        val finanzas = modelo()
        val id = inicioDe(finanzas).tarjetas.first { it.marca == "Visa" }.id
        val detalle = detalleDe(finanzas, id, hoy)!!

        assertEquals("Visa", detalle.titulo)
        assertEquals("VISA", detalle.pastilla)
        assertEquals(ColorTarjeta.Rosa, detalle.color)
        assertEquals(487_321L, detalle.total)
        assertEquals("$487.321", pesos(detalle.total))
        assertEquals("26/11", detalle.vencimiento)
        assertEquals("Noviembre, cierra el 26/11", detalle.ciclo)
        assertFalse(detalle.ciclo.contains("2026"))
        assertFalse(detalle.ciclo.contains("-"))

        assertEquals(listOf("Jumbo", "Rappi", "Coto", "Farmacity"), detalle.consumos.map { it.comercio })
        assertEquals(
            listOf("SUPER · HOY", "COMIDA FUERA · AYER", "SUPER · 8 NOV", "SALUD"),
            detalle.consumos.map { it.meta },
        )
        assertEquals(listOf("J", "R", "C", "F"), detalle.consumos.map { it.inicial })
        assertEquals(48_320L, detalle.consumos[0].monto)
        assertEquals("$48.320", pesos(detalle.consumos[0].monto))
        assertEquals(ColorConsumo.Lima, detalle.consumos[3].color)
        assertTrue(detalle.consumos.none { it.meta.contains("2026") })

        assertEquals("TV Samsung", detalle.cuotas[0].descripcion)
        assertEquals(85_400L, detalle.cuotas[0].monto)
        assertEquals("2 DE 6", detalle.cuotas[0].avance)
        assertEquals(341_600L, detalle.cuotas[0].restan)
        assertEquals("$341.600", pesos(detalle.cuotas[0].restan))
        assertEquals(2f / 6f, detalle.cuotas[0].fraccion)
        assertEquals("5 DE 12", detalle.cuotas[1].avance)
        assertEquals(5f / 12f, detalle.cuotas[1].fraccion)
        assertEquals(202_300L, detalle.cuotas[1].restan)
    }

    @Test
    fun amexEsNaranjaYElTotalLargoNoLlevaDecimales() {
        val finanzas = modelo(
            tarjetas = listOf(Tarjeta("Amex", 1_300_000.4, emptyList(), emptyList())),
        )
        val id = inicioDe(finanzas).tarjetas.single().id
        val detalle = detalleDe(finanzas, id, hoy)!!

        assertEquals(ColorTarjeta.Naranja, detalle.color)
        assertEquals("AMEX", detalle.pastilla)
        assertEquals("$1.300.000", pesos(detalle.total))
        assertTrue(detalle.consumos.isEmpty())
        assertTrue(detalle.cuotas.isEmpty())
    }

    @Test
    fun verTodosJuntaLosConsumosDelCicloPorFecha() {
        val todos = consumosDelCiclo(modelo(), hoy)

        assertEquals("Noviembre, cierra el 26/11", todos.ciclo)
        assertEquals(listOf("Jumbo", "YPF", "Rappi", "Coto", "Farmacity"), todos.consumos.map { it.comercio })
        assertEquals("NAFTA · HOY", todos.consumos[1].meta)
        assertEquals("COMIDA FUERA · AYER", todos.consumos[2].meta)
        assertFalse(todos.ciclo.contains("2026-11"))
    }

    @Test
    fun unIdQueNoEstaNoInventaUnaTarjeta() {
        assertNull(detalleDe(modelo(), "9-Visa", hoy))
    }

    @Test
    fun laUltimaCuotaNoDejaRestoYUnaDeMasTampoco() {
        val finanzas = modelo(
            tarjetas = listOf(
                Tarjeta(
                    marca = "Visa",
                    total = 10.0,
                    consumos = emptyList(),
                    cuotas = listOf(
                        Cuota("Última", 15_000.0, actual = 6, total = 6),
                        Cuota("Pasada", 15_000.0, actual = 8, total = 6),
                    ),
                ),
            ),
        )
        val cuotas = detalleDe(finanzas, inicioDe(finanzas).tarjetas.single().id, hoy)!!.cuotas

        assertEquals("6 DE 6", cuotas[0].avance)
        assertEquals(0L, cuotas[0].restan)
        assertEquals(1f, cuotas[0].fraccion)
        assertEquals(0L, cuotas[1].restan)
        assertEquals(1f, cuotas[1].fraccion)
    }

    private fun modelo(
        tarjetas: List<Tarjeta> = listOf(visa(), amex()),
    ) = ModeloFinanzas(
        ciclo = Ciclo(
            codigo = "2026-11",
            nombreMes = "Noviembre",
            cierre = LocalDate.of(2026, 11, 26),
            diasHastaCierre = 8,
        ),
        resumen = ResumenCiclo(
            tope = 1_300_000.0,
            gastado = 1_020_749.0,
            teQuedan = 279_251.0,
            porDia = 17_450.0,
            estado = EstadoSemaforo.Verde,
            pasadoDeTope = false,
        ),
        tarjetas = tarjetas,
        ultimosMovimientos = emptyList(),
        historial = emptyList(),
        ahorros = Ahorros(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, emptyList()),
    )

    private fun visa() = Tarjeta(
        marca = "Visa",
        total = 487_320.6,
        consumos = listOf(
            consumo("Jumbo", 48_320.0, "Super", hoy),
            consumo("Rappi", 12_450.0, "Comida fuera", hoy.minusDays(1)),
            consumo("Coto", 67_890.0, "Super", LocalDate.of(2026, 11, 8)),
            consumo("Farmacity", 18_740.0, "Salud", null),
        ),
        cuotas = listOf(
            Cuota("TV Samsung", 85_400.0, actual = 2, total = 6),
            Cuota("Seguro del auto", 28_900.0, actual = 5, total = 12),
        ),
    )

    private fun amex() = Tarjeta(
        marca = "Amex",
        total = 214_650.0,
        consumos = listOf(consumo("YPF", 32_100.0, "Nafta", hoy)),
        cuotas = emptyList(),
    )

    private fun consumo(comercio: String, monto: Double, categoria: String, fecha: LocalDate?) =
        ConsumoTarjeta(fecha = fecha, comercio = comercio, monto = monto, categoria = categoria)
}
