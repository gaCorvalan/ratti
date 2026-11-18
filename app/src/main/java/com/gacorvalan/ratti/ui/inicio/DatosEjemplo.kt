package com.gacorvalan.ratti.ui.inicio

/**
 * Valores de ejemplo, solo para previews de Compose.
 * Son los del frame de Figma (Inicio, `26:315`). La pantalla real no los usa.
 */
object DatosEjemplo {
    val inicio = EstadoInicio(
        saludo = "HOLA, GABRIEL.",
        inicialAvatar = "R",
        mes = "Noviembre",
        cierre = "26/11",
        usado = 1_020_749L,
        tope = 1_300_000L,
        porDia = 17_450L,
        tarjetas = listOf(
            TarjetaInicio(
                id = "visa",
                marca = "Visa",
                banco = "Santander",
                monto = 487_320L,
                venceEl = "12/11",
                color = ColorTarjeta.Rosa,
            ),
            TarjetaInicio(
                id = "amex",
                marca = "Amex",
                banco = "",
                monto = 214_650L,
                venceEl = "12/11",
                color = ColorTarjeta.Naranja,
            ),
        ),
        consumos = listOf(
            ConsumoInicio(
                id = "jumbo",
                comercio = "Jumbo",
                categoria = "SUPER",
                monto = 48_320L,
                inicial = "J",
                color = ColorConsumo.Teal,
            ),
            ConsumoInicio(
                id = "rappi",
                comercio = "Rappi",
                categoria = "COMIDA FUERA",
                monto = 12_450L,
                inicial = "R",
                color = ColorConsumo.Rosa,
            ),
            ConsumoInicio(
                id = "ypf",
                comercio = "YPF",
                categoria = "TRANSPORTE",
                monto = 32_100L,
                inicial = "Y",
                color = ColorConsumo.Naranja,
            ),
        ),
        ejemplo = true,
    )
}
