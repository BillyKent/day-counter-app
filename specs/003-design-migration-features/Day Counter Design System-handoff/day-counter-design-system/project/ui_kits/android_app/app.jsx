// ─── App — root with navigation + state ─────────────────────────────────────
// Counter model (paused-aware):
//   startDate   ISO date the streak began
//   status      'active' | 'paused'
//   pausePeriods completed pauses: [{ start, end }] (ISO) — excluded from the count
//   pausedSince  ISO date the current pause began (only when status === 'paused')
// Streak days are DERIVED from these via counterDays() — never stored — so the
// number is always consistent between Contadores y Estadísticas. A real Android
// build persists this same shape in local storage (Room / DataStore).
const SEED_COUNTERS = [
  { id: 'c1', name: 'Sin fumar',       category: 'Salud',     goal: 90,  milestone: 30,  startDate: '2026-04-29', status: 'active', pausePeriods: [] },
  { id: 'c2', name: 'Correr cada día', category: 'Ejercicio', goal: 30,  milestone: 7,   startDate: '2026-05-17', status: 'active', pausePeriods: [] },
  { id: 'c3', name: 'Sin alcohol',     category: 'Salud',     goal: 365, milestone: 100, startDate: '2026-02-07', status: 'active', pausePeriods: [{ start: '2026-03-01', end: '2026-03-10' }] },
  { id: 'c4', name: 'Meditar 10 min',  category: 'Mente',     goal: 30,  milestone: 7,   startDate: '2026-05-20', status: 'paused', pausedSince: '2026-05-26', pausePeriods: [] },
];

function App({ initialRoute = 'main', initialTab = 'home' }) {
  // Navigation state
  const [route, setRoute] = React.useState(initialRoute); // onboarding | empty | main | detail | history
  const [tab, setTab] = React.useState(initialTab);       // home | stats | settings
  const [openId, setOpenId] = React.useState(null);
  const [creating, setCreating] = React.useState(false);
  const [editing, setEditing] = React.useState(false);
  const [resetConfirm, setResetConfirm] = React.useState(null);
  const [celebrateMilestone, setCelebrateMilestone] = React.useState(null);

  const [counters, setCounters] = React.useState(SEED_COUNTERS);
  const openCounter = counters.find(c => c.id === openId);

  // ── handlers ──
  const onCreate = (counter) => {
    setCounters(cs => [counter, ...cs]);
    if (route === 'empty') setRoute('main');
  };
  const onSaveEdit = (updated) => {
    setCounters(cs => cs.map(c => c.id === updated.id ? updated : c));
  };
  const onReset = () => { if (openCounter) setResetConfirm(openCounter.id); };
  const confirmReset = () => {
    setCounters(cs => cs.map(c => c.id === resetConfirm
      ? { ...c, startDate: TODAY_ISO, status: 'active', pausedSince: null, pausePeriods: [] }
      : c));
    setResetConfirm(null);
  };
  const onDelete = () => {
    if (!openCounter) return;
    setCounters(cs => cs.filter(c => c.id !== openCounter.id));
    setOpenId(null);
    setRoute('main');
  };
  // Pause: freeze the clock at today. Resume: bank the just-finished pause
  // window into pausePeriods so its duration stays excluded forever.
  const onTogglePause = () => {
    if (!openCounter) return;
    setCounters(cs => cs.map(c => {
      if (c.id !== openCounter.id) return c;
      if (c.status === 'paused') {
        const periods = [...(c.pausePeriods || [])];
        if (c.pausedSince) periods.push({ start: c.pausedSince, end: TODAY_ISO });
        return { ...c, status: 'active', pausedSince: null, pausePeriods: periods };
      }
      return { ...c, status: 'paused', pausedSince: TODAY_ISO };
    }));
  };

  // ── routes ──
  if (route === 'onboarding') {
    return (
      <Phone>
        <OnboardingScreen onFinish={() => { setRoute('empty'); }}/>
      </Phone>
    );
  }

  return (
    <Phone>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0, position: 'relative' }}>
        {/* Milestone overlay (full-screen) */}
        {celebrateMilestone && openCounter && (
          <MilestoneScreen
            counter={openCounter}
            milestone={celebrateMilestone}
            onContinue={() => setCelebrateMilestone(null)}
          />
        )}

        {/* History view */}
        {!celebrateMilestone && route === 'history' && openCounter && (
          <div style={{ flex: 1, overflowY: 'auto' }}>
            <HistoryScreen counter={openCounter} onBack={() => setRoute('detail')}/>
          </div>
        )}

        {/* Detail view */}
        {!celebrateMilestone && route === 'detail' && openCounter && (
          <div style={{ flex: 1, overflowY: 'auto' }}>
            <DetailScreen
              counter={openCounter}
              onBack={() => { setRoute('main'); setOpenId(null); }}
              onReset={onReset}
              onEdit={() => setEditing(true)}
              onDelete={onDelete}
              onHistory={() => setRoute('history')}
              onTogglePause={onTogglePause}
              onCelebrate={() => setCelebrateMilestone(openCounter.milestone || counterDays(openCounter))}
            />
          </div>
        )}

        {/* Empty state */}
        {!celebrateMilestone && route === 'empty' && (
          <EmptyHomeScreen onCreate={() => setCreating(true)}/>
        )}

        {/* Main tabs */}
        {!celebrateMilestone && route === 'main' && (
          <>
            {tab === 'home' && (
              counters.length === 0
                ? <EmptyHomeScreen onCreate={() => setCreating(true)}/>
                : <HomeScreen
                    counters={counters}
                    onOpenCounter={(id) => { setOpenId(id); setRoute('detail'); }}
                    onCreateNew={() => setCreating(true)}
                    onOpenStats={() => setTab('stats')}
                  />
            )}
            {tab === 'stats' && <StatsScreen counters={counters}/>}
            {tab === 'settings' && <SettingsScreen/>}
            {tab === 'home' && counters.length > 0 && <FAB onClick={() => setCreating(true)}/>}
          </>
        )}

        {/* Bottom nav — only on main route */}
        {!celebrateMilestone && (route === 'main' || route === 'empty') && (
          <BottomNav active={tab} onChange={(t) => { setTab(t); if (route === 'empty') setRoute('main'); }}/>
        )}
      </div>

      {/* Sheets */}
      <CreateSheet open={creating} onClose={() => setCreating(false)} onCreate={onCreate}/>
      <EditSheet open={editing} onClose={() => setEditing(false)} counter={openCounter} onSave={onSaveEdit}/>
      <Sheet open={!!resetConfirm} onClose={() => setResetConfirm(null)} title="¿Reiniciar contador?">
        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 15, color: 'var(--ink-2)',
          lineHeight: 1.5, marginBottom: 20,
        }}>
          Tu racha actual se guardará en el historial. Empezamos de nuevo. El día 1 también cuenta.
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          <div style={{ flex: 1 }}><Button variant="ghost" full onClick={() => setResetConfirm(null)}>Cancelar</Button></div>
          <div style={{ flex: 1.4 }}><Button variant="danger" full onClick={confirmReset}>Reiniciar</Button></div>
        </div>
      </Sheet>
    </Phone>
  );
}

window.App = App;
