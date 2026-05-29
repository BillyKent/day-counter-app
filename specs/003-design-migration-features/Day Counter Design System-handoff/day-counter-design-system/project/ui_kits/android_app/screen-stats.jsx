// ─── Stats / Estadísticas screen ─────────────────────────────────────────────
function StatsScreen({ counters }) {
  // Effective days exclude every paused interval, so these never disagree with
  // the numbers shown on the Contadores tab.
  const activeCounters = counters.filter(c => !isPaused(c));
  const pausedCounters = counters.filter(c => isPaused(c));

  const totalDays = counters.reduce((s, c) => s + counterDays(c), 0);
  const longest = counters.reduce((m, c) => Math.max(m, counterDays(c)), 0);
  const milestones = counters.reduce((s, c) => {
    const d = counterDays(c);
    return s + (d >= 7 ? 1 : 0) + (d >= 30 ? 1 : 0) + (d >= 100 ? 1 : 0);
  }, 0);
  const active = activeCounters.length;
  // Promedio de racha calculado solo sobre días efectivos (sin pausas).
  const avgStreak = counters.length ? Math.round(totalDays / counters.length) : 0;

  // Métricas de pausa
  const pausedNow = pausedCounters.length;
  const totalPaused = counters.reduce((s, c) => s + totalPausedDays(c), 0);
  const totalPauses = counters.reduce((s, c) => s + pauseCount(c), 0);

  // Días cumplidos esta semana (de un total de 4 contadores activos por día)
  const weekData = [
    { day: 'L', value: 3 },
    { day: 'M', value: 4 },
    { day: 'X', value: 4 },
    { day: 'J', value: 2 },
    { day: 'V', value: 4 },
    { day: 'S', value: 3 },
    { day: 'D', value: 4 },
  ];
  const weekMax = Math.max(...weekData.map(d => d.value), 1);
  const weekTotal = weekData.reduce((s, d) => s + d.value, 0);

  return (
    <>
      <TopBar large title="Estadísticas"/>
      <div style={{ padding: '0 20px 20px', flex: 1, overflowY: 'auto' }}>

        {/* Big stat — total days */}
        <Card style={{ marginBottom: 12 }}>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
            color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
            marginBottom: 6,
          }}>Total acumulado</div>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 64, fontWeight: 600,
            color: 'var(--brand)', letterSpacing: '-0.04em', lineHeight: 1,
            fontVariantNumeric: 'tabular-nums',
          }}>{totalDays}
            <span style={{ fontSize: 18, color: 'var(--ink-muted)', fontWeight: 500, marginLeft: 8 }}>días</span>
          </div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 13, color: 'var(--ink-2)', marginTop: 8,
          }}>Días efectivos sumando todas tus rachas. El tiempo en pausa no se cuenta.</div>
        </Card>

        {/* Grid stats */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 12 }}>
          <Card padding={16}>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
              color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
            }}>Mejor racha</div>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 32, fontWeight: 600,
              color: 'var(--success)', letterSpacing: '-0.03em', lineHeight: 1.1, marginTop: 4,
              fontVariantNumeric: 'tabular-nums',
            }}>{longest}<span style={{ fontSize: 14, color: 'var(--ink-muted)', fontWeight: 500, marginLeft: 4 }}>días</span></div>
          </Card>
          <Card padding={16}>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
              color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
            }}>Hitos</div>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 32, fontWeight: 600,
              color: 'var(--milestone)', letterSpacing: '-0.03em', lineHeight: 1.1, marginTop: 4,
              fontVariantNumeric: 'tabular-nums',
            }}>{milestones}<span style={{ fontSize: 14, color: 'var(--ink-muted)', fontWeight: 500, marginLeft: 4 }}>alcanzados</span></div>
          </Card>
          <Card padding={16}>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
              color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
            }}>Contadores</div>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 32, fontWeight: 600,
              color: 'var(--ink)', letterSpacing: '-0.03em', lineHeight: 1.1, marginTop: 4,
              fontVariantNumeric: 'tabular-nums',
            }}>{active}<span style={{ fontSize: 14, color: 'var(--ink-muted)', fontWeight: 500, marginLeft: 4 }}>activos</span></div>
          </Card>
          <Card padding={16}>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
              color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
            }}>Racha media</div>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 32, fontWeight: 600,
              color: 'var(--ink)', letterSpacing: '-0.03em', lineHeight: 1.1, marginTop: 4,
              fontVariantNumeric: 'tabular-nums',
            }}>{avgStreak}<span style={{ fontSize: 14, color: 'var(--ink-muted)', fontWeight: 500, marginLeft: 4 }}>días</span></div>
          </Card>
        </div>

        {/* Pausas — métricas dedicadas */}
        <Card style={{ marginBottom: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 }}>
            <div style={{
              width: 32, height: 32, borderRadius: 10, background: 'var(--bg-sunken)',
              color: 'var(--ink-muted)', display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}><Icon name="pause" size={16}/></div>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 17, fontWeight: 600,
              color: 'var(--ink)', letterSpacing: '-0.01em',
            }}>Pausas</div>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            {[
              { v: pausedNow, l: 'en pausa ahora' },
              { v: totalPaused, l: 'días pausados' },
              { v: totalPauses, l: 'pausas totales' },
            ].map((m, i) => (
              <div key={i} style={{
                flex: 1, background: 'var(--bg-sunken)', borderRadius: 16, padding: '14px 12px',
                display: 'flex', flexDirection: 'column', gap: 2, alignItems: 'flex-start',
              }}>
                <div style={{
                  fontFamily: 'var(--font-display)', fontSize: 28, fontWeight: 600,
                  color: 'var(--ink)', lineHeight: 1, letterSpacing: '-0.03em',
                  fontVariantNumeric: 'tabular-nums',
                }}>{m.v}</div>
                <div style={{
                  fontFamily: 'var(--font-body)', fontSize: 11, color: 'var(--ink-muted)',
                  fontWeight: 500, lineHeight: 1.3,
                }}>{m.l}</div>
              </div>
            ))}
          </div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 12, color: 'var(--ink-muted)',
            lineHeight: 1.5, marginTop: 14,
          }}>El tiempo en pausa nunca resta días a tus rachas — simplemente no suma.</div>
        </Card>

        {/* Weekly bars */}
        <Card style={{ marginBottom: 12 }}>
          <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 17, fontWeight: 600,
              color: 'var(--ink)', letterSpacing: '-0.01em',
            }}>Esta semana</div>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 13, fontWeight: 600, color: 'var(--success)',
            }}>{weekTotal} días cumplidos</div>
          </div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 13, color: 'var(--ink-muted)',
            marginBottom: 18,
          }}>Días cumplidos por contador</div>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 10, height: 132 }}>
            {weekData.map((d, i) => {
              const isToday = i === weekData.length - 1;
              const barH = 24 + Math.round((d.value / weekMax) * 84); // 24–108px
              return (
                <div key={i} style={{
                  flex: 1, height: '100%', display: 'flex', flexDirection: 'column',
                  alignItems: 'center', justifyContent: 'flex-end', gap: 6,
                }}>
                  <div style={{
                    fontFamily: 'var(--font-display)', fontSize: 13, fontWeight: 600,
                    color: isToday ? 'var(--brand)' : 'var(--ink-2)',
                    fontVariantNumeric: 'tabular-nums',
                  }}>{d.value}</div>
                  <div style={{
                    width: '100%', height: barH, borderRadius: 10,
                    background: isToday ? 'var(--brand)' : 'var(--brand-soft)',
                    transition: 'height 320ms var(--ease-out-soft)',
                  }}/>
                  <div style={{
                    fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
                    color: isToday ? 'var(--brand)' : 'var(--ink-muted)',
                  }}>{d.day}</div>
                </div>
              );
            })}
          </div>
        </Card>

        <div style={{ height: 80 }}/>
      </div>
    </>
  );
}

window.StatsScreen = StatsScreen;
