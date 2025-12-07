/**
 * @fileoverview Member Dashboard main component.
 *
 * This component serves as the central hub for member-facing features,
 * providing access to personal bookings and account settings through
 * a tabbed interface.
 *
 * @module views/member
 * @author Philipp Borkovic
 */

import {useCallback, useMemo, useState} from 'react';
import {ViewConfig} from '@vaadin/hilla-file-router/types.js';
import {FaBars, FaTimes} from 'react-icons/fa';
import {Toaster} from '@/components/ui/sonner';
import BookingsTab from './components/_BookingsTab';
import SettingsTab from './components/_SettingsTab';
import logo from 'Frontend/assets/images/logo_bunkermuseum.jpg';
import {useCurrentUser} from './hooks';
import {type TabConfig, TabId} from './types';
import {BookingsIcon, PROFILE_PICTURE, SettingsIcon, UI_TEXT, UserIcon} from './constants';

/**
 * Hilla view configuration for the member dashboard.
 */
export const config: ViewConfig = {
  menu: { title: 'Member Dashboard', icon: 'vaadin:user' },
  route: 'member',
  loginRequired: false,
  flowLayout: false,
};

/**
 * Member Dashboard Component.
 *
 * Provides a comprehensive member interface for the Bunker Museum application,
 * featuring tabbed navigation between bookings overview and settings management.
 *
 * **Features:**
 * - User profile display with avatar
 * - Tabbed navigation (Bookings, Settings)
 * - Responsive design for mobile and desktop
 * - Automatic user data loading and refresh
 * - Error handling and loading states
 *
 * @component
 * @returns {JSX.Element} The rendered member dashboard
 *
 * @author Philipp Borkovic
 */
