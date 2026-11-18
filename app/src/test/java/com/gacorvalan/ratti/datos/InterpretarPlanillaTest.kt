package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.ErrorModelo
import com.gacorvalan.ratti.datos.modelo.EstadoSemaforo
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Montos, nombres y fechas inventados para el ejemplo.
 * No son los de la planilla real.
 */
class InterpretarPlanillaTest {
    private val hoy = LocalDate.of(2024, 4, 1)

    @Test
    fun armaElModeloConDatosDeEjemplo() {
        val modelo = modeloCompleto()

        assertEquals("2024-04", modelo.ciclo.codigo)
        assertEquals("Abril", modelo.ciclo.nombreMes)
        assertEquals(LocalDate.of(2024, 4, 5), modelo.ciclo.cierre)
        assertEquals(4, modelo.ciclo.diasHastaCierre)
        assertEquals("Abril, cierra el 05/04", modelo.ciclo.etiqueta)
        assertFalse(modelo.ciclo.etiqueta.contains("2024-04"))

        assertEquals(100_000.0, modelo.resumen.tope, 0.001)
        assertEquals(40_000.0, modelo.resumen.gastado, 0.001)
        assertEquals(60_000.0, modelo.resumen.teQuedan, 0.001)
        assertEquals(1111.0, modelo.resumen.porDia, 0.001)
        assertEquals(EstadoSemaforo.Verde, modelo.resumen.estado)
        assertFalse(modelo.resumen.pasadoDeTope)

        val visa = modelo.tarjetas[0]
        val amex = modelo.tarjetas[1]
        assertEquals(listOf("Visa", "Amex"), modelo.tarjetas.map { it.marca })
        assertEquals(4_500.0, visa.total, 0.001)
        assertEquals(800.0, amex.total, 0.001)
        assertEquals(
            listOf("Tarde ejemplo", "Temprano ejemplo", "Devolucion ejemplo", "Kiosco ejemplo", "Fecha rota ejemplo"),
            visa.consumos.map { it.comercio },
        )
        assertEquals(listOf(20.0, 10.0, -100.0, 1500.0, 70.0), visa.consumos.map { it.monto })
        assertNull(visa.consumos.last().fecha)
        assertEquals(listOf("Cafe ejemplo"), amex.consumos.map { it.comercio })
        assertEquals(listOf("Silla ejemplo", "Curso ejemplo"), visa.cuotas.map { it.descripcion })
        assertEquals("2 de 6", visa.cuotas[0].texto)
        assertEquals(3000.0, visa.cuotas[0].monto, 0.001)
        assertEquals("4 de 10", visa.cuotas[1].texto)
        assertEquals(2000.0, visa.cuotas[1].monto, 0.001)
        assertTrue(amex.cuotas.isEmpty())

        assertEquals(
            listOf(
                "Tarde ejemplo",
                "Temprano ejemplo",
                "Devolucion ejemplo",
                "Colectivo ejemplo",
                "Cafe ejemplo",
                "Kiosco ejemplo",
                "Almacen ejemplo",
                "Fecha rota ejemplo",
            ),
            modelo.ultimosMovimientos.map { it.comercio },
        )
        assertEquals("2024-03", modelo.ultimosMovimientos[6].ciclo)
        assertEquals("Débito", modelo.ultimosMovimientos[3].medio)

        assertEquals(listOf("2024-03", "2024-04"), modelo.historial.map { it.codigo })
        assertEquals("Marzo", modelo.historial[0].nombreMes)
        assertEquals("Cerrado", modelo.historial[0].estado)
        assertEquals(50_000.0, modelo.historial[0].gasto, 0.001)
        assertEquals(80_000.0, modelo.historial[0].tope, 0.001)
        assertEquals(32_000.0, monto(modelo.historial[0], "Super"), 0.001)
        assertEquals(18_000.0, monto(modelo.historial[0], "Ocio"), 0.001)
        assertEquals(0.0, monto(modelo.historial[0], "Salud"), 0.001)
        assertEquals(40_000.0, modelo.historial[1].gasto, 0.001)
        assertEquals(20_000.0, monto(modelo.historial[1], "Super"), 0.001)
        assertEquals(800.0, monto(modelo.historial[1], "Comida fuera"), 0.001)

        assertEquals(50_000.0, modelo.ahorros.colchonPesos, 0.001)
        assertEquals(80_000.0, modelo.ahorros.metaColchon, 0.001)
        assertEquals(2.5, modelo.ahorros.voogCuotapartes, 0.001)
        assertEquals(200.0, modelo.ahorros.voogCostoUsd, 0.001)
        assertEquals(180.0, modelo.ahorros.voogMercadoUsd, 0.001)
        assertEquals(100.0, modelo.ahorros.usdcTotal, 0.001)
        assertEquals(
            listOf("MUELAS", "ANTEOJOS", "FOCUS", "ELECTRO", "GUSTOS", "SIN_ASIGNAR"),
            modelo.ahorros.asignacionUsdc.map { it.fondo },
        )
        assertEquals(listOf(30.0, 10.0, 0.0, 20.0, 5.0, 35.0), modelo.ahorros.asignacionUsdc.map { it.monto })
        assertEquals(100.0, modelo.ahorros.asignacionUsdc.sumOf { it.monto }, 0.001)

        assertEquals(
            "Abril, cierra el 05/04 · faltan 4 días · gastado $40.000 · te quedan $60.000 · verde · Visa $4.500 · Amex $800",
            textoParaDepurar(modelo),
        )
        assertFalse(Regex("\\d{4}-\\d{2}").containsMatchIn(textoParaDepurar(modelo)))
    }

