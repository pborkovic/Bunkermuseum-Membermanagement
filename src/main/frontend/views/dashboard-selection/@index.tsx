import { useNavigate } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Icon } from '@vaadin/react-components/Icon.js';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export const config: ViewConfig = {
  menu: { exclude: true },
  route: 'dashboard-selection',
  loginRequired: false,
  flowLayout: false,
};

export default function DashboardSelectionView() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen w-full bg-white p-8">
      {/* Header */}
      <div className="max-w-4xl mx-auto mb-12">
        <h1 className="text-4xl font-bold text-black mb-4">Dashboard auswählen</h1>
        <p className="text-lg text-black">
          Sie haben mehrere Rollen. Bitte wählen Sie, welches Dashboard Sie besuchen möchten.
        </p>
      </div>

      {/* Dashboard Cards */}
      <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Member Dashboard Card */}
        <Card
          className="cursor-pointer transition-all shadow-md hover:shadow-xl bg-white"
          onClick={() => navigate('/member')}
        >
          <CardHeader className="text-center pb-4 bg-white">
            <div className="mx-auto w-20 h-20 flex items-center justify-center mb-4">
              <Icon
                icon="vaadin:user"
                style={{ width: '60px', height: '60px', color: 'black' }}
              />
            </div>
            <CardTitle className="text-2xl text-black font-bold">Mitglieder-Dashboard</CardTitle>
          </CardHeader>
          <CardContent className="text-center bg-white">
            <CardDescription className="text-base text-black">
              Zugriff auf Ihre persönlichen Daten und Buchungen
            </CardDescription>
          </CardContent>
        </Card>

        {/* Admin Dashboard Card */}
        <Card
          className="cursor-pointer transition-all shadow-md hover:shadow-xl bg-white"
          onClick={() => navigate('/admin')}
        >
          <CardHeader className="text-center pb-4 bg-white">
            <div className="mx-auto w-20 h-20 flex items-center justify-center mb-4">
              <Icon
                icon="vaadin:dashboard"
                style={{ width: '60px', height: '60px', color: 'black' }}
              />
            </div>
            <CardTitle className="text-2xl text-black font-bold">Admin-Dashboard</CardTitle>
          </CardHeader>
          <CardContent className="text-center bg-white">
            <CardDescription className="text-base text-black">
              Verwaltung von Benutzern, Buchungen und Systemeinstellungen
            </CardDescription>
          </CardContent>
        </Card>
      </div>

      {/* Footer Tip */}
      <div className="max-w-4xl mx-auto mt-8 text-center">
        <p className="text-sm text-black/60">
          Tipp: Sie können jederzeit zwischen den Dashboards wechseln
        </p>
      </div>
    </div>
  );
}
