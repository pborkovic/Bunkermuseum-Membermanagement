import { useState, useEffect } from 'react';
import { Grid } from '@vaadin/react-components/Grid';
import { GridColumn } from '@vaadin/react-components/GridColumn';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Icon } from '@vaadin/react-components';
import { UserController } from 'Frontend/generated/endpoints';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';

/**
 * UsersTab component - Displays all users in a grid with detailed modal view.
 *
 * Features:
 * - Grid view of all users
 * - Click to open detailed user information modal
 * - Loading and error states
 * - Responsive layout
 *
 * @component
 *
 * @returns {JSX.Element} The users tab content
 *
 * @author Philipp Borkovic
 */
export default function UsersTab(): JSX.Element {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  /**
   * Loads all users from the backend on component mount.
   */
  useEffect(() => {
    loadUsers();
  }, []);

  /**
   * Fetches all users from the UserController.
   *
   * @author Philipp Borkovic
   */
  const loadUsers = async (): Promise<void> => {
    try {
      setIsLoading(true);
      setError('');
      const allUsers = await UserController.getAllUsers();
      setUsers((allUsers || []).filter((user): user is User => user !== undefined));
    } catch (err: any) {
      setError(err.message || 'Fehler beim Laden der Benutzer');
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Opens the user details modal for a specific user.
   *
   * @param {User} user - The user to display
   *
   * @author Philipp Borkovic
   */
  const handleUserClick = (user: User): void => {
    setSelectedUser(user);
    setIsModalOpen(true);
  };

  /**
   * Closes the user details modal.
   *
   * @author Philipp Borkovic
   */
  const handleCloseModal = (): void => {
    setIsModalOpen(false);
    setSelectedUser(null);
  };

  /**
   * Formats a date to German locale string.
   *
   * @param {string | null | undefined} dateString - The date string to format
   * @returns {string} Formatted date or 'N/A'
   *
   * @author Philipp Borkovic
   */
  const formatDate = (dateString: string | null | undefined): string => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('de-DE');
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="mb-4">
        <h2 className="text-xl font-semibold">Benutzerverwaltung</h2>
        <p className="text-sm text-muted-foreground">
          Übersicht aller registrierten Benutzer
        </p>
      </div>

      {/* Error message */}
      {error && (
        <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
          {error}
        </div>
      )}

      {/* Users grid */}
      <div className="bg-white rounded-lg p-4">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:spinner" className="animate-spin text-primary mb-2" style={{ width: '32px', height: '32px' }} />
              <p className="text-sm text-muted-foreground">Lädt Benutzer...</p>
            </div>
          </div>
        ) : users.length === 0 ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Icon icon="vaadin:users" className="text-muted-foreground mb-2" style={{ width: '48px', height: '48px' }} />
              <p className="text-sm text-muted-foreground">Keine Benutzer gefunden</p>
            </div>
          </div>
        ) : (
          <Grid
            items={users}
            className="cursor-pointer"
          >
            <GridColumn path="name" header="Name" autoWidth />
            <GridColumn path="email" header="E-Mail" autoWidth />
            <GridColumn
              path="createdAt"
              header="Erstellt am"
              autoWidth
              renderer={({ item }: any) => formatDate(item.createdAt)}
            />
            <GridColumn
              header="Aktionen"
              autoWidth
              renderer={({ item }: any) => (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleUserClick(item);
                  }}
                  className="p-1 hover:bg-muted rounded"
                >
                  <Icon icon="vaadin:ellipsis-dots-v" style={{ width: '20px', height: '20px' }} />
                </button>
              )}
            />
          </Grid>
        )}
      </div>

      {/* User details modal */}
      <Dialog
        opened={isModalOpen}
        onOpenedChanged={(e: any) => {
          if (!e.detail.value) handleCloseModal();
        }}
        headerTitle="Benutzerdetails"
      >
        {selectedUser && (
          <div className="space-y-4 p-4 min-w-[600px]">
            {/* User icon */}
            <div className="flex justify-center">
              <Icon icon="vaadin:user-card" className="text-primary" style={{ width: '64px', height: '64px' }} />
            </div>

            {/* User information */}
            <div className="space-y-3">
              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Name:</span>
                <span className="col-span-2">{selectedUser.name}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">E-Mail:</span>
                <span className="col-span-2">{selectedUser.email}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">ID:</span>
                <span className="col-span-2 font-mono text-sm">{selectedUser.id}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">E-Mail verifiziert:</span>
                <span className="col-span-2">
                  {selectedUser.emailVerifiedAt ? (
                    <span className="text-success">
                      <Icon icon="vaadin:check-circle" className="inline mr-1" style={{ width: '16px', height: '16px' }} />
                      {formatDate(selectedUser.emailVerifiedAt)}
                    </span>
                  ) : (
                    <span className="text-destructive">
                      <Icon icon="vaadin:close-circle" className="inline mr-1" style={{ width: '16px', height: '16px' }} />
                      Nicht verifiziert
                    </span>
                  )}
                </span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Google verknüpft:</span>
                <span className="col-span-2">
                  {selectedUser.googleId ? (
                    <Icon icon="vaadin:check" className="text-success" style={{ width: '16px', height: '16px' }} />
                  ) : (
                    <Icon icon="vaadin:close" className="text-muted-foreground" style={{ width: '16px', height: '16px' }} />
                  )}
                </span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Microsoft verknüpft:</span>
                <span className="col-span-2">
                  {selectedUser.microsoftId ? (
                    <Icon icon="vaadin:check" className="text-success" style={{ width: '16px', height: '16px' }} />
                  ) : (
                    <Icon icon="vaadin:close" className="text-muted-foreground" style={{ width: '16px', height: '16px' }} />
                  )}
                </span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Erstellt am:</span>
                <span className="col-span-2">{formatDate(selectedUser.createdAt)}</span>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <span className="font-medium">Aktualisiert am:</span>
                <span className="col-span-2">{formatDate(selectedUser.updatedAt)}</span>
              </div>
            </div>

            {/* Close button */}
            <div className="flex justify-end pt-4">
              <Button onClick={handleCloseModal}>Schließen</Button>
            </div>
          </div>
        )}
      </Dialog>
    </div>
  );
}