    @Test
    fun leeElSemaforoPorPosicionAunqueElColorBrutoDigaOtraCosa() {
        val semaforo = semaforo(etiquetas = false)
            .p("E6", "ROJO")
            .p("C16", 999_999)
            .p("C17", -50)
        val modelo = modelo(hoy, semaforo)

        assertEquals(40_000.0, modelo.resumen.gastado, 0.001)
        assertEquals(60_000.0, modelo.resumen.teQuedan, 0.001)
        assertEquals(EstadoSemaforo.Verde, modelo.resumen.estado)
        assertEquals(4, modelo.ciclo.diasHastaCierre)
    }

    @Test
    fun laEtiquetaGanaALaCeldaFija() {
        val semaforo = Hoja()
            .p("C7", "01/01/2020")
            .p("B20", "Próximo cierre")
            .p("C20", "05/04/2024")
            .p("C11", "2020-01")
            .p("B21", "Ciclo")
            .p("C21", "2024-04")
            .p("C13", 1)
            .p("B22", "Presupuesto del ciclo")
            .p("C22", 100_000)
            .p("F31", 1)
            .p("E40", "Gasto neto de aportes")
            .p("F40", 40_000)
            .p("F32", 1)
            .p("E41", "Disponible neto")
            .p("F41", 60_000)
            .p("F33", 1)
            .p("E42", "$ / día restante (neto)")
            .p("F42", 1111)
        val modelo = modelo(hoy, semaforo)

        assertEquals(LocalDate.of(2024, 4, 5), modelo.ciclo.cierre)
        assertEquals("2024-04", modelo.ciclo.codigo)
        assertEquals(100_000.0, modelo.resumen.tope, 0.001)
        assertEquals(40_000.0, modelo.resumen.gastado, 0.001)
        assertEquals(60_000.0, modelo.resumen.teQuedan, 0.001)
        assertEquals(1111.0, modelo.resumen.porDia, 0.001)
    }

    @Test
    fun elNombreDelMesSaleDelCierreYElFiltroUsaElCodigo() {
        val semaforo = semaforo(cierre = "05/04/2024", codigo = "2024-05", etiquetas = false)
        val movimientos = Hoja()
            .f(4, "Fecha", "Comercio", "Monto", "Categoría", "Medio", "Notas", "Ciclo")
            .f(5, "02/04/2024", "Entra ejemplo", 10, "Super", "Visa", null, "2024-05")
            .f(6, "02/04/2024", "Queda afuera ejemplo", 99, "Super", "Visa", null, "2024-04")
        val modelo = modelo(hoy, semaforo, "Movimientos" to movimientos)

        assertEquals("Abril", modelo.ciclo.nombreMes)
        assertEquals("Abril, cierra el 05/04", modelo.ciclo.etiqueta)
        assertEquals("2024-05", modelo.ciclo.codigo)
        assertEquals(listOf("Entra ejemplo"), modelo.tarjetas[0].consumos.map { it.comercio })
        assertEquals(listOf("Queda afuera ejemplo", "Entra ejemplo"), modelo.ultimosMovimientos.map { it.comercio })
    }

