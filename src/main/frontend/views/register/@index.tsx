import { useState } from 'react';
import { useNavigate } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { Icon } from '@vaadin/react-components';

/**
 * Validation constants for the registration form.
 *
 * @author Philipp Borkovic
 */
const VALIDATION = {
  MIN_PASSWORD_LENGTH: 8,
  MAX_POSTLEITZAHL_LENGTH: 5,
} as const;

/**
 * Error messages for form validation.
 *
 * @author Philipp Borkovic
 */
const ERROR_MESSAGES = {
  PASSWORD_MISMATCH: 'Passwörter stimmen nicht überein',
  PASSWORD_TOO_SHORT: 'Passwort muss mindestens 8 Zeichen lang sein',
  GENERIC_ERROR: 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.',
} as const;

/**
 * Gender options for the Anrede (salutation) field.
 *
 * @author Philipp Borkovic
 */
const ANREDE_OPTIONS = [
  { value: 'männlich', label: 'Männlich' },
  { value: 'weiblich', label: 'Weiblich' },
  { value: 'divers', label: 'Divers' },
] as const;

/**
 * Route configuration for the registration view.
 * Configures this route to be excluded from navigation menu and accessible without login.
 *
 * @author Philipp Borkovic
 */
export const config: ViewConfig = {
  menu: { exclude: true },
  route: 'register',
  loginRequired: false,
  flowLayout: false,
};

/**
 * User registration data interface.
 * Represents all fields collected during the registration process.
 *
 * @interface RegistrationFormData
 *
 * @author Philipp Borkovic
 */
interface RegistrationFormData {
  anrede: string;
  akademischerTitel: string;
  dienstgrad: string;
  name: string;
  email: string;
  geburtstag?: Date;
  telefon: string;
  strasse: string;
  stadt: string;
  postleitzahl: string;
  password: string;
}

/**
 * Validates password requirements.
 *
 * @param {string} password - The password to validate
 * @param {string} confirmPassword - The password confirmation
 * @returns {string | null} Error message if validation fails, null otherwise
 *
 * @author Philipp Borkovic
 */
function validatePassword(password: string, confirmPassword: string): string | null {
  if (password !== confirmPassword) {
    return ERROR_MESSAGES.PASSWORD_MISMATCH;
  }

  if (password.length < VALIDATION.MIN_PASSWORD_LENGTH) {
    return ERROR_MESSAGES.PASSWORD_TOO_SHORT;
  }

  return null;
}

/**
 * Registration view component for the Bunker Museum application.
 * Provides a comprehensive registration form with German language support.
 *
 * Features:
 * - Multi-field registration form with personal and address information
 * - Password validation with confirmation
 * - Date picker for birthday selection
 * - Loading states and error handling
 * - Responsive two-column layout
 * - German language interface
 *
 * @component
 *
 * @returns {JSX.Element} The registration view
 *
 * @index.tsx
 *
 * @author Philipp Borkovic
 */
