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

/**
 * Colores de Foundations (`37:801`). Los hex están hardcodeados en las pantallas:
 * no hay paint styles ni variables ligadas.
 */
object RattiColor {
    val Cream = Color(0xFFF8F4EB)
    val Ink = Color(0xFF101013)
    val Cobalt = Color(0xFF2C3CFA)
    val Pink = Color(0xFFFF6FBA)
    val Lime = Color(0xFFDFFF42)
    val Tangerine = Color(0xFFFF9C50)
    val Teal = Color(0xFF72E6EC)
    val White = Color(0xFFFFFFFF)
    val Danger = Color(0xFFE0243A)

    /** Pastilla, subrayado y barra de la tarjeta de saldo (`94:551`–`94:553`). */
    val SaldoVerde = Color(0xFF12B886)
    val SaldoVerdeSuave = Color(0xFFE6F9F3)
    val SaldoAmbar = Color(0xFFC8870A)
    val SaldoAmbarSuave = Color(0xFFFFF8E6)
    val SaldoRojo = Color(0xFFD92D20)
    val SaldoRojoSuave = Color(0xFFFDECEC)

    /** Track del semáforo de saldo. Negro al 28%. */
    val Black28 = Color(0x47000000)

    /** Monto y porcentaje cuando el mes se pasa del tope. */
    val DangerDark = Color(0xFFB5152C)

    /** Fondo de la fila de un mes excedido. */
    val DangerSubtle = Color(0xFFFDE8EA)

    /** Captions secundarios. `#101013` al 72%. */
    val Ink72 = Color(0xB8101013)

    /** Banco en la tile de tarjeta. */
    val Ink70 = Color(0xB3101013)

    /** Sombra de la tab bar. `#101013` al 14% (`#10101324`). */
    val Ink14 = Color(0x24101013)

    /** Track de semáforos sobre fondo claro. `#101013` al 12%. */
    val Ink12 = Color(0x1F101013)

    /** Home indicator. `#101013` al 30%. */
    val Ink30 = Color(0x4D101013)

    /** Texto “por día” sobre la tarjeta de saldo. */
    val White85 = Color(0xD9FFFFFF)

    /** Track del semáforo sobre cobalto. */
    val White28 = Color(0x47FFFFFF)

    /** Orbe decorativo sobre Visa y CardTile. */
    val White30 = Color(0x4DFFFFFF)
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

    /** Aire inferior para que la tab bar flotante no tape el final de la lista. */
    val contentBottom = 88.dp
}

/** Radios medidos en las pantallas. El de la tab bar es una píldora (100). */
object RattiRadius {
    val savingsCard = 24.dp
    val hero = 22.dp
    val avatar = 20.dp
    val tile = 18.dp
    val row = 15.dp
    val segment = 14.dp
    val pill = 13.dp
    val bar = 8.dp
    val track = 4.dp
    val categoryTrack = 3.dp
    val full = 100.dp
}

/** Geometría de `Moni / TabBar` (`36:298`): 183×67, trazo 1,5, sombra blur 18. */
object RattiTabMetrics {
    val width = 183.dp
    val height = 67.dp
    val paddingHorizontal = 12.dp
    val paddingVertical = 10.dp
    val gap = 12.dp
    val item = 44.dp
    val icon = 22.dp
    val stroke = 1.5.dp
    val shadowBlur = 18.dp
    val shadowOffsetY = 6.dp

    /** Distancia entre la píldora y el home indicator en el frame de 844. */
    val aboveNavigation = 20.dp
}

/**
 * Archivo Black es un corte único: el archivo declara peso 400 y los contornos
 * ya son Black. Se registra en [FontWeight.Normal] para no sintetizar negrita.
 */
private val ArchivoBlack = FontFamily(
    Font(R.font.archivo_black, FontWeight.Normal),
)

private val DmSans = FontFamily(
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
    Font(R.font.dm_sans_extrabold, FontWeight.ExtraBold),
)

/** Subconjunto OFL con el microtexto decorativo de Inicio. No es copy de producto. */
private val NotoSansJp = FontFamily(
    Font(R.font.noto_sans_jp_bold, FontWeight.Bold),
)

private fun archivo(size: Float, line: Float) = TextStyle(
    fontFamily = ArchivoBlack,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.sp,
)

private fun dm(weight: FontWeight, size: Float, line: Float) = TextStyle(
    fontFamily = DmSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = 0.sp,
)