    @Test
    fun estadosDelSemaforoYDias() {
        assertEquals(EstadoSemaforo.Verde, resumen(quedan = 20_000).estado)
        assertEquals(EstadoSemaforo.Amarillo, resumen(quedan = 19_999).estado)
        assertEquals(EstadoSemaforo.Amarillo, resumen(quedan = 0).estado)
        assertFalse(resumen(quedan = 0).pasadoDeTope)
        val rojo = resumen(quedan = -1, porDia = 0)
        assertEquals(EstadoSemaforo.Rojo, rojo.estado)
        assertTrue(rojo.pasadoDeTope)
        assertEquals(0.0, rojo.porDia, 0.001)
        assertTrue(textoParaDepurar(modelo(hoy, semaforo(quedan = -1, porDia = 0))).contains("te pasaste $1 · rojo"))

        assertEquals("Noviembre, cierra el 03/11", ciclo(LocalDate.of(2024, 11, 3), "03/11/2024").etiqueta)
        assertEquals(0, ciclo(LocalDate.of(2024, 11, 3), "03/11/2024").diasHastaCierre)
        assertEquals(1, ciclo(LocalDate.of(2024, 11, 2), "03/11/2024").diasHastaCierre)
        assertEquals(-1, ciclo(LocalDate.of(2024, 11, 4), "03/11/2024").diasHastaCierre)
        assertEquals(-5, ciclo(LocalDate.of(2024, 11, 8), "03/11/2024").diasHastaCierre)
        assertTrue(textoParaDepurar(modelo(LocalDate.of(2024, 11, 3), semaforo(cierre = "03/11/2024", codigo = "2024-11"))).contains("cierra hoy"))
        assertTrue(textoParaDepurar(modelo(LocalDate.of(2024, 11, 4), semaforo(cierre = "03/11/2024", codigo = "2024-11"))).contains("cerró ayer"))
        assertTrue(textoParaDepurar(modelo(LocalDate.of(2024, 11, 2), semaforo(cierre = "03/11/2024", codigo = "2024-11"))).contains("falta 1 día"))
        assertTrue(textoParaDepurar(modelo(LocalDate.of(2024, 11, 8), semaforo(cierre = "03/11/2024", codigo = "2024-11"))).contains("cerró hace 5 días"))
    }

    @Test
    fun elPorDiaVacioSeRecalculaYElCeroDeLaCeldaSeRespeta() {
        val recalculado = modelo(hoy, semaforo(porDia = null)).resumen.porDia
        assertEquals(15_000.0, recalculado, 0.001)
        assertEquals(0.0, modelo(hoy, semaforo(porDia = 0)).resumen.porDia, 0.001)
        assertEquals(100_000.0, modelo(hoy, semaforo(tope = "$ 100.000")).resumen.tope, 0.001)
    }

    @Test
    fun erroresClarosSiFaltaONoSeLeeElSemaforo() {
        assertThrows(ErrorModelo.SinCierre::class.java) {
            modelo(hoy, Hoja())
        }
        assertThrows(ErrorModelo.SinTope::class.java) {
            modelo(hoy, semaforo().p("C13", null).p("B13", null))
        }
        assertThrows(ErrorModelo.TopeIlegible::class.java) {
            modelo(hoy, semaforo(etiquetas = false, tope = "abc"))
        }
        assertThrows(ErrorModelo.PorDiaIlegible::class.java) {
            modelo(hoy, semaforo(etiquetas = false, porDia = "abc"))
        }
        val error = assertThrows(ErrorModelo.Pestana::class.java) {
            interpretarPlanilla(emptyMap(), hoy)
        }
        assertEquals("Semáforo", error.nombre)
    }

    @Test
    fun sinAsignarCierraLaSumaAunqueElFondoNoEsteEnUsdc() {
        val fondos = Hoja()
            .f(5, "Bolsillo", "Saldo")
            .f(6, "USDC", 10)
            .f(13, "ID", "Objetivo", "Bolsillo", "Meta", "Asignado (unidad del bolsillo)")
            .f(14, "MUELAS", "Muelas ejemplo", "ARS", 1000, 30)
            .f(15, "ANTEOJOS", "Anteojos ejemplo", "USDC", 100, 12)
            .f(16, "SIN_ASIGNAR", "Sin asignar ejemplo", "USDC", null, 99)
        val ahorros = modelo(hoy, semaforo(), "Fondos" to fondos).ahorros

        assertEquals(0.0, ahorros.asignacionUsdc.first { it.fondo == "MUELAS" }.monto, 0.001)
        assertEquals(12.0, ahorros.asignacionUsdc.first { it.fondo == "ANTEOJOS" }.monto, 0.001)
        assertEquals(-2.0, ahorros.asignacionUsdc.first { it.fondo == SIN_ASIGNAR }.monto, 0.001)
        assertEquals(10.0, ahorros.asignacionUsdc.sumOf { it.monto }, 0.001)
    }

