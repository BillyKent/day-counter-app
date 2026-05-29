// ─── Edit counter — sheet with prefilled values ─────────────────────────────
function EditSheet({ open, onClose, counter, onSave }) {
  const [name, setName] = React.useState('');
  const [category, setCategory] = React.useState('Salud');
  const [goal, setGoal] = React.useState(30);

  React.useEffect(() => {
    if (open && counter) {
      setName(counter.name);
      setCategory(counter.category);
      setGoal(counter.goal || 30);
    }
  }, [open, counter]);

  if (!counter) return null;

  const submit = () => {
    onSave({ ...counter, name: name.trim() || counter.name, category, goal });
    onClose();
  };

  return (
    <Sheet open={open} onClose={onClose} title="Editar contador">
      <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <Input label="Meta" value={name} onChange={setName} leading="target"/>
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
        <div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 12, fontWeight: 600,
            color: 'var(--ink-muted)', letterSpacing: '0.04em', textTransform: 'uppercase',
            marginBottom: 8,
          }}>Hito objetivo</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {[7, 30, 100, 365].map(g => (
              <Chip key={g} active={goal === g} onClick={() => setGoal(g)}>{g} días</Chip>
            ))}
          </div>
        </div>
        <div style={{
          padding: '14px 16px', borderRadius: 16,
          background: 'var(--bg-sunken)',
          fontFamily: 'var(--font-body)', fontSize: 13, color: 'var(--ink-2)',
          lineHeight: 1.4,
        }}>
          La fecha de inicio no se puede cambiar. Para empezar de cero, usa <strong>Reiniciar</strong>.
        </div>
        <div style={{ display: 'flex', gap: 10, marginTop: 4 }}>
          <div style={{ flex: 1 }}><Button variant="ghost" full onClick={onClose}>Cancelar</Button></div>
          <div style={{ flex: 2 }}><Button variant="primary" full onClick={submit}>Guardar cambios</Button></div>
        </div>
      </div>
    </Sheet>
  );
}

window.EditSheet = EditSheet;
