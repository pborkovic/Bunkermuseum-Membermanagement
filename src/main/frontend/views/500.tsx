import { useNavigate } from 'react-router';
import { Button } from 'Frontend/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from 'Frontend/components/ui/card';
import { FiHome, FiRefreshCw } from 'react-icons/fi';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';

export const config: ViewConfig = {
  menu: { exclude: true },
  loginRequired: false,
};

/**
 * 500 Server Error Page
 *
 * Displays when a server error occurs or an unexpected error happens.
 * Provides options to reload the page or return to home.
 *
 * @component
 *
 * @returns {JSX.Element} The 500 error page
 *
 * @author Philipp Borkovic
 */
export default function ServerErrorPage(): JSX.Element {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate('/');
  };

  const handleReload = () => {
    window.location.reload();
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-white p-4">
      <Card className="w-full max-w-md bg-white border shadow-lg">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4 text-6xl font-bold text-red-600">500</div>
          <CardTitle className="text-2xl text-gray-900">Etwas ist schiefgelaufen</CardTitle>
          <CardDescription className="text-gray-600">
            Es tut uns leid, aber es ist ein unerwarteter Fehler aufgetreten. Bitte versuchen Sie es erneut oder kontaktieren Sie den Support, falls das Problem weiterhin besteht.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button
            onClick={handleReload}
            className="w-full bg-black text-white hover:bg-black/90"
            size="lg"
          >
            <FiRefreshCw className="mr-2 h-5 w-5" />
            Seite neu laden
          </Button>
          <Button
            onClick={handleGoHome}
            variant="outline"
            className="w-full"
            size="lg"
          >
            <FiHome className="mr-2 h-5 w-5" />
            Zur Startseite
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
