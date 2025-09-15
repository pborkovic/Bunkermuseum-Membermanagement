import React from 'react';
import { useTheme } from 'Frontend/contexts/ThemeContext';
import { Icon } from '@vaadin/react-components';

export const ThemeToggle: React.FC = () => {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      className="theme-toggle"
      onClick={toggleTheme}
      title={`Switch to ${theme === 'light' ? 'dark' : 'light'} mode`}
      aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} mode`}
    >
      <Icon
        icon={theme === 'light' ? 'vaadin:moon' : 'vaadin:sun-o'}
        className="theme-toggle-icon"
      />
    </button>
  );
};