import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AuthController } from 'Frontend/generated/endpoints';
import { getErrorMessage } from '../../types/vaadin';
import logo from 'Frontend/assets/images/logo_bunkermuseum.jpg';
import { z } from 'zod';

/**
 * Password validation schema with OWASP requirements.
 *
 * Password must meet the following criteria:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 */
const passwordSchema = z
  .string()
  .min(8, 'Passwort muss mindestens 8 Zeichen lang sein')
  .regex(/[a-z]/, 'Passwort muss mindestens einen Kleinbuchstaben enthalten')
  .regex(/[A-Z]/, 'Passwort muss mindestens einen Großbuchstaben enthalten')
  .regex(/\d/, 'Passwort muss mindestens eine Ziffer enthalten')
  .regex(/[@$!%*?&#]/, 'Passwort muss mindestens ein Sonderzeichen (@$!%*?&#) enthalten');

/**
 * Route configuration for the password setup view.
 * Configures this route to be excluded from navigation menu and accessible without login.
 */
export const config: ViewConfig = {
  menu: { exclude: true },
  route: 'setup-password',
  loginRequired: false,
  flowLayout: false,
};

/**
 * SetupPasswordView component - Password setup page for newly created users.
 * Allows users to set their password using a token received via email.
 *
 * Features:
 * - Token-based password setup
 * - OWASP password requirements validation
 * - Password confirmation matching
 * - Visual password requirements display
 * - Error handling for invalid/expired tokens
 * - Loading states during password setup
 *
 * @component
 *
 * @returns {JSX.Element} The password setup view
 *
 * @author Philipp Borkovic
 */
export default function SetupPasswordView(): JSX.Element {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  /**
   * Validates password in real-time as user types.
   */
  useEffect(() => {
    if (password) {
      const result = passwordSchema.safeParse(password);
      if (!result.success) {
        setValidationErrors(result.error.issues.map((issue) => issue.message));
      } else {
        setValidationErrors([]);
      }
    } else {
      setValidationErrors([]);
    }
  }, [password]);

  /**
   * Checks if token is present on component mount.
   */
  useEffect(() => {
    if (!token) {
      setError('Ungültiger oder fehlender Token. Bitte überprüfen Sie den Link aus Ihrer E-Mail.');
    }
  }, [token]);

  /**
   * Handles password setup form submission.
   * Validates password requirements and confirmation match before submission.
   *
   * @param {React.FormEvent} e - The form submission event
   *
   * @author Philipp Borkovic
   */
  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    setError('');

    if (!token) {
      setError('Ungültiger oder fehlender Token');
      return;
    }

    const result = passwordSchema.safeParse(password);
    if (!result.success) {
      setError(result.error.issues[0].message);
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwörter stimmen nicht überein');
      return;
    }

    setIsLoading(true);

    try {
      const response = await AuthController.setupPassword(token, password);

      if (response && response.success) {
        setSuccess(true);
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        setError('Fehler beim Einrichten des Passworts. Bitte versuchen Sie es erneut.');
      }
    } catch (err: unknown) {
      const errorMessage = getErrorMessage(err);

      setError(errorMessage || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Password requirement indicator component.
   */
  const PasswordRequirement = ({ met, text }: { met: boolean; text: string }) => (
    <div className="flex items-center gap-2 text-sm">
      <div className={`w-4 h-4 rounded-full flex items-center justify-center ${met ? 'bg-green-500' : 'bg-gray-300'}`}>
        {met && <span className="text-white text-xs">✓</span>}
      </div>
      <span className={met ? 'text-green-600' : 'text-gray-600'}>{text}</span>
    </div>
  );

  if (success) {
    return (
      <div className="flex min-h-screen w-full items-center justify-center p-4">
        <div className="w-full max-w-md space-y-6 text-center">
          <div className="flex flex-col items-center space-y-4">
            <img
              src={logo}
              alt="Bunkermuseum Logo"
              className="h-16 w-auto object-contain mb-2"
            />
            <div className="rounded-full bg-green-100 p-4">
              <svg className="h-12 w-12 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h1 className="text-2xl font-bold text-black">Passwort erfolgreich eingerichtet!</h1>
            <p className="text-gray-600">
              Sie werden in Kürze zur Anmeldeseite weitergeleitet...
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen w-full items-center justify-center p-4">
      <div className="w-full max-w-md space-y-6">
        {/* Logo */}
        <div className="flex flex-col items-center space-y-2 text-center">
          <img
            src={logo}
            alt="Bunkermuseum Logo"
            className="h-16 w-auto object-contain mb-2"
          />
          <h1 className="text-2xl font-bold text-black">Passwort einrichten</h1>
          <p className="text-sm text-muted-foreground">
            Bitte wählen Sie ein sicheres Passwort für Ihr Konto
          </p>
        </div>

        {/* Password Setup Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="password">Neues Passwort</Label>
            <Input
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={isLoading || !token}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Passwort bestätigen</Label>
            <Input
              id="confirmPassword"
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={isLoading || !token}
              required
            />
          </div>

          {/* Password Requirements */}
          {password && (
            <div className="rounded-md bg-gray-50 p-4 space-y-2">
              <p className="text-sm font-medium text-gray-700 mb-2">Passwort-Anforderungen:</p>
              <PasswordRequirement
                met={password.length >= 8}
                text="Mindestens 8 Zeichen"
              />
              <PasswordRequirement
                met={/[a-z]/.test(password)}
                text="Mindestens ein Kleinbuchstabe"
              />
              <PasswordRequirement
                met={/[A-Z]/.test(password)}
                text="Mindestens ein Großbuchstabe"
              />
              <PasswordRequirement
                met={/\d/.test(password)}
                text="Mindestens eine Ziffer"
              />
              <PasswordRequirement
                met={/[@$!%*?&#]/.test(password)}
                text="Mindestens ein Sonderzeichen (@$!%*?&#)"
              />
              {confirmPassword && (
                <PasswordRequirement
                  met={password === confirmPassword}
                  text="Passwörter stimmen überein"
                />
              )}
            </div>
          )}

          {error && (
            <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
              {error}
            </div>
          )}

          <Button
            type="submit"
            className="w-full bg-black text-white hover:bg-black/90"
            disabled={isLoading || !token || validationErrors.length > 0 || password !== confirmPassword}
          >
            {isLoading ? 'Passwort wird eingerichtet...' : 'Passwort einrichten'}
          </Button>
        </form>

        {/* Back to Login Link */}
        <div className="text-center text-sm">
          <span className="text-muted-foreground">Bereits ein Passwort? </span>
          <a href="/login" className="text-primary hover:underline">
            Zur Anmeldung
          </a>
        </div>
      </div>
    </div>
  );
}
