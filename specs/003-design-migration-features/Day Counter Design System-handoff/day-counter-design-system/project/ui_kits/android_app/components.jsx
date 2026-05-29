// ============================================================================
// Day Counter — UI Kit components
// All components are visual recreations only; no real persistence.
// ============================================================================

// ─── Icon — inline Lucide-style SVG ─────────────────────────────────────────
const ICON_PATHS = {
  plus: <><path d="M12 5v14M5 12h14"/></>,
  pencil: <path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5z"/>,
  rotate: <><path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5"/></>,
  trash: <><path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></>,
  bell: <><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></>,
  chart: <><path d="M3 3v18h18"/><path d="M7 14v4M12 9v9M17 13v5"/></>,
  settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.6 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></>,
  chevronRight: <path d="m9 18 6-6-6-6"/>,
  chevronLeft: <path d="m15 18-6-6 6-6"/>,
  check: <path d="M20 6 9 17l-5-5"/>,
  x: <><path d="M18 6 6 18M6 6l12 12"/></>,
  calendar: <><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></>,
  target: <><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></>,
  trophy: <><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></>,
  clock: <><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></>,
  pause: <><rect x="6" y="5" width="4" height="14" rx="1"/><rect x="14" y="5" width="4" height="14" rx="1"/></>,
  play: <><polygon points="6 4 20 12 6 20 6 4"/></>,
  home: <><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2h-4v-7H9v7H5a2 2 0 0 1-2-2z"/></>,
  more: <><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></>,
  flame: <><path d="M8.5 14.5A2.5 2.5 0 0 0 11 17h2a2.5 2.5 0 0 0 2.5-2.5c0-2.5-3.5-3-3.5-5.5a1.5 1.5 0 1 1 3 0v.5"/><path d="M12 2C9 6 6 8 6 14a6 6 0 0 0 12 0c0-3-2-5-2-7"/></>,
  globe: <><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></>,
  moon: <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>,
  database: <><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></>,
};

function Icon({ name, size = 24, color = 'currentColor', strokeWidth = 2, style }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
      fill="none" stroke={color} strokeWidth={strokeWidth}
      strokeLinecap="round" strokeLinejoin="round"
      style={{ flexShrink: 0, ...style }}>
      {ICON_PATHS[name] || null}
    </svg>
  );
}

// ─── Date helpers + paused-aware streak math ────────────────────────────────
// The prototype's "today". A real app would use the device clock; here we fix
// it so the seeded pause periods compute to stable, demonstrable values.
const TODAY_ISO = '2026-05-29';
const _MONTHS = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];

// Whole-day difference between two ISO dates (UTC math → no DST drift).
function dayDiff(aIso, bIso) {
  const [ay, am, ad] = aIso.split('-').map(Number);
  const [by, bm, bd] = bIso.split('-').map(Number);
  return Math.round((Date.UTC(by, bm - 1, bd) - Date.UTC(ay, am - 1, ad)) / 86400000);
}

// Sum of all COMPLETED pause periods, in days.
function pausedDurationDays(c) {
  return (c.pausePeriods || []).reduce((s, p) => s + Math.max(0, dayDiff(p.start, p.end)), 0);
}

// Effective streak: elapsed time MINUS every paused interval. While paused,
// the clock is frozen at `pausedSince`, so the number stops growing.
function counterDays(c) {
  if (c.startedAt && !c.startDate) return c.days || 0;      // legacy fallback
  const endIso = (c.status === 'paused' && c.pausedSince) ? c.pausedSince : TODAY_ISO;
  const elapsed = dayDiff(c.startDate, endIso);
  return Math.max(1, elapsed - pausedDurationDays(c));
}

// Total time the counter has spent paused (completed pauses + ongoing pause).
function totalPausedDays(c) {
  let t = pausedDurationDays(c);
  if (c.status === 'paused' && c.pausedSince) t += Math.max(0, dayDiff(c.pausedSince, TODAY_ISO));
  return t;
}

