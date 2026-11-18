package com.gacorvalan.ratti.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTheme
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun InicioSesion(
    email: String?,
    error: String?,
    ocupado: Boolean,
    onEntrar: () -> Unit,
    onSalir: () -> Unit,
    onDatosCrudos: (() -> Unit)? = null,
    onActualizar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (email != null) {
            Text(
                text = stringResource(R.string.entraste_con),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(RattiSpace.sectionInner))
            Text(
                text = email,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(RattiSpace.sectionGap))
            BotonSesion(
                texto = stringResource(R.string.salir),
                onClick = onSalir,
                enabled = !ocupado,
                container = RattiColor.White,
                content = RattiColor.Ink,
            )
            if (onActualizar != null) {
                Spacer(Modifier.height(RattiSpace.sectionInner))
                EnlaceSesion(texto = stringResource(R.string.actualizar), onClick = onActualizar)
            }
            if (onDatosCrudos != null) {
                Spacer(Modifier.height(RattiSpace.sectionInner))
                EnlaceSesion(texto = stringResource(R.string.datos_crudos), onClick = onDatosCrudos)
            }
        } else {
            BotonSesion(
                texto = stringResource(
                    if (ocupado) R.string.entrando else R.string.entrar_google,
                ),
                onClick = onEntrar,
                enabled = !ocupado,
                container = RattiColor.Cobalt,
                content = RattiColor.White,
            )
        }
        if (error != null) {
            Spacer(Modifier.height(RattiSpace.sectionGap))
            Text(
                text = error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun EnlaceSesion(
    texto: String,
    onClick: () -> Unit,
) {
    Text(
        text = texto,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick,
        ),
        style = RattiType.link,
        color = RattiColor.Cobalt,
    )
}

@Composable
private fun BotonSesion(
    texto: String,
    onClick: () -> Unit,
    enabled: Boolean,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container.copy(alpha = 0.6f),
            disabledContentColor = content.copy(alpha = 0.7f),
        ),
        contentPadding = PaddingValues(
            horizontal = RattiSpace.cardPadding,
            vertical = RattiSpace.rowPadding,
        ),
    ) {
        Text(text = texto)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F4EB, widthDp = 360)
@Composable
private fun InicioSesionPreview() {
    RattiTheme {
        InicioSesion(
            email = null,
            error = null,
            ocupado = false,
            onEntrar = {},
            onSalir = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F4EB, widthDp = 360)
@Composable
private fun InicioConCuentaPreview() {
    RattiTheme {
        InicioSesion(
            email = "tu-cuenta@gmail.com",
            error = null,
            ocupado = false,
            onEntrar = {},
            onSalir = {},
        )
    }
}
