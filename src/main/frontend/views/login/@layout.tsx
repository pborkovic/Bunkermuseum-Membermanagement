import { Outlet } from 'react-router';
import { ThemeProvider } from 'Frontend/contexts/ThemeContext';

/**
 * LoginLayout component - Minimal layout wrapper for the login page.
 *
 * @component
 *
 * @returns {JSX.Element} The login layout wrapper
 *
 * @layout.tsx
 *
 * @author Philipp Borkovic
 */
export default function LoginLayout(): JSX.Element {
  return (
    <ThemeProvider>
      <Outlet />
    </ThemeProvider>
  );
}