    @Test
    fun elDesgloseDelCicloAbiertoSaleDeCategoriasSiHistorialNoLoTiene() {
        val historial = Hoja()
            .f(4, "Ciclo", "Tope", "Gasto neto", "Gasto bruto")
            .f(5, "2024-03", 80_000, 1000, 5000)
            .f(6, "2024-04", 100_000, 40_000, 90_000)
        val categorias = Hoja()
            .f(4, "Categoría", "Comprometido (Fijos)", "Variable (Movimientos)", "Total")
            .f(5, "Super", 1, 2, 2222)
            .f(6, "Ocio", 0, 0, 10)
            .f(7, "TOTAL", 0, 0, 999_999)
        val meses = modelo(
            hoy,
            semaforo(),
            "Historial" to historial,
            "Categorías resumen" to categorias,
        ).historial

        assertEquals(1000.0, meses[0].gasto, 0.001)
        assertTrue(meses[0].categorias.all { it.monto == 0.0 })
        assertEquals(40_000.0, meses[1].gasto, 0.001)
        assertEquals(2222.0, monto(meses[1], "Super"), 0.001)
        assertEquals(10.0, monto(meses[1], "Ocio"), 0.001)
        assertEquals(0.0, monto(meses[1], "Fijos"), 0.001)
    }

    @Test
    fun sinProximoMesElTotalDeLaTarjetaSumaConsumosYFijos() {
        val movimientos = Hoja()
            .f(4, "Fecha", "Comercio", "Monto", "Categoría", "Medio", "Notas", "Ciclo")
            .f(5, "02/04/2024", "Kiosco ejemplo", 100, "Super", "Visa", null, "2024-04")
        val fijos = Hoja()
            .f(4, "Concepto", "Tarjeta", "Tipo", "Activo", "Monto ARS", "ARS equivalente")
            .f(5, "Luz ejemplo", "Visa", "Servicio", "Sí", 50, 50)
            .f(6, "Baja ejemplo", "Visa", "Servicio", "No", 999, 999)
        val visa = modelo(
            hoy,
            semaforo(),
            "Movimientos" to movimientos,
            "Fijos y cuotas" to fijos,
        ).tarjetas[0]

        assertEquals(150.0, visa.total, 0.001)
        assertTrue(visa.cuotas.isEmpty())
    }

    @Test
    fun movimientosEHistorialSinEncabezadoUsanLasColumnasFijas() {
        val movimientos = Hoja()
            .f(4, "01/04/2024", "No va ejemplo", 999, "Super", "Visa", null, "2024-04")
            .f(5, "02/04/2024", "Kiosco ejemplo", 10, "Super", "Visa", null, "2024-04")
            .f(6, "03/04/2024", "Sin ciclo ejemplo", 5, "Super", "Efectivo", null, null)
        val historial = Hoja()
            .f(5, "2024-04", null, "05/04/2024", "Abierto", 100_000, null, null, 1, null, 40_000, null, null, null, null, 77)
        val modelo = modelo(hoy, semaforo(), "Movimientos" to movimientos, "Historial" to historial)

        assertEquals(listOf("Kiosco ejemplo"), modelo.ultimosMovimientos.map { it.comercio })
        assertEquals(40_000.0, modelo.historial.single().gasto, 0.001)
        assertEquals(77.0, monto(modelo.historial.single(), "Fijos"), 0.001)
        assertEquals(0.0, monto(modelo.historial.single(), "Super"), 0.001)
    }

    @Test
    fun planillaVaciaDeMasNoRompeYDejaLosAhorrosEnCero() {
        val modelo = modelo(hoy, semaforo())
        assertTrue(modelo.ultimosMovimientos.isEmpty())
        assertTrue(modelo.historial.isEmpty())
        assertEquals(listOf(0.0, 0.0), modelo.tarjetas.map { it.total })
        assertEquals(0.0, modelo.ahorros.colchonPesos, 0.001)
        assertEquals(0.0, modelo.ahorros.asignacionUsdc.sumOf { it.monto }, 0.001)
    }

    private fun resumen(quedan: Number, porDia: Any? = 1111) =
        modelo(hoy, semaforo(quedan = quedan, porDia = porDia)).resumen

