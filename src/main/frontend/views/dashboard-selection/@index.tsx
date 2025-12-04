import { useNavigate } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';

export const config: ViewConfig = {
  menu: { exclude: true },
  route: 'dashboard-selection',
  loginRequired: false,
  flowLayout: false,
};

export default function DashboardSelectionView() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: '40px', textAlign: 'center', backgroundColor: 'white' }}>
      <h1 style={{ fontSize: '32px', marginBottom: '20px', color: 'black' }}>
        Dashboard auswählen
      </h1>
      <p style={{ fontSize: '18px', marginBottom: '40px', color: '#666' }}>
        Sie haben mehrere Rollen. Bitte wählen Sie, welches Dashboard Sie besuchen möchten.
      </p>

      <div style={{ display: 'flex', gap: '20px', justifyContent: 'center', flexWrap: 'wrap' }}>
        <button
          onClick={() => navigate('/member')}
          style={{
            padding: '40px',
            backgroundColor: '#f0f0f0',
            border: '2px solid #ddd',
            borderRadius: '8px',
            cursor: 'pointer',
            minWidth: '250px',
          }}
        >
          <h2 style={{ fontSize: '24px', marginBottom: '10px', color: 'black' }}>
            Mitglieder-Dashboard
          </h2>
          <p style={{ color: '#666' }}>
            Zugriff auf Ihre persönlichen Daten und Buchungen
          </p>
        </button>

        <button
          onClick={() => navigate('/admin')}
          style={{
            padding: '40px',
            backgroundColor: '#f0f0f0',
            border: '2px solid #ddd',
            borderRadius: '8px',
            cursor: 'pointer',
            minWidth: '250px',
          }}
        >
          <h2 style={{ fontSize: '24px', marginBottom: '10px', color: 'black' }}>
            Admin-Dashboard
          </h2>
          <p style={{ color: '#666' }}>
            Verwaltung von Benutzern, Buchungen und Systemeinstellungen
          </p>
        </button>
      </div>
    </div>
  );
}
