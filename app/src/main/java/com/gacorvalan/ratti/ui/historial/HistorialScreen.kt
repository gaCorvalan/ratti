package com.gacorvalan.ratti.ui.historial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gacorvalan.ratti.R
import com.gacorvalan.ratti.datos.PlanillaRepositorio
import com.gacorvalan.ratti.ui.InicioSesion
import com.gacorvalan.ratti.ui.inicio.CargaInicio
import com.gacorvalan.ratti.ui.inicio.InicioViewModel
import com.gacorvalan.ratti.ui.inicio.pesos
import com.gacorvalan.ratti.ui.theme.RattiColor
import com.gacorvalan.ratti.ui.theme.RattiRadius
import com.gacorvalan.ratti.ui.theme.RattiSpace
import com.gacorvalan.ratti.ui.theme.RattiTabMetrics
import com.gacorvalan.ratti.ui.theme.RattiTheme
import com.gacorvalan.ratti.ui.theme.RattiType

private val AltoBarras = 120.dp
private val AnchoBarra = 32.dp

@Composable
fun HistorialScreen(
    email: String?,
    errorSesion: String?,
    ocupado: Boolean,
    onEntrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val modelo = viewModel { InicioViewModel(PlanillaRepositorio()) }
    val context = LocalContext.current
    LaunchedEffect(email) {
        modelo.sincronizar(context, email)
    }
    when (val carga = modelo.carga) {
        CargaInicio.SinSesion -> HistorialAviso(modifier) {
            InicioSesion(
                error = errorSesion,
                ocupado = ocupado,
                onEntrar = onEntrar,
            )
        }
        CargaInicio.Cargando -> HistorialAviso(modifier) {
            CircularProgressIndicator(
                modifier = Modifier.size(RattiTabMetrics.icon),
                color = RattiColor.Cobalt,
                strokeWidth = RattiRadius.track,
            )
            Text(
                text = stringResource(R.string.leyendo_planilla),
                style = RattiType.pocketCaption,
                color = RattiColor.Ink,
            )
        }
        is CargaInicio.Error -> HistorialAviso(modifier) {
            Text(
                text = carga.mensaje,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = RattiType.pocketCaption,
                color = RattiColor.Ink,
            )
            Text(
                text = stringResource(R.string.reintentar),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = { modelo.refrescar(context) },
                ),
                style = RattiType.link,
                color = RattiColor.Cobalt,
            )
            InicioSesion(
                error = errorSesion,
                ocupado = ocupado,
                onEntrar = onEntrar,
            )
        }
        is CargaInicio.Listo -> HistorialContenido(
            ciclos = ciclosDe(carga.historial, carga.codigoCiclo),
            modifier = modifier,
        )
    }
}

@Composable
private fun HistorialAviso(
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        CabeceraHistorial()
        Regla()
        Column(
            modifier = Modifier
                .padding(
                    start = RattiSpace.screenMargin,
                    end = RattiSpace.screenMargin,
                    top = RattiSpace.sectionGap,
                    bottom = RattiSpace.contentBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        ) {
            contenido()
        }
    }
}

@Composable
private fun HistorialContenido(
    ciclos: List<CicloHistorial>,
    modifier: Modifier = Modifier,
) {
    var seleccionado by rememberSaveable { mutableStateOf<String?>(null) }
    val ciclo = ciclos.firstOrNull { it.id == seleccionado }
    BackHandler(enabled = ciclo != null) {
        seleccionado = null
    }
    if (ciclo == null) {
        ListaHistorial(
            ciclos = ciclos,
            onMes = { seleccionado = it },
            modifier = modifier,
        )
    } else {
        DetalleCiclo(
            ciclo = ciclo,
            onVolver = { seleccionado = null },
            modifier = modifier,
        )
    }
}