    private fun ciclo(hoy: LocalDate, cierre: String) =
        modelo(hoy, semaforo(cierre = cierre, codigo = "2024-11", etiquetas = false)).ciclo

    private fun monto(mes: com.gacorvalan.ratti.datos.modelo.MesHistorial, categoria: String) =
        mes.categorias.first { it.categoria == categoria }.monto

    private fun modeloCompleto() = modelo(
        hoy,
        semaforo().p("B10", "Tipo de cambio").p("C10", 1000),
        "Próximo mes" to proximoMes(),
        "Fijos y cuotas" to fijos(),
        "Movimientos" to movimientos(),
        "Historial" to historial(),
        "Fondos" to fondos(),
    )

    private fun modelo(hoy: LocalDate, semaforo: Hoja, vararg extras: Pair<String, Hoja>) =
        interpretarPlanilla(planilla(("Semáforo" to semaforo) toList extras), hoy)

    private fun planilla(partes: List<Pair<String, Hoja>>): Map<String, List<List<ValorCrudo?>>> {
        val cargadas = partes.associate { (nombre, hoja) -> nombre to hoja.build() }
        return PESTANAS_FINNI.associateWith { cargadas[it].orEmpty() }
    }

    private infix fun Pair<String, Hoja>.toList(resto: Array<out Pair<String, Hoja>>) = listOf(this) + resto

    private fun semaforo(
        cierre: String = "05/04/2024",
        codigo: String = "2024-04",
        tope: Any = 100_000,
        gastado: Any = 40_000,
        quedan: Any = 60_000,
        porDia: Any? = 1111,
        etiquetas: Boolean = true,
    ): Hoja {
        val hoja = Hoja()
        if (etiquetas) {
            hoja.p("B7", "Próximo cierre").p("C7", cierre)
                .p("B11", "Ciclo").p("C11", codigo)
                .p("B13", "Presupuesto del ciclo").p("C13", tope)
                .p("E31", "Gasto neto de aportes").p("F31", gastado)
                .p("E32", "Disponible neto").p("F32", quedan)
                .p("E6", "ROJO")
                .p("C16", 999_999)
                .p("C17", -1)
            if (porDia != null) hoja.p("E33", "$ / día restante (neto)").p("F33", porDia)
        } else {
            hoja.p("C7", cierre).p("C11", codigo).p("C13", tope).p("F31", gastado).p("F32", quedan)
            if (porDia != null) hoja.p("F33", porDia)
        }
        return hoja
    }

    private fun proximoMes() = Hoja()
        .f(8, "Visa – consumos variables", 1500)
        .f(9, "Amex – consumos variables", 800)
        .f(10, "Visa – fijos, cuotas y suscripciones", 3000)
        .f(11, "Amex – fijos, cuotas y suscripciones", 0)

    private fun fijos() = Hoja()
        .f(4, "Concepto", "Categoría", "Monto ARS", "Monto USD", "Tarjeta", "Tipo", "Cuota actual/total", "Activo", "Notas", "ARS equivalente")
        .f(5, "Silla ejemplo", "Casa/hogar", 3000, null, "Visa", "Cuota", "2 de 6", "Sí", null, 3000)
        .f(6, "Luz ejemplo", "Casa/hogar", 500, null, "Visa", "Servicio", null, "Sí", null, 500)
        .f(7, "Auriculares ejemplo", "Ocio", 9000, null, "Amex", "Cuota", "1 de 3", "No", null, 0)
        .f(8, "Curso ejemplo", "Otros", null, 2, "Visa", "Cuota", "4 de 10", "Revisar", null, null)
        .f(9, "Mal formato ejemplo", "Ocio", 100, null, "Visa", "Cuota", "2/6", "Sí", null, 100)

