import { useNavigate } from 'react-router';
import { Button } from 'Frontend/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from 'Frontend/components/ui/card';
import { FiHome, FiArrowLeft } from 'react-icons/fi';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';

export const config: ViewConfig = {
  menu: { exclude: true },
  loginRequired: false,
};

/**
 * 404 Seite nicht gefunden - Fehlerseite
 *
 * Wird angezeigt, wenn ein Benutzer zu einer nicht existierenden Route navigiert.
 * Bietet Optionen zur Rückkehr zur Startseite oder zur vorherigen Seite.
 *
 * @component
 *
 * @returns {JSX.Element} Die 404 Fehlerseite
 *
 * @author Philipp Borkovic
 */
export default function NotFoundPage(): JSX.Element {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate('/');
  };

  const handleGoBack = () => {
    navigate(-1);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-white p-4">
      <Card className="w-full max-w-md bg-white border shadow-lg">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4 text-6xl font-bold text-blue-600">404</div>
          <CardTitle className="text-2xl text-gray-900">Seite nicht gefunden</CardTitle>
          <CardDescription className="text-gray-600">
            Die gesuchte Seite existiert nicht oder wurde verschoben.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button
            onClick={handleGoHome}
            className="w-full bg-black text-white hover:bg-black/90"
            size="lg"
          >
            <FiHome className="mr-2 h-5 w-5" />
            Zur Startseite
          </Button>
          <Button
            onClick={handleGoBack}
            variant="outline"
            className="w-full"
            size="lg"
          >
            <FiArrowLeft className="mr-2 h-5 w-5" />
            Zurück
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
