import { Grid } from '@vaadin/react-components/Grid';
import { GridColumn } from '@vaadin/react-components/GridColumn';
import { Icon } from '@vaadin/react-components';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';

/**
 * Pagination component props.
 */
interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

/**
 * Pagination component - Simple previous/current/next design.
 *
 * @author Philipp Borkovic
 */
function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-2">
      {/* Previous Button */}
      <Button
        variant="outline"
        size="sm"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="h-9 w-9 p-0 border-black hover:bg-black hover:text-white disabled:opacity-30 disabled:border-gray-300"
      >
        <Icon icon="vaadin:angle-left" style={{ width: '18px', height: '18px' }} />
      </Button>

      {/* Current Page Display */}
      <div className="flex items-center gap-2 px-4 py-2 min-w-[80px] justify-center">
        <span className="text-sm font-medium text-black">
          {currentPage} / {totalPages}
        </span>
      </div>

      {/* Next Button */}
      <Button
        variant="outline"
        size="sm"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className="h-9 w-9 p-0 border-black hover:bg-black hover:text-white disabled:opacity-30 disabled:border-gray-300"
      >
        <Icon icon="vaadin:angle-right" style={{ width: '18px', height: '18px' }} />
      </Button>
    </div>
  );
}

/**
 * UsersList component props.
 */
interface UsersListProps {
  users: User[];
  isLoading: boolean;
  searchQuery: string;
  currentPage: number;
  totalPages: number;
  totalElements: number;
  usersPerPage: number;
  isMobile: boolean;
  onUserClick: (user: User) => void;
  onDeleteClick: (user: User) => void;
  onPageChange: (page: number) => void;
}

/**
 * UsersList component - Displays users in a grid with pagination.
 *
 * Clean black & white design with no blue colors.
 *
 * Features:
 * - Responsive grid layout
 * - Pagination controls
 * - Action menu per user
 * - Loading and empty states
 * - Search result indicators
 *
 * @component
 *
 * @author Philipp Borkovic
 */
export default function UsersList({
  users,
  isLoading,
  searchQuery,
  currentPage,
  totalPages,
  totalElements,
  usersPerPage,
  isMobile,
  onUserClick,
  onDeleteClick,
  onPageChange,
}: UsersListProps): JSX.Element {
  // Calculate display range
  const startIndex = (currentPage - 1) * usersPerPage + 1;
  const endIndex = Math.min(currentPage * usersPerPage, totalElements);

  if (isLoading) {
    return (
      <div className="rounded-lg bg-white flex items-center justify-center" style={{ height: '560px' }}>
        <div className="text-center space-y-3">
          <Icon
            icon="vaadin:spinner"
            className="animate-spin text-black mx-auto"
            style={{ width: '40px', height: '40px' }}
          />
          <p className="text-sm text-gray-600">Lädt Benutzer...</p>
        </div>
      </div>
    );
  }

  if (users.length === 0) {
    return (
      <div className="rounded-lg bg-white flex items-center justify-center" style={{ height: '560px' }}>
        <div className="text-center space-y-4">
          <Icon
            icon="vaadin:users"
            className="text-gray-300 mx-auto"
            style={{ width: '64px', height: '64px' }}
          />
          <div>
            <p className="text-base font-medium text-gray-900">
              {searchQuery ? 'Keine Ergebnisse gefunden' : 'Keine Benutzer vorhanden'}
            </p>
            <p className="text-sm text-gray-500 mt-1">
              {searchQuery
                ? `Keine Mitglieder entsprechen der Suche "${searchQuery}"`
                : 'Es wurden noch keine Benutzer registriert'}
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Users Grid - Fixed height for 10 entries */}
      <div className="rounded-lg overflow-hidden bg-white" style={{ height: '560px' }}>
        <Grid
          items={users}
          className="w-full h-full users-grid"
          style={{ height: '100%' }}
        >
          {/* Name Column */}
          <GridColumn
            header="Name"
            flexGrow={1}
            headerRenderer={() => (
              <div className="text-center w-full font-medium">Name</div>
            )}
            renderer={({ item }: any) => (
              <button
                onClick={() => onUserClick(item)}
                className="text-left hover:underline focus:outline-none w-full py-2"
              >
                <span className="font-medium text-black">{item.name}</span>
              </button>
            )}
          />

          {/* Email Column - Hidden on mobile */}
          {!isMobile && (
            <GridColumn
              header="E-Mail"
              flexGrow={1}
              headerRenderer={() => (
                <div className="text-center w-full font-medium">E-Mail</div>
              )}
              renderer={({ item }: any) => (
                <div className="text-left">
                  <span className="text-gray-700">{item.email}</span>
                </div>
              )}
            />
          )}

          {/* Actions Column */}
          <GridColumn
            header="Aktionen"
            width="100px"
            headerRenderer={() => (
              <div className="text-center w-full font-medium">Aktionen</div>
            )}
            renderer={({ item }: any) => (
              <div className="flex justify-center">
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <button
                      onClick={(e) => e.stopPropagation()}
                      className="p-2 hover:bg-gray-100 rounded-md transition-colors"
                      aria-label="Aktionen"
                    >
                      <Icon
                        icon="vaadin:ellipsis-dots-v"
                        style={{ width: '18px', height: '18px' }}
                      />
                    </button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="bg-white border-black">
                    <DropdownMenuItem
                      onClick={() => onUserClick(item)}
                      className="text-black hover:bg-gray-100 focus:bg-gray-100 cursor-pointer"
                    >
                      <Icon
                        icon="vaadin:eye"
                        className="mr-2"
                        style={{ width: '16px', height: '16px' }}
                      />
                      Details anzeigen
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      onClick={() => onDeleteClick(item)}
                      className="text-red-600 hover:bg-gray-100 focus:bg-gray-100 focus:text-red-600 cursor-pointer"
                    >
                      <Icon
                        icon="vaadin:trash"
                        className="mr-2"
                        style={{ width: '16px', height: '16px' }}
                      />
                      Löschen
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            )}
          />
        </Grid>
      </div>

      {/* Pagination Footer */}
      <div className="flex flex-col-reverse sm:flex-row items-center justify-between gap-4 px-1 pt-4">
        {/* Results Info - Left side */}
        <div className="text-sm text-gray-600 flex-shrink-0">
          Zeige <span className="font-medium text-black">{startIndex}</span> bis{' '}
          <span className="font-medium text-black">{endIndex}</span> von{' '}
          <span className="font-medium text-black">{totalElements}</span> Mitgliedern
          {searchQuery && (
            <span className="text-gray-500"> (gefiltert)</span>
          )}
        </div>

        {/* Pagination Controls - Right side */}
        <div className="flex-shrink-0">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={onPageChange}
          />
        </div>
      </div>
    </div>
  );
}
