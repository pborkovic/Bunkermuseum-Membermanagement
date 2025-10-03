import { useState } from 'react';
import { useNavigate } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Icon } from '@vaadin/react-components';
import { AuthController } from 'Frontend/generated/endpoints';

/**
 * Error messages for login failures
 */
const ERROR_MESSAGES = {
  INVALID_CREDENTIALS: 'Invalid email or password. Please try again.',
  GENERIC_ERROR: 'An error occurred during login. Please try again.',
} as const;

/**
 * Route configuration for the login view.
 * Configures this route to be excluded from navigation menu and accessible without login.
 */
export const config: ViewConfig = {
  menu: { exclude: true },
  route: 'login',
  loginRequired: false,
  flowLayout: false,
};

/**
 * LoginView component - User authentication page for the Bunker Museum application.
 * Provides email/password login with German language interface.
 *
 * Features:
 * - Email and password authentication via AuthController
 * - Loading states during authentication
 * - Error handling for invalid credentials and account lockout
 * - Full page reload on successful login to refresh security context
 * - Link to registration page for new users
 * - Responsive split layout (form on right, blank area on left for desktop)
 *
 * @component
 *
 * @returns {JSX.Element} The login view
 *
 * @index.tsx
 *
 * @author Philipp Borkovic
 */
export default function LoginView(): JSX.Element {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  /**
   * Handles login form submission.
   * Authenticates user via AuthController and redirects on success.
   *
   * @param {React.FormEvent} e - The form submission event
   *
   * @author Philipp Borkovic
   */
  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await AuthController.login(email, password);

      if (response) {
        window.location.href = '/';
      } else {
        setError(ERROR_MESSAGES.INVALID_CREDENTIALS);
      }
    } catch (err: any) {
      if (err.message?.includes('locked')) {
        setError(err.message);
      } else if (err.message) {
        setError(err.message);
      } else {
        setError(ERROR_MESSAGES.GENERIC_ERROR);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen w-full">
      {/* Left side - blank (desktop only) */}
      <div className="hidden lg:flex lg:w-1/2 bg-muted" />

      {/* Right side - login form */}
      <div className="flex w-full items-center justify-center p-4 lg:w-1/2 lg:p-8">
        <div className="w-full max-w-sm space-y-6">
          {/* Logo */}
          <div className="flex flex-col items-center space-y-2 text-center">
            <Icon icon="vaadin:cubes" className="text-primary" style={{ width: '48px', height: '48px' }} />
            <h1 className="text-2xl font-semibold tracking-tight">Bunker Museum</h1>
            <p className="text-sm text-muted-foreground">Melden Sie sich an</p>
          </div>

          {/* Login form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">E-Mail</Label>
              <Input
                id="email"
                type="email"
                placeholder="name@beispiel.de"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isLoading}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Passwort</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isLoading}
                required
              />
            </div>
            {error && (
              <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
                {error}
              </div>
            )}
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Anmelden...' : 'Anmelden'}
            </Button>
          </form>

          {/* Register link */}
          <div className="text-center text-sm">
            <span className="text-muted-foreground">Noch kein Mitglied? </span>
            <a href="/register" className="text-primary hover:underline">
              Jetzt registrieren
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
