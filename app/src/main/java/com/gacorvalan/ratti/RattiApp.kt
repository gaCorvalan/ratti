package com.gacorvalan.ratti

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTheme

private enum class RattiTab(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    Inicio(R.string.tab_inicio, Icons.Rounded.Home),
    Ahorros(R.string.tab_ahorros, Icons.Rounded.Savings),
    Historial(R.string.tab_historial, Icons.Rounded.History),
}

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
            .statusBarsPadding()
            .padding(
                horizontal = RattiSpace.screenMargin,
                vertical = RattiSpace.sectionGap,
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
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = RattiSpace.screenMargin,
                end = RattiSpace.screenMargin,
                bottom = RattiSpace.sectionGap,
            )
            .shadow(elevation = 16.dp, shape = CircleShape)
            .background(RattiColor.Blanco, CircleShape)
            .padding(RattiSpace.sectionInner),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RattiTab.entries.forEach { tab ->
            RattiTabButton(
                tab = tab,
                selected = tab == selected,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RattiTabButton(
    tab: RattiTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(vertical = RattiSpace.sectionInner),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) RattiColor.Lima else RattiColor.Blanco,
                    shape = CircleShape,
                )
                .padding(
                    horizontal = RattiSpace.rowPadding,
                    vertical = RattiSpace.sectionInner,
                ),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = stringResource(tab.titleRes),
                tint = RattiColor.Tinta,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RattiAppPreview() {
    RattiTheme {
        RattiApp()
    }
}
