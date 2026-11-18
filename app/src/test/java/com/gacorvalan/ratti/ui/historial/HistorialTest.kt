package com.gacorvalan.ratti.ui.historial

import com.gacorvalan.ratti.datos.modelo.GastoCategoria
import com.gacorvalan.ratti.datos.modelo.MesHistorial
import com.gacorvalan.ratti.ui.inicio.pesos
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorialTest {
    private val ciclos = DatosHistorial.ciclos
    private val noviembre = ciclos.first { it.mes == "Noviembre" }
    private val septiembre = ciclos.first { it.mes == "Septiembre" }

    @Test
    fun losNumerosDeFigmaQuedanSoloParaLaPreview() {
        assertEquals(1_020_749L, noviembre.gasto)
        assertEquals(1_300_000L, noviembre.tope)
        assertTrue(DatosHistorial.ciclos.none { it.id.contains('-') && it.id.first().isDigit() })
    }

    @Test
    fun losMesesSeNombranComoCicloYNuncaComoAnioMes() {
        val iso = Regex("\\d{4}-\\d{2}")
        ciclos.forEach { ciclo ->
            assertEquals("${ciclo.mes}, cierra el ${ciclo.cierre}", ciclo.frase)
            assertFalse(iso.containsMatchIn(ciclo.frase))
            assertFalse(iso.containsMatchIn(pastillaCiclo(ciclo.mes, ciclo.cierre)))
        }
        assertEquals("Noviembre, cierra el 26/11", noviembre.frase)
        assertEquals("NOVIEMBRE · CIERRA 26/11", pastillaCiclo(noviembre.mes, noviembre.cierre))
    }

    @Test
    fun noviembreQuedaDentroDelTopeYSeptiembreLoPasa() {
        assertEquals(TonoMes.Actual, noviembre.tono)
        assertFalse(noviembre.excedido)
        assertEquals(78, noviembre.porcentajeEntero)
        assertEquals("78%", textoPorcentajeTope(noviembre))
        assertEquals("$1.020.749", pesos(noviembre.gasto))
        assertEquals("de $1.300.000  ·  te quedan $279.251", subtituloHero(noviembre))
        assertEquals("Usaste $1.020.749 de $1.300.000", textoUsaste(noviembre))
        assertEquals("QUEDA 21,5%", marcaSemaforo(noviembre))

        assertEquals(TonoMes.Excedido, septiembre.tono)
        assertTrue(septiembre.excedido)
        assertEquals(103, septiembre.porcentajeEntero)
        assertEquals("103%", textoPorcentajeTope(septiembre))
        assertEquals("$1.342.000", pesos(septiembre.gasto))
        assertEquals("de $1.300.000  ·  te pasaste $42.000", subtituloHero(septiembre))
        assertEquals("TE PASASTE 3,2%", marcaSemaforo(septiembre))
        assertEquals(1f, fraccionDelTope(septiembre.gasto, septiembre.tope))
    }

    @Test
    fun unMesPasadoDelTopeSePintaEnDangerAunqueSeaElActual() {
        val pasado = noviembre.copy(gasto = noviembre.tope + 1, actual = true)
        assertEquals(TonoMes.Excedido, pasado.tono)
    }

    @Test
    fun lasCategoriasDeNoviembreSumenElGastoYVanContraElTope() {
        assertEquals(noviembre.gasto, noviembre.categorias.sumOf { it.monto })
        val superMercado = noviembre.categorias.first { it.nombre == "Super" }
        assertEquals("15,3%", textoPorcentajeCategoria(superMercado.monto, noviembre.tope))
        assertEquals("24,0%", textoPorcentajeCategoria(312_400L, noviembre.tope))
        ciclos.filter { it.categorias.isNotEmpty() }.forEach { ciclo ->
            assertEquals(ciclo.gasto, ciclo.categorias.sumOf { it.monto })
        }
    }

    @Test
    fun elGraficoVaDelMasViejoAlMasNuevoContraElTope() {
        assertEquals(
            listOf("JUN", "JUL", "AGO", "SEP", "OCT", "NOV"),
            cronologico(ciclos).map { siglaMes(it.mes) },
        )
        assertEquals(1_300_000L, topeDelGrafico(ciclos))
        assertTrue(fraccionDelTope(noviembre.gasto, noviembre.tope) < 1f)
    }

    @Test
    fun unTopeEnCeroNoDivide() {
        val vacio = CicloHistorial(
            id = "marzo",
            mes = "Marzo",
            cierre = "26/03",
            gasto = 0L,
            tope = 0L,
            actual = false,
            categorias = emptyList(),
        )
        assertFalse(vacio.excedido)
        assertEquals(0f, fraccionDelTope(vacio.gasto, vacio.tope))
        assertEquals(0, vacio.porcentajeEntero)
        assertEquals(TonoMes.Excedido, vacio.copy(gasto = 10L).tono)
        assertEquals(1f, fraccionDelTope(10L, 0L))
        assertEquals("−$10", pesos(-10L))
        assertEquals("Marzo", vacio.copy(cierre = "").frase)
    }

    @Test
    fun elHistorialDeLaPlanillaUsaLaFechaDeCierreYUnSoloGasto() {
        val ciclosReales = ciclosDe(
            meses = listOf(
                MesHistorial(
                    codigo = "2026-09",
                    nombreMes = "Septiembre",
                    cierre = LocalDate.of(2026, 9, 26),
                    estado = "cerrado",
                    gasto = 1_342_000.4,
                    tope = 1_300_000.0,
                    categorias = listOf(
                        GastoCategoria("Super", 280_000.0),
                        GastoCategoria("Fijos", 0.0),
                    ),
                ),
                MesHistorial(
                    codigo = "2026-11",
                    nombreMes = "Noviembre",
                    cierre = LocalDate.of(2026, 11, 26),
                    estado = "abierto",
                    gasto = 1_020_749.0,
                    tope = 1_300_000.0,
                    categorias = listOf(GastoCategoria("Super", 198_450.2)),
                ),
            ),
            codigoActual = "2026-11",
        )

        assertEquals(listOf("Noviembre", "Septiembre"), ciclosReales.map { it.mes })
        assertEquals("Noviembre, cierra el 26/11", ciclosReales[0].frase)
        assertEquals("Septiembre, cierra el 26/09", ciclosReales[1].frase)
        assertFalse(ciclosReales.any { it.frase.contains("2026-") })
        assertEquals(TonoMes.Actual, ciclosReales[0].tono)
        assertEquals(TonoMes.Excedido, ciclosReales[1].tono)
        assertEquals(1_342_000L, ciclosReales[1].gasto)
        assertEquals(listOf("Super"), ciclosReales[1].categorias.map { it.nombre })
        assertEquals(198_450L, ciclosReales[0].categorias.single().monto)
        assertEquals(listOf("SEP", "NOV"), cronologico(ciclosReales).map { siglaMes(it.mes) })
    }

    @Test
    fun sinFechaDeCierreQuedaElNombreDelMes() {
        val ciclo = ciclosDe(
            meses = listOf(
                MesHistorial(
                    codigo = "2026-03",
                    nombreMes = "Marzo",
                    cierre = null,
                    estado = "",
                    gasto = 10.0,
                    tope = 100.0,
                    categorias = emptyList(),
                ),
            ),
            codigoActual = "2026-04",
        ).single()
        assertEquals("Marzo", ciclo.mes)
        assertEquals("Marzo", ciclo.frase)
        assertEquals("MARZO", pastillaCiclo(ciclo.mes, ciclo.cierre))
        assertEquals(TonoMes.Normal, ciclo.tono)
        assertTrue(ciclo.categorias.isEmpty())
    }
}
