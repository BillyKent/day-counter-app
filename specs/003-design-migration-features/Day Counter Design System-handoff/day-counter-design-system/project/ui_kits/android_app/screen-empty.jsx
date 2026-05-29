// ─── Empty state — first launch after onboarding ────────────────────────────
function EmptyHomeScreen({ onCreate }) {
  return (
    <>
      <TopBar large title="Contadores"/>
      <div style={{
        flex: 1, padding: '0 28px 40px', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', gap: 28, textAlign: 'center',
      }}>
        {/* Decorative ring — empty */}
        <div style={{ position: 'relative', width: 180, height: 180 }}>
          <svg viewBox="0 0 180 180" width="180" height="180">
            <circle cx="90" cy="90" r="78" fill="none"
              stroke="var(--brand-soft)" strokeWidth="12"
              strokeDasharray="4 8" strokeLinecap="round"/>
          </svg>
          <div style={{
            position: 'absolute', inset: 0, display: 'flex', alignItems: 'center',
            justifyContent: 'center', color: 'var(--brand)', opacity: 0.5,
          }}>
            <Icon name="plus" size={56} strokeWidth={2}/>
          </div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, maxWidth: 300 }}>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 600,
            color: 'var(--ink)', letterSpacing: '-0.02em', lineHeight: 1.15,
          }}>Empieza por una meta pequeña.</div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 15, color: 'var(--ink-2)',
            lineHeight: 1.5, textWrap: 'pretty',
          }}>Aún no tienes contadores. Crea el primero y empezamos hoy mismo — el día 1 también cuenta.</div>
        </div>
        <Button variant="primary" leading="plus" size="lg" onClick={onCreate}>Crear contador</Button>
      </div>
    </>
  );
}

window.EmptyHomeScreen = EmptyHomeScreen;
