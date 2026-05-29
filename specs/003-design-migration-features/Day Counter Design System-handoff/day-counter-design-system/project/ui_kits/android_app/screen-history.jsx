// ─── History / Calendar view ────────────────────────────────────────────────
function HistoryScreen({ counter, onBack }) {
  const days = counterDays(counter);
  const paused = isPaused(counter);
  // Real month layout: May 2026 has 31 days, starts on Friday.
  // Grid is 6 rows × 7 cols = 42 cells. Leading blanks fill until day 1.
  const monthDays = 31;
  const startWeekday = 4;       // 0=Mon … 4=Fri (May 1, 2026 is a Friday)
  const cells = 42;
  // "Today" = May 28 (Thursday) → its index = startWeekday + 28 - 1 = 31
  const todayDay = 28;
  const todayIdx = startWeekday + todayDay - 1;
  const streakStart = todayIdx - (days - 1);

  const labels = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  const monthLabel = 'Mayo · 2026';

  const cellState = (i) => {
    if (i < startWeekday) return 'blank';
    const dayNum = i - startWeekday + 1;
    if (dayNum > monthDays) return 'blank';
    // Visible day cell — figure out its streak state.
    if (i > todayIdx) return 'future';
    if (i < streakStart) return 'past';
    if (i === todayIdx) return 'today';
    return 'streak';
  };
  const cellDay = (i) => i - startWeekday + 1;

  // Build a sparkline showing growth (mock)
  const sparkPoints = React.useMemo(() => {
    const n = 24;
    return Array.from({ length: n }, (_, i) => {
      const v = Math.min(days, Math.round((i / (n - 1)) * days));
      return v;
    });
  }, [days]);
  const maxV = Math.max(...sparkPoints, 1);
  const path = sparkPoints.map((v, i) => {
    const x = (i / (sparkPoints.length - 1)) * 100;
    const y = 40 - (v / maxV) * 36;
    return `${i === 0 ? 'M' : 'L'} ${x.toFixed(2)} ${y.toFixed(2)}`;
  }).join(' ');

  return (
    <>
      <TopBar
        leading={<IconButton name="chevronLeft" onClick={onBack}/>}
        title="Historial"
        trailing={<IconButton name="calendar"/>}
      />
      <div style={{ flex: 1, padding: '0 20px 20px', overflowY: 'auto' }}>

        {/* Header summary */}
        <Card style={{ marginBottom: 16 }}>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 12, fontWeight: 600,
            color: 'var(--ink-muted)', letterSpacing: '0.04em', textTransform: 'uppercase',
            marginBottom: 4,
          }}>{counter.name}</div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 56, fontWeight: 600,
              color: 'var(--brand)', letterSpacing: '-0.04em', lineHeight: 1,
              fontVariantNumeric: 'tabular-nums',
            }}>{days}</div>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 14, color: 'var(--ink-muted)', fontWeight: 500,
            }}>días consecutivos</div>
          </div>
          {/* Sparkline */}
          <svg viewBox="0 0 100 40" preserveAspectRatio="none" style={{ width: '100%', height: 48, marginTop: 12, overflow: 'visible' }}>
            <path d={`${path} L 100 40 L 0 40 Z`} fill="var(--brand-softer)"/>
            <path d={path} fill="none" stroke="var(--brand)" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" vectorEffect="non-scaling-stroke"/>
          </svg>
          <div style={{
            display: 'flex', justifyContent: 'space-between',
            fontFamily: 'var(--font-mono)', fontSize: 10, color: 'var(--ink-subtle)', marginTop: 4,
          }}>
            <span>{fmtStart(counter)}</span>
            <span>hoy</span>
          </div>
        </Card>

        {/* Calendar */}
        <Card style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
            <IconButton name="chevronLeft" size={32}/>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 17, fontWeight: 600,
              color: 'var(--ink)', letterSpacing: '-0.01em',
            }}>{monthLabel}</div>
            <IconButton name="chevronRight" size={32}/>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 6 }}>
            {labels.map(l => (
              <div key={l} style={{
                textAlign: 'center', fontFamily: 'var(--font-body)', fontSize: 11,
                fontWeight: 600, color: 'var(--ink-subtle)', letterSpacing: '0.04em',
                paddingBottom: 4,
              }}>{l}</div>
            ))}
            {Array.from({ length: cells }).map((_, i) => {
              const state = cellState(i);
              if (state === 'blank') return <div key={i} style={{ aspectRatio: '1' }}/>;
              const dayNum = cellDay(i);
              const styles = {
                future: { bg: 'transparent', fg: 'var(--ink-subtle)', border: '1px dashed var(--border)' },
                past:   { bg: 'transparent', fg: 'var(--ink-subtle)', border: 'none' },
                streak: { bg: 'var(--brand-soft)', fg: 'var(--brand)', border: 'none' },
                today:  { bg: 'var(--brand)', fg: '#fff', border: 'none' },
              }[state];
              return (
                <div key={i} style={{
                  aspectRatio: '1', borderRadius: 12,
                  background: styles.bg, color: styles.fg, border: styles.border,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontFamily: 'var(--font-body)', fontSize: 13,
                  fontWeight: state === 'today' ? 700 : 500,
                  fontVariantNumeric: 'tabular-nums',
                }}>{dayNum}</div>
              );
            })}
          </div>
          {/* Legend */}
          <div style={{ display: 'flex', gap: 16, marginTop: 14, justifyContent: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <div style={{ width: 12, height: 12, borderRadius: 4, background: 'var(--brand-soft)' }}/>
              <span style={{ fontFamily: 'var(--font-body)', fontSize: 11, color: 'var(--ink-muted)' }}>Día en racha</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <div style={{ width: 12, height: 12, borderRadius: 4, background: 'var(--brand)' }}/>
              <span style={{ fontFamily: 'var(--font-body)', fontSize: 11, color: 'var(--ink-muted)' }}>Hoy</span>
            </div>
          </div>
        </Card>

        {/* Past streaks */}
        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
          color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
          marginBottom: 8, padding: '0 4px',
        }}>Rachas anteriores</div>
        <Card padding={4}>
          {[
            { days: 14, ended: '14 feb', reason: 'Reiniciado' },
            { days: 3,  ended: '28 ene', reason: 'Reiniciado' },
          ].map((r, i, arr) => (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', gap: 14, padding: '14px 16px',
              borderBottom: i < arr.length - 1 ? '1px solid var(--border-soft)' : 'none',
            }}>
              <div style={{
                width: 44, height: 44, borderRadius: 14, background: 'var(--bg-sunken)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: 'var(--font-display)', fontSize: 18, fontWeight: 600, color: 'var(--ink-2)',
                fontVariantNumeric: 'tabular-nums', flexShrink: 0,
              }}>{r.days}</div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{
                  fontFamily: 'var(--font-body)', fontSize: 14, fontWeight: 600,
                  color: 'var(--ink)',
                }}>{r.days} días</div>
                <div style={{
                  fontFamily: 'var(--font-body)', fontSize: 12, color: 'var(--ink-muted)',
                }}>{r.reason} · {r.ended}</div>
              </div>
            </div>
          ))}
        </Card>
        <div style={{ height: 80 }}/>
      </div>
    </>
  );
}

window.HistoryScreen = HistoryScreen;
