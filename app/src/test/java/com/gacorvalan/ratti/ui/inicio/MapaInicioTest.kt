package com.gacorvalan.ratti.ui.inicio

import com.gacorvalan.ratti.datos.modelo.Ahorros
import com.gacorvalan.ratti.datos.modelo.Ciclo
import com.gacorvalan.ratti.datos.modelo.EstadoSemaforo
import com.gacorvalan.ratti.datos.modelo.ModeloFinanzas
import com.gacorvalan.ratti.datos.modelo.Movimiento
import com.gacorvalan.ratti.datos.modelo.ResumenCiclo
import com.gacorvalan.ratti.datos.modelo.Tarjeta
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapaInicioTest {
    @Test
    fun pesosArgentinosSinDecimales() {
        assertEquals("$1.300.000", pesos(1_300_000L))
        assertEquals("$279.251", pesos(279_251L))
        assertEquals("−$42.000", pesos(-42_000L))
        assertEquals("$0", pesos(0L))
        assertFalse(pesos(1_300_000L).contains(','))
        assertFalse(pesos(1_300_000L).contains(' '))
    }

    @Test
    fun redondeaAPesosEnteros() {
        assertEquals(1_300_001L, pesosEnteros(1_300_000.5))
        assertEquals(1_300_000L, pesosEnteros(1_300_000.4))
        assertEquals(-42_000L, pesosEnteros(-42_000.4))
        assertEquals(0L, pesosEnteros(Double.NaN))
    }

    @Test
    fun armaElCicloLasTarjetasYLosUltimosTres() {
        val inicio = inicioDe(modelo())

        assertEquals("Noviembre, cierra el 26/11", inicio.ciclo)
        assertFalse(inicio.ciclo.contains("2026-11"))
        assertEquals("HOLA, GABRIEL.", inicio.saludo)
        assertEquals("R", inicio.inicialAvatar)
        assertFalse(inicio.ejemplo)
        assertEquals(1_020_749L, inicio.usado)
        assertEquals(1_300_000L, inicio.tope)
        assertEquals(279_251L, inicio.queda)
        assertEquals(17_450L, inicio.porDia)
        assertFalse(inicio.excedido)
        assertEquals(BandaSaldo.Naranja, inicio.bandaSaldo)
        assertEquals("QUEDA 21,5% · MENOS DEL 25%", textoEstadoSaldo(inicio))
        assertEquals(
            "Te quedan $279.251 · ~$17.450\npor día hasta el 26/11",
            captionSaldo(inicio),
        )
        assertTrue(textoCinta(inicio).contains("$17.450 POR DÍA HASTA EL 26/11"))
        assertTrue(textoCinta(inicio).contains("QUEDA"))

        assertEquals(listOf("Visa", "Amex"), inicio.tarjetas.map { it.marca })
        assertEquals(listOf(ColorTarjeta.Rosa, ColorTarjeta.Naranja), inicio.tarjetas.map { it.color })
        assertEquals(listOf(487_321L, 214_650L), inicio.tarjetas.map { it.monto })
        assertEquals(listOf("26/11", "26/11"), inicio.tarjetas.map { it.venceEl })
        assertTrue(inicio.tarjetas.all { it.banco.isEmpty() })

        assertEquals(listOf("Jumbo", "Rappi", "Ypf"), inicio.consumos.map { it.comercio })
        assertEquals(listOf("SUPER", "COMIDA FUERA", "NAFTA"), inicio.consumos.map { it.categoria })
        assertEquals(listOf("J", "R", "Y"), inicio.consumos.map { it.inicial })
        assertEquals(48_320L, inicio.consumos[0].monto)
    }

    @Test
    fun teQuedanNegativoUsaDangerYEscondePorDiaCero() {
        val inicio = inicioDe(
            modelo(
                teQuedan = -42_000.0,
                porDia = 0.0,
                gastado = 1_342_000.0,
                estado = EstadoSemaforo.Rojo,
                pasado = true,
            ),
        )

        assertTrue(inicio.excedido)
        assertEquals(0f, inicio.fraccionRestante)
        assertEquals(BandaSaldo.Rojo, inicio.bandaSaldo)
        assertEquals("Te quedan −$42.000", captionSaldo(inicio))
        assertEquals("QUEDA −3,2% · MENOS DEL 15%", textoEstadoSaldo(inicio))
        assertFalse(captionSaldo(inicio).contains("por día"))
        assertEquals("TE PASASTE 3,2%", marcaSemaforo(inicio))
        assertFalse(textoCinta(inicio).contains("POR DÍA"))
        assertTrue(textoCinta(inicio).contains("TE PASASTE"))
    }

    @Test
    fun unNetoNegativoChicoSiguePasadoDeTope() {
        val inicio = inicioDe(
            modelo(teQuedan = -0.4, porDia = 0.0, estado = EstadoSemaforo.Rojo, pasado = true),
        )

        assertEquals(0L, inicio.queda)
        assertTrue(inicio.excedido)
        assertEquals(BandaSaldo.Rojo, inicio.bandaSaldo)
    }

    @Test
    fun verdeHastaCuarentaYCincoYDespuesSinColetilla() {
        val justo = EstadoInicio(
            saludo = "HOLA, GABRIEL.",
            inicialAvatar = "R",
            mes = "Noviembre",
            cierre = "26/11",
            usado = 845_000L,
            tope = 1_300_000L,
            porDia = 28_438L,
            tarjetas = emptyList(),
            consumos = emptyList(),
            teQuedan = 455_000L,
        )
        assertEquals(BandaSaldo.Verde, justo.bandaSaldo)
        assertEquals("QUEDA 35% · MENOS DEL 45%", textoEstadoSaldo(justo))

        val holgado = justo.copy(teQuedan = 650_000L, usado = 650_000L)
        assertEquals(BandaSaldo.Verde, holgado.bandaSaldo)
        assertEquals("QUEDA 50%", textoEstadoSaldo(holgado))
    }

    @Test
    fun listasVacias() {
        val inicio = inicioDe(modelo(tarjetas = emptyList(), movimientos = emptyList()))

        assertTrue(inicio.tarjetas.isEmpty())
        assertTrue(inicio.consumos.isEmpty())
    }

    @Test
    fun montoLargoDeTarjetaSinDecimales() {
        val inicio = inicioDe(
            modelo(
                tarjetas = listOf(Tarjeta("Visa", 1_300_000.0, emptyList(), emptyList())),
            ),
        )

        assertEquals("$1.300.000", pesos(inicio.tarjetas.single().monto))
    }

    private fun modelo(
        teQuedan: Double = 279_251.0,
        porDia: Double = 17_450.0,
        gastado: Double = 1_020_749.0,
        tope: Double = 1_300_000.0,
        estado: EstadoSemaforo = EstadoSemaforo.Verde,
        pasado: Boolean = teQuedan < 0.0,
        tarjetas: List<Tarjeta> = listOf(
            Tarjeta("Visa", 487_320.6, emptyList(), emptyList()),
            Tarjeta("Amex", 214_650.0, emptyList(), emptyList()),
        ),
        movimientos: List<Movimiento> = listOf(
            movimiento("Jumbo", 48_320.0, "Super"),
            movimiento("Rappi", 12_450.0, "Comida fuera"),
            movimiento("Ypf", 32_100.0, "Nafta"),
            movimiento("Coto", 1_000.0, "Super"),
        ),
    ) = ModeloFinanzas(
        ciclo = Ciclo(
            codigo = "2026-11",
            nombreMes = "Noviembre",
            cierre = LocalDate.of(2026, 11, 26),
            diasHastaCierre = 8,
        ),
        resumen = ResumenCiclo(
            tope = tope,
            gastado = gastado,
            teQuedan = teQuedan,
            porDia = porDia,
            estado = estado,
            pasadoDeTope = pasado,
        ),
        tarjetas = tarjetas,
        ultimosMovimientos = movimientos,
        historial = emptyList(),
        ahorros = Ahorros(
            colchonPesos = 0.0,
            metaColchon = 0.0,
            voogCuotapartes = 0.0,
            voogMercadoUsd = 0.0,
            voogCostoUsd = 0.0,
            usdcTotal = 0.0,
            asignacionUsdc = emptyList(),
        ),
    )

    private fun movimiento(comercio: String, monto: Double, categoria: String) = Movimiento(
        fecha = LocalDate.of(2026, 11, 18),
        comercio = comercio,
        monto = monto,
        categoria = categoria,
        medio = "Visa",
        ciclo = "2026-11",
    )
}