    private fun movimientos() = Hoja()
        .f(4, "Fecha", "Comercio", "Monto", "Categoría", "Medio", "Notas", "Ciclo")
        .f(5, "08/04/2024", "Temprano ejemplo", 10, "Super", "Visa", null, "2024-04")
        .f(6, "08/04/2024", "Tarde ejemplo", 20, "Super", "Visa", null, "2024-04")
        .f(7, "07/04/2024", "Devolucion ejemplo", -100, "Super", "Visa", null, "2024-04")
        .f(8, "06/04/2024", "Colectivo ejemplo", 400, "Transporte", "Débito", null, "2024-04")
        .f(9, "05/04/2024", "Cafe ejemplo", 800, "Comida fuera", "Amex", null, "2024-04")
        .f(10, "01/04/2024", "Kiosco ejemplo", 1500, "Super", "Visa", null, "2024-04")
        .f(11, "02/03/2024", "Almacen ejemplo", 900, "Super", "Visa", null, "2024-03")
        .f(12, "03/04/2024", "Ayuda ejemplo", 200, "Movimiento de ahorro", "Transferencia", null, "2024-04")
        .f(13, "04/04/2024", "Sin ciclo ejemplo", 50, "Super", "Efectivo", null, null)
        .f(14, "04/04/2024", "Muestra ejemplo", 1, "Super", "Visa", "EJEMPLO", "2024-04")
        .f(15, "no-es-fecha", "Fecha rota ejemplo", 70, "Otros", "Visa", null, "2024-04")
        .f(16, "01/04/2024", "Sin monto ejemplo", null, "Super", "Visa", null, "2024-04")

    private fun historial() = Hoja()
        .f(
            4,
            "Ciclo", "Inicio", "Cierre", "Estado", "Tope", "Fijos comprometido", "Variable", "Gasto bruto",
            "Aportes", "Gasto neto", "Disponible bruto", "Disponible neto", "x", "y",
            "Fijos", "Suscripciones", "Super", "Comida fuera", "Ocio", "Transporte", "Mascotas",
            "Casa/hogar", "Salud", "Extraordinario", "Otros",
        )
        .f(
            5,
            "2024-03", "01/02/2024", "20/03/2024", "Cerrado", 80_000, null, null, 70_000, null, 50_000,
            null, null, null, null, null, null, 32_000, null, 18_000,
        )
        .f(
            6,
            "2024-04", "21/03/2024", "05/04/2024", "Abierto", 100_000, null, null, 90_000, null, 40_000,
            null, null, null, null, null, null, 20_000, 800,
        )

    private fun fondos() = Hoja()
        .f(5, "Bolsillo", "Dónde está", "Unidad", "Saldo", "Precio costo USD", "Valor a costo USD", "Precio mercado USD", "Fecha precio mercado", "Valor a mercado USD")
        .f(6, "ARS", "Banco ejemplo", "ARS", 50_000)
        .f(7, "USDC", "Billetera ejemplo", "USD", 100, 1, 999, 1, "01/04/2024", 888)
        .f(8, "VOOG", "Casa ejemplo", "cuotapartes", 2.5, 80, 200, 72, "01/04/2024", 180)
        .f(13, "ID", "Objetivo", "Bolsillo", "Prioridad", "Meta", "Moneda meta", "Asignado (unidad del bolsillo)")
        .f(14, "COLCHON", "Colchon ejemplo", "ARS", 1, 80_000, "ARS", 50_000)
        .f(15, "MUELAS", "Muelas ejemplo", "USDC", 2, 1000, "USD", 30)
        .f(16, "ANTEOJOS", "Anteojos ejemplo", "USDC", 2, 200, "USD", 10)
        .f(17, "FOCUS", "Focus ejemplo", "USDC", 2, 100, "USD", 0)
        .f(18, "ELECTRO", "Electro ejemplo", "USDC", 3, 50, "USD", 20)
        .f(19, "CASA", "Casa ejemplo", "VOOG", 4, null, "USD", 2.5)
        .f(20, "GUSTOS", "Gustos ejemplo", "USDC", 5, null, "ARS", 5)
        .f(21, "SIN_ASIGNAR", "Sin asignar ejemplo", "USDC", null, "USD", 99)
}

private class Hoja {
    private val filas = mutableListOf<MutableList<ValorCrudo?>>()

    fun p(ref: String, valor: Any?): Hoja {
        val columna = ref[0].uppercaseChar() - 'A'
        val fila = ref.drop(1).toInt()
        while (filas.size < fila) filas.add(mutableListOf())
        val row = filas[fila - 1]
        while (row.size <= columna) row.add(null)
        row[columna] = crudo(valor)
        return this
    }

    fun f(numero: Int, vararg valores: Any?): Hoja {
        while (filas.size < numero) filas.add(mutableListOf())
        filas[numero - 1] = valores.map { crudo(it) }.toMutableList()
        return this
    }

    fun build(): List<List<ValorCrudo?>> = filas.map { it.toList() }
}

private fun crudo(valor: Any?): ValorCrudo? = when (valor) {
    null -> null
    is ValorCrudo -> valor
    is String -> ValorCrudo.Texto(valor)
    is Number -> ValorCrudo.Numero(valor.toDouble())
    else -> error("dato de ejemplo")
}
