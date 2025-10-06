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
    <div className="min-h-screen bg-white">
      {/* Full Navbar */}
      <nav className="border-b bg-white">
        <div className="container mx-auto px-6 py-4">
          {/* Logo/Title */}
          <div className="flex items-center justify-center gap-3 mb-6">
            <Icon icon="vaadin:dashboard" className="text-primary" style={{ width: '32px', height: '32px' }} />
            <div className="text-center">
              <h1 className="text-2xl font-semibold">Admin Dashboard</h1>
              <p className="text-sm text-muted-foreground">Verwaltung von Benutzern und Buchungen</p>
            </div>
          </div>

          {/* Centered Tab Navigation */}
          <div className="flex gap-2 justify-center">
            <Button
              variant={selectedTab === 0 ? "default" : "outline"}
              onClick={() => setSelectedTab(0)}
              className="gap-2"
            >
              <Icon icon="vaadin:users" style={{ width: '16px', height: '16px' }} />
              Benutzer
            </Button>
            <Button
              variant={selectedTab === 1 ? "default" : "outline"}
              onClick={() => setSelectedTab(1)}
              className="gap-2"
            >
              <Icon icon="vaadin:invoice" style={{ width: '16px', height: '16px' }} />
              Buchungen
            </Button>
            <Button
              variant={selectedTab === 2 ? "default" : "outline"}
              onClick={() => setSelectedTab(2)}
              className="gap-2"
            >
              <Icon icon="vaadin:cog" style={{ width: '16px', height: '16px' }} />
              Einstellungen
            </Button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="container mx-auto p-6">
        {selectedTab === 0 && <UsersTab />}
        {selectedTab === 1 && <BookingsTab />}
        {selectedTab === 2 && <SettingsTab />}
      </main>
    </div>
  );
}
