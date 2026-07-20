import {useState} from 'react';
import {ViewConfig} from '@vaadin/hilla-file-router/types.js';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {AuthController} from 'Frontend/generated/endpoints';
import {executeRecaptcha} from '../../lib/recaptcha';
import {getErrorMessage} from '../../types/vaadin';
import logo from 'Frontend/assets/images/logo_bunkermuseum.jpg';

/**
 * Route configuration for the forgot-password view.
 * Excluded from navigation menu and accessible without login.
 */
export const config: ViewConfig = {
  menu: { exclude: true },
  route: 'forgot-password',
  loginRequired: false,
  flowLayout: false,
};

/**
 * ForgotPasswordView component - request a password reset link.
 *
 * The user enters their email address; the backend sends a reset link if an
 * account exists. For security (to prevent account enumeration) the UI always
 * shows the same generic confirmation, regardless of whether the email exists.
 *
 * Features:
 * - Email entry with basic validation
 * - reCAPTCHA v3 bot protection (action "reset_password")
 * - Generic, enumeration-safe confirmation message
 * - Loading state during submission
 *
 * @component
 * @returns {JSX.Element} The forgot-password view
 *
 * @author Philipp Borkovic
 */
export default function ForgotPasswordView(): JSX.Element {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [submitted, setSubmitted] = useState(false);

  /**
   * Handles the reset-request form submission. Generates a reCAPTCHA token and
   * calls the backend, then shows a generic confirmation.
   *
   * @param {React.FormEvent} e - The form submission event
   *
   * @author Philipp Borkovic
   */
  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    setError('');

    if (!email.trim()) {
      setError('Bitte geben Sie Ihre E-Mail-Adresse ein.');
      return;
    }

    setIsLoading(true);

    try {
      const recaptchaToken = await executeRecaptcha('reset_password');
      await AuthController.requestPasswordReset(email.trim(), recaptchaToken);

      // Always show the same confirmation, regardless of whether the account exists.
      setSubmitted(true);
    } catch (err: unknown) {
      const errorMessage = getErrorMessage(err);

      setError(errorMessage || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');
    } finally {
      setIsLoading(false);
    }
  };

  if (submitted) {
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
            <h1 className="text-2xl font-bold text-black">E-Mail gesendet</h1>
            <p className="text-gray-600">
              Falls ein Konto mit dieser E-Mail-Adresse existiert, haben wir Ihnen einen Link zum
              Zurücksetzen Ihres Passworts gesendet. Bitte prüfen Sie auch Ihren Spam-Ordner.
            </p>
          </div>
          <div className="text-center text-sm">
            <a href="/login" className="text-primary hover:underline">
              Zurück zur Anmeldung
            </a>
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
          <h1 className="text-2xl font-bold text-black">Passwort vergessen?</h1>
          <p className="text-sm text-muted-foreground">
            Geben Sie Ihre E-Mail-Adresse ein und wir senden Ihnen einen Link zum Zurücksetzen.
          </p>
        </div>

        {/* Request Form */}
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

          {error && (
            <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
              {error}
            </div>
          )}

          <Button
            type="submit"
            className="w-full bg-black text-white hover:bg-black/90"
            disabled={isLoading}
          >
            {isLoading ? 'Wird gesendet...' : 'Link zum Zurücksetzen senden'}
          </Button>
        </form>

        {/* Back to Login Link */}
        <div className="text-center text-sm">
          <span className="text-muted-foreground">Passwort wieder eingefallen? </span>
          <a href="/login" className="text-primary hover:underline">
            Zur Anmeldung
          </a>
        </div>
      </div>
    </div>
  );
}
