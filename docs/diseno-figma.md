# Ratti — especificación de diseño (Figma UX v1)

Fuente: archivo [Ratti — UX v1](https://www.figma.com/design/ioUTuMmVUtinp0KFdqDQh9/Ratti-%E2%80%94-UX-v1) (`fileKey` `ioUTuMmVUtinp0KFdqDQh9`), página **Screens** (`0:1`).  
Lectura de solo consulta el 18 de noviembre de 2026. No se modificó el archivo. Ante una diferencia entre esta spec y el archivo, manda Figma.

**Cómo leer esta spec.** Los tokens que hay que implementar son los de la lámina **Foundations** (`37:801`) y los valores *realmente aplicados* en las pantallas. En el archivo también hay colecciones de variables (violeta/menta/gris) y estilos de texto en Inter: **no están ligados a ningún nodo de las pantallas** y no deben usarse.

Todos los textos, montos, fechas, porcentajes y nombres de comercios o bancos que aparecen abajo son **datos de ejemplo**, salvo que se indique lo contrario (estructura, tokens y medidas sí son de diseño).

Capturas 1× (390×844, y la tab bar a tamaño natural) en `docs/figma/`.

---

## 1. Color

No hay estilos de pintura (`Paint styles`) en el archivo. Los colores MONI están hardcodeados en las pantallas y documentados en Foundations.

| Token (Foundations) | Hex | Uso principal |
|---|---|---|
| Cream | `#F8F4EB` | Fondo de pantalla, status bar, nav, contenido |
| Ink | `#101013` | Texto e iconos, regla de 2 px, pastillas oscuras, trazo de la tab bar |
| Cobalt | `#2C3CFA` | Bloque de saldo (Inicio), héroe de mes, barra de noviembre, “VER TODOS”, badge Netflix, barras de categoría |
| Pink | `#FF6FBA` | Tarjeta Visa, punto de marca, sol, orbes, filas de cuota, barras de historial (meses en rango) |
| Lime | `#DFFF42` | Cinta, pastillas sobre cobalto, acentos (subrayado, destellos, tab activa, mes actual, tarjeta USDC sin asignar, fill del colchón) |
| Tangerine | `#FF9C50` | Tarjeta Amex, centro del sol, badge YPF |
| Teal | `#72E6EC` | Orbe turquesa, badge Jumbo/Farmacity, swatch del colchón |
| White | `#FFFFFF` | Superficie de filas y cards, texto sobre cobalto, tab bar |
| Danger | `#E0243A` | Barra de septiembre (mes que se pasó del tope) |

Colores de apoyo que aparecen en pantalla y no tienen swatch en Foundations:

| Nombre sugerido | Hex | Uso |
|---|---|---|
| Danger dark | `#B5152C` | Monto y % en rojo (septiembre, resultado de Casa) |
| Danger subtle | `#FDE8EA` | Fondo de la fila de septiembre |
| Ink 72% | `#101013B8` | Captions secundarios (USD a mercado, “de $2.000.000”, lotes) |
| Ink 12% | `#1010131F` | Track de semáforos y barras de progreso sobre fondo claro |
| Ink 14% | `#10101324` | Sombra de la tab bar |
| Ink 30% | `#101013` a 30% | Píldora del home indicator (en la mayoría de las pantallas) |
| White 85% | `#FFFFFFD9` | Texto “por día” sobre la tarjeta de saldo |
| White 28% | `#FFFFFF47` | Track del semáforo sobre cobalto |
| White 30% | `#FFFFFF` a 30% | Orbe decorativo sobre Visa / CardTile |

En las tiles de tarjeta, el nombre del banco va en Ink con **opacidad 0,7**.

### Variables de Figma (no usar para implementar)

Hay tres colecciones, **ninguna variable está bound** a las 9 pantallas (`boundCount = 0`):

- **Primitives** (modo Value): escalas `violet/*`, `mint/*`, `gray/0–900`, `amber/*`, `red/*`. No coinciden con los hex MONI.
- **Color** (modo Light): semánticos tipo `color/bg/canvas` → `#F7F7FA`, `color/bg/brand` → violeta `#6D3FD9`, etc.
- **Spacing** (modo Value): ver la sección 3. Los valores numéricos sí coinciden con la escala usada, pero **tampoco están ligados** a los nodos.

---

## 2. Tipografía

La lámina Foundations indica, y las pantallas cumplen:

- **Archivo Black Regular** — titulares y montos
- **DM Sans Bold** — filas, pastillas y etiquetas fuertes
- **DM Sans Medium** — meta, captions y hora
- **DM Sans ExtraBold** — cintas y overlines
- **Noto Sans JP Bold 8/10** — microtexto decorativo “今月の残高” en Inicio (ejemplo, no es copy de producto)

El letter-spacing de todo el texto aplicado es **0**. No hay estilos de efecto. Hay 10 **text styles** locales llamados `Ratti/Display`, `Ratti/Title`, etc., todos en **Inter**, y **ningún texto de las pantallas los usa**.

Recetas realmente usadas (familia / peso / tamaño / interlineado):

### Archivo Black Regular

| Tamaño / interlínea | Uso |
|---|---|
| 34 / 36 | Saldo principal de Inicio |
| 28 / 32 | Títulos HISTORIAL y AHORROS; total a pagar; montos de bolsillos |
| 28 / 30 | Marca “RATTI”; monto gastado en el héroe de noviembre |
| 26 / 30 | Monto de CardTile |
| 26 / 28 | Saludo “HOLA, GABRIEL.” |
| 22 / 28 | Nombre del banco en detalle Colchón |
| 22 / 26 | Fecha de vencimiento en el héroe Visa |
| 22 / 24 | Títulos de sección (“TUS TARJETAS”, “ÚLTIMOS CONSUMOS”) |
| 20 / 24 | Título de NavHeader |
| 18 / 22 | “GASTO VS TOPE” |
| 16 / 18 | Marca de la tile (Visa/Amex) y letra del avatar |

### DM Sans Bold

| Tamaño / interlínea | Uso |
|---|---|
| 21 / 21,8 | Inicial del badge de movimiento |
| 17 / 24 | Importe de TxRow |
| 17 / 22 | Título e importe de CuotaRow y de movimientos de ahorros |
| 17 / 17,7 | Comercio en TxRow |
| 16 / 20 | Mes e importe en MonthRow |
| 14 / 18 | Categoría e importe en CategoryRow |
| 13 / 16 | Segmented control; porcentaje en MonthRow |
| 11 / 14 | “VENCE EL…”, “QUEDA 21,5%”, labels de barras |
| 11 / 13 | Texto de pastillas (“TE QUEDAN”, “VISA · SANTANDER”) |
| 10 / 12 | Pastilla editorial “NOVIEMBRE · CIERRA 26/11” |

### DM Sans Medium

| Tamaño / interlínea | Uso |
|---|---|
| 13 / 18 | Captions de bolsillos y USD |
| 13 / 16 | Hora de la status bar (“9:41”) |
| 12 / 16 | Porcentaje de categoría; subtítulo del héroe de noviembre |
| 11 / 16 | Lotes y nota de tipo de cambio |
| 11 / 14 | Banco en la tile (además, opacidad 0,7); metas del semáforo; “TOPE …” |
| 11 / 13 | “TOTAL A PAGAR”, “VENCIMIENTO” |
| 11 / 11,4 | Categoría/fecha en TxRow |

### DM Sans ExtraBold

| Tamaño / interlínea | Uso |
|---|---|
| 11 / 14 | Cinta de Inicio; overlines (“TOTAL A MERCADO”, “COLCHÓN”, “DÓNDE ESTÁ”, “MOVIMIENTOS”) |
| 11 / 12 | “VER TODOS” (color Cobalt) |

---

## 3. Espaciado, radios, sombras, trazos e iconos

Regla general, confirmada en las pantallas:

- margen lateral **24**
- entre secciones **16**
- dentro de una sección (título → contenido, gaps internos) **8**
- padding de card grande **16**
- padding de fila chica **12**
- la columna de contenido deja **88** de padding inferior para que la tab bar flotante no tape el final de la lista

Escala de la colección Spacing (valores, no bindings): `space/1=4`, `2=8`, `3=12`, `4=16`, `5=20`, `6=24`, `8=32`, `10=40`, `12=48`. También `radius/sm=8`, `md=12`, `lg=16`, `xl=20`, `full=999`; `size/icon-sm=20`, `icon=24`, `avatar=40`, `touch=44`, `bar=8`.

Radios realmente usados:

| Radio | Uso |
|---|---|
| 24 | Cards grandes de Ahorros (héroe y bolsillos) |
| 22 | Tarjeta de saldo, héroe Visa, héroe de noviembre, ChartCard; círculo de tab activa (44×44) |
| 20 | Avatar de Inicio (círculo 40×40) |
| 18 | CardTile, SegmentedControl, CatsCard |
| 15 | TxRow, CuotaRow, MonthRow, fila de movimiento, GoalRow |
| 14 | Segmento activo (36 de alto); pastilla “TE QUEDAN” |
| 13 | Pastillas editoriales / “VISA · SANTANDER” / “NOVIEMBRE · CIERRA 26/11” |
| 8 | Barras del gráfico |
| 4 | Tracks de 8 px (saldo, cuotas, colchón) |
| 3 | Tracks de 6 px (categorías) |
| 100 / 999 | Tab bar (píldora), badge de movimiento, home indicator |

Sombra (única, sin effect style):

- Tab bar: `DROP_SHADOW`, blur **18**, offset **(0, 6)**, spread **0**, color `#10101324`

Trazos:

- Tab bar: Ink `#101013`, **1,5** px
- Iconos de la tab bar: Ink, **1,83333** (el SVG exportado; en la lámina se lee como 1,8)
- Chevron atrás (NavHeader): Ink, **2** px
- No hay borde en las cards ni en las filas

Tamaños de icono / avatares:

| Medida | Uso |
|---|---|
| 44×44 | Hit area de atrás y de cada tab; trailing vacío del NavHeader |
| 40×40 | Avatar de Inicio |
| 28×28 | Destello lima junto a HISTORIAL / AHORROS |
| 24×24 | Icono atrás (dentro de 44×44) |
| 22×22 | Iconos de la tab bar (casa, billetera, barras) |
| 12×12 | Swatch de color en PocketCard |
| 10×10 | Punto rosa al lado de “RATTI” |
| 46×46 | Badge circular de TxRow |
| 48×48 | Motivo sol en Inicio |

---

## 4. Tamaño de frame

Todas las pantallas principales miden **390×844**.

Estructura de chrome común, de arriba hacia abajo:

1. **StatusBar** 390×44, padding `14, 24, 0, 24`, fondo Cream. Hora a la izquierda (DM Sans Medium 13/16, Ink). Iconos de señal y batería a la derecha (49×12, gap 6). Es chrome de dispositivo: no hace falta reproducirlo 1:1 en Android (se usa la status bar del sistema).
2. Contenido (header propio de cada pantalla + lista), con padding inferior **88**.
3. **TabBar** flotante 183×67 en `(104, 723)` — centrada: `(390 − 183) / 2 = 103,5`, redondeado a 104. Se superpone al contenido.
4. **HomeIndicator** 390×34 en y=810, píldora 134×5, radio 100, Ink al 30% en Inicio, detalles e Historial. En las cuatro pantallas de Ahorros el volcado no reportó opacidad (posible inconsistencia del archivo). Es chrome de iPhone: en Android corresponde el gesture/navigation inset, no dibujar la píldora.

---

## 5. Componentes compartidos

### Tab bar — `Moni / TabBar` (`36:298`)

Tres variantes: `Active=Inicio` (`36:299`), `Active=Historial` (`36:307`), `Active=Ahorros` (`55:346`). Todas 183×67.

- Layout horizontal, gap **12**, padding **10** vertical / **12** horizontal, align center
- Fondo White, trazo Ink 1,5, radio **100**
- Sombra: blur 18, offset (0, 6), `#10101324`
- Tres tabs de **44×44**, radio 22. La activa tiene fill Lime; las inactivas, sin fill
- Icono 22×22 centrado, trazo Ink 1,83333, puntas y uniones redondas. Glifos: **casa** (Inicio), **billetera** (Ahorros), **barras** (Historial), en ese orden. No son los iconos Material Home / Savings / History
- Sin labels de texto

### NavHeader — `Moni / NavHeader` (`36:315`)

390×52, horizontal, padding **4** vertical / **16** horizontal, fondo Cream.

- Atrás: 44×44, icono 24×24, chevron Ink stroke 2
- Título: Archivo Black 20/24, Ink (ejemplo: “Visa”, “Colchón”, “Noviembre”)
- Trailing: 44×44 vacío

### CardTile — `Moni / CardTile` (`26:264`)

252×94. Variantes `Color=Pink` (`#FF6FBA`) y `Color=Orange` (`#FF9C50`). Vertical, gap 4, padding 12, radio 18, clip. Orbe blanco al 30%, 72×72, asomando arriba a la derecha.

- Marca: Archivo Black 16/18, Ink
- Banco: DM Sans Medium 11/14, Ink al 70%
- Monto: Archivo Black 26/30, Ink
- Vencimiento: DM Sans Bold 11/14, Ink

### TxRow — `Moni / TxRow` (`26:279`)

342×71. Horizontal, gap 12, padding 0 vertical / 12 horizontal, radio 15, fondo White. Variantes de badge: Teal, Pink, Orange, Lime, Cobalt.

- Badge 46×46, radio 999. Inicial DM Sans Bold 21/21,8. En Cobalt la inicial es White; en el resto, Ink
- Comercio: DM Sans Bold 17/17,7
- Categoría/fecha: DM Sans Medium 11/11,4
- Importe: DM Sans Bold 17/24, alineado a la derecha

### SegmentedControl — `Moni / SegmentedControl` (`36:321`)

342×44, radio 18, fondo White, gap 4, padding 4. Variantes `Active=Consumos` y `Active=Cuotas`. Cada segmento 165×36, radio 14. El activo lleva fill Lime. Texto: DM Sans Bold 13/16, Ink.

### CuotaRow — `Moni / CuotaRow` (`36:332`)

342×84, vertical, gap 8, padding 12, radio 15, fondo White.

- Título e importe del mes: DM Sans Bold 17/22
- “N DE M” y “RESTAN $…”: DM Sans Medium 11/14 *(ejemplo)*
- Track 318×8, fill Ink 12%, radio 4; fill Pink con ancho proporcional a las cuotas pagadas

### MonthRow — `Moni / MonthRow` (`36:358`)

342×44, horizontal, gap 12, padding 12, radio 15.

- Mes actual: fondo Lime, texto Ink
- Mes normal: fondo White, texto Ink
- Mes excedido: fondo `#FDE8EA`, monto y % en `#B5152C`
- Mes e importe: DM Sans Bold 16/20; porcentaje: DM Sans Bold 13/16

### CategoryRow

318×44, vertical, gap 4, padding 6 vertical. Nombre e importe DM Sans Bold 14/18; porcentaje DM Sans Medium 12/16. Track 318×6, Ink 12%, radio 3; fill Cobalt con ancho proporcional al %.

### PocketCard — `Moni / PocketCard` (`57:383`)

Ancho 342, radio 24, padding 16, gap 8, fondo White. Overline ExtraBold 11/14; monto Archivo Black 28/32; caption Medium 13/18 (Ink 72%, o Danger dark si hay pérdida). Swatch 12×12 a la izquierda del título. En el listado de Ahorros se ve un chevron a la derecha (vector; no se volcó el path). El de Colchón además tiene track 310×8 con fill Lime.

---

## 6. Pantallas

### 6.1 Inicio — `26:315` — `figma/inicio.png`

Frame 390×844, vertical, clip, fondo Cream.

1. StatusBar `26:316` 390×44 (ver chrome).
2. Content `26:320` 390×766 en y=44. Vertical, **gap 16**, padding inferior 88, fondo Cream.
   1. **Header** `46:303` 390×42, vertical.
      - Cabecera `26:321` 390×40, horizontal, padding 0/24, center.
        - Marca: “RATTI” Archivo Black 28/30, Ink, + punto rosa 10×10 (`#FF6FBA`), gap 4.
        - Avatar 40×40, Lime, radio 20. Letra “R” *(ejemplo)* Archivo Black 16/18, Ink.
      - Regla: wrap 390×2, padding 0/24; trazo 342×2, Ink.
   2. **Editorial** `26:329` 390×60, horizontal, gap 8, padding 0/24.
      - Pastilla negra 148×22, padding 5/10, radio 13. Texto White, DM Sans Bold 10/12: “NOVIEMBRE · CIERRA 26/11” *(ejemplo)*.
      - Saludo Archivo Black 26/28, Ink: “HOLA, GABRIEL.” *(ejemplo)*. Gap 8 entre pastilla y saludo.
      - Motivo a la derecha: sol 48×48 (estrella Pink + centro Tangerine 15×15) y microtexto Noto Sans JP Bold 8/10, “今月の残高” *(ejemplo decorativo)*.
   3. **Saldo** `46:304` 390×227, vertical, gap 8. Relectura el 18 de noviembre de 2026. La tarjeta es la instancia `96:500` del componente `Moni / Tarjeta de saldo` (`96:499`). En Inicio está en **Naranja**.
      - Tarjeta 342×191, margen 24, padding 16, gap 4, **Cobalt**, radio 22, clip.
        - Orbe Teal 116×116 en (246, −58). Destello Lime 40×40 en (230, 36). Ya no hay orbe rosa.
        - Pastilla: padding 5/10, radio píldora (999). DM Sans Bold 11/13. El texto y el color dependen del saldo restante, no de lo gastado:
          - Verde (`94:551`), 25 % ≤ restante < 45 %: fondo `#12B886`, texto Ink, “TE QUEDAN · DISPONIBLE”.
          - Naranja (`94:552`), 15 % ≤ restante < 25 %: fondo `#C8870A`, texto Ink, “TE QUEDAN · ATENCIÓN”.
          - Rojo (`94:553`), restante < 15 %: fondo `#D92D20`, texto White, “TE QUEDAN · SALDO BAJO”.
          - Desde el 45 % el componente no tiene otra variante: se usa Verde y el estado queda en “QUEDA x%”, sin “MENOS DEL 45%”.
        - Monto Archivo Black 34/36, White. En Naranja el ejemplo es `$279.251`.
        - Subrayado 196×4. El color es el suave de la banda, no el de la pastilla: Verde `#E6F9F3`, Naranja `#FFF8E6`, Rojo `#FDECEC`.
        - Caption White (no al 85 %), DM Sans Medium 12/16, dos líneas: “Te quedan $279.251 · ~$17.450 / por día hasta el 26/11” *(ejemplo de Naranja)*.
        - Semáforo: track 310×8, negro al 28 % (`rgba(0,0,0,0.28)`), radio píldora. El fill es el **saldo restante**, en el suave de la banda. En Naranja mide **66,6** px (≈21,5 % de 310). “Usaste $1.020.749 de $1.300.000” Medium 12/16 White. Estado Bold 12/16 White: “QUEDA 21,5% · MENOS DEL 25%” en Naranja, “QUEDA 35% · MENOS DEL 45%” en Verde, “QUEDA 10% · MENOS DEL 15%” en Rojo. *(ejemplos)*
      - **Cinta** `26:354` 390×28, fondo Lime, texto centrado ExtraBold 11/14, Ink. Releída igual: “●   $17.450 POR DÍA HASTA EL 26/11   ✦   QUEDA 21,5%” *(ejemplo)*. No usa la coletilla de la tarjeta.
   4. **Tarjetas** `26:356` 390×126, vertical, gap 8, padding izquierdo 24.
      - Título “TUS TARJETAS” Archivo Black 22/24, Ink.
      - Carrusel 366×94 (se sale 24 px a la derecha para recortar la segunda tile), horizontal, gap 12.
        - VisaTile 252×94, Pink *(ejemplo: Visa, Santander, $487.320, VENCE EL 12/11)*.
        - AmexTile 252×94, Tangerine, empieza en x=264 (se ve recortada). Banco vacío en el archivo. *(ejemplo: Amex, $214.650, VENCE EL 12/11)*.
   5. **Actividad** `26:373` 390×261, vertical, gap 8, padding 0/24.
      - Cabezal space-between: “ÚLTIMOS CONSUMOS” Archivo Black 22/24; “VER TODOS” ExtraBold 11/12, Cobalt, en un wrap 70×20 con padding 4.
      - Tres TxRow 342×71, gap 8 entre ellas (el gap del padre es 8; 71+8=79). *(ejemplos: Jumbo/SUPER/$48.320 badge Teal; Rappi/COMIDA FUERA/$12.450 Pink; YPF/TRANSPORTE/$32.100 Tangerine)*. En esta pantalla la categoría va sin fecha.
3. HomeIndicator `26:410`.
4. TabBar `55:358`, variante Inicio activa, en (104, 723).

### 6.2 Detalle Visa — Consumos — `37:298` — `figma/detalle-visa-consumos.png`

390×844, vertical, fondo Cream.

1. StatusBar.
2. NavHeader `37:312`, título “Visa” *(ejemplo)*.
3. Content `37:318` 390×714 en y=96. Vertical, **gap 8**, padding `0, 24, 88, 24`.
   - **VisaHero** 342×110, padding 16, gap 8, Pink, radio 22. Orbe White 30%, 88×88, en (270, −28).
     - Pastilla Ink 119×23, padding 5/10, radio 13, “VISA · SANTANDER” Bold 11/13, White *(ejemplo)*.
     - Totals horizontal, align bottom: “TOTAL A PAGAR” Medium 11/13 + `$487.320` Archivo Black 28/32 a la izquierda; “VENCIMIENTO” + “12/11” Archivo Black 22/26 a la derecha *(ejemplo)*.
   - Segments `37:330`, activo **Consumos** (fill Lime en Seg/Consumos 165×36).
   - TxList 342×466, vertical, gap 8. Seis filas *(ejemplo)*:
     - Jumbo · SUPER · HOY · $48.320 · badge Teal
     - Rappi · COMIDA FUERA · HOY · $12.450 · Pink
     - YPF · TRANSPORTE · AYER · $32.100 · Tangerine
     - Coto · SUPER · 8 NOV · $67.890 · Lime
     - Farmacity · SALUD · 8 NOV · $18.740 · Teal
     - Netflix · SUSCRIPCIONES · USD 15,99 · 7 NOV · $25.105 · badge **Cobalt**, inicial White
4. HomeIndicator + TabBar (en este frame la tab activa sigue siendo Inicio).

### 6.3 Detalle Visa — Cuotas — `37:412` — `figma/detalle-visa-cuotas.png`

Igual al anterior hasta el héroe y el segmented (aquí activo **Cuotas**).

- CuotaList 342×360, vertical, gap 8. Cuatro CuotaRow *(ejemplo)*:
  - TV Samsung · $85.400 · 2 DE 6 · RESTAN $341.600 · fill Pink 106/318 (2/6)
  - Notebook Lenovo · $62.150 · 4 DE 12 · RESTAN $497.200 · fill 106/318 (4/12)
  - Seguro del auto · $28.900 · 5 DE 12 · RESTAN $202.300 · fill 133/318 (5/12)
  - Mercado Libre · $45.200 · 1 DE 3 · RESTAN $90.400 · fill 106/318 (1/3)

TabBar: Inicio activa (se entra desde Inicio).

### 6.4 Historial — `37:508` — `figma/historial.png`

390×844, vertical, fondo Cream.

1. StatusBar.
2. Header `37:522` 390×48, padding 8/24. “HISTORIAL” Archivo Black 28/32, Ink. Destello Lime 28×28 a la derecha.
3. Regla 342×2, Ink, margen 24, en y=92.
4. Content `37:527` 390×716 en y=94. Vertical, **gap 16**, padding `8, 24, 88, 24`.
   - **ChartCard** 342×212, padding 16, gap 12, White, radio 22.
     - “GASTO VS TOPE” Archivo Black 18/22; “TOPE $1.300.000” Medium 11/14 a la derecha *(ejemplo)*.
     - Barras 310×120, horizontal, gap 16, align bottom. Cada barra 32 px de ancho, radio 8. Alturas *(ejemplo)*: Jun 102 Pink, Jul 114 Pink, Ago 98 Pink, Sep **120 Danger**, Oct 106 Pink, Nov **91 Cobalt**.
     - Labels JUN–NOV, Bold 11/14, Ink, gap 16, cada una ~38,3 px de ancho.
   - **MonthList** 342×304, vertical, gap 8. Seis MonthRow *(ejemplo)*:
     - Noviembre · $1.020.749 · 78% · fondo Lime (mes actual)
     - Octubre · $1.187.000 · 91% · White
     - Septiembre · $1.342.000 · 103% · `#FDE8EA`, monto y % `#B5152C`
     - Agosto · $1.098.000 · 84% · White
     - Julio · $1.280.000 · 98% · White
     - Junio · $1.145.000 · 88% · White
5. HomeIndicator + TabBar con **Historial** activo (círculo Lime en el icono de barras).

### 6.5 Historial — Noviembre — `37:594` — `figma/historial-noviembre.png`

1. StatusBar + NavHeader título “Noviembre” *(ejemplo)*.
2. Content `37:614` 390×714, gap **16**, padding `0, 24, 88, 24`.
   - **Hero** 342×118, padding `8, 16, 4, 16`, gap 4, **Cobalt**, radio 22.
     - Orbe Teal 100×100 en (260, −48); orbe Pink 64×64 en (296, 70).
     - Pastilla Lime 148×22, padding 5/10, radio 13, “NOVIEMBRE · CIERRA 26/11” Bold 10/12, Ink *(ejemplo)*.
     - `$1.020.749` Archivo Black 28/30, White *(ejemplo)*.
     - “de $1.300.000  ·  te quedan $279.251” Medium 12/16, White *(ejemplo)*.
     - Semáforo igual que en Inicio: track 310×8 White 28%, fill Lime 243, “Usaste …” White Medium 11/14 y “QUEDA 21,5%” Lime Bold 11/14. *(ejemplo)*
   - **CatsCard** 342×492, padding 4/12, White, radio 18. Once CategoryRow, fill de barra siempre Cobalt. Datos de ejemplo:

     | Categoría | Importe | % | Ancho del fill (px / 318) |
     |---|---|---|---|
     | Fijos | $312.400 | 24,0% | 76 |
     | Super | $198.450 | 15,3% | 49 |
     | Comida fuera | $87.320 | 6,7% | 21 |
     | Extraordinario | $85.400 | 6,6% | 21 |
     | Transporte | $72.640 | 5,6% | 18 |
     | Suscripciones | $68.235 | 5,2% | 17 |
     | Ocio | $54.180 | 4,2% | 13 |
     | Mascotas | $41.200 | 3,2% | 10 |
     | Casa/hogar | $38.900 | 3,0% | 10 |
     | Otros | $33.284 | 2,6% | 8 |
     | Salud | $28.740 | 2,2% | 7 |

3. HomeIndicator + TabBar Historial activo.

### 6.6 Ahorros — `51:297` — `figma/ahorros.png`

1. StatusBar.
2. Header `51:321` 390×48, padding 8/24, gap 8. “AHORROS” Archivo Black 28/32 + destello Lime 28×28 (junto al título, no pegado al borde).
3. Regla 342×2, Ink.
4. Content `51:313` 390×716, gap **16**, padding `8, 24, 88, 24`.
   - **Hero** 342×134, padding 16, gap 8, White, radio 24.
     - “TOTAL A MERCADO” ExtraBold 11/14
     - `$3.237.000,77` Archivo Black 28/32 *(ejemplo)*
     - “USD 1.204,22 a mercado” y “USD 1.235,00 a costo” Medium 13/18, Ink 72% *(ejemplo)*
   - **Pocket/Colchón** 342×134, White, radio 24, padding 16. Swatch Teal 12×12. “COLCHÓN” / “67,3%” ExtraBold 11/14. `$1.346.378` Archivo Black 28/32. “de $2.000.000” Medium 13/18 Ink 72%. Track 310×8, fill Lime **209** px (≈67,4%). Chevron a la derecha. *(ejemplo)*
   - **Pocket/Casa** 342×118. Swatch Pink. “CASA · VOOG”. Porcentaje y caption en `#B5152C`: “−3,7%”, “−USD 30,78 (−3,7%) vs costo USD 835”. Monto “USD 804,22”. Sin track. *(ejemplo)*
   - **Pocket/Dólares** 342×118. Swatch Lime. “DÓLARES · USDC”. “USDC 400”. “Sin asignar 400 · 5 objetivos” Ink 72%. *(ejemplo)*
5. HomeIndicator + TabBar con **Ahorros** activo (círculo Lime en la billetera).

### 6.7 Ahorros — Colchón — `57:419` — `figma/ahorros-colchon.png`

NavHeader título “Colchón”. Content gap 16, padding `8, 24, 88, 24`.

- Card 342×154, White, radio 24, padding 16. Overline “COLCHÓN” y “67,3%”. Monto `$1.346.378`. Caption “de $2.000.000  ·  faltan $653.622” Ink 72%. Track 310×8, fill Lime 209. “100% de los pesos” Ink 72%. *(ejemplo)*
- Card “DÓNDE ESTÁ” 342×108, radio 24, padding 16. Banco “Carrefour Banco” Archivo Black 22/28. “Fondo de emergencia · actualizado 09/11” Medium 13/18 Ink 72%. *(ejemplo)*
- Bloque “MOVIMIENTOS”: overline ExtraBold 11/14 + fila 342×68, radio 15, padding 12, White. “Ajuste de saldo” / `$1.346.378` Bold 17/22; “09/11  ·  Saldo inicial” Medium 13/18 Ink 72%. *(ejemplo)*

TabBar: Ahorros activo.

### 6.8 Ahorros — Casa — `57:457` — `figma/ahorros-casa.png`

NavHeader “Casa”. Una sola card 342×300, White, radio 24, padding 16, gap 8. *(todo ejemplo)*

- “CASA · VOOG” y “−3,7%” en `#B5152C`, ExtraBold 11/14
- “USD 804,22” Archivo Black 28/32
- “a mercado  ·  USD 79,31 cierre 10/11” Medium 13/18 Ink 72%
- Fila COSTO “USD 835,00” Bold 17/22 (Ink) vs RESULTADO “−USD 30,78” Bold 17/22 (`#B5152C`)
- “10,140188176 papeles  ·  100% a Casa”
- “Sin meta fija  ·  plan USD 50/mes”
- Tres lotes Medium 11/16 Ink 72%: “13/10  ·  2,703327171 @ 86,56”, “04/11  ·  4,892103972 @ 81,56”, “06/11  ·  2,544757033 @ 78,20”
- “USD 1 de comisión en cada compra (USD 3 en total)”

### 6.9 Ahorros — Dólares — `57:493` — `figma/ahorros-dolares.png`

NavHeader “Dólares”. Content **gap 8**, padding `8, 24, 88, 24`.

- Card “sin asignar” 342×112, **fondo Lime**, radio 24, padding 16. “DÓLARES · USDC” ExtraBold 11/14. “USDC 400” Archivo Black 28/32. “Sin asignar  ·  listo para una meta” Medium 13/18 Ink 72%. *(ejemplo)*
- Cinco GoalRow, White, radio 15, padding 12, gap 8, track 318×8 con fill Lime de **0** px (todas en 0%):
  - Muelas 342×88 — “USD 0  /  ~$3.000.000”
  - Anteojos 342×88 — “USD 0  /  ~$300.000”
  - Focus 342×88 — “USD 0  /  ~USD 1.500”
  - Electrodomésticos 342×106 — “USD 0  /  USD 2.040” + “USD 120/mes · dic-26→abr-28”
  - Gustos 342×72 — no tiene track; overline “FONDO”; “USD 0  ·  plan ~$30.000/mes”
  Título Bold 17/22; % o “FONDO” ExtraBold 11/14; caption Medium 13/18 Ink 72%. *(ejemplo)*
- Nota FX 342×16, padding 0/4: “TC USD $1.570 (oficial venta 09/11).” Medium 11/16, Ink 72%. *(ejemplo)*

TabBar: Ahorros activo.

---

## 7. Inventario de frames

| Pantalla o componente | Node id | Tamaño |
|---|---|---|
| Inicio | `26:315` | 390×844 |
| Detalle Visa — Consumos | `37:298` | 390×844 |
| Detalle Visa — Cuotas | `37:412` | 390×844 |
| Historial | `37:508` | 390×844 |
| Historial — Noviembre | `37:594` | 390×844 |
| Ahorros | `51:297` | 390×844 |
| Ahorros — Colchón | `57:419` | 390×844 |
| Ahorros — Casa | `57:457` | 390×844 |
| Ahorros — Dólares | `57:493` | 390×844 |
| Foundations | `37:801` | 780×1510 |
| Moni / TabBar | `36:298` | set 613×99; variante 183×67 |
| Moni / CardTile | `26:264` | variante 252×94 |
| Moni / TxRow | `26:279` | variante 342×71 |
| Moni / NavHeader | `36:315` | 390×52 |
| Moni / StatusBar | `36:375` | 390×44 |
| Moni / SegmentedControl | `36:321` | variante 342×44 |
| Moni / CuotaRow | `36:332` | 342×84 |
| Moni / MonthRow | `36:358` | 342×44 |
| Moni / CategoryRow | `36:351` | 342×44 |
| Moni / ChartBar | `36:371` | 32×(variable) dentro de 144×120 |
| Moni / GoalRow | `49:297` | varias alturas (72–106) |
| Moni / PocketCard | `57:383` | 342×118 o 134 |
| Movimiento / Café | `27:317` | 342×71 (estudio; no es pantalla) |

---

## 8. Límites de esta lectura

Relectura de solo consulta el 18 de noviembre de 2026. No se modificó el archivo.

- `get_metadata` y `get_screenshot` de Foundations (`37:801`): cada swatch se llama con su hex (`#f8f4eb`, `#101013`, `#2c3cfa`, `#ff6fba`, `#dfff42`, `#ff9c50`, `#72e6ec`, `#ffffff`, `#e0243a`).
- `get_variable_defs` en Foundations (`37:801`) y en TabBar (`36:298`) devolvió `{}`. No hay variables ligadas a esos nodos.
- `get_design_context` del bloque de tipo (`37:842`): Archivo Black Regular 22/26, DM Sans Bold 17/22, DM Sans Medium 13/18 (`opsz` 14), DM Sans ExtraBold 11/14 (`opsz` 14). Es una muestra de las familias. La escala completa de la sección 2 sale de las pantallas.
- `get_design_context` de TabBar (`36:298` y la variante `36:299`): píldora blanca, trazo `#101013` de 1,5, padding 12 horizontal y 10 vertical, gap 12, radio 100, ítem activo 44×44 en `#dfff42`, iconos 22×22. El código generado traduce la sombra a `drop-shadow: 0 6px 9px rgba(16,16,19,0.14)`: el color es `#101013` al 14% (`#10101324`) y el blur de CSS queda en la mitad del blur de Figma. El efecto del archivo es blur **18**, offset **(0, 6)**, spread **0**.
- Los SVG de casa, billetera y barras se exportaron de ese nodo. Trazo 1.83333, caps y joins redondos, color `#101013`. No se extrajeron el chevron atrás ni el de los bolsillos.
- Las medidas de las pantallas (sección 6) vienen de la lectura anterior. Esta pasada volvió a exportar las capturas 1× a `docs/figma/`.
- En Ahorros y sus detalles, el home indicator no reportó opacidad 30% (en el resto sí).
- En la tile Amex de Inicio el campo Banco está vacío.
