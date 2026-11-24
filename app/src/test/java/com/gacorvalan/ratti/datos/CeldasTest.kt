package com.gacorvalan.ratti.datos

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Números y fechas inventados. No salen de la planilla. */
class CeldasTest {
    @Test
    fun leeNumerosArgentinosYCrudos() {
        assertEquals(1234.5, parsearNumeroArgentino("1.234,50")!!, 0.001)
        assertEquals(2_500_000.0, parsearNumeroArgentino("$ 2.500.000")!!, 0.001)
        assertEquals(1_000.5, parsearNumeroArgentino("$\u00A01.000,50")!!, 0.001)
        assertEquals(-24.15, parsearNumeroArgentino("−24,15")!!, 0.001)
        assertEquals(-24.15, parsearNumeroArgentino("-24,15")!!, 0.001)
        assertEquals(-1234.5, parsearNumeroArgentino("(1.234,50)")!!, 0.001)
        assertEquals(10.5, parsearNumeroArgentino("10,5")!!, 0.001)
        assertEquals(2.5, parsearNumeroArgentino("2.5")!!, 0.001)
        assertEquals(1.5, parsearNumeroArgentino("1.50")!!, 0.001)
        assertEquals(1_234_567.0, parsearNumeroArgentino("1.234.567")!!, 0.001)
        assertEquals(6000.0, parsearNumeroArgentino("6000")!!, 0.001)
        assertEquals(4.9, parsearNumeroArgentino("U\$S 4,90")!!, 0.001)
        assertEquals(0.0, parsearNumeroArgentino("$0")!!, 0.001)
        assertNull(parsearNumeroArgentino(""))
        assertNull(parsearNumeroArgentino("   "))
        assertNull(parsearNumeroArgentino("—"))
        assertNull(parsearNumeroArgentino("abc"))
        assertEquals(12.5, numeroDe(ValorCrudo.Numero(12.5))!!, 0.001)
        assertNull(numeroDe(null))
        assertNull(numeroDe(ValorCrudo.Texto("")))
    }

    @Test
    fun leeFechasYSerieDeSheets() {
        assertEquals(LocalDate.of(2024, 4, 5), parsearFecha("05/04/2024"))
        assertEquals(LocalDate.of(2024, 4, 5), parsearFecha("5/4/2024"))
        assertEquals(LocalDate.of(2024, 4, 5), parsearFecha("05/04/24"))
        assertNull(parsearFecha("32/01/2024"))
        assertNull(parsearFecha(""))
        val fecha = LocalDate.of(2024, 4, 5)
        val serial = ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), fecha).toDouble()
        assertEquals(fecha, fechaDe(ValorCrudo.Numero(serial)))
        assertNull(fechaDe(ValorCrudo.Numero(12.5)))
    }
}
