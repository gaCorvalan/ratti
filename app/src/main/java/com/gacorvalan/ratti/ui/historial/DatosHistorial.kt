package com.gacorvalan.ratti.ui.historial

/**
 * Números del frame de Figma (Historial `37:508` y Noviembre `37:594`).
 * Solo para las previews de Compose. La pantalla usa el historial de la planilla.
 */
internal object DatosHistorial {
    val ciclos: List<CicloHistorial> = listOf(
        CicloHistorial(
            id = "noviembre",
            mes = "Noviembre",
            cierre = "26/11",
            gasto = 1_020_749L,
            tope = 1_300_000L,
            actual = true,
            categorias = listOf(
                CategoriaHistorial("Fijos", 312_400L),
                CategoriaHistorial("Super", 198_450L),
                CategoriaHistorial("Comida fuera", 87_320L),
                CategoriaHistorial("Extraordinario", 85_400L),
                CategoriaHistorial("Transporte", 72_640L),
                CategoriaHistorial("Suscripciones", 68_235L),
                CategoriaHistorial("Ocio", 54_180L),
                CategoriaHistorial("Mascotas", 41_200L),
                CategoriaHistorial("Casa/hogar", 38_900L),
                CategoriaHistorial("Otros", 33_284L),
                CategoriaHistorial("Salud", 28_740L),
            ),
        ),
        CicloHistorial(
            id = "octubre",
            mes = "Octubre",
            cierre = "26/10",
            gasto = 1_187_000L,
            tope = 1_300_000L,
            actual = false,
            categorias = listOf(
                CategoriaHistorial("Super", 320_000L),
                CategoriaHistorial("Fijos", 310_000L),
                CategoriaHistorial("Transporte", 180_000L),
                CategoriaHistorial("Ocio", 150_000L),
                CategoriaHistorial("Comida fuera", 127_000L),
                CategoriaHistorial("Otros", 100_000L),
            ),
        ),
        CicloHistorial(
            id = "septiembre",
            mes = "Septiembre",
            cierre = "26/09",
            gasto = 1_342_000L,
            tope = 1_300_000L,
            actual = false,
            categorias = listOf(
                CategoriaHistorial("Fijos", 400_000L),
                CategoriaHistorial("Super", 280_000L),
                CategoriaHistorial("Extraordinario", 220_000L),
                CategoriaHistorial("Transporte", 150_000L),
                CategoriaHistorial("Comida fuera", 142_000L),
                CategoriaHistorial("Otros", 150_000L),
            ),
        ),
        CicloHistorial(
            id = "agosto",
            mes = "Agosto",
            cierre = "26/08",
            gasto = 1_098_000L,
            tope = 1_300_000L,
            actual = false,
            categorias = listOf(
                CategoriaHistorial("Fijos", 300_000L),
                CategoriaHistorial("Super", 250_000L),
                CategoriaHistorial("Suscripciones", 148_000L),
                CategoriaHistorial("Transporte", 140_000L),
                CategoriaHistorial("Salud", 120_000L),
                CategoriaHistorial("Otros", 140_000L),
            ),
        ),
        CicloHistorial(
            id = "julio",
            mes = "Julio",
            cierre = "26/07",
            gasto = 1_280_000L,
            tope = 1_300_000L,
            actual = false,
            categorias = listOf(
                CategoriaHistorial("Fijos", 340_000L),
                CategoriaHistorial("Super", 290_000L),
                CategoriaHistorial("Extraordinario", 210_000L),
                CategoriaHistorial("Ocio", 160_000L),
                CategoriaHistorial("Transporte", 150_000L),
                CategoriaHistorial("Comida fuera", 130_000L),
            ),
        ),
        CicloHistorial(
            id = "junio",
            mes = "Junio",
            cierre = "26/06",
            gasto = 1_145_000L,
            tope = 1_300_000L,
            actual = false,
            categorias = listOf(
                CategoriaHistorial("Fijos", 305_000L),
                CategoriaHistorial("Super", 260_000L),
                CategoriaHistorial("Transporte", 155_000L),
                CategoriaHistorial("Mascotas", 140_000L),
                CategoriaHistorial("Casa/hogar", 125_000L),
                CategoriaHistorial("Otros", 160_000L),
            ),
        ),
    )
}
