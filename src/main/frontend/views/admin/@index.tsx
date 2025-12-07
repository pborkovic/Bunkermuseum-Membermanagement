import { useState, useEffect, useCallback, memo } from 'react';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { FaUsers, FaFileInvoice, FaEnvelope, FaCog, FaUser } from 'react-icons/fa';
import { Toaster } from '@/components/ui/sonner';
import { toast } from 'sonner';
import { AuthController } from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import UsersTab from './components/UsersTab';
import BookingsTab from './components/BookingsTab';
import EmailsTab from './components/EmailsTab';
import SettingsTab from './components/SettingsTab';
import { TabId } from './types';
import logo from 'Frontend/assets/images/logo_bunkermuseum.jpg';

/**
 * Route configuration for the admin dashboard view.
 *
 * @property {Object} menu - Menu configuration
 * @property {string} menu.title - Display title in navigation menu
 * @property {string} menu.icon - Vaadin icon identifier
 * @property {string} route - URL route path
 * @property {boolean} loginRequired - Whether authentication is required
 * @property {boolean} flowLayout - Whether to use Flow layout system
 */
export const config: ViewConfig = {
  menu: { title: 'Admin Dashboard', icon: 'vaadin:dashboard' },
  route: 'admin',
  loginRequired: false,
  flowLayout: false,
};

/**
 * Admin Dashboard Component
 *
 * Central administration interface for the Bunker Museum application.
 * Provides comprehensive management capabilities for users, bookings, and settings.
 *
 * @component
 *
 * @description
 * This component serves as the main administrative control panel, featuring:
 * - **Tab-based Navigation**: Organized interface for different admin functions
 * - **User Management**: View and manage all registered users
 * - **Booking Management**: Track and manage all bookings with filtering
 * - **Profile Settings**: Admin account configuration and preferences
 * - **Responsive Design**: Adapts to mobile and desktop viewports
 *
 * @features
 * - Real-time user profile loading with avatar support
 * - Persistent tab selection during session
 * - Profile picture caching with cache-busting for updates
 * - Error handling with user-friendly toast notifications
 * - Lazy-loaded tab content for performance optimization
 *
 * @returns {JSX.Element} The rendered admin dashboard with navigation and content
 *
 * @author Philipp Borkovic
 */