function pauseCount(c) {
  return (c.pausePeriods || []).length + (c.status === 'paused' ? 1 : 0);
}

function isPaused(c) { return c.status === 'paused'; }

function fmtStart(c) {
  if (c.startedAt) return c.startedAt;
  const [, m, d] = c.startDate.split('-').map(Number);
  return `${d} ${_MONTHS[m - 1]}`;
}

function plural(n, sing, plur) { return `${n} ${n === 1 ? sing : plur}`; }

// ─── Phone — Day Counter–branded device frame ───────────────────────────────
function Phone({ children, width = 392, height = 820 }) {
  return (
    <div style={{
      width, height, borderRadius: 44, overflow: 'hidden',
      background: 'var(--bg)',
      border: '10px solid #1B2A33',
      boxShadow: '0 30px 80px rgba(15, 95, 110, 0.22), 0 8px 16px rgba(15,95,110,0.10)',
      display: 'flex', flexDirection: 'column', boxSizing: 'border-box',
      position: 'relative',
    }}>
      {/* status bar */}
      <div style={{
        height: 38, padding: '12px 22px 0',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        fontFamily: 'var(--font-body)', fontSize: 13, fontWeight: 600, color: 'var(--ink)',
        flexShrink: 0,
      }}>
        <span>9:30</span>
        <div style={{
          position: 'absolute', left: '50%', top: 10, transform: 'translateX(-50%)',
          width: 18, height: 18, borderRadius: '50%', background: '#1B2A33',
        }} />
        <div style={{ display: 'flex', gap: 5, alignItems: 'center' }}>
          {/* signal */}
          <svg width="14" height="14" viewBox="0 0 16 16"><path d="M1 13h2v1H1zM5 11h2v3H5zM9 8h2v6H9zM13 4h2v10h-2z" fill="currentColor"/></svg>
          {/* battery */}
          <svg width="20" height="14" viewBox="0 0 22 12">
            <rect x="0.5" y="0.5" width="19" height="11" rx="2.5" fill="none" stroke="currentColor"/>
            <rect x="2" y="2" width="14" height="8" rx="1" fill="currentColor"/>
            <rect x="20" y="4" width="1.5" height="4" rx="0.5" fill="currentColor"/>
          </svg>
        </div>
      </div>
      {/* content */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
        {children}
      </div>
      {/* gesture pill */}
      <div style={{ height: 22, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
        <div style={{ width: 108, height: 4, borderRadius: 2, background: '#1B2A33', opacity: 0.5 }} />
      </div>
    </div>
  );
}

// ─── TopBar — header inside the phone ────────────────────────────────────────
function TopBar({ title, leading, trailing, large = false }) {
  return (
    <div style={{ padding: large ? '8px 20px 4px' : '12px 20px', flexShrink: 0 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', minHeight: 44 }}>
        <div style={{ width: 44, display: 'flex', alignItems: 'center' }}>{leading}</div>
        {!large && (
          <div style={{
            fontFamily: 'var(--font-display)', fontWeight: 600, fontSize: 17, color: 'var(--ink)',
            letterSpacing: '-0.01em',
          }}>{title}</div>
        )}
        <div style={{ width: 44, display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>{trailing}</div>
      </div>
      {large && (
        <div style={{
          fontFamily: 'var(--font-display)', fontWeight: 600, fontSize: 28, color: 'var(--ink)',
          letterSpacing: '-0.02em', padding: '12px 0 6px',
        }}>{title}</div>
      )}
    </div>
  );
}

// ─── IconButton — round button for top bar / inline actions ─────────────────
function IconButton({ name, onClick, color = 'var(--ink)', bg = 'transparent', size = 44 }) {
  return (
    <button onClick={onClick} style={{
      width: size, height: size, borderRadius: 999, border: 'none', background: bg,
      display: 'flex', alignItems: 'center', justifyContent: 'center', color, cursor: 'pointer',
      transition: 'transform 180ms var(--ease-out-soft), background 180ms var(--ease-out-soft)',
    }}
      onMouseDown={(e) => e.currentTarget.style.transform = 'scale(0.92)'}
      onMouseUp={(e) => e.currentTarget.style.transform = 'scale(1)'}
      onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}>
      <Icon name={name} size={22} />
    </button>
  );
}

// ─── Button — pill ───────────────────────────────────────────────────────────
function Button({ children, variant = 'primary', onClick, full = false, leading, size = 'md' }) {
  const variants = {
    primary:   { bg: 'var(--brand)', color: '#fff', shadow: 'none' },
    secondary: { bg: '#fff', color: 'var(--brand)', shadow: 'inset 0 0 0 1.5px var(--brand)' },
    soft:      { bg: 'var(--brand-soft)', color: 'var(--brand)', shadow: 'none' },
    ghost:     { bg: 'transparent', color: 'var(--brand)', shadow: 'none' },
    danger:    { bg: 'var(--danger)', color: '#fff', shadow: 'none' },
    dangerSoft:{ bg: 'var(--danger-soft)', color: 'var(--danger)', shadow: 'none' },
  };
  const sizes = {
    sm: { px: 16, py: 9,  fs: 13 },
    md: { px: 22, py: 13, fs: 15 },
    lg: { px: 28, py: 17, fs: 17 },
  };
  const v = variants[variant], s = sizes[size];
  return (
    <button onClick={onClick} style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8,
      fontFamily: 'var(--font-body)', fontWeight: 600, fontSize: s.fs,
      padding: `${s.py}px ${s.px}px`, borderRadius: 999, border: 'none',
      background: v.bg, color: v.color, boxShadow: v.shadow, cursor: 'pointer',
      width: full ? '100%' : 'auto',
      transition: 'transform 180ms var(--ease-out-soft)',
    }}
      onMouseDown={(e) => e.currentTarget.style.transform = 'scale(0.97)'}
      onMouseUp={(e) => e.currentTarget.style.transform = 'scale(1)'}
      onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}>
      {leading && <Icon name={leading} size={18} />}
      {children}
    </button>
  );
}

// ─── Ring — circular progress ────────────────────────────────────────────────
function Ring({ value, goal, size = 88, stroke = 8, milestone = false, paused = false }) {
  const r = (size - stroke) / 2;
  const C = 2 * Math.PI * r;
  const pct = Math.max(0, Math.min(1, value / goal));
  const offset = C * (1 - pct);
  const color = paused ? 'var(--ink-subtle)' : milestone ? 'var(--milestone)' : 'var(--brand)';
  const trackColor = paused ? 'var(--bg-sunken)' : milestone ? 'var(--milestone-soft)' : 'var(--bg-sunken)';
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={trackColor} strokeWidth={stroke}/>
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={color} strokeWidth={stroke}
        strokeLinecap="round" strokeDasharray={paused ? '2 7' : C} strokeDashoffset={paused ? 0 : offset}
        transform={`rotate(-90 ${size/2} ${size/2})`}
        style={{ transition: 'stroke-dashoffset 600ms var(--ease-out-soft)' }}/>
    </svg>
  );
}

// ─── Chip ────────────────────────────────────────────────────────────────────
function Chip({ children, active = false, onClick, variant = 'default' }) {
  const variants = {
    default:   active ? { bg: 'var(--brand)', color: '#fff' } : { bg: 'var(--bg-sunken)', color: 'var(--ink-2)' },
    brand:     { bg: 'var(--brand-soft)', color: 'var(--brand)' },
    success:   { bg: 'var(--success-soft)', color: 'var(--success)' },
    milestone: { bg: 'var(--milestone-soft)', color: 'var(--milestone)' },
    paused:    { bg: 'var(--bg-sunken)', color: 'var(--ink-muted)' },
  };
  const v = variants[variant];
  return (
    <button onClick={onClick} style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      fontFamily: 'var(--font-body)', fontSize: 13, fontWeight: 600,
      padding: '8px 14px', borderRadius: 999, border: 'none',
      background: v.bg, color: v.color, cursor: 'pointer',
      whiteSpace: 'nowrap',
    }}>{children}</button>
  );
}

