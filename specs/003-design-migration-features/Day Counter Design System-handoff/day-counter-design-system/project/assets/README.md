# Assets

Placeholder visual assets for Day Counter. **All of these should be replaced when the client provides final brand artwork.**

| File | Purpose | Status |
|---|---|---|
| `logo.svg` | Horizontal lockup (mark + wordmark) | placeholder |
| `mark.svg` | Standalone isotype (the ring) | placeholder |

## Icon set

Icons are loaded from **[Lucide](https://lucide.dev/)** via CDN, not stored here:

```html
<script src="https://unpkg.com/lucide@latest/dist/umd/lucide.js"></script>
<script>lucide.createIcons();</script>
```

`<i data-lucide="flame"></i>` then renders an SVG inline. We do this to avoid checking in 1000+ icon files. If the project moves offline, run `npm i lucide-static` and copy the SVGs we use into `assets/icons/`.

## Icons in use

`flame` · `plus` · `pencil` · `rotate-ccw` · `trash-2` · `bell` · `bar-chart-3` · `settings` · `chevron-right` · `chevron-left` · `check` · `x` · `calendar` · `target` · `trophy` · `clock` · `more-horizontal` · `home`
