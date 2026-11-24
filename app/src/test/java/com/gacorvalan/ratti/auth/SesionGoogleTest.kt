package com.gacorvalan.ratti.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionGoogleTest {
    @Test
    fun requiereTokenParaLeerPlanillas() {
        assertFalse(puedeLeerPlanillas(null, listOf(PLANILLAS_SOLO_LECTURA)))
        assertFalse(puedeLeerPlanillas(" ", listOf(PLANILLAS_SOLO_LECTURA)))
    }

    @Test
    fun aceptaElAlcanceDeSoloLectura() {
        assertTrue(puedeLeerPlanillas("token", listOf(PLANILLAS_SOLO_LECTURA)))
    }

    @Test
    fun noAceptaUnAlcanceDistinto() {
        assertFalse(
            puedeLeerPlanillas(
                "token",
                listOf("https://www.googleapis.com/auth/drive.readonly"),
            ),
        )
        assertFalse(
            puedeLeerPlanillas(
                "token",
                listOf("https://www.googleapis.com/auth/spreadsheets"),
            ),
        )
    }

    @Test
    fun aceptaLaRespuestaSinListaDeAlcancesDeGoogle() {
        assertTrue(puedeLeerPlanillas("token", emptyList()))
    }
}
