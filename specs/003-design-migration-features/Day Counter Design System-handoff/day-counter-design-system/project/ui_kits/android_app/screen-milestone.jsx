// ─── Milestone celebration — full-screen ─────────────────────────────────────
function MilestoneScreen({ counter, milestone, onContinue }) {
  // Confetti-ish floating dots
  const dots = React.useMemo(() => Array.from({ length: 14 }, (_, i) => ({
    x: 10 + (i * 31) % 80,
    y: 8 + (i * 17) % 70,
    size: 4 + (i % 3) * 3,
    color: ['var(--milestone)', 'var(--brand)', 'var(--success)', 'var(--warning)'][i % 4],
    delay: (i * 80) % 600,
  })), []);

  const milestoneCopy = {
    1: 'Día 1. El más difícil ya empezó.',
    7: 'Una semana completa. Esto ya es un hábito que empieza.',
    30: '30 días. Pasaste el mes.',
    100: 'Cien días. Algo cambió en ti.',
    365: 'Un año entero. Esto ya no es una meta, es quién eres.',
    1000: 'Mil días. No hay palabras suficientes.',
  };

  return (
    <div style={{
      flex: 1, position: 'relative', overflow: 'hidden',
      background: `
        radial-gradient(ellipse 400px 300px at 50% 30%, rgba(217, 135, 106, 0.18), transparent 70%),
        radial-gradient(ellipse 500px 400px at 10% 90%, rgba(15, 95, 110, 0.12), transparent 70%),
        var(--bg)
      `,
      display: 'flex', flexDirection: 'column',
    }}>
      {/* Floating dots */}
      {dots.map((d, i) => (
        <div key={i} style={{
          position: 'absolute', left: `${d.x}%`, top: `${d.y}%`,
          width: d.size, height: d.size, borderRadius: '50%',
          background: d.color, opacity: 0.55,
          animation: `floatUp 3.6s ${d.delay}ms var(--ease-out-soft) infinite alternate`,
        }}/>
      ))}

      <div style={{ padding: '12px 20px', display: 'flex', justifyContent: 'flex-end' }}>
        <IconButton name="x" onClick={onContinue}/>
      </div>

      <div style={{
        flex: 1, padding: '0 28px', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', gap: 32, textAlign: 'center',
        zIndex: 1,
      }}>
        {/* Hero ring with glow */}
        <div style={{
          position: 'relative', width: 240, height: 240,
          filter: 'drop-shadow(0 0 24px rgba(217, 135, 106, 0.35))',
          animation: 'pop 700ms var(--ease-bounce)',
        }}>
          <Ring value={milestone} goal={milestone} size={240} stroke={16} milestone/>
          <div style={{
            position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column',
            alignItems: 'center', justifyContent: 'center',
          }}>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 12, fontWeight: 600,
              color: 'var(--milestone)', letterSpacing: '0.12em', textTransform: 'uppercase',
              marginBottom: 4,
            }}>Hito alcanzado</div>
            <div style={{
              fontFamily: 'var(--font-display)', fontSize: 104, fontWeight: 600,
              color: 'var(--milestone)', letterSpacing: '-0.04em', lineHeight: 1,
              fontVariantNumeric: 'tabular-nums',
            }}>{milestone}</div>
            <div style={{
              fontFamily: 'var(--font-body)', fontSize: 14, color: 'var(--ink-2)',
              marginTop: 6,
            }}>días</div>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, maxWidth: 320 }}>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 28, fontWeight: 600,
            color: 'var(--ink)', letterSpacing: '-0.025em', lineHeight: 1.15,
          }}>{milestoneCopy[milestone] || `¡${milestone} días!`}</div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 15, color: 'var(--ink-2)',
            lineHeight: 1.5,
          }}>«{counter.name}»</div>
        </div>
      </div>

      <div style={{ padding: '0 28px 24px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <Button variant="primary" full size="lg" onClick={onContinue}>Seguir así</Button>
        <Button variant="ghost" full onClick={onContinue}>Compartir</Button>
      </div>

      <style>{`
        @keyframes floatUp {
          from { transform: translateY(0px); opacity: 0.55; }
          to   { transform: translateY(-20px); opacity: 0.2; }
        }
        @keyframes pop {
          0%   { transform: scale(0.6); opacity: 0; }
          60%  { transform: scale(1.08); opacity: 1; }
          100% { transform: scale(1); opacity: 1; }
        }
      `}</style>
    </div>
  );
}

window.MilestoneScreen = MilestoneScreen;
