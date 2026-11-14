package com.gacorvalan.ratti.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gacorvalan.ratti.R

/** Colores MONI. Cobalto es el bloque de saldo, rosa las tarjetas, lima la cinta y los acentos. */
object RattiColor {
    val Crema = Color(0xFFFFF6E8)
    val Cobalto = Color(0xFF2546E8)
    val Rosa = Color(0xFFFF5E93)
    val Lima = Color(0xFFD2F34C)
    val Tinta = Color(0xFF1B1916)
    val Blanco = Color(0xFFFFFFFF)
}

object RattiSpace {
    /** Margen lateral de la pantalla. */
    val screenMargin = 24.dp

    /** Entre una sección y la siguiente. */
    val sectionGap = 16.dp

    /** Dentro de una sección: el título queda a esta distancia de su contenido. */
    val sectionInner = 8.dp

    /** Relleno de una tarjeta grande. */
    val cardPadding = 16.dp

    /** Relleno de una fila chica. */
    val rowPadding = 12.dp
}

private val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

private val RattiColorScheme = lightColorScheme(
    primary = RattiColor.Cobalto,
    onPrimary = RattiColor.Blanco,
    secondary = RattiColor.Rosa,
    onSecondary = RattiColor.Tinta,
    tertiary = RattiColor.Lima,
    onTertiary = RattiColor.Tinta,
    background = RattiColor.Crema,
    onBackground = RattiColor.Tinta,
    surface = RattiColor.Blanco,
    onSurface = RattiColor.Tinta,
    surfaceContainerLowest = RattiColor.Blanco,
    surfaceContainerLow = RattiColor.Crema,
    surfaceContainer = RattiColor.Crema,
    surfaceContainerHigh = RattiColor.Crema,
    surfaceContainerHighest = RattiColor.Blanco,
)

private val RattiShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private fun rattiTypography(): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.nunito(FontWeight.Bold),
        displayMedium = base.displayMedium.nunito(FontWeight.Bold),
        displaySmall = base.displaySmall.nunito(FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.nunito(FontWeight.Bold).copy(
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.4).sp,
        ),
        headlineMedium = base.headlineMedium.nunito(FontWeight.Bold),
        headlineSmall = base.headlineSmall.nunito(FontWeight.SemiBold),
        titleLarge = base.titleLarge.nunito(FontWeight.SemiBold),
        titleMedium = base.titleMedium.nunito(FontWeight.SemiBold),
        titleSmall = base.titleSmall.nunito(FontWeight.Medium),
        bodyLarge = base.bodyLarge.nunito(FontWeight.Normal),
        bodyMedium = base.bodyMedium.nunito(FontWeight.Normal),
        bodySmall = base.bodySmall.nunito(FontWeight.Normal),
        labelLarge = base.labelLarge.nunito(FontWeight.SemiBold),
        labelMedium = base.labelMedium.nunito(FontWeight.Medium),
        labelSmall = base.labelSmall.nunito(FontWeight.Medium),
    )
}

private fun TextStyle.nunito(weight: FontWeight) = copy(
    fontFamily = Nunito,
    fontWeight = weight,
)

@Composable
fun RattiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RattiColorScheme,
        typography = rattiTypography(),
        shapes = RattiShapes,
        content = content,
    )
}
