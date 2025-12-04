import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { AuthController } from 'Frontend/generated/endpoints.js';

export const config: ViewConfig = {
  menu: { exclude: true },
  loginRequired: false,
};

/**
 * Root view that redirects based on authentication and user roles.
 *
 * @returns Redirect component
 */
export default function RootView() {
  const navigate = useNavigate();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const checkAuthAndRedirect = async () => {
      try {
        const user = await AuthController.getCurrentUser();

        if (!user) {
          navigate('/login');
        } else {
          const roleNames = Array.from(user.roles || [])
            .filter((role): role is { name: string } => Boolean(role?.name))
            .map(role => role.name.toUpperCase());

          const hasAdmin = roleNames.includes('ADMIN');
          const hasMember = roleNames.includes('USER') || roleNames.includes('MEMBER');

          if (hasAdmin && hasMember) {
            navigate('/dashboard-selection');
          } else if (hasAdmin) {
            navigate('/admin');
          } else if (hasMember) {
            navigate('/member');
          } else {
            navigate('/member');
          }
        }
      } catch (error) {
        navigate('/login');
      } finally {
        setIsChecking(false);
      }
    };

    checkAuthAndRedirect();
  }, [navigate]);

  if (isChecking) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          <p className="mt-4 text-gray-600">Lädt...</p>
        </div>
      </div>
    );
  }

  return null;
}
