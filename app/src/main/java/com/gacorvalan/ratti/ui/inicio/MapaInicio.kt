package com.gacorvalan.ratti.ui.inicio

import com.gacorvalan.ratti.datos.modelo.ModeloFinanzas
import com.gacorvalan.ratti.datos.modelo.Movimiento
import com.gacorvalan.ratti.datos.modelo.Tarjeta
import java.util.Locale

/**
 * Pasa el modelo de la planilla al estado de Inicio.
 * El saludo y la inicial siguen el frame: la planilla no trae el nombre.
 * Verde y amarillo se pintan en lima, como el semáforo del frame en rango.
 * Rojo (te quedan negativo) usa Danger, como septiembre pasado de tope.
 */
internal fun inicioDe(modelo: ModeloFinanzas): EstadoInicio {
    val cierre = fechaCorta(modelo.ciclo.cierre.dayOfMonth, modelo.ciclo.cierre.monthValue)
    val resumen = modelo.resumen
    return EstadoInicio(
        saludo = "HOLA, GABRIEL.",
        inicialAvatar = "R",
        mes = modelo.ciclo.nombreMes,
        cierre = cierre,
        usado = pesosEnteros(resumen.gastado),
        tope = pesosEnteros(resumen.tope),
        porDia = pesosEnteros(resumen.porDia),
        teQuedan = pesosEnteros(resumen.teQuedan),
        pasadoDeTope = resumen.pasadoDeTope,
        tarjetas = modelo.tarjetas.mapIndexed { indice, tarjeta ->
            tarjeta.aInicio(indice, cierre)
        },
        consumos = modelo.ultimosMovimientos.take(ULTIMOS).mapIndexed { indice, movimiento ->
            movimiento.aInicio(indice)
        },
    )
}

private fun Tarjeta.aInicio(indice: Int, cierre: String) = TarjetaInicio(
    id = "$indice-${marca.ifBlank { "tarjeta" }}",
    marca = marca.ifBlank { "Tarjeta" },
    banco = "",
    monto = pesosEnteros(total),
    venceEl = cierre,
    color = when (marca.lowercase(Locale.ROOT)) {
        "visa" -> ColorTarjeta.Rosa
        "amex" -> ColorTarjeta.Naranja
        else -> if (indice % 2 == 0) ColorTarjeta.Rosa else ColorTarjeta.Naranja
    },
)

private fun Movimiento.aInicio(indice: Int) = ConsumoInicio(
    id = "$indice-${comercio.trim()}-$monto",
    comercio = comercio.trim().ifBlank { "Sin comercio" },
    categoria = categoria.trim().uppercase(Locale.forLanguageTag("es-AR")).ifBlank { "SIN CATEGORÍA" },
    monto = pesosEnteros(monto),
    inicial = inicialDe(comercio),
    color = COLORES_CONSUMO[indice % COLORES_CONSUMO.size],
)

private fun inicialDe(comercio: String): String {
    val letra = comercio.trim().firstOrNull { it.isLetterOrDigit() } ?: return "·"
    return letra.uppercase(Locale.forLanguageTag("es-AR"))
}

private fun fechaCorta(dia: Int, mes: Int): String {
    val d = dia.toString().padStart(2, '0')
    val m = mes.toString().padStart(2, '0')
    return "$d/$m"
}

private const val ULTIMOS = 3

private val COLORES_CONSUMO = listOf(
    ColorConsumo.Teal,
    ColorConsumo.Rosa,
    ColorConsumo.Naranja,
    ColorConsumo.Lima,
    ColorConsumo.Cobalto,
)