// ─── Card ────────────────────────────────────────────────────────────────────
function Card({ children, onClick, padding = 20, style }) {
  return (
    <div onClick={onClick} style={{
      background: 'var(--bg-elevated)', borderRadius: 24, padding,
      boxShadow: 'var(--shadow-md)', cursor: onClick ? 'pointer' : 'default',
      transition: 'transform 180ms var(--ease-out-soft)',
      ...style,
    }}
      onMouseDown={(e) => { if (onClick) e.currentTarget.style.transform = 'scale(0.985)'; }}
      onMouseUp={(e) => { if (onClick) e.currentTarget.style.transform = 'scale(1)'; }}
      onMouseLeave={(e) => { if (onClick) e.currentTarget.style.transform = 'scale(1)'; }}>
      {children}
    </div>
  );
}

// ─── Bottom Sheet ────────────────────────────────────────────────────────────
function Sheet({ open, onClose, children, title }) {
  if (!open) return null;
  return (
    <div onClick={onClose} style={{
      position: 'absolute', inset: 0, zIndex: 50,
      background: 'rgba(15, 95, 110, 0.25)', backdropFilter: 'blur(8px)',
      display: 'flex', alignItems: 'flex-end',
      animation: 'fadeIn 240ms var(--ease-out-soft)',
    }}>
      <div onClick={(e) => e.stopPropagation()} style={{
        background: 'var(--bg)', width: '100%',
        borderTopLeftRadius: 32, borderTopRightRadius: 32,
        padding: '12px 20px 28px',
        boxShadow: 'var(--shadow-xl)',
        animation: 'slideUp 320ms var(--ease-out-soft)',
        maxHeight: '90%', overflowY: 'auto',
      }}>
        <div style={{
          width: 40, height: 4, borderRadius: 2, background: 'var(--border-strong)',
          margin: '4px auto 16px',
        }}/>
        {title && (
          <div style={{
            fontFamily: 'var(--font-display)', fontWeight: 600, fontSize: 22,
            color: 'var(--ink)', letterSpacing: '-0.02em', marginBottom: 16,
          }}>{title}</div>
        )}
        {children}
      </div>
    </div>
  );
}