@Composable
private fun ListaHistorial(
    ciclos: List<CicloHistorial>,
    onMes: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        CabeceraHistorial()
        Regla()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(
                    start = RattiSpace.screenMargin,
                    end = RattiSpace.screenMargin,
                    top = RattiSpace.sectionInner,
                    bottom = RattiSpace.contentBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionGap),
        ) {
            if (ciclos.isEmpty()) {
                Text(
                    text = stringResource(R.string.historial_vacio),
                    style = RattiType.pocketCaption,
                    color = RattiColor.Ink72,
                )
            } else {
                GraficoGasto(ciclos = ciclos, onMes = onMes)
                Column(verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner)) {
                    ciclos.forEach { ciclo ->
                        FilaMes(ciclo = ciclo, onClick = { onMes(ciclo.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraHistorial() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = RattiSpace.screenMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.historial_titulo),
            modifier = Modifier.weight(1f),
            style = RattiType.screenTitle,
            color = RattiColor.Ink,
        )
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_destello_historial),
                contentDescription = null,
                modifier = Modifier.size(width = 26.6296.dp, height = 28.dp),
            )
        }
    }
}

@Composable
private fun Regla() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RattiSpace.screenMargin)
            .height(2.dp)
            .background(RattiColor.Ink),
    )
}

@Composable
private fun GraficoGasto(
    ciclos: List<CicloHistorial>,
    onMes: (String) -> Unit,
) {
    val tope = topeDelGrafico(ciclos) ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RattiRadius.hero))
            .background(RattiColor.White)
            .padding(RattiSpace.cardPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.historial_gasto_vs_tope),
                modifier = Modifier.weight(1f),
                style = RattiType.chartTitle,
                color = RattiColor.Ink,
            )
            Text(
                text = stringResource(R.string.historial_tope, pesos(tope)),
                style = RattiType.meta,
                color = RattiColor.Ink,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(RattiSpace.sectionGap),
                verticalAlignment = Alignment.Bottom,
            ) {
                cronologico(ciclos).forEach { ciclo ->
                    BarraMes(ciclo = ciclo, onClick = { onMes(ciclo.id) })
                }
            }
        }
    }
}

@Composable
private fun BarraMes(
    ciclo: CicloHistorial,
    onClick: () -> Unit,
) {
    val descripcion = descripcionMes(ciclo)
    Column(
        modifier = Modifier
            .width(AnchoBarra)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AltoBarras),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltoBarras * fraccionDelTope(ciclo.gasto, ciclo.tope))
                    .clip(RoundedCornerShape(RattiRadius.bar))
                    .background(colorBarra(ciclo.tono)),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = siglaMes(ciclo.mes),
            modifier = Modifier.fillMaxWidth(),
            style = RattiType.captionBold,
            color = RattiColor.Ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun FilaMes(
    ciclo: CicloHistorial,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(RattiRadius.row))
            .background(fondoMes(ciclo.tono))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = RattiSpace.rowPadding)
            .semantics { contentDescription = descripcionMes(ciclo) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = ciclo.mes,
            modifier = Modifier.weight(1f),
            style = RattiType.monthAmount,
            color = RattiColor.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = pesos(ciclo.gasto),
            style = RattiType.monthAmount,
            color = colorCifra(ciclo.tono),
            maxLines = 1,
        )
        Text(
            text = textoPorcentajeTope(ciclo),
            style = RattiType.segment,
            color = colorCifra(ciclo.tono),
            maxLines = 1,
        )
    }
}

@Composable
private fun DetalleCiclo(
    ciclo: CicloHistorial,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        NavHistorial(titulo = ciclo.mes, onVolver = onVolver)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(
                    start = RattiSpace.screenMargin,
                    end = RattiSpace.screenMargin,
                    bottom = RattiSpace.contentBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(RattiSpace.sectionGap),
        ) {
            HeroCiclo(ciclo)
            if (ciclo.categorias.isEmpty()) {
                Text(
                    text = stringResource(R.string.historial_sin_categorias),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RattiRadius.tile))
                        .background(RattiColor.White)
                        .padding(RattiSpace.cardPadding),
                    style = RattiType.pocketCaption,
                    color = RattiColor.Ink72,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(RattiRadius.tile))
                        .background(RattiColor.White)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    ciclo.categorias
                        .sortedByDescending { it.monto }
                        .forEach { categoria ->
                            FilaCategoria(categoria = categoria, tope = ciclo.tope)
                        }
                }
            }
        }
    }
}

