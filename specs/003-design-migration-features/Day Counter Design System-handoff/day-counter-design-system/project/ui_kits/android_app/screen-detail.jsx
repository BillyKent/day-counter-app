// ─── Detail screen — single counter ──────────────────────────────────────────
function DetailScreen({ counter, onBack, onReset, onEdit, onDelete, onHistory, onCelebrate, onTogglePause }) {
  const goal = counter.goal || 90;
  const days = counterDays(counter);
  const paused = isPaused(counter);
  const reached = !paused && counter.milestone && days >= counter.milestone;
  const pausedTotal = totalPausedDays(counter);

  // Recent milestone history (mock)
  const history = [
    { day: 1,  label: 'Día 1. El más difícil ya empezó.', date: fmtStart(counter) },
    { day: 7,  label: 'Una semana completa.', date: '12 mar' },
    { day: 30, label: '30 días. Pasaste el mes.', date: '4 abr', current: days >= 30 },
  ].filter(h => days >= h.day);

  const nextMilestone = [7, 30, 100, 365, 1000].find(m => m > days) || 1000;
  const daysToNext = nextMilestone - days;

  return (
    <>
      <TopBar
        leading={<IconButton name="chevronLeft" onClick={onBack}/>}
        title={counter.name}
        trailing={<IconButton name="calendar" onClick={onHistory}/>}
      />
      {/* Hero ring */}
      <div style={{ padding: '12px 20px 20px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ position: 'relative', width: 240, height: 240 }}>
          <Ring value={days} goal={goal} size={240} stroke={14} milestone={reached} paused={paused}/>
          <div style={{
            position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column',
            alignItems: 'center', justifyContent: 'center',
          }}>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
              color: paused ? 'var(--ink-subtle)' : 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
              marginBottom: 4,
            }}>{paused ? 'En pausa' : 'Llevas'}</div>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 88, fontWeight: 600,
              color: paused ? 'var(--ink-subtle)' : reached ? 'var(--milestone)' : 'var(--brand)',
              letterSpacing: '-0.04em', lineHeight: 1, fontVariantNumeric: 'tabular-nums',
            }}>{days}</div>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 14, color: 'var(--ink-muted)',
              marginTop: 6,
            }}>días consecutivos</div>
          </div>
        </div>
        <div style={{
          marginTop: 16, fontFamily: 'var(--font-body)', fontSize: 14,
          color: 'var(--ink-2)', textAlign: 'center', maxWidth: 290,
        }}>
          {paused
            ? <>El conteo está congelado. Reanuda cuando quieras y seguirás desde el día {days}.</>
            : <>Faltan <strong style={{ color: 'var(--brand)' }}>{daysToNext} días</strong> para tu próximo hito de {nextMilestone}.</>}
        </div>
      </div>

      {/* Paused banner */}
      {paused && (
        <div style={{ padding: '0 20px 16px' }}>
          <div style={{
            display: 'flex', alignItems: 'center', gap: 12, padding: '14px 16px',
            borderRadius: 16, background: 'var(--bg-sunken)',
          }}>
            <div style={{
              width: 38, height: 38, borderRadius: 12, background: 'var(--bg-elevated)',
              color: 'var(--ink-muted)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            }}><Icon name="pause" size={18}/></div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontFamily: 'var(--font-body)', fontSize: 14, fontWeight: 600, color: 'var(--ink)' }}>Contador en pausa</div>
              <div style={{ fontFamily: 'var(--font-body)', fontSize: 12, color: 'var(--ink-muted)', marginTop: 1 }}>
                Sin notificaciones · {plural(pausedTotal, 'día en pausa', 'días en pausa')}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Primary pause / resume button */}
      <div style={{ padding: '0 20px 12px' }}>
        {paused
          ? <Button variant="primary" leading="play" full onClick={onTogglePause}>Reanudar contador</Button>
          : <Button variant="soft" leading="pause" full onClick={onTogglePause}>Pausar contador</Button>}
      </div>

      {/* Secondary action row */}
      <div style={{ padding: '0 20px 20px', display: 'flex', gap: 8, justifyContent: 'center' }}>
        <Button variant="secondary" leading="pencil" size="sm" onClick={onEdit}>Editar</Button>
        <Button variant="soft" leading="rotate" size="sm" onClick={onReset}>Reiniciar</Button>
        <Button variant="dangerSoft" leading="trash" size="sm" onClick={onDelete}>Eliminar</Button>
      </div>

      {/* Demo CTA to preview celebration — visual kit only */}
      {reached && (
        <div style={{ padding: '0 20px 16px' }}>
          <button onClick={onCelebrate} style={{
            width: '100%', padding: '14px 16px', borderRadius: 16, border: 'none',
            background: 'var(--milestone-soft)', color: 'var(--milestone)',
            display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer',
            fontFamily: 'var(--font-body)', fontSize: 14, fontWeight: 600, textAlign: 'left',
          }}>
            <Icon name="trophy" size={20}/>
            <span style={{ flex: 1 }}>Ver pantalla de celebración</span>
            <Icon name="chevronRight" size={18}/>
          </button>
        </div>
      )}

      {/* History card */}
      <div style={{ padding: '0 20px 24px' }}>
        <div style={{
          fontFamily: 'var(--font-display)', fontSize: 18, fontWeight: 600,
          color: 'var(--ink)', marginBottom: 12, letterSpacing: '-0.01em',
        }}>Hitos alcanzados</div>
        <Card padding={4}>
          {history.map((h, i) => (
            <div key={h.day} style={{
              display: 'flex', alignItems: 'center', gap: 14,
              padding: '14px 16px',
              borderBottom: i < history.length - 1 ? '1px solid var(--border-soft)' : 'none',
            }}>
              <div style={{
                width: 40, height: 40, borderRadius: 999,
                background: h.current ? 'var(--milestone-soft)' : 'var(--brand-soft)',
                color: h.current ? 'var(--milestone)' : 'var(--brand)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                flexShrink: 0,
              }}>
                <Icon name="trophy" size={20}/>
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{
                  fontFamily: 'var(--font-body)', fontSize: 15, fontWeight: 600,
                  color: 'var(--ink)', lineHeight: 1.3,
                }}>Día {h.day}</div>
                <div style={{
                  fontFamily: 'var(--font-body)', fontSize: 13, color: 'var(--ink-muted)',
                  marginTop: 2,
                }}>{h.label}</div>
              </div>
              <div style={{
                fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--ink-subtle)',
                whiteSpace: 'nowrap',
              }}>{h.date}</div>
            </div>
          ))}
        </Card>
      </div>
      <div style={{ height: 80 }}/>
    </>
  );
}

window.DetailScreen = DetailScreen;
