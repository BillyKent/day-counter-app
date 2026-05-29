// ─── Onboarding — 3-slide intro ─────────────────────────────────────────────
function OnboardingScreen({ onFinish }) {
  const [step, setStep] = React.useState(0);

  const slides = [
    {
      illustration: 'ring',
      eyebrow: 'Bienvenido',
      title: 'Cada día cuenta.',
      body: 'Day Counter te ayuda a ver crecer tus rachas — un día a la vez, sin presión, sin culpa.',
    },
    {
      illustration: 'targets',
      eyebrow: 'Cómo funciona',
      title: 'Una meta. Una fecha.',
      body: 'Crea contadores para lo que quieras: dejar de fumar, correr cada día, meditar. Elige una fecha de inicio y nosotros llevamos la cuenta.',
    },
    {
      illustration: 'bell',
      eyebrow: 'Lo que recibirás',
      title: 'Mensajes en cada hito.',
      body: 'Te avisaremos al llegar al día 7, 30, 100, 365… los hitos que importan. También podemos enviarte un recordatorio diario.',
    },
  ];

  const s = slides[step];
  const isLast = step === slides.length - 1;

  const Illustration = () => {
    if (s.illustration === 'ring') {
      return (
        <div style={{ position: 'relative', width: 200, height: 200 }}>
          <Ring value={73} goal={100} size={200} stroke={14}/>
          <div style={{
            position: 'absolute', inset: 0, display: 'flex', alignItems: 'center',
            justifyContent: 'center',
            fontFamily: 'var(--font-display)', fontSize: 76, fontWeight: 600,
            color: 'var(--brand)', letterSpacing: '-0.04em', fontVariantNumeric: 'tabular-nums',
          }}>73</div>
        </div>
      );
    }
    if (s.illustration === 'targets') {
      return (
        <div style={{ display: 'flex', gap: 24, alignItems: 'center' }}>
          <div style={{ position: 'relative', width: 88, height: 88, opacity: 0.5 }}>
            <Ring value={7} goal={30} size={88} stroke={8}/>
            <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-display)', fontSize: 30, fontWeight: 600, color: 'var(--brand)' }}>7</div>
          </div>
          <div style={{ position: 'relative', width: 132, height: 132 }}>
            <Ring value={30} goal={90} size={132} stroke={11}/>
            <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-display)', fontSize: 48, fontWeight: 600, color: 'var(--brand)' }}>30</div>
          </div>
          <div style={{ position: 'relative', width: 88, height: 88, opacity: 0.5 }}>
            <Ring value={102} goal={365} size={88} stroke={8}/>
            <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 600, color: 'var(--brand)' }}>102</div>
          </div>
        </div>
      );
    }
    // bell
    return (
      <div style={{
        width: 200, height: 200, borderRadius: '50%',
        background: 'var(--brand-soft)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        position: 'relative',
      }}>
        <Icon name="trophy" size={96} color="var(--brand)" strokeWidth={1.8}/>
        <div style={{
          position: 'absolute', top: 22, right: 22,
          width: 48, height: 48, borderRadius: '50%',
          background: 'var(--milestone)', color: '#fff',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: 'var(--shadow-md)',
        }}>
          <Icon name="bell" size={22}/>
        </div>
      </div>
    );
  };

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
      <div style={{
        padding: '12px 20px', display: 'flex', justifyContent: 'flex-end', flexShrink: 0,
      }}>
        {!isLast && (
          <button onClick={onFinish} style={{
            border: 'none', background: 'transparent',
            fontFamily: 'var(--font-body)', fontSize: 14, fontWeight: 600,
            color: 'var(--ink-muted)', cursor: 'pointer', padding: '6px 12px',
          }}>Saltar</button>
        )}
      </div>
      <div style={{
        flex: 1, padding: '0 28px', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', gap: 36, textAlign: 'center',
      }}>
        <Illustration/>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, maxWidth: 320 }}>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 12, fontWeight: 600,
            color: 'var(--brand)', letterSpacing: '0.12em', textTransform: 'uppercase',
          }}>{s.eyebrow}</div>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 32, fontWeight: 600,
            color: 'var(--ink)', letterSpacing: '-0.025em', lineHeight: 1.1,
          }}>{s.title}</div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 15, color: 'var(--ink-2)',
            lineHeight: 1.5, textWrap: 'pretty',
          }}>{s.body}</div>
        </div>
      </div>
      <div style={{ padding: '0 28px 24px', display: 'flex', flexDirection: 'column', gap: 20 }}>
        {/* Dots */}
        <div style={{ display: 'flex', gap: 8, justifyContent: 'center' }}>
          {slides.map((_, i) => (
            <div key={i} style={{
              width: i === step ? 24 : 8, height: 8, borderRadius: 999,
              background: i === step ? 'var(--brand)' : 'var(--border-strong)',
              transition: 'all 320ms var(--ease-out-soft)',
            }}/>
          ))}
        </div>
        <Button
          variant="primary" full size="lg"
          onClick={() => isLast ? onFinish() : setStep(step + 1)}>
          {isLast ? 'Crear mi primer contador' : 'Continuar'}
        </Button>
      </div>
    </div>
  );
}

window.OnboardingScreen = OnboardingScreen;
