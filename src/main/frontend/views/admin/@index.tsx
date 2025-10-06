import { useState } from 'react';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Icon } from '@vaadin/react-components';
import { Button } from '@/components/ui/button';
import UsersTab from './components/UsersTab';
import BookingsTab from './components/BookingsTab';
import SettingsTab from './components/SettingsTab';

/**
 * Route configuration for the admin dashboard view.
 * Requires login to access.
 */
export const config: ViewConfig = {
  menu: { title: 'Admin Dashboard', icon: 'vaadin:dashboard' },
  route: 'admin',
  loginRequired: false,
};

/**
 * Admin Dashboard component - Central administration interface for the Bunker Museum application.
 *
 * Features:
 * - Tab-based navigation (Users, Bookings, Settings)
 * - Users tab: View all users with detailed modal
 * - Bookings tab: View all bookings with detailed modal
 * - Settings tab: Admin profile management
 *
 * @component
 *
 * @returns {JSX.Element} The admin dashboard view
 *
 * @author Philipp Borkovic
 */
export default function AdminDashboard(): JSX.Element {
  const [selectedTab, setSelectedTab] = useState(0);

  return (
    <div className="flex min-h-screen flex-col">
      {/* Header */}
      <div className="border-b">
        <div className="container mx-auto px-6 py-6">
          <div className="flex items-center gap-3">
            <Icon icon="vaadin:dashboard" className="text-primary" style={{ width: '32px', height: '32px' }} />
            <div>
              <h1 className="text-2xl font-semibold">Admin Dashboard</h1>
              <p className="text-sm text-muted-foreground">Verwaltung von Benutzern und Buchungen</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main content with tabs */}
      <div className="container mx-auto flex-1 px-6 py-6">
        {/* Tab navigation */}
        <div className="mb-6 flex gap-1 border-b">
          <Button
            variant={selectedTab === 0 ? "default" : "ghost"}
            onClick={() => setSelectedTab(0)}
            className="gap-2"
          >
            <Icon icon="vaadin:users" style={{ width: '16px', height: '16px' }} />
            Benutzer
          </Button>
          <Button
            variant={selectedTab === 1 ? "default" : "ghost"}
            onClick={() => setSelectedTab(1)}
            className="gap-2"
          >
            <Icon icon="vaadin:invoice" style={{ width: '16px', height: '16px' }} />
            Buchungen
          </Button>
          <Button
            variant={selectedTab === 2 ? "default" : "ghost"}
            onClick={() => setSelectedTab(2)}
            className="gap-2"
          >
            <Icon icon="vaadin:cog" style={{ width: '16px', height: '16px' }} />
            Einstellungen
          </Button>
        </div>

        {/* Tab content */}
        <div className="pt-4">
          {selectedTab === 0 && <UsersTab />}
          {selectedTab === 1 && <BookingsTab />}
          {selectedTab === 2 && <SettingsTab />}
        </div>
      </div>
    </div>
  );
}
