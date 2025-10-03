import { useState } from 'react';
import { useNavigate } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { Icon } from '@vaadin/react-components';
import { z } from 'zod';
import { subYears } from 'date-fns';
import { AuthController } from 'Frontend/generated/endpoints';

/**
 * Validation constants for the registration form.
 *
 * @author Philipp Borkovic
 */
const VALIDATION = {
  MIN_PASSWORD_LENGTH: 8,
  MAX_PASSWORD_LENGTH: 128,
  MIN_SPECIAL_CHARS: 2,
  MAX_AGE_YEARS: 110,
  MIN_POSTLEITZAHL_LENGTH: 4,
} as const;

/**
 * Regex pattern for counting special characters in password.
 * Matches: !@#$%^&*()_+-=[]{}|;:'",.<>?/~`
 */
const SPECIAL_CHAR_REGEX = /[!@#$%^&*()_+\-=\[\]{}|;:'",.<>?/~`]/g;

/**
 * List of commonly used passwords that must be rejected.
 * These passwords are too weak and commonly found in breach databases.
 *
 * @author Philipp Borkovic
 */
const FORBIDDEN_PASSWORDS = [
  "password", "123456", "123456789", "12345678", "12345", "1234567",
  "password1", "1234567890", "qwerty", "abc123", "111111", "123123",
  "admin", "letmein", "welcome", "monkey", "dragon", "master", "sunshine",
  "princess", "football", "qwerty123", "solo", "passw0rd", "starwars",
  "password123", "login", "admin123", "root", "toor", "pass", "test",
  "guest", "oracle", "cisco", "changeme", "administrator", "user"
] as const;

/**
 * Zod validation schema for registration form.
 * Validates all required fields with appropriate constraints.
 *
 * @author Philipp Borkovic
 */
const registrationSchema = z.object({
  salutation: z.string().min(1, 'Anrede ist erforderlich'),
  academicTitle: z.string().optional(),
  rank: z.string().optional(),
  name: z.string().min(1, 'Name ist erforderlich'),
  email: z.string().email('Ungültige E-Mail-Adresse'),
  birthday: z.date()
    .max(new Date(), 'Geburtsdatum darf nicht in der Zukunft liegen')
    .min(
      subYears(new Date(), VALIDATION.MAX_AGE_YEARS),
      `Sie können nicht älter als ${VALIDATION.MAX_AGE_YEARS} Jahre sein`
    )
    .optional()
    .refine((date) => date !== undefined, 'Geburtstag ist erforderlich'),
  phone: z.string().min(1, 'Telefonnummer ist erforderlich'),
  street: z.string().min(1, 'Straße ist erforderlich'),
  city: z.string().min(1, 'Stadt ist erforderlich'),
  postalCode: z.string()
    .min(VALIDATION.MIN_POSTLEITZAHL_LENGTH, `Postleitzahl muss mindestens ${VALIDATION.MIN_POSTLEITZAHL_LENGTH} Zeichen lang sein`)
    .regex(/^\d+$/, 'Postleitzahl darf nur Zahlen enthalten'),
  password: z.string()
    .min(VALIDATION.MIN_PASSWORD_LENGTH, `Passwort muss mindestens ${VALIDATION.MIN_PASSWORD_LENGTH} Zeichen lang sein`)
    .max(VALIDATION.MAX_PASSWORD_LENGTH, `Passwort darf maximal ${VALIDATION.MAX_PASSWORD_LENGTH} Zeichen lang sein`)
    .refine(
      (password) => (password.match(SPECIAL_CHAR_REGEX) || []).length >= VALIDATION.MIN_SPECIAL_CHARS,
      `Passwort muss mindestens ${VALIDATION.MIN_SPECIAL_CHARS} Sonderzeichen enthalten`
    )
    .refine(
      (password) => !FORBIDDEN_PASSWORDS.includes(password.toLowerCase() as any),
      'Dieses Passwort ist zu häufig verwendet und nicht sicher'
    ),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Passwörter stimmen nicht überein',
  path: ['confirmPassword'],
});

/**
 * Type derived from Zod schema for type-safety
 */
type RegistrationFormInput = z.infer<typeof registrationSchema>;

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
   * Validates all form fields using Zod schema, then submits registration data.
   *
   * @param {React.FormEvent} e - The form submission event
   *
   * @author Philipp Borkovic
   */
  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    setError('');

    try {
      const validatedData = registrationSchema.parse({
        salutation: anrede,
        academicTitle: akademischerTitel,
        rank: dienstgrad,
        name,
        email,
        birthday: geburtstag,
        phone: telefon,
        street: strasse,
        city: stadt,
        postalCode: postleitzahl,
        password,
        confirmPassword,
      });

      setIsLoading(true);

      const response = await AuthController.register({
        name: validatedData.name,
        email: validatedData.email,
        password: validatedData.password,
        salutation: validatedData.salutation,
        academicTitle: validatedData.academicTitle || '',
        rank: validatedData.rank || '',
        birthday: validatedData.birthday!.toISOString().split('T')[0],
        phone: validatedData.phone,
        street: validatedData.street,
        city: validatedData.city,
        postalCode: validatedData.postalCode,
      });

      if (response?.success) {
        navigate('/login');
      } else {
        setError(response?.message || 'Registrierung fehlgeschlagen');
      }
    } catch (err: any) {
      if (err instanceof z.ZodError) {
        setError(err.issues[0].message);
      } else {
        setError(err.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');
      }
    } finally {
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
                  placeholder="1234"
                  value={postleitzahl}
                  onChange={(e) => setPostleitzahl(e.target.value)}
                  disabled={isLoading}
                  required
                  minLength={VALIDATION.MIN_POSTLEITZAHL_LENGTH}
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
                  maxLength={VALIDATION.MAX_PASSWORD_LENGTH}
                  className="h-9"
                />
                <p className="text-xs text-muted-foreground">
                  {VALIDATION.MIN_PASSWORD_LENGTH}-{VALIDATION.MAX_PASSWORD_LENGTH} Zeichen, mindestens {VALIDATION.MIN_SPECIAL_CHARS} Sonderzeichen
                </p>
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
                  maxLength={VALIDATION.MAX_PASSWORD_LENGTH}
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
