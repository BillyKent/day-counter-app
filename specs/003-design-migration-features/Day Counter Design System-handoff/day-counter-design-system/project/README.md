# Day Counter — Design System

Day Counter es una aplicación Android para llevar el conteo de **días consecutivos** relacionados con metas personales y hábitos: dejar de fumar, evitar el alcohol, mantener una rutina de ejercicio, o cualquier objetivo de superación personal.

Los usuarios crean contadores personalizados (meta + fecha de inicio) y la app calcula automáticamente la racha de días alcanzada. Pueden editarlos, reiniciarlos y eliminarlos, con persistencia local. Incluye **widgets de pantalla de inicio** y **notificaciones motivacionales** cuando se alcanzan hitos importantes (7, 30, 100, 365 días…).

> El producto vive en español. Toda la voz, los hitos y los mensajes motivacionales están escritos para hispanohablantes.

## Dirección de marca

Dos restricciones fundadoras dictaron el sistema completo:

1. **"Los colores deben inspirar confianza y tranquilidad."** → paleta de azul-teal profundo + crema cálida + verde salvia. Sin neones, sin gradientes púrpura, sin saturación extrema.
2. **"Las formas tienen que ser curvas para dar calidez visual."** → radios generosos (16–32px), tarjetas tipo *squircle*, anillos circulares como representación principal de progreso, sin esquinas duras en componentes interactivos.

El resultado se siente más cercano a una app de meditación o de bienestar que a una app de productividad — la racha es emocional, no clínica.

## Fuentes / referencias

No se entregó código, Figma ni assets existentes. Este sistema se construyó desde el brief en español del producto. Si más adelante hay:

- **Código Android** (Kotlin/Compose, XML themes, drawables)
- **Diseños en Figma**
- **Logo / mark final**
- **Capturas de la app real**

…por favor adjúntalos al proyecto y los reintegraremos. Por ahora, los íconos vienen de **Lucide** (variante redondeada) y las fuentes de **Google Fonts** — ambas sustituciones razonables que deberíamos reemplazar con el sistema final del cliente cuando exista.

---

## Content fundamentals

**Idioma:** español neutro, con preferencia por construcciones rioplatenses/latinoamericanas suaves (no hay "vosotros", no hay leísmo marcado).

**Persona:** la app habla **al usuario en segunda persona singular ("tú" implícito o "tú" explícito según el caso, nunca "usted")**, con tono de amigo cercano que celebra contigo. Nunca paternalista, nunca clínico.

**Casing:** Frases en *sentence case*. Solo los nombres propios y la primera letra van en mayúscula. Los CTAs son verbos en infinitivo o imperativo corto.

**Sobre el uso de "tú" vs "yo":**
- **Sobre el progreso del usuario, usar "tu / tuyo":** *"Tu racha más larga"*, *"Tus metas"*, *"Tu día 30"*.
- **En diálogo de hitos, usar "yo" implícito desde la app pero en tono de aliento:** *"¡Lo lograste!"*, *"Llevas 30 días"*, *"Hoy es el día 1. Cada día cuenta."*
- **En empty states, usar invitación abierta:** *"Crea tu primer contador"*, *"Empieza por una meta pequeña"*.

**Hitos celebrados:** 1, 7, 30, 100, 365, 1000 días. Cada hito tiene un mensaje propio:

| Día | Mensaje |
|---|---|
| 1 | *"Día 1. El más difícil ya empezó."* |
| 7 | *"Una semana completa. Esto ya es un hábito que empieza."* |
| 30 | *"30 días. Pasaste el mes."* |
| 100 | *"Cien días. Algo cambió en ti."* |
| 365 | *"Un año entero. Esto ya no es una meta, es quién eres."* |

**Emoji:** **No.** El sistema no usa emoji en UI ni notificaciones. Reemplazamos cualquier necesidad emocional con tipografía, color, y el anillo de progreso. Se permite excepcionalmente en notificaciones push grandes (1 emoji máx, opcional).

