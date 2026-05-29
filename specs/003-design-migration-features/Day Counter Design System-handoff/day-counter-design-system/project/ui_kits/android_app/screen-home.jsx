// ─── Home / Contadores screen ────────────────────────────────────────────────
function HomeScreen({ counters, onOpenCounter, onCreateNew, onOpenStats }) {
  const [filter, setFilter] = React.useState('all'); // all | active | paused

  // Totals reflect only effective (paused-excluded) days.
  const totalDays = counters.reduce((s, c) => s + counterDays(c), 0);
  const longest = counters.reduce((m, c) => Math.max(m, counterDays(c)), 0);

  const counts = {
    all: counters.length,
    active: counters.filter(c => !isPaused(c)).length,
    paused: counters.filter(c => isPaused(c)).length,
  };
  const FILTERS = [
    { id: 'all', label: 'Todos' },
    { id: 'active', label: 'Activos' },
    { id: 'paused', label: 'Pausados' },
  ];
  const visible = counters.filter(c =>
    filter === 'all' ? true : filter === 'paused' ? isPaused(c) : !isPaused(c)
  );

  return (
    <>
      <TopBar
        large
        title="Contadores"
      />
      <div style={{ padding: '0 20px', display: 'flex', gap: 10, marginBottom: 14 }}>
        <Card padding={14} style={{ flex: 1 }}>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
            color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
          }}>Total</div>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 28, fontWeight: 600,
            color: 'var(--ink)', letterSpacing: '-0.02em', lineHeight: 1.1, marginTop: 4,
            fontVariantNumeric: 'tabular-nums',
          }}>{totalDays}<span style={{ fontSize: 13, color: 'var(--ink-muted)', fontWeight: 500, marginLeft: 4 }}>días</span></div>
        </Card>
        <Card padding={14} style={{ flex: 1 }}>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
            color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
          }}>Mejor racha</div>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 28, fontWeight: 600,
            color: 'var(--success)', letterSpacing: '-0.02em', lineHeight: 1.1, marginTop: 4,
            fontVariantNumeric: 'tabular-nums',
          }}>{longest}<span style={{ fontSize: 13, color: 'var(--ink-muted)', fontWeight: 500, marginLeft: 4 }}>días</span></div>
        </Card>
      </div>

      {/* Filtros — chips horizontales que filtran la lista en vivo */}
      <div style={{ padding: '0 20px', display: 'flex', gap: 8, marginBottom: 12, overflowX: 'auto' }}>
        {FILTERS.map(f => (
          <Chip key={f.id} active={filter === f.id} onClick={() => setFilter(f.id)}>
            {f.label}
            <span style={{
              fontVariantNumeric: 'tabular-nums', opacity: 0.7,
              marginLeft: 2,
            }}>{counts[f.id]}</span>
          </Chip>
        ))}
      </div>

      <div style={{
        flex: 1, padding: '4px 20px 100px', display: 'flex', flexDirection: 'column', gap: 12,
        overflowY: 'auto',
      }}>
        {visible.length === 0 ? (
          <div style={{
            display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
            gap: 12, padding: '48px 24px', textAlign: 'center',
          }}>
            <div style={{
              width: 56, height: 56, borderRadius: '50%', background: 'var(--bg-sunken)',
              color: 'var(--ink-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <Icon name={filter === 'paused' ? 'pause' : 'target'} size={24}/>
            </div>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 14, color: 'var(--ink-muted)', maxWidth: 240,
            }}>{filter === 'paused' ? 'No tienes contadores en pausa.' : 'No hay contadores activos.'}</div>
          </div>
        ) : visible.map(c => (
          <StreakCard key={c.id} counter={c} onClick={() => onOpenCounter(c.id)}/>
        ))}
        <div style={{ height: 12 }}/>
      </div>
    </>
  );
}

window.HomeScreen = HomeScreen;
