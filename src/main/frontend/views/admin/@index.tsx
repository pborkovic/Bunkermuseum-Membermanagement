import { useState, useEffect } from 'react';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Icon } from '@vaadin/react-components';
import { Button } from '@/components/ui/button';
import { AuthController } from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import UsersTab from './components/UsersTab';
import BookingsTab from './components/BookingsTab';
import SettingsTab from './components/SettingsTab';
import logo from 'Frontend/assets/images/logo_bunkermuseum.jpg';

/**
 * Route configuration for the admin dashboard view.
 * Requires login to access.
 */
export const config: ViewConfig = {
  menu: { title: 'Admin Dashboard', icon: 'vaadin:dashboard' },
  route: 'admin',
  loginRequired: false,
  flowLayout: false,
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
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);

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
    <div className="h-screen bg-white flex flex-col overflow-hidden">
      {/* Navbar */}
      <nav className="w-full border-b bg-white z-10 flex-shrink-0">
        <div className="flex flex-col px-3 py-2 gap-2 sm:flex-row sm:items-center sm:justify-between sm:px-6 sm:py-4 sm:gap-0">
          {/* Logo/Title */}
          <div className="flex items-center gap-2 sm:gap-3">
            <img
              src={logo}
              alt="Bunkermuseum Logo"
              className="h-8 sm:h-12 w-auto object-contain"
            />
            <div>
              <h1 className="text-sm sm:text-xl font-semibold text-black">Membermanagement</h1>
              <p className="text-xs sm:text-sm text-gray-600">Admin Dashboard</p>
            </div>
          </div>

          {/* Navigation Links */}
          <div className="flex items-center gap-2 sm:gap-8 overflow-x-auto">
            <button
              onClick={() => setSelectedTab(0)}
              className={`flex items-center gap-1 sm:gap-2 px-2 sm:px-3 py-1.5 sm:py-2 text-xs sm:text-sm font-medium transition-colors whitespace-nowrap ${
                selectedTab === 0
                  ? 'text-black border-b-2 border-black'
                  : 'text-gray-600 hover:text-black'
              }`}
            >
              <Icon icon="vaadin:users" style={{ width: '16px', height: '16px' }} />
              Mitglieder
            </button>
            <button
              onClick={() => setSelectedTab(1)}
              className={`flex items-center gap-1 sm:gap-2 px-2 sm:px-3 py-1.5 sm:py-2 text-xs sm:text-sm font-medium transition-colors whitespace-nowrap ${
                selectedTab === 1
                  ? 'text-black border-b-2 border-black'
                  : 'text-gray-600 hover:text-black'
              }`}
            >
              <Icon icon="vaadin:invoice" style={{ width: '16px', height: '16px' }} />
              Buchungen
            </button>
            <button
              onClick={() => setSelectedTab(2)}
              className={`flex items-center gap-1 sm:gap-2 px-2 sm:px-3 py-1.5 sm:py-2 text-xs sm:text-sm font-medium transition-colors whitespace-nowrap ${
                selectedTab === 2
                  ? 'text-black border-b-2 border-black'
                  : 'text-gray-600 hover:text-black'
              }`}
            >
              <Icon icon="vaadin:cog" style={{ width: '16px', height: '16px' }} />
              Einstellungen
            </button>
          </div>

          {/* Right side - User Info */}
          <div className="hidden sm:flex items-center gap-3">
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
      <main className="flex-1 w-full px-6 py-6 overflow-hidden">
        {selectedTab === 0 && <UsersTab />}
        {selectedTab === 1 && <BookingsTab />}
        {selectedTab === 2 && <SettingsTab />}
      </main>
    </div>
  );
}