**Caracteres especiales:** `·` para separar metadatos (`Día 30 · Iniciado el 5 mar`). Comillas latinas `«»` no se usan; usamos rectas `" "`. Apóstrofo redondeado.

**Números grandes:** sin separador de miles hasta 9999 (`365`, `1024`). Cuatro dígitos y más con espacio fino o punto (`1.024`).

**Errores y reinicios:** lenguaje sin culpa. Nunca *"fallaste"*. En su lugar: *"Reiniciar contador"* / *"Empezamos de nuevo. El día 1 también cuenta."*

---

## Visual foundations

### Paleta
Tres familias, todas tibias:

- **Teal profundo (`#0F5F6E`)** — color de marca, transmite confianza calmada (océano + bosque). Aparece en headers, CTAs primarios, anillo de racha activa.
- **Crema arena (`#FBF6EE`)** — fondo de toda la app. Cálido, no blanco puro. Reduce fatiga visual.
- **Salvia (`#7FA88A`)** y **terracota (`#D9876A`)** — acentos secundarios: salvia para "racha en crecimiento", terracota para hitos celebrados.

Sin gradientes vibrantes. Si hay gradiente, es de teal a teal-claro (`#0F5F6E → #2A8597`), suavísimo, sólo en anillos de progreso o en el header de "hito alcanzado".

### Tipografía
- **Outfit** para display y números grandes (el número del día es el héroe de la app — tabular, peso 600).
- **Plus Jakarta Sans** para body, labels y todo lo demás.

Ambas son sans humanistas geométricas con letterforms muy redondeados. Excelente cobertura de tildes/eñes/¿¡ en español.

### Forma y radios
- Cards y contenedores: **24px** (el "squircle" característico).
- Botones: **999px** (píldora completa).
- Inputs y chips: **16px**.
- Sheets/modales: **32px** arriba, 0 abajo.
- **Nada está a 0px de radio** salvo elementos full-bleed.

### Espaciado
Escala de 4: `4, 8, 12, 16, 20, 24, 32, 40, 48, 64`. Padding estándar de pantalla: 20px lateral. Gap vertical entre cards: 12px.

### Fondos
- Fondo base: crema arena lisa, sin patrones.
- Fondo de "logro/hito": gradiente sutil de teal apagado, con un **círculo orgánico** desenfocado al 30% de opacidad en una esquina (forma blob, no degradado lineal).
- Sin imágenes fotográficas en chrome. Si en el futuro hay fotos, deben ser desaturadas, cálidas, sin personas.

### Sombras
Sistema de elevación cálido (sombras teñidas de teal, no negras puras):
- `shadow-sm`: `0 1px 2px rgba(15, 95, 110, 0.06)`
- `shadow-md`: `0 4px 12px rgba(15, 95, 110, 0.08)`
- `shadow-lg`: `0 12px 28px rgba(15, 95, 110, 0.12)`

### Bordes
- `1px solid var(--border)` donde haga falta separar — pero preferimos sombra y fondo elevado a borde.
- En cards sobre crema: borde `#EFE6D6` muy sutil + sombra `sm`.

### Estados interactivos
- **Hover (irrelevante en móvil pero útil para previews web):** opacidad 0.9 o un tinte 4% más oscuro.
- **Press / active:** `scale(0.97)` + un instante de sombra reducida. Es la marca de calidez del sistema — los botones "se asientan" al presionarlos.
- **Disabled:** opacidad 0.4, sin cambio de color.
- **Focus (accesibilidad):** anillo teal 2px con offset 2px.

### Animación
- **Easing dominante:** `cubic-bezier(0.32, 0.72, 0, 1)` (curva tipo iOS spring suave). Toda transición usa esto.
- **Duraciones:** 180ms para micro (botón, chip), 320ms para sheets/transiciones de pantalla, 600ms para celebración de hito (con un pequeño "bounce" del número).
- **Fades** sí, sutiles. **Bounces** sólo en momentos de logro (hito alcanzado).
- El anillo de progreso anima al cargar la pantalla del contador (320ms, easing arriba).

