---
name: day-counter-design
description: Use this skill to generate well-branded interfaces and assets for Day Counter, a Spanish-language Android app for tracking habit streaks (días consecutivos). Contains essential design guidelines, colors, type, fonts, assets, and UI kit components for prototyping.
user-invocable: true
---

Read the README.md file within this skill, and explore the other available files.

Day Counter is an **Android-first habit-streak app** with content written in **Spanish**. The brand brief is two constraints:
1. Colors that inspire **confianza y tranquilidad** (trust + tranquility) — never neon, never vibrant gradients.
2. **Curved shapes** for visual warmth — generous radii, pill buttons, circular progress rings.

Key files:
- `README.md` — full brand guide (content fundamentals, visual foundations, iconography).
- `colors_and_type.css` — design tokens (CSS custom properties).
- `ui_kits/android_app/` — JSX recreations of the core Android screens + an `index.html` clickable prototype.
- `preview/` — Design System cards for quick reference.

If creating visual artifacts (slides, mocks, throwaway prototypes, etc), copy assets out and create static HTML files for the user to view. If working on production code (Kotlin/Compose, XML themes), the tokens in `colors_and_type.css` translate directly to Material 3 theme attributes — copy hex values, radii, and the type pairing.

**Always write copy in Spanish.** Use sentence case, no emoji in UI, address the user with implicit "tú" (e.g. *"Tu racha"*, *"Crea un contador"*). Celebrate milestones at 1, 7, 30, 100, 365, 1000 days with the wording in the README.

If the user invokes this skill without any other guidance, ask them what they want to build or design, ask some questions, and act as an expert designer who outputs HTML artifacts _or_ production code, depending on the need.
