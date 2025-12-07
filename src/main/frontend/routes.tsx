/**
 * Custom Routes Configuration
 *
 * This file overrides the auto-generated routes to add custom error handling.
 * Instead of using file-routes (which may not be generated), we manually define
 * routes and add proper 404/500 error handling.
 *
 * IMPORTANT: We do NOT use withFallback(Flow) because it would intercept all
 * unmatched routes and cause "Connection lost" errors. Instead, we use a catch-all
 * route (*) to show our custom 404 page.
 */
import { RouterConfigurationBuilder } from '@vaadin/hilla-file-router/runtime.js';
import MainLayout from 'Frontend/views/@layout';
import RootView from 'Frontend/views/@index';
import LoginLayout from 'Frontend/views/login/@layout';
import LoginView from 'Frontend/views/login/@index';
import RegisterLayout from 'Frontend/views/register/@layout';
import RegisterView from 'Frontend/views/register/@index';
import SetupPasswordView from 'Frontend/views/setup-password/@index';
import DashboardSelectionView from 'Frontend/views/dashboard-selection/@index';
import AdminView from 'Frontend/views/admin/@index';
import MemberView from 'Frontend/views/member/@index';
import NotFoundPage from 'Frontend/views/404';
import ServerErrorPage from 'Frontend/views/500';

export const { router, routes } = new RouterConfigurationBuilder()
  .withReactRoutes([
    {
      element: <MainLayout />,
      handle: { title: 'Bunkermuseum' },
      children: [
        { path: '/', element: <RootView />, handle: { title: 'Home' } },
        {
          path: '/login',
          element: <LoginLayout />,
          handle: { title: 'Login' },
          children: [
            { index: true, element: <LoginView /> },
          ],
        },
        {
          path: '/register',
          element: <RegisterLayout />,
          handle: { title: 'Register' },
          children: [
            { index: true, element: <RegisterView /> },
          ],
        },
        { path: '/setup-password', element: <SetupPasswordView />, handle: { title: 'Passwort einrichten' } },
        { path: '/dashboard-selection', element: <DashboardSelectionView />, handle: { title: 'Select Dashboard' } },
        { path: '/admin/*', element: <AdminView />, handle: { title: 'Admin Dashboard' } },
        { path: '/member/*', element: <MemberView />, handle: { title: 'Member Dashboard' } },
        { path: '/404', element: <NotFoundPage />, handle: { title: 'Page Not Found' } },
        { path: '/500', element: <ServerErrorPage />, handle: { title: 'Server Error' } },
        { path: '*', element: <NotFoundPage />, handle: { title: 'Page Not Found' } },
      ],
    },
  ])
  .protect()
  .build();