### Transparencia y blur
- Bottom sheets: backdrop `rgba(15, 95, 110, 0.25)` con `backdrop-filter: blur(8px)`.
- Headers fijos al hacer scroll: fondo crema con `backdrop-filter: blur(12px)` y 92% de opacidad.

### Cards
La pieza más reconocible del sistema:
- Fondo blanco puro `#FFFFFF` sobre el crema base (contraste sutil).
- Radio 24px.
- Sombra `shadow-md`.
- Sin borde (la sombra basta).
- Padding interno: 20px.
- El **número del día** dentro de la card es enorme (56-72px), peso 600, color teal profundo. Es el héroe.

### Reglas de layout
- 20px de padding lateral siempre.
- El número de la racha siempre alineado al centro horizontalmente.
- Listados de contadores: una card por fila (no grid en móvil), 12px gap.
- Bottom navigation con 3 ítems: **Contadores · Estadísticas · Ajustes**.

---

## Iconography

**Sistema:** [Lucide](https://lucide.dev/) (variante rounded/default, stroke 2, no fill).

¿Por qué? Lucide tiene stroke-linecap `round` y stroke-linejoin `round` por defecto — encaja perfecto con el ADN curvado del sistema. Fontawesome y Material Symbols se evaluaron y se descartaron (demasiado afilados o demasiado neutros respectivamente).

**Uso:**
- Tamaño base: 24×24.
- Stroke 2px (default).
- Color: hereda `currentColor` — usar `--ink` para neutral, `--brand` para activo.
- Cargado desde CDN: `https://unpkg.com/lucide@latest/dist/umd/lucide.js`.

**Conjunto utilizado en la app:**
`flame` (racha), `plus` (crear), `pencil` (editar), `rotate-ccw` (reiniciar), `trash-2` (eliminar), `bell` (notificaciones), `bar-chart-3` (estadísticas), `settings`, `chevron-right`, `chevron-left`, `check`, `x`, `calendar`, `target` (meta), `trophy` (hito), `clock`.

**Emoji:** no se usa en UI. Las notificaciones push pueden incluir 1 emoji (opcional) sólo en hitos grandes (30, 100, 365 días).

**Logo / marca:** placeholder construido con un anillo circular + número "30". Cuando el cliente provea el logo final, reemplazar `assets/logo.svg`.

---

## Index — qué hay en este folder

```
/
├── README.md                    ← estás aquí
├── SKILL.md                     ← invocación como Claude Skill
├── colors_and_type.css          ← tokens CSS (colores, fuentes, espaciado, sombras)
├── assets/
│   ├── logo.svg                 ← placeholder, reemplazar
│   └── mark.svg                 ← isotipo (anillo)
├── fonts/                       ← cargadas desde Google Fonts (no hay .ttf locales aún)
├── preview/                     ← cards del Design System tab
│   ├── colors-*.html
│   ├── type-*.html
│   ├── spacing-*.html
│   └── components-*.html
└── ui_kits/
    └── android_app/
        ├── README.md
        ├── index.html           ← prototipo navegable con state picker (10 estados)
        ├── widgets.html         ← Android widgets + notificaciones push
        ├── components.jsx       ← Phone, Button, Card, Ring, Chip, Sheet, BottomNav, FAB, Input
        ├── screen-onboarding.jsx
        ├── screen-empty.jsx
        ├── screen-home.jsx
        ├── screen-detail.jsx
        ├── screen-history.jsx
        ├── screen-create.jsx
        ├── screen-edit.jsx
        ├── screen-milestone.jsx ← celebración a pantalla completa
        ├── screen-stats.jsx
        ├── screen-settings.jsx
        └── app.jsx              ← root + state machine de navegación
```
