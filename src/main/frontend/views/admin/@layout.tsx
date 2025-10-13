import { Outlet } from 'react-router';
import { ThemeProvider } from 'Frontend/contexts/ThemeContext';

/**
 * AdminLayout component - Layout wrapper for the admin section.
 *
 * @component
 *
 * @returns {JSX.Element} The admin layout wrapper
 *
 * @layout.tsx
 *
 * @author Philipp Borkovic
 */
export default function AdminLayout(): JSX.Element {
  return (
    <ThemeProvider>
      <Outlet />
    </ThemeProvider>
  );
}