@Composable
private fun NavHistorial(
    titulo: String,
    onVolver: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(role = Role.Button, onClick = onVolver),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nav_atras),
                contentDescription = stringResource(R.string.historial_volver),
                tint = RattiColor.Ink,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = titulo,
            modifier = Modifier.weight(1f),
            style = RattiType.navTitle,
            color = RattiColor.Ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun HeroCiclo(ciclo: CicloHistorial) {
    val marca = colorMarca(ciclo.excedido)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RattiRadius.hero))
            .background(RattiColor.Cobalt),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_orbe_turquesa),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(100.dp)
                .offset(x = 18.dp, y = (-48).dp),
        )
        Image(
            painter = painterResource(R.drawable.ic_orbe_rosa),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(64.dp)
                .offset(x = 18.dp, y = 70.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = pastillaCiclo(ciclo.mes, ciclo.cierre),
                modifier = Modifier
                    .clip(RoundedCornerShape(RattiRadius.pill))
                    .background(RattiColor.Lime)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = RattiType.editorialPill,
                color = RattiColor.Ink,
                maxLines = 1,
            )
            Text(
                text = pesos(ciclo.gasto),
                style = RattiType.heroAmount,
                color = RattiColor.White,
            )
            Text(
                text = subtituloHero(ciclo),
                style = RattiType.heroSubtitle,
                color = RattiColor.White,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(RattiRadius.track))
                        .background(RattiColor.White28),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraccionDelTope(ciclo.gasto, ciclo.tope))
                            .background(marca),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = textoUsaste(ciclo),
                        modifier = Modifier.weight(1f),
                        style = RattiType.meta,
                        color = RattiColor.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = marcaSemaforo(ciclo),
                        style = RattiType.captionBold,
                        color = marca,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaCategoria(
    categoria: CategoriaHistorial,
    tope: Long,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(top = 6.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RattiSpace.sectionInner),
        ) {
            Text(
                text = categoria.nombre,
                modifier = Modifier.weight(1f),
                style = RattiType.categoryAmount,
                color = RattiColor.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pesos(categoria.monto),
                style = RattiType.categoryAmount,
                color = RattiColor.Ink,
                maxLines = 1,
            )
            Text(
                text = textoPorcentajeCategoria(categoria.monto, tope),
                style = RattiType.heroSubtitle,
                color = RattiColor.Ink,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(RattiRadius.categoryTrack))
                .background(RattiColor.Ink12),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraccionDelTope(categoria.monto, tope))
                    .background(RattiColor.Cobalt),
            )
        }
    }
}

private fun colorBarra(tono: TonoMes): Color = when (tono) {
    TonoMes.Actual -> RattiColor.Cobalt
    TonoMes.Normal -> RattiColor.Pink
    TonoMes.Excedido -> RattiColor.Danger
}

private fun fondoMes(tono: TonoMes): Color = when (tono) {
    TonoMes.Actual -> RattiColor.Lime
    TonoMes.Normal -> RattiColor.White
    TonoMes.Excedido -> RattiColor.DangerSubtle
}

private fun colorCifra(tono: TonoMes): Color =
    if (tono == TonoMes.Excedido) RattiColor.DangerDark else RattiColor.Ink

private fun colorMarca(excedido: Boolean): Color =
    if (excedido) RattiColor.Danger else RattiColor.Lime

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun HistorialPreview() {
    RattiTheme {
        HistorialContenido(ciclos = DatosHistorial.ciclos)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun HistorialNoviembrePreview() {
    RattiTheme {
        DetalleCiclo(
            ciclo = DatosHistorial.ciclos.first { it.actual },
            onVolver = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, backgroundColor = 0xFFF8F4EB)
@Composable
private fun HistorialVacioPreview() {
    RattiTheme {
        HistorialContenido(ciclos = emptyList())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 420, backgroundColor = 0xFFF8F4EB)
@Composable
private fun HistorialSinCategoriasPreview() {
    RattiTheme {
        DetalleCiclo(
            ciclo = DatosHistorial.ciclos.first().copy(categorias = emptyList()),
            onVolver = {},
        )
    }
}
