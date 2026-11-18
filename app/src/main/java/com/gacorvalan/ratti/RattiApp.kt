package com.gacorvalan.ratti

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTabMetrics
import com.gacorvalan.ratti.ui.theme.RattiTheme

private enum class RattiTab(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
) {
    Inicio(R.string.tab_inicio, R.drawable.ic_tab_inicio),
    Ahorros(R.string.tab_ahorros, R.drawable.ic_tab_ahorros),
    Historial(R.string.tab_historial, R.drawable.ic_tab_historial),
}

private val TabShape = RoundedCornerShape(RattiRadius.full)

@Composable
fun RattiApp() {
    var selectedName by rememberSaveable { mutableStateOf(RattiTab.Inicio.name) }
    val selected = RattiTab.valueOf(selectedName)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        PantallaVacia(title = stringResource(selected.titleRes))
        RattiTabBar(
            selected = selected,
            onSelect = { selectedName = it.name },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PantallaVacia(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                start = RattiSpace.screenMargin,
                end = RattiSpace.screenMargin,
                top = RattiSpace.sectionGap,
                bottom = RattiSpace.contentBottom,
            ),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun RattiTabBar(
    selected: RattiTab,
    onSelect: (RattiTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = RattiTabMetrics.aboveNavigation)
            .width(RattiTabMetrics.width)
            .height(RattiTabMetrics.height)
            .dropShadow(
                shape = TabShape,
                shadow = Shadow(
                    radius = RattiTabMetrics.shadowBlur,
                    spread = 0.dp,
                    offset = DpOffset(0.dp, RattiTabMetrics.shadowOffsetY),
                    color = RattiColor.Ink14,
                ),
            )
            .border(RattiTabMetrics.stroke, RattiColor.Ink, TabShape)
            .background(RattiColor.White, TabShape)
            .padding(
                horizontal = RattiTabMetrics.paddingHorizontal,
                vertical = RattiTabMetrics.paddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(RattiTabMetrics.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RattiTab.entries.forEach { tab ->
            RattiTabButton(
                tab = tab,
                selected = tab == selected,
                onClick = { onSelect(tab) },
            )
        }
    }
}

@Composable
private fun RattiTabButton(
    tab: RattiTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(RattiTabMetrics.item)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .background(
                color = if (selected) RattiColor.Lime else Color.Transparent,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = stringResource(tab.titleRes),
            tint = RattiColor.Ink,
            modifier = Modifier.size(RattiTabMetrics.icon),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RattiAppPreview() {
    RattiTheme {
        RattiApp()
    }
}
