package com.gacorvalan.ratti.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gacorvalan.ratti.ui.InicioSesion
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTheme

@Composable
fun InicioScreen(
    estado: EstadoInicio,
    modifier: Modifier = Modifier,
    onTarjeta: (TarjetaInicio) -> Unit = {},
    onVerTodos: () -> Unit = {},
    email: String? = null,
    errorSesion: String? = null,
    ocupado: Boolean = false,
    onEntrar: (() -> Unit)? = null,
    onSalir: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = RattiSpace.contentBottom),
        verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionGap),
    ) {
        CabeceraInicio(inicial = estado.inicialAvatar)
        EditorialInicio(estado = estado)
        TarjetaSaldoInicio(estado = estado)
        // TODO(paso 5): el toque abre el detalle de la tarjeta. Hoy no navega.
        CarruselTarjetas(tarjetas = estado.tarjetas, onTarjeta = onTarjeta)
        ConsumosInicio(consumos = estado.consumos, onVerTodos = onVerTodos)
        if (onEntrar != null) {
            InicioSesion(
                email = email,
                error = errorSesion,
                ocupado = ocupado,
                onEntrar = onEntrar,
                onSalir = onSalir,
                modifier = Modifier.padding(horizontal = RattiSpace.screenMargin),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun InicioScreenPreview() {
    RattiTheme {
        InicioScreen(estado = DatosEjemplo.inicio)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 420, backgroundColor = 0xFFF8F4EB)
@Composable
private fun SaldoExcedidoPreview() {
    RattiTheme {
        TarjetaSaldoInicio(
            estado = DatosEjemplo.inicio.copy(usado = 1_342_000L),
        )
    }
}
