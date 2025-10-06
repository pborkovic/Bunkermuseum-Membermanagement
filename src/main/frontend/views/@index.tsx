import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';

export const config: ViewConfig = {
  menu: { exclude: true },
  loginRequired: false,
};

/**
 * Root view that redirects to login page.
 *
 * @returns Redirect component
 */
export default function RootView() {
  const navigate = useNavigate();

  useEffect(() => {
    navigate('/login');
  }, [navigate]);

  return null;
}
