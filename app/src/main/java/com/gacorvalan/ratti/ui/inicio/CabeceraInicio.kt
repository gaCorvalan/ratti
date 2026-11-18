package com.gacorvalan.ratti.ui.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiType

@Composable
internal fun CabeceraInicio(
    inicial: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = RattiSpace.screenMargin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = stringResource(R.string.marca),
                    style = RattiType.brand,
                    color = RattiColor.Ink,
                )
                Image(
                    painter = painterResource(R.drawable.ic_punto_marca),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(10.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(RattiColor.Lime, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = inicial,
                    style = RattiType.tileBrand,
                    color = RattiColor.Ink,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RattiSpace.screenMargin)
                .height(2.dp)
                .background(RattiColor.Ink),
        )
    }
}