export default function AdminDashboard(): JSX.Element {
  const [selectedTab, setSelectedTab] = useState<TabId>(TabId.USERS);
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState<string | null>(null);
  const [isLoadingUser, setIsLoadingUser] = useState<boolean>(true);

  /**
   * Loads the current authenticated user's profile data.
   *
   * Fetches user information from the backend and updates the profile picture URL
   * with a cache-busting timestamp to ensure the latest avatar is displayed.
   *
   * @async
   * @function
   *
   * @description
   * This function:
   * 1. Calls the AuthController to fetch current user data
   * 2. Updates the currentUser state with the response
   * 3. Generates a profile picture URL if avatar path exists
   * 4. Adds timestamp query parameter for cache-busting
   * 5. Handles errors gracefully with console logging and toast notifications
   *
   * @throws {Error} If the backend request fails or user is not authenticated
   *
   * @returns {Promise<void>} A promise that resolves when the user data is loaded
   *
   * @author Philipp Borkovic
   */
  const loadCurrentUser = useCallback(async (): Promise<void> => {
    try {
      setIsLoadingUser(true);
      const user = await AuthController.getCurrentUser();

      if (!user) {
        console.warn('No user data returned from AuthController');
        setCurrentUser(null);
        setProfilePictureUrl(null);

        return;
      }

      setCurrentUser(user);

      if (user.avatarPath && user.id) {
        const timestamp = Date.now();

        setProfilePictureUrl(`/api/upload/profile-picture/${user.id}?t=${timestamp}`);
      } else {
        setProfilePictureUrl(null);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      toast.error('Fehler beim Laden der Benutzerdaten');

      setCurrentUser(null);
      setProfilePictureUrl(null);
    } finally {
      setIsLoadingUser(false);
    }
  }, []);

  useEffect(() => {
    loadCurrentUser();
  }, [loadCurrentUser]);

  /**
   * Handles tab navigation changes.
   *
   * @param {TabId} tabId - The identifier of the tab to navigate to
   *
   * @author Philipp Borkovic
   */
  const handleTabChange = useCallback((tabId: TabId): void => {
    setSelectedTab(tabId);
  }, []);

  /**
   * Renders a navigation tab button.
   *
   * @param {Object} props - Tab button properties
   * @param {TabId} props.tabId - Tab identifier
   * @param {JSX.Element} props.icon - React icon element
   * @param {string} props.label - Tab display label
   * @returns {JSX.Element} Rendered tab button
   */
  const renderTabButton = useCallback(
    ({ tabId, icon, label }: { tabId: TabId; icon: JSX.Element; label: string }): JSX.Element => {
      const isActive = selectedTab === tabId;

      return (
        <button
          onClick={() => handleTabChange(tabId)}
          className={`flex items-center gap-1 sm:gap-2 px-2 sm:px-3 py-1.5 sm:py-2 text-xs sm:text-sm font-medium transition-colors whitespace-nowrap ${
            isActive
              ? 'text-black border-b-2 border-black'
              : 'text-gray-600 hover:text-black'
          }`}
          aria-label={`Navigate to ${label}`}
          aria-current={isActive ? 'page' : undefined}
        >
          <span style={{ width: '16px', height: '16px', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
            {icon}
          </span>
          {label}
        </button>
      );
    },
    [selectedTab, handleTabChange]
  );

  return (
    <div className="h-screen bg-white flex flex-col overflow-hidden">
      <Toaster />

      {/* Navigation Bar */}
      <nav className="w-full border-b bg-white z-10 flex-shrink-0" role="navigation" aria-label="Admin navigation">
        <div className="flex flex-col px-3 py-2 gap-2 sm:flex-row sm:items-center sm:justify-between sm:px-6 sm:py-4 sm:gap-0">
          {/* Logo and Title */}
          <div className="flex items-center gap-2 sm:gap-3 min-w-0 flex-shrink-0">
            <img
              src={logo}
              alt="Bunkermuseum Logo"
              className="h-7 sm:h-10 md:h-12 w-auto object-contain flex-shrink-0"
            />
            <div className="min-w-0 flex-1">
              <h1 className="text-xs sm:text-base md:text-lg lg:text-xl font-semibold text-black whitespace-nowrap overflow-hidden text-ellipsis">
                Mitgliederverwaltung
              </h1>
              <p className="text-[10px] sm:text-xs md:text-sm text-gray-600 whitespace-nowrap">Admin Dashboard</p>
            </div>
          </div>

          {/* Tab Navigation */}
          <div className="flex items-center gap-2 sm:gap-8 overflow-x-auto" role="tablist">
            {renderTabButton({ tabId: TabId.USERS, icon: <FaUsers style={{ width: '16px', height: '16px' }} />, label: 'Mitglieder' })}
            {renderTabButton({ tabId: TabId.BOOKINGS, icon: <FaFileInvoice style={{ width: '16px', height: '16px' }} />, label: 'Buchungen' })}
            {renderTabButton({ tabId: TabId.EMAILS, icon: <FaEnvelope style={{ width: '16px', height: '16px' }} />, label: 'E-Mails' })}
            {renderTabButton({ tabId: TabId.SETTINGS, icon: <FaCog style={{ width: '16px', height: '16px' }} />, label: 'Einstellungen' })}
          </div>

          {/* User Profile Section */}
          <div className="hidden sm:flex items-center gap-3">
            {isLoadingUser ? (
              <div className="text-right">
                <p className="text-sm font-medium text-gray-400">Laden...</p>
              </div>
            ) : currentUser ? (
              <>
                <div className="text-right">
                  <p className="text-sm font-medium text-black">{currentUser.name}</p>
                  <p className="text-xs text-gray-600">{currentUser.email}</p>
                </div>
                <div className="flex items-center justify-center w-10 h-10 rounded-full bg-gray-200 overflow-hidden">
                  {profilePictureUrl ? (
                    <img
                      src={profilePictureUrl}
                      alt={`${currentUser.name} profile picture`}
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        console.error('Failed to load profile picture');
                        // Hide broken image
                        e.currentTarget.style.display = 'none';
                      }}
                    />
                  ) : (
                    <FaUser style={{ width: '20px', height: '20px' }} aria-label="Default user avatar" />
                  )}
                </div>
              </>
            ) : (
              <div className="text-right">
                <p className="text-sm font-medium text-red-600">Fehler beim Laden</p>
              </div>
            )}
          </div>
        </div>
      </nav>

      {/* Main Content Area */}
      <main className="flex-1 w-full px-6 py-6 overflow-y-auto" role="main">
        {selectedTab === TabId.USERS && <UsersTab />}
        {selectedTab === TabId.BOOKINGS && <BookingsTab />}
        {selectedTab === TabId.EMAILS && <EmailsTab />}
        {selectedTab === TabId.SETTINGS && <SettingsTab onProfileUpdate={loadCurrentUser} />}
      </main>
    </div>
  );
}
