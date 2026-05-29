// ─── Create / Edit counter — bottom sheet form ──────────────────────────────
function CreateSheet({ open, onClose, onCreate }) {
  const [name, setName] = React.useState('');
  const [category, setCategory] = React.useState('Salud');
  const [goal, setGoal] = React.useState(30);
  const [startedAt, setStartedAt] = React.useState('Hoy');

  React.useEffect(() => {
    if (open) { setName(''); setCategory('Salud'); setGoal(30); setStartedAt('Hoy'); }
  }, [open]);

  const submit = () => {
    if (!name.trim()) return;
    onCreate({
      id: 'c' + Date.now(),
      name: name.trim(),
      category,
      goal,
      milestone: goal,
      startDate: TODAY_ISO,   // nuevos contadores empiezan hoy
      status: 'active',
      pausePeriods: [],
    });
    onClose();
  };

  return (
    <Sheet open={open} onClose={onClose} title="Nuevo contador">
      <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <Input
          label="Meta"
          value={name}
          onChange={setName}
          placeholder="Sin fumar, correr cada día…"
          leading="target"
        />
        <div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 12, fontWeight: 600,
            color: 'var(--ink-muted)', letterSpacing: '0.04em', textTransform: 'uppercase',
            marginBottom: 8,
          }}>Categoría</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {['Salud', 'Ejercicio', 'Ahorro', 'Estudio', 'Mente'].map(cat => (
              <Chip key={cat} active={category === cat} onClick={() => setCategory(cat)}>
                {cat}
              </Chip>
            ))}
          </div>
        </div>
        <Input
          label="Fecha de inicio"
          value={startedAt}
          onChange={setStartedAt}
          leading="calendar"
        />
        <div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 12, fontWeight: 600,
            color: 'var(--ink-muted)', letterSpacing: '0.04em', textTransform: 'uppercase',
            marginBottom: 8,
          }}>Hito objetivo</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {[7, 30, 100, 365].map(g => (
              <Chip key={g} active={goal === g} onClick={() => setGoal(g)}>
                {g} días
              </Chip>
            ))}
          </div>
        </div>
        <div style={{
          padding: '14px 16px', borderRadius: 16,
          background: 'var(--brand-softer)', display: 'flex', gap: 10, alignItems: 'flex-start',
          marginTop: 4,
        }}>
          <Icon name="bell" size={18} color="var(--brand)"/>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 13, color: 'var(--ink-2)', lineHeight: 1.4,
          }}>Te enviaremos un mensaje motivacional cuando alcances tu hito.</div>
        </div>
        <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
          <div style={{ flex: 1 }}><Button variant="ghost" full onClick={onClose}>Cancelar</Button></div>
          <div style={{ flex: 2 }}><Button variant="primary" full onClick={submit}>Crear contador</Button></div>
        </div>
      </div>
    </Sheet>
  );
}

window.CreateSheet = CreateSheet;
