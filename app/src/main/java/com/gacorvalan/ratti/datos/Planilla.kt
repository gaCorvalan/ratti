package com.gacorvalan.ratti.datos

/** Planilla de Finni. La app solo la lee. */
internal const val ID_PLANILLA_FINNI = "1Ms40BavASb83s370IDAviwG03z8FX3_RZvm8jRs1mNU"

internal val PESTANAS_FINNI: List<String> = listOf(
    "Semáforo",
    "Próximo mes",
    "Fijos y cuotas",
    "Movimientos",
    "Categorías resumen",
    "Aportes",
    "Historial",
    "Fondos",
    "Movimientos de ahorro",
)

/** Celda tal como llega de la planilla, sin interpretar. El vacío es null. */
internal sealed interface ValorCrudo {
    data class Texto(val valor: String) : ValorCrudo
    data class Numero(val valor: Double) : ValorCrudo
}

internal sealed class ErrorPlanilla : Exception() {
    data object SinConexion : ErrorPlanilla()
    data object Prohibido : ErrorPlanilla()
    data object PermisoVencido : ErrorPlanilla()
    data object Ilegible : ErrorPlanilla()
    data object NoEncontrada : ErrorPlanilla()
    data object Lectura : ErrorPlanilla()
    data class PestanaFaltante(val nombre: String?) : ErrorPlanilla()
    data class Http(val codigo: Int) : ErrorPlanilla()
}