/** Recetas aplicadas en las pantallas. El tracking es 0 en todas. */
object RattiType {
    val amountHero = archivo(34f, 36f)
    val screenTitle = archivo(28f, 32f)
    val brand = archivo(28f, 30f)
    val heroAmount = archivo(28f, 30f)
    val cardAmount = archivo(26f, 30f)
    val greeting = archivo(26f, 28f)
    val bankName = archivo(22f, 28f)
    val dueDate = archivo(22f, 26f)
    val sectionTitle = archivo(22f, 24f)
    val navTitle = archivo(20f, 24f)
    val chartTitle = archivo(18f, 22f)
    val tileBrand = archivo(16f, 18f)

    val badgeInitial = dm(FontWeight.Bold, 21f, 21.8f)
    val txAmount = dm(FontWeight.Bold, 17f, 24f)
    val rowTitle = dm(FontWeight.Bold, 17f, 22f)
    val merchant = dm(FontWeight.Bold, 17f, 17.7f)
    val monthAmount = dm(FontWeight.Bold, 16f, 20f)
    val categoryAmount = dm(FontWeight.Bold, 14f, 18f)
    val segment = dm(FontWeight.Bold, 13f, 16f)
    val captionBold = dm(FontWeight.Bold, 11f, 14f)
    val pill = dm(FontWeight.Bold, 11f, 13f)
    val editorialPill = dm(FontWeight.Bold, 10f, 12f)

    val pocketCaption = dm(FontWeight.Medium, 13f, 18f)
    val statusTime = dm(FontWeight.Medium, 13f, 16f)
    val heroSubtitle = dm(FontWeight.Medium, 12f, 16f)

    /** Estado del semáforo de saldo. DM Sans Bold 12/16. */
    val saldoEstado = dm(FontWeight.Bold, 12f, 16f)
    val lotNote = dm(FontWeight.Medium, 11f, 16f)
    val meta = dm(FontWeight.Medium, 11f, 14f)
    val heroLabel = dm(FontWeight.Medium, 11f, 13f)
    val txMeta = dm(FontWeight.Medium, 11f, 11.4f)

    val overline = dm(FontWeight.ExtraBold, 11f, 14f)
    val link = dm(FontWeight.ExtraBold, 11f, 12f)

    /** Noto Sans JP Bold 8/10. Microtexto decorativo de Inicio. */
    val microJp = TextStyle(
        fontFamily = NotoSansJp,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.sp,
    )
}

private val RattiColorScheme = lightColorScheme(
    primary = RattiColor.Cobalt,
    onPrimary = RattiColor.White,
    secondary = RattiColor.Pink,
    onSecondary = RattiColor.Ink,
    tertiary = RattiColor.Lime,
    onTertiary = RattiColor.Ink,
    background = RattiColor.Cream,
    onBackground = RattiColor.Ink,
    surface = RattiColor.White,
    onSurface = RattiColor.Ink,
    error = RattiColor.Danger,
    onError = RattiColor.White,
    surfaceContainerLowest = RattiColor.White,
    surfaceContainerLow = RattiColor.Cream,
    surfaceContainer = RattiColor.Cream,
    surfaceContainerHigh = RattiColor.Cream,
    surfaceContainerHighest = RattiColor.White,
)

private val RattiShapes = Shapes(
    extraSmall = RoundedCornerShape(RattiRadius.pill),
    small = RoundedCornerShape(RattiRadius.row),
    medium = RoundedCornerShape(RattiRadius.tile),
    large = RoundedCornerShape(RattiRadius.hero),
    extraLarge = RoundedCornerShape(RattiRadius.savingsCard),
)

private fun rattiTypography(): Typography = Typography(
    displayLarge = RattiType.amountHero,
    displayMedium = RattiType.screenTitle,
    displaySmall = RattiType.brand,
    headlineLarge = RattiType.screenTitle,
    headlineMedium = RattiType.sectionTitle,
    headlineSmall = RattiType.chartTitle,
    titleLarge = RattiType.navTitle,
    titleMedium = RattiType.rowTitle,
    titleSmall = RattiType.monthAmount,
    bodyLarge = RattiType.pocketCaption,
    bodyMedium = RattiType.heroSubtitle,
    bodySmall = RattiType.meta,
    labelLarge = RattiType.segment,
    labelMedium = RattiType.overline,
    labelSmall = RattiType.editorialPill,
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
