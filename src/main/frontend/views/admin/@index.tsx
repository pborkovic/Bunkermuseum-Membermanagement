import { useState, useEffect } from 'react';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Icon } from '@vaadin/react-components';
import { Button } from '@/components/ui/button';
import { AuthController } from 'Frontend/generated/endpoints';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';
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
  const [currentUser, setCurrentUser] = useState<User | null>(null);

  useEffect(() => {
    loadCurrentUser();
  }, []);

  const loadCurrentUser = async () => {
    try {
      const user = await AuthController.getCurrentUser();
      setCurrentUser(user || null);
    } catch (err) {
      console.error('Failed to load current user:', err);
    }
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Navbar */}
      <nav className="w-full border-b bg-white sticky top-0 z-10">
        <div className="flex items-center justify-between px-6 py-4">
          {/* Logo/Title */}
          <div className="flex items-center gap-3">
            <Icon icon="vaadin:dashboard" style={{ width: '32px', height: '32px' }} />
            <div>
              <h1 className="text-xl font-semibold text-black">Bunkermuseum Membermanagement</h1>
              <p className="text-sm text-gray-600">Wurzenpass Kärnten</p>
            </div>
          </div>

          {/* Navigation Links */}
          <div className="flex items-center gap-8">
            <button
              onClick={() => setSelectedTab(0)}
              className={`flex items-center gap-2 px-3 py-2 text-sm font-medium transition-colors ${
                selectedTab === 0
                  ? 'text-black border-b-2 border-black'
                  : 'text-gray-600 hover:text-black'
              }`}
            >
              <Icon icon="vaadin:users" style={{ width: '18px', height: '18px' }} />
              Benutzer
            </button>
            <button
              onClick={() => setSelectedTab(1)}
              className={`flex items-center gap-2 px-3 py-2 text-sm font-medium transition-colors ${
                selectedTab === 1
                  ? 'text-black border-b-2 border-black'
                  : 'text-gray-600 hover:text-black'
              }`}
            >
              <Icon icon="vaadin:invoice" style={{ width: '18px', height: '18px' }} />
              Buchungen
            </button>
            <button
              onClick={() => setSelectedTab(2)}
              className={`flex items-center gap-2 px-3 py-2 text-sm font-medium transition-colors ${
                selectedTab === 2
                  ? 'text-black border-b-2 border-black'
                  : 'text-gray-600 hover:text-black'
              }`}
            >
              <Icon icon="vaadin:cog" style={{ width: '18px', height: '18px' }} />
              Einstellungen
            </button>
          </div>

          {/* Right side - User Info */}
          <div className="flex items-center gap-3">
            <div className="text-right">
              <p className="text-sm font-medium text-black">{currentUser?.name || 'Loading...'}</p>
              <p className="text-xs text-gray-600">{currentUser?.email || ''}</p>
            </div>
            <div className="flex items-center justify-center w-10 h-10 rounded-full bg-gray-200">
              <Icon icon="vaadin:user" style={{ width: '20px', height: '20px' }} />
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="w-full px-6 py-6">
        {selectedTab === 0 && <UsersTab />}
        {selectedTab === 1 && <BookingsTab />}
        {selectedTab === 2 && <SettingsTab />}
      </main>
    </div>
  );
}