// ─── BottomNav ───────────────────────────────────────────────────────────────
function BottomNav({ active, onChange }) {
  const items = [
    { id: 'home', label: 'Contadores', icon: 'home' },
    { id: 'stats', label: 'Estadísticas', icon: 'chart' },
    { id: 'settings', label: 'Ajustes', icon: 'settings' },
  ];
  return (
    <div style={{
      display: 'flex', justifyContent: 'space-around', alignItems: 'center',
      padding: '8px 12px 6px',
      background: 'rgba(251, 246, 238, 0.92)',
      backdropFilter: 'blur(12px)',
      borderTop: '1px solid var(--border-soft)',
      flexShrink: 0,
    }}>
      {items.map(it => {
        const isActive = it.id === active;
        return (
          <button key={it.id} onClick={() => onChange(it.id)} style={{
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
            border: 'none', background: 'transparent', cursor: 'pointer', padding: '6px 18px',
            color: isActive ? 'var(--brand)' : 'var(--ink-muted)',
            fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
            transition: 'color 180ms var(--ease-out-soft)',
          }}>
            <div style={{
              padding: '4px 14px', borderRadius: 999,
              background: isActive ? 'var(--brand-soft)' : 'transparent',
              transition: 'background 180ms var(--ease-out-soft)',
            }}>
              <Icon name={it.icon} size={22}/>
            </div>
            <span>{it.label}</span>
          </button>
        );
      })}
    </div>
  );
}

