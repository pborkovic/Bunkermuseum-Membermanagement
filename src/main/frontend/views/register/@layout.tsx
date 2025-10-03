import { Outlet } from 'react-router';
import { ThemeProvider } from 'Frontend/contexts/ThemeContext';

/**
 * RegisterLayout component - Minimal layout wrapper for the registration page.
 *
 * @component
 *
 * @returns {JSX.Element} The registration layout wrapper
 *
 * @layout.tsx
 *
 * @author Philipp Borkovic
 */
export default function RegisterLayout(): JSX.Element {
  return (
    <ThemeProvider>
      <Outlet />
    </ThemeProvider>
  );
}
