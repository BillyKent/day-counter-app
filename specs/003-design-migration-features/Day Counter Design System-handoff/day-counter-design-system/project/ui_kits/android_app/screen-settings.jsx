// ─── Settings / Ajustes screen ──────────────────────────────────────────────
function SettingsScreen() {
  const [notifs, setNotifs] = React.useState(true);
  const [dailyReminder, setDailyReminder] = React.useState(true);
  const [darkMode, setDarkMode] = React.useState(false);

  // Editable values
  const [language, setLanguage] = React.useState('es');
  const [reminderTime, setReminderTime] = React.useState({ h: 9, m: 0 });

  // Sheet visibility
  const [langOpen, setLangOpen] = React.useState(false);
  const [timeOpen, setTimeOpen] = React.useState(false);
  const [wipeOpen, setWipeOpen] = React.useState(false);
  const [wiped, setWiped] = React.useState(false);

  const LANGUAGES = [
    { id: 'es', label: 'Español', native: 'Español' },
    { id: 'en', label: 'Inglés', native: 'English' },
    { id: 'pt', label: 'Portugués', native: 'Português' },
    { id: 'fr', label: 'Francés', native: 'Français' },
    { id: 'de', label: 'Alemán', native: 'Deutsch' },
    { id: 'it', label: 'Italiano', native: 'Italiano' },
  ];
  const currentLang = LANGUAGES.find(l => l.id === language);
  const fmtTime = (t) => `${String(t.h).padStart(2, '0')}:${String(t.m).padStart(2, '0')}`;

  const Toggle = ({ on, onClick }) => (
    <button onClick={onClick} style={{
      width: 46, height: 28, borderRadius: 999, border: 'none',
      background: on ? 'var(--brand)' : 'var(--border-strong)',
      padding: 3, cursor: 'pointer', display: 'flex',
      transition: 'background 240ms var(--ease-out-soft)',
    }}>
      <div style={{
        width: 22, height: 22, borderRadius: '50%', background: '#fff',
        boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
        transform: `translateX(${on ? 18 : 0}px)`,
        transition: 'transform 240ms var(--ease-out-soft)',
      }}/>
    </button>
  );

  const Row = ({ icon, title, desc, trailing, onClick, danger }) => (
    <div onClick={onClick} style={{
      display: 'flex', alignItems: 'center', gap: 14, padding: '14px 16px',
      cursor: onClick ? 'pointer' : 'default',
    }}>
      <div style={{
        width: 36, height: 36, borderRadius: 12,
        background: danger ? 'var(--danger-soft)' : 'var(--brand-soft)',
        color: danger ? 'var(--danger)' : 'var(--brand)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        flexShrink: 0,
      }}><Icon name={icon} size={18}/></div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 15, fontWeight: 600,
          color: danger ? 'var(--danger)' : 'var(--ink)', lineHeight: 1.3,
        }}>{title}</div>
        {desc && (
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 12, color: 'var(--ink-muted)',
            marginTop: 1,
          }}>{desc}</div>
        )}
      </div>
      {trailing}
    </div>
  );

  const Group = ({ children }) => {
    const items = React.Children.toArray(children);
    return (
      <Card padding={4} style={{ marginBottom: 16 }}>
        {items.map((child, i) => (
          <React.Fragment key={i}>
            {child}
            {i < items.length - 1 && (
              <div style={{ height: 1, background: 'var(--border-soft)', marginLeft: 66 }}/>
            )}
          </React.Fragment>
        ))}
      </Card>
    );
  };

  const ValueTrailing = ({ children }) => (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexShrink: 0 }}>
      <span style={{
        fontFamily: 'var(--font-body)', fontSize: 14, fontWeight: 600, color: 'var(--brand)',
      }}>{children}</span>
      <Icon name="chevronRight" size={20} color="var(--ink-subtle)"/>
    </div>
  );

  return (
    <>
      <TopBar large title="Ajustes"/>
      <div style={{ padding: '0 20px 20px', flex: 1, overflowY: 'auto' }}>
        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
          color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
          marginBottom: 8, padding: '0 4px',
        }}>Notificaciones</div>
        <Group>
          <Row icon="bell" title="Recordatorios diarios" desc="Te avisamos cada día para mantener tu racha"
            trailing={<Toggle on={dailyReminder} onClick={() => setDailyReminder(!dailyReminder)}/>}/>
          <Row icon="trophy" title="Hitos alcanzados" desc="Mensajes motivacionales en cada hito"
            trailing={<Toggle on={notifs} onClick={() => setNotifs(!notifs)}/>}/>
          <Row icon="clock" title="Hora del recordatorio"
            desc={dailyReminder ? `Todos los días a las ${fmtTime(reminderTime)}` : 'Recordatorio desactivado'}
            onClick={dailyReminder ? () => setTimeOpen(true) : undefined}
            trailing={dailyReminder
              ? <ValueTrailing>{fmtTime(reminderTime)}</ValueTrailing>
              : <span style={{ fontFamily: 'var(--font-body)', fontSize: 13, color: 'var(--ink-subtle)' }}>—</span>}/>
        </Group>

        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
          color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
          marginBottom: 8, padding: '0 4px',
        }}>Apariencia</div>
        <Group>
          <Row icon="moon" title="Modo oscuro" desc="Sigue el sistema"
            trailing={<Toggle on={darkMode} onClick={() => setDarkMode(!darkMode)}/>}/>
          <Row icon="globe" title="Idioma" desc="Idioma de la aplicación"
            onClick={() => setLangOpen(true)}
            trailing={<ValueTrailing>{currentLang.native}</ValueTrailing>}/>
        </Group>

        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 11, fontWeight: 600,
          color: 'var(--ink-muted)', letterSpacing: '0.08em', textTransform: 'uppercase',
          marginBottom: 8, padding: '0 4px',
        }}>Datos</div>
        <Group>
          <Row icon="trash" title="Borrar todo" desc="Eliminar contadores y reiniciar la app" danger
            onClick={() => setWipeOpen(true)}
            trailing={<Icon name="chevronRight" size={20} color="var(--ink-subtle)"/>}/>
        </Group>

        <div style={{
          fontFamily: 'var(--font-body)', fontSize: 12, color: 'var(--ink-subtle)',
          textAlign: 'center', marginTop: 12,
        }}>Day Counter · versión 1.0</div>

        <div style={{ height: 80 }}/>
      </div>

      {/* ── Idioma ── */}
      <Sheet open={langOpen} onClose={() => setLangOpen(false)} title="Idioma">
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {LANGUAGES.map(l => {
            const active = l.id === language;
            return (
              <button key={l.id} onClick={() => { setLanguage(l.id); setLangOpen(false); }} style={{
                display: 'flex', alignItems: 'center', gap: 14,
                padding: '14px 16px', borderRadius: 16, border: 'none', cursor: 'pointer',
                background: active ? 'var(--brand-soft)' : '#fff',
                boxShadow: active ? 'none' : 'var(--shadow-sm)',
                textAlign: 'left',
                transition: 'background 180ms var(--ease-out-soft)',
              }}>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{
                    fontFamily: 'var(--font-body)', fontSize: 15, fontWeight: 600,
                    color: active ? 'var(--brand)' : 'var(--ink)',
                  }}>{l.native}</div>
                  <div style={{
                    fontFamily: 'var(--font-body)', fontSize: 12, color: 'var(--ink-muted)', marginTop: 1,
                  }}>{l.label}</div>
                </div>
                {active && (
                  <div style={{
                    width: 26, height: 26, borderRadius: '50%', background: 'var(--brand)',
                    color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                  }}>
                    <Icon name="check" size={16}/>
                  </div>
                )}
              </button>
            );
          })}
        </div>
      </Sheet>

      {/* ── Hora del recordatorio ── */}
      <TimePickerSheet
        open={timeOpen}
        onClose={() => setTimeOpen(false)}
        value={reminderTime}
        onSave={(t) => { setReminderTime(t); setTimeOpen(false); }}
      />

      {/* ── Borrar todo ── */}
      <Sheet open={wipeOpen} onClose={() => setWipeOpen(false)} title="¿Borrar todo?">
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: 16 }}>
          <div style={{
            width: 64, height: 64, borderRadius: '50%', background: 'var(--danger-soft)',
            color: 'var(--danger)', display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon name="trash" size={28}/>
          </div>
          <div style={{
            fontFamily: 'var(--font-body)', fontSize: 15, color: 'var(--ink-2)', lineHeight: 1.5, maxWidth: 300,
          }}>
            Se eliminarán tus <strong style={{ color: 'var(--ink)' }}>4 contadores</strong> y todo su historial. Esta acción no se puede deshacer.
          </div>
        </div>
        <div style={{ display: 'flex', gap: 10, marginTop: 22 }}>
          <div style={{ flex: 1 }}><Button variant="ghost" full onClick={() => setWipeOpen(false)}>Cancelar</Button></div>
          <div style={{ flex: 1.4 }}><Button variant="danger" full onClick={() => { setWipeOpen(false); setWiped(true); }}>Borrar todo</Button></div>
        </div>
      </Sheet>

      {/* Confirmation toast after wipe */}
      {wiped && (
        <div style={{
          position: 'absolute', left: 20, right: 20, bottom: 24, zIndex: 60,
          background: 'var(--ink)', color: '#fff', borderRadius: 16,
          padding: '14px 18px', display: 'flex', alignItems: 'center', gap: 12,
          boxShadow: 'var(--shadow-lg)',
          animation: 'slideUp 320ms var(--ease-out-soft)',
        }}>
          <Icon name="check" size={18} color="var(--success)"/>
          <span style={{ flex: 1, fontFamily: 'var(--font-body)', fontSize: 14, fontWeight: 500 }}>
            Todos los datos fueron eliminados.
          </span>
          <button onClick={() => setWiped(false)} style={{
            border: 'none', background: 'transparent', color: 'var(--success)',
            fontFamily: 'var(--font-body)', fontSize: 14, fontWeight: 700, cursor: 'pointer',
          }}>Deshacer</button>
        </div>
      )}
    </>
  );
}

