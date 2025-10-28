import { useState, useEffect, useCallback } from 'react';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Icon } from '@vaadin/react-components';
import { Toaster } from '@/components/ui/sonner';
import { toast } from 'sonner';
import { AuthController } from 'Frontend/generated/endpoints';
import type UserDTO from 'Frontend/generated/com/bunkermuseum/membermanagement/dto/UserDTO';
import BookingsTab from './components/_BookingsTab';
import SettingsTab from './components/_SettingsTab';
import logo from 'Frontend/assets/images/logo_bunkermuseum.jpg';

export const config: ViewConfig = {
  menu: { title: 'Member Dashboard', icon: 'vaadin:user' },
  route: 'member',
  loginRequired: false,
  flowLayout: false,
};

enum TabId {
  BOOKINGS = 0,
  SETTINGS = 1,
}

/**
 * Member Dashboard Component
 *
 * Central member interface for the Bunker Museum application.
 * Provides access to personal bookings and settings.
 *
 * @component
 * @author Philipp Borkovic
 */
export default function MemberDashboard(): JSX.Element {
  const [selectedTab, setSelectedTab] = useState<TabId>(TabId.BOOKINGS);
  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState<string | null>(null);
  const [isLoadingUser, setIsLoadingUser] = useState<boolean>(true);

  /**
   * Loads the current authenticated user's profile data.
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
   */
  const handleTabChange = useCallback((tabId: TabId): void => {
    setSelectedTab(tabId);
  }, []);

  /**
   * Renders a navigation tab button.
   */
  const renderTabButton = useCallback(
    ({ tabId, icon, label }: { tabId: TabId; icon: string; label: string }): JSX.Element => {
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
          <Icon icon={icon} style={{ width: '16px', height: '16px' }} />
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
      <nav className="w-full border-b bg-white z-10 flex-shrink-0" role="navigation" aria-label="Member navigation">
        <div className="flex flex-col px-3 py-2 gap-2 sm:flex-row sm:items-center sm:justify-between sm:px-6 sm:py-4 sm:gap-0">
          {/* Logo and Title */}
          <div className="flex items-center gap-2 sm:gap-3">
            <img
              src={logo}
              alt="Bunkermuseum Logo"
              className="h-8 sm:h-12 w-auto object-contain"
            />
            <div>
              <h1 className="text-sm sm:text-xl font-semibold text-black">Membermanagement</h1>
              <p className="text-xs sm:text-sm text-gray-600">Member Dashboard</p>
            </div>
          </div>

          {/* Tab Navigation */}
          <div className="flex items-center gap-2 sm:gap-8 overflow-x-auto" role="tablist">
            {renderTabButton({ tabId: TabId.BOOKINGS, icon: 'vaadin:invoice', label: 'Buchungen' })}
            {renderTabButton({ tabId: TabId.SETTINGS, icon: 'vaadin:cog', label: 'Einstellungen' })}
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
                    <Icon icon="vaadin:user" style={{ width: '20px', height: '20px' }} aria-label="Default user avatar" />
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
        {selectedTab === TabId.BOOKINGS && <BookingsTab />}
        {selectedTab === TabId.SETTINGS && <SettingsTab onProfileUpdate={loadCurrentUser} />}
      </main>
    </div>
  );
}
