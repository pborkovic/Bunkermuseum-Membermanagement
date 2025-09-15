import { Outlet, useLocation, useNavigate } from 'react-router';
import '@vaadin/icons';
import { AppLayout, Icon, ProgressBar, Scroller, SideNav, SideNavItem } from '@vaadin/react-components';
import { Suspense, useMemo } from 'react';
import { createMenuItems } from '@vaadin/hilla-file-router/runtime.js';
import { ThemeToggle } from 'Frontend/components/ThemeToggle';
import { ThemeProvider } from 'Frontend/contexts/ThemeContext';

function Header() {
  // TODO Replace with real application logo and name
  return (
    <div className="flex p-m gap-m items-center justify-between" slot="drawer">
      <div className="flex gap-m items-center">
        <Icon icon="vaadin:cubes" className="text-primary icon-l" />
        <span className="font-semibold text-l">Bunker Museum</span>
      </div>
      <ThemeToggle />
    </div>
  );
}

function MainMenu() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <SideNav className="mx-m" onNavigate={({ path }) => path != null && navigate(path)} location={location}>
      {createMenuItems().map(({ to, icon, title }) => (
        <SideNavItem path={to} key={to}>
          {icon && <Icon icon={icon} slot="prefix" />}
          {title}
        </SideNavItem>
      ))}
    </SideNav>
  );
}

export default function MainLayout() {
  return (
    <ThemeProvider>
      <AppLayout primarySection="drawer">
        <Header />
        <Scroller slot="drawer">
          <MainMenu />
        </Scroller>
        <Suspense fallback={<ProgressBar indeterminate={true} className="m-0" />}>
          <Outlet />
        </Suspense>
      </AppLayout>
    </ThemeProvider>
  );
}
