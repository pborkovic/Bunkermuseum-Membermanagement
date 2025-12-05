import { Outlet } from 'react-router';
import { ProgressBar } from '@vaadin/react-components';
import { Suspense } from 'react';
import { ThemeProvider } from 'Frontend/contexts/ThemeContext';
import { ErrorBoundary } from 'Frontend/components';

/**
 * MainLayout component - Root layout for the entire application.
 *
 * This component provides the foundational structure for all application routes:
 * - Wraps the app in ThemeProvider for dark/light mode support
 * - Provides error boundary to catch React errors
 * - Provides a loading indicator during route transitions via Suspense
 * - Renders child routes through React Router's Outlet
 *
 * Layout hierarchy:
 * MainLayout (this component)
 *   └─ ThemeProvider (theme context)
 *      └─ ErrorBoundary (error handling)
 *         └─ Suspense (loading boundary)
 *            └─ Outlet (route-specific content)
 *
 * @component
 *
 * @returns {JSX.Element} The main application layout wrapper
 *
 * @layout.tsx
 */
export default function MainLayout(): JSX.Element {
  return (
    <ThemeProvider>
      <ErrorBoundary>
        <Suspense fallback={<ProgressBar indeterminate={true} className="m-0" />}>
          <Outlet />
        </Suspense>
      </ErrorBoundary>
    </ThemeProvider>
  );
}
