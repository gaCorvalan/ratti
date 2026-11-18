package com.gacorvalan.ratti.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTheme

@Composable
internal fun InicioSesion(
    error: String?,
    ocupado: Boolean,
    onEntrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onEntrar,
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = RattiColor.Cobalt,
                contentColor = RattiColor.White,
                disabledContainerColor = RattiColor.Cobalt.copy(alpha = 0.6f),
                disabledContentColor = RattiColor.White.copy(alpha = 0.7f),
            ),
            contentPadding = PaddingValues(
                horizontal = RattiSpace.cardPadding,
                vertical = RattiSpace.rowPadding,
            ),
        ) {
            Text(
                text = stringResource(
                    if (ocupado) R.string.entrando else R.string.entrar_google,
                ),
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

@Preview(showBackground = true, backgroundColor = 0xFFF8F4EB, widthDp = 360)
@Composable
private fun InicioSesionPreview() {
    RattiTheme {
        InicioSesion(
            error = null,
            ocupado = false,
            onEntrar = {},
        )
    }
}