export default function MemberDashboard(): JSX.Element {
  const [selectedTab, setSelectedTab] = useState<TabId>(TabId.BOOKINGS);
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);
  const { user, profilePictureUrl, isLoading: isLoadingUser, refetch } = useCurrentUser();

  /**
   * Tab configurations with JSX icons.
   */
  const TAB_CONFIGS: readonly TabConfig[] = useMemo(() => [
    {
      tabId: TabId.BOOKINGS,
      icon: <BookingsIcon style={{ width: '16px', height: '16px' }} />,
      label: 'Buchungen',
    },
    {
      tabId: TabId.SETTINGS,
      icon: <SettingsIcon style={{ width: '16px', height: '16px' }} />,
      label: 'Einstellungen',
    },
  ], []);

  /**
   * Handles tab navigation changes.
   *
   * @param {TabId} tabId - The ID of the tab to navigate to
   */
  const handleTabChange = useCallback((tabId: TabId): void => {
    setSelectedTab(tabId);
  }, []);

  /**
   * Renders a navigation tab button with active state styling.
   *
   * @param {TabConfig} config - Tab configuration object
   * @returns {JSX.Element} The rendered tab button
   */
  const renderTabButton = useCallback(
    (config: TabConfig): JSX.Element => {
      const isActive = selectedTab === config.tabId;

      return (
        <button
          key={config.tabId}
          onClick={() => handleTabChange(config.tabId)}
          className={`flex items-center gap-1 sm:gap-2 px-2 sm:px-3 py-1.5 sm:py-2 text-xs sm:text-sm font-medium transition-colors whitespace-nowrap ${
            isActive
              ? 'text-black border-b-2 border-black'
              : 'text-gray-600 hover:text-black'
          }`}
          aria-label={`Navigate to ${config.label}`}
          aria-current={isActive ? 'page' : undefined}
          type="button"
        >
          <span style={{ width: '16px', height: '16px', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
            {config.icon}
          </span>
          {config.label}
        </button>
      );
    },
    [selectedTab, handleTabChange]
  );

  /**
   * Memoized tab buttons to prevent unnecessary re-renders.
   */
  const tabButtons = useMemo(
    () => TAB_CONFIGS.map(renderTabButton),
    [renderTabButton]
  );

  /**
   * Renders the user profile section in the navbar.
   *
   * @returns {JSX.Element} The rendered user profile section
   */
  const renderUserProfile = useCallback((): JSX.Element => {
    if (isLoadingUser) {
      return (
        <div className="text-right">
          <p className="text-sm font-medium text-gray-400">{UI_TEXT.LOADING}</p>
        </div>
      );
    }

    if (!user) {
      return (
        <div className="text-right">
          <p className="text-sm font-medium text-red-600">Fehler beim Laden</p>
        </div>
      );
    }

    return (
      <>
        <div className="text-right">
          <p className="text-sm font-medium text-black">{user.name}</p>
          <p className="text-xs text-gray-600">{user.email}</p>
        </div>
        <div className="flex items-center justify-center w-10 h-10 rounded-full bg-gray-200 overflow-hidden">
          {profilePictureUrl ? (
            <img
              src={profilePictureUrl}
              alt={`${user.name} profile picture`}
              className="w-full h-full object-cover"
              onError={(e) => {
                e.currentTarget.style.display = 'none';
              }}
            />
          ) : (
            <UserIcon style={{ width: `${PROFILE_PICTURE.ICON_WIDTH}px`, height: `${PROFILE_PICTURE.ICON_HEIGHT}px` }} />
          )}
        </div>
      </>
    );
  }, [isLoadingUser, user, profilePictureUrl]);

  /**
   * Renders the appropriate tab content based on selected tab.
   *
   * @returns {JSX.Element} The rendered tab content
   */
  const renderTabContent = useCallback((): JSX.Element => {
    switch (selectedTab) {
      case TabId.BOOKINGS:
        return <BookingsTab />;
      case TabId.SETTINGS:
        return <SettingsTab onProfileUpdate={refetch} />;
      default:
        return <BookingsTab />;
    }
  }, [selectedTab, refetch]);

  return (
    <div className="h-screen bg-white flex flex-col overflow-hidden">
      <Toaster />

      {/* Navigation Bar */}
      <nav
        className="w-full border-b bg-white z-10 flex-shrink-0"
        role="navigation"
        aria-label="Member navigation"
      >
        <div className="flex items-center justify-between px-3 py-2 sm:px-6 sm:py-4 gap-2">
          {/* Logo and Title */}
          <div className="flex items-center gap-2 sm:gap-3 min-w-0 flex-shrink">
            <img
              src={logo}
              alt="Bunkermuseum Logo"
              className="h-7 sm:h-10 md:h-12 w-auto object-contain flex-shrink-0"
            />
            <div className="min-w-0 overflow-hidden">
              <h1 className="text-xs sm:text-base md:text-lg lg:text-xl font-semibold text-black whitespace-nowrap overflow-hidden text-ellipsis">
                Mitgliederverwaltung
              </h1>
              <p className="text-[10px] sm:text-xs md:text-sm text-gray-600 whitespace-nowrap overflow-hidden text-ellipsis">Member Dashboard</p>
            </div>
          </div>

          {/* Desktop Tab Navigation */}
          <div
            className="hidden md:flex items-center gap-2 md:gap-8 overflow-x-auto"
            role="tablist"
          >
            {tabButtons}
          </div>

          {/* Mobile Hamburger Menu Button */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors flex-shrink-0"
            aria-label="Toggle menu"
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? (
              <FaTimes style={{ width: '20px', height: '20px' }} />
            ) : (
              <FaBars style={{ width: '20px', height: '20px' }} />
            )}
          </button>

          {/* User Profile Section */}
          <div className="hidden md:flex items-center gap-3">
            {renderUserProfile()}
          </div>
        </div>

        {/* Mobile Menu Dropdown */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 bg-white">
            <div className="flex flex-col">
              <button
                onClick={() => {
                  setSelectedTab(TabId.BOOKINGS);
                  setMobileMenuOpen(false);
                }}
                className={`flex items-center gap-3 px-6 py-4 text-left transition-colors ${
                  selectedTab === TabId.BOOKINGS
                    ? 'bg-gray-200 text-black'
                    : 'text-gray-700 hover:bg-gray-100'
                }`}
              >
                <BookingsIcon style={{ width: '18px', height: '18px' }} />
                <span className="font-medium">Buchungen</span>
              </button>
              <button
                onClick={() => {
                  setSelectedTab(TabId.SETTINGS);
                  setMobileMenuOpen(false);
                }}
                className={`flex items-center gap-3 px-6 py-4 text-left transition-colors ${
                  selectedTab === TabId.SETTINGS
                    ? 'bg-gray-200 text-black'
                    : 'text-gray-700 hover:bg-gray-100'
                }`}
              >
                <SettingsIcon style={{ width: '18px', height: '18px' }} />
                <span className="font-medium">Einstellungen</span>
              </button>
            </div>
          </div>
        )}
      </nav>

      {/* Main Content Area */}
      <main className="flex-1 w-full px-6 py-6 overflow-y-auto" role="main">
        {renderTabContent()}
      </main>
    </div>
  );
}