// ─── Time picker sheet — scrollable hour / minute columns ────────────────────
function TimePickerSheet({ open, onClose, value, onSave }) {
  const [h, setH] = React.useState(value.h);
  const [m, setM] = React.useState(value.m);

  React.useEffect(() => { if (open) { setH(value.h); setM(value.m); } }, [open]);

  const hours = Array.from({ length: 24 }, (_, i) => i);
  const minutes = Array.from({ length: 12 }, (_, i) => i * 5); // 5-min steps

  const ROW = 48;

  const Column = ({ items, sel, onSel, fmt }) => {
    const ref = React.useRef(null);
    // Scroll the selected item into the centre when the sheet opens.
    React.useEffect(() => {
      if (open && ref.current) {
        const idx = items.indexOf(sel);
        ref.current.scrollTop = Math.max(0, idx * ROW);
      }
    }, [open]);
    return (
      <div style={{ position: 'relative', flex: 1, height: ROW * 3, overflow: 'hidden' }}>
        <div ref={ref} style={{
          height: '100%', overflowY: 'auto', scrollSnapType: 'y mandatory',
          paddingTop: ROW, paddingBottom: ROW,
          maskImage: 'linear-gradient(to bottom, transparent, #000 32%, #000 68%, transparent)',
          WebkitMaskImage: 'linear-gradient(to bottom, transparent, #000 32%, #000 68%, transparent)',
        }}>
          {items.map(it => {
            const active = it === sel;
            return (
              <div key={it} onClick={() => onSel(it)} style={{
                height: ROW, display: 'flex', alignItems: 'center', justifyContent: 'center',
                scrollSnapAlign: 'center', cursor: 'pointer',
                fontFamily: 'var(--font-display)',
                fontSize: active ? 26 : 20,
                fontWeight: 600,
                color: active ? 'var(--brand)' : 'var(--ink-subtle)',
                fontVariantNumeric: 'tabular-nums',
                transition: 'color 180ms var(--ease-out-soft), font-size 180ms var(--ease-out-soft)',
              }}>{fmt(it)}</div>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <Sheet open={open} onClose={onClose} title="Hora del recordatorio">
      <div style={{
        fontFamily: 'var(--font-body)', fontSize: 14, color: 'var(--ink-muted)',
        marginTop: -8, marginBottom: 16,
      }}>Te enviaremos un recordatorio diario a esta hora.</div>

      <div style={{ position: 'relative', display: 'flex', alignItems: 'center', padding: '0 24px' }}>
        {/* Centre selection band */}
        <div style={{
          position: 'absolute', left: 16, right: 16, top: '50%', transform: 'translateY(-50%)',
          height: 48, borderRadius: 16, background: 'var(--brand-softer)', zIndex: 0,
        }}/>
        <div style={{ position: 'relative', zIndex: 1, display: 'flex', alignItems: 'center', width: '100%' }}>
          <Column items={hours} sel={h} onSel={setH} fmt={(x) => String(x).padStart(2, '0')}/>
          <div style={{
            fontFamily: 'var(--font-display)', fontSize: 26, fontWeight: 600, color: 'var(--ink)',
            padding: '0 4px',
          }}>:</div>
          <Column items={minutes} sel={m} onSel={setM} fmt={(x) => String(x).padStart(2, '0')}/>
        </div>
      </div>

      {/* Quick presets */}
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', justifyContent: 'center', marginTop: 18 }}>
        {[{ h: 8, m: 0, l: 'Mañana' }, { h: 13, m: 0, l: 'Mediodía' }, { h: 21, m: 0, l: 'Noche' }].map(p => (
          <Chip key={p.l} active={h === p.h && m === p.m} onClick={() => { setH(p.h); setM(p.m); }}>
            {p.l} · {String(p.h).padStart(2,'0')}:{String(p.m).padStart(2,'0')}
          </Chip>
        ))}
      </div>

      <div style={{ display: 'flex', gap: 10, marginTop: 22 }}>
        <div style={{ flex: 1 }}><Button variant="ghost" full onClick={onClose}>Cancelar</Button></div>
        <div style={{ flex: 2 }}><Button variant="primary" full onClick={() => onSave({ h, m })}>Guardar hora</Button></div>
      </div>
    </Sheet>
  );
}

window.SettingsScreen = SettingsScreen;
window.TimePickerSheet = TimePickerSheet;