export default function RegisterView(): JSX.Element {
  const navigate = useNavigate();

  const [anrede, setAnrede] = useState('');
  const [akademischerTitel, setAkademischerTitel] = useState('');
  const [dienstgrad, setDienstgrad] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [geburtstag, setGeburtstag] = useState<Date>();
  const [telefon, setTelefon] = useState('');
  const [strasse, setStrasse] = useState('');
  const [stadt, setStadt] = useState('');
  const [postleitzahl, setPostleitzahl] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  /**
   * Handles form submission and registration process.
   * Validates passwords, submits registration data, and navigates to login on success.
   *
   * @param {React.FormEvent} e - The form submission event
   *
   * @author Philipp Borkovic
   */
  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    setError('');

    const passwordError = validatePassword(password, confirmPassword);
    if (passwordError) {
      setError(passwordError);
      return;
    }

    setIsLoading(true);

    try {
      // TODO: Implement registration API call
      setTimeout(() => {
        setIsLoading(false);
        navigate('/login');
      }, 1000);
    } catch (err: any) {
      setError(err.message || ERROR_MESSAGES.GENERIC_ERROR);
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen w-full">
      {/* Left side - blank (desktop only) */}
      <div className="hidden lg:flex lg:w-1/2 bg-muted" />

      {/* Right side - register form */}
      <div className="flex w-full items-center justify-center p-4 lg:w-1/2 lg:p-8">
        <div className="w-full max-w-2xl space-y-4">
          {/* Logo */}
          <div className="flex flex-col items-center space-y-2 text-center">
            <Icon icon="vaadin:cubes" className="text-primary" style={{ width: '48px', height: '48px' }} />
            <h1 className="text-2xl font-semibold tracking-tight">Bunker Museum</h1>
            <p className="text-sm text-muted-foreground">Erstellen Sie Ihr Konto</p>
          </div>

          {/* Register form */}
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="anrede" className="text-sm">Anrede</Label>
                <Select value={anrede} onValueChange={setAnrede} disabled={isLoading} required>
                  <SelectTrigger className="h-9">
                    <SelectValue placeholder="Wählen" />
                  </SelectTrigger>
                  <SelectContent>
                    {ANREDE_OPTIONS.map(option => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="akademischerTitel" className="text-sm">Akademischer Titel</Label>
                <Input
                  id="akademischerTitel"
                  type="text"
                  placeholder="Dr., Prof."
                  value={akademischerTitel}
                  onChange={(e) => setAkademischerTitel(e.target.value)}
                  disabled={isLoading}
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="dienstgrad" className="text-sm">Dienstgrad</Label>
                <Input
                  id="dienstgrad"
                  type="text"
                  placeholder="Optional"
                  value={dienstgrad}
                  onChange={(e) => setDienstgrad(e.target.value)}
                  disabled={isLoading}
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="name" className="text-sm">Name</Label>
                <Input
                  id="name"
                  type="text"
                  placeholder="Max Mustermann"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  disabled={isLoading}
                  required
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="email" className="text-sm">E-Mail</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="name@beispiel.de"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isLoading}
                  required
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="geburtstag" className="text-sm">Geburtstag</Label>
                <DatePicker
                  value={geburtstag}
                  onChange={setGeburtstag}
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="telefon" className="text-sm">Telefon</Label>
                <Input
                  id="telefon"
                  type="tel"
                  placeholder="+49 123 456789"
                  value={telefon}
                  onChange={(e) => setTelefon(e.target.value)}
                  disabled={isLoading}
                  required
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="strasse" className="text-sm">Straße & Hausnummer</Label>
                <Input
                  id="strasse"
                  type="text"
                  placeholder="Musterstraße 123"
                  value={strasse}
                  onChange={(e) => setStrasse(e.target.value)}
                  disabled={isLoading}
                  required
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="postleitzahl" className="text-sm">Postleitzahl</Label>
                <Input
                  id="postleitzahl"
                  type="text"
                  placeholder="12345"
                  value={postleitzahl}
                  onChange={(e) => setPostleitzahl(e.target.value)}
                  disabled={isLoading}
                  required
                  maxLength={VALIDATION.MAX_POSTLEITZAHL_LENGTH}
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="stadt" className="text-sm">Stadt/Ort</Label>
                <Input
                  id="stadt"
                  type="text"
                  placeholder="Berlin"
                  value={stadt}
                  onChange={(e) => setStadt(e.target.value)}
                  disabled={isLoading}
                  required
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="password" className="text-sm">Passwort</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={isLoading}
                  required
                  minLength={VALIDATION.MIN_PASSWORD_LENGTH}
                  className="h-9"
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="confirmPassword" className="text-sm">Passwort bestätigen</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  disabled={isLoading}
                  required
                  minLength={VALIDATION.MIN_PASSWORD_LENGTH}
                  className="h-9"
                />
              </div>
            </div>
            {error && (
              <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
                {error}
              </div>
            )}
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Registrieren...' : 'Registrieren'}
            </Button>
          </form>

          {/* Login link */}
          <div className="text-center text-sm">
            <span className="text-muted-foreground">Bereits Mitglied? </span>
            <a href="/login" className="text-primary hover:underline">
              Jetzt anmelden
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