// ─── FAB ─────────────────────────────────────────────────────────────────────
function FAB({ onClick, icon = 'plus' }) {
  return (
    <button onClick={onClick} style={{
      position: 'absolute', right: 20, bottom: 96,
      width: 60, height: 60, borderRadius: 22,
      background: 'var(--brand)', color: '#fff', border: 'none',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      boxShadow: 'var(--shadow-lg)', cursor: 'pointer',
      transition: 'transform 180ms var(--ease-out-soft)',
    }}
      onMouseDown={(e) => e.currentTarget.style.transform = 'scale(0.94)'}
      onMouseUp={(e) => e.currentTarget.style.transform = 'scale(1)'}
      onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}>
      <Icon name={icon} size={26}/>
    </button>
  );
}

// ─── Input ───────────────────────────────────────────────────────────────────
function Input({ label, value, onChange, placeholder, leading, type = 'text' }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      {label && (
        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 12, fontWeight: 600,
          color: 'var(--ink-muted)', letterSpacing: '0.04em', textTransform: 'uppercase',
        }}>{label}</div>
      )}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 10,
        padding: '14px 16px', borderRadius: 16,
        background: '#fff', border: '1px solid var(--border)',
      }}>
        {leading && <Icon name={leading} size={18} color="var(--ink-muted)"/>}
        <input value={value || ''} onChange={(e) => onChange?.(e.target.value)}
          placeholder={placeholder} type={type} style={{
            flex: 1, border: 'none', outline: 'none', background: 'transparent',
            fontFamily: 'var(--font-body)', fontSize: 15, color: 'var(--ink)',
          }}/>
      </div>
    </div>
  );
}

// ─── StreakCard — the canonical home-row card ────────────────────────────────
function StreakCard({ counter, onClick }) {
  const days = counterDays(counter);
  const paused = isPaused(counter);
  const reached = !paused && counter.milestone && days >= counter.milestone;
  const numberColor = paused ? 'var(--ink-subtle)' : reached ? 'var(--milestone)' : 'var(--brand)';
  return (
    <Card onClick={onClick} style={paused ? { background: 'var(--bg-sunken)' } : undefined}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 18 }}>
        <div style={{ position: 'relative', width: 88, height: 88, flexShrink: 0 }}>
          <Ring value={days} goal={counter.goal || days * 1.5} milestone={reached} paused={paused}/>
          <div style={{
            position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column',
            alignItems: 'center', justifyContent: 'center',
            fontFamily: 'var(--font-display)', fontWeight: 600,
            color: numberColor,
            letterSpacing: '-0.03em',
          }}>
            {paused ? (
              <Icon name="pause" size={24} color="var(--ink-subtle)"/>
            ) : (
              <div style={{ fontSize: 28, lineHeight: 1, fontVariantNumeric: 'tabular-nums' }}>{days}</div>
            )}
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 9, fontWeight: 600,
              letterSpacing: '0.08em', textTransform: 'uppercase',
              color: 'var(--ink-muted)', marginTop: paused ? 4 : 2,
            }}>{paused ? `${days} días` : 'días'}</div>
          </div>
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 19, fontWeight: 600,
            color: paused ? 'var(--ink-2)' : 'var(--ink)', letterSpacing: '-0.01em', marginBottom: 2,
            overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          }}>{counter.name}</div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 13, color: 'var(--ink-muted)',
            marginBottom: 10,
          }}>{paused ? `En pausa · ${plural(totalPausedDays(counter), 'día', 'días')}` : `Iniciado el ${fmtStart(counter)}`}</div>
          <div style={{ display: 'flex', gap: 6 }}>
            {paused
              ? <Chip variant="paused"><Icon name="pause" size={12}/> Pausado</Chip>
              : <>
                  {counter.category && <Chip variant="brand">{counter.category}</Chip>}
                  {reached && <Chip variant="milestone">Hito alcanzado</Chip>}
                </>}
          </div>
        </div>
      </div>
    </Card>
  );
}

// ─── Export to window ────────────────────────────────────────────────────────
Object.assign(window, {
  Icon, Phone, TopBar, IconButton, Button, Ring, Chip, Card, Sheet, BottomNav, FAB, Input, StreakCard,
  counterDays, totalPausedDays, pauseCount, isPaused, fmtStart, plural, dayDiff, TODAY_ISO,
});
