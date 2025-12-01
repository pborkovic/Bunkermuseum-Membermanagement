import { memo, useMemo } from 'react';
import { Grid } from '@vaadin/react-components/Grid';
import { GridColumn } from '@vaadin/react-components/GridColumn';
import { Icon } from '@vaadin/react-components';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type User from 'Frontend/generated/com/bunkermuseum/membermanagement/model/User';
import Pagination from './shared/_Pagination';
import PaginationInfo from './shared/_PaginationInfo';
import LoadingState from './shared/_LoadingState';
import EmptyState from './shared/_EmptyState';
import { LIST_CONTAINER_HEIGHT } from '../utils/constants';

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
  onExportClick: (user: User) => void;
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
 * - Memoized for performance optimization
 *
 * @component
 *
 * @author Philipp Borkovic
 */
function UsersList({
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
  onExportClick,
  onPageChange,
}: UsersListProps): JSX.Element {
  /**
   * Calculate display range for pagination info.
   * Memoized to prevent unnecessary recalculations.
   */
  const { startIndex, endIndex } = useMemo(() => ({
    startIndex: (currentPage - 1) * usersPerPage + 1,
    endIndex: Math.min(currentPage * usersPerPage, totalElements),
  }), [currentPage, usersPerPage, totalElements]);

  /**
   * Determine empty state message based on search query.
   */
  const emptyStateMessage = useMemo(() => ({
    title: searchQuery ? 'Keine Ergebnisse gefunden' : 'Keine Benutzer vorhanden',
    description: searchQuery
      ? `Keine Mitglieder entsprechen der Suche "${searchQuery}"`
      : 'Es wurden noch keine Benutzer registriert',
  }), [searchQuery]);

  if (isLoading) {
    return <LoadingState message="Lädt Benutzer..." className={`h-[${LIST_CONTAINER_HEIGHT}px]`} />;
  }

  if (users.length === 0) {
    return (
      <EmptyState
        icon="vaadin:users"
        title={emptyStateMessage.title}
        description={emptyStateMessage.description}
        className={`h-[${LIST_CONTAINER_HEIGHT}px]`}
      />
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
                      onClick={() => onExportClick(item)}
                      className="text-black hover:bg-gray-100 focus:bg-gray-100 cursor-pointer"
                    >
                      <Icon
                        icon="vaadin:download"
                        className="mr-2"
                        style={{ width: '16px', height: '16px' }}
                      />
                      Exportieren
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
        <PaginationInfo
          startIndex={startIndex}
          endIndex={endIndex}
          totalElements={totalElements}
          itemLabel="Mitgliedern"
          isFiltered={!!searchQuery}
        />

        {/* Pagination Controls - Right side */}
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={onPageChange}
        />
      </div>
    </div>
  );
}

/**
 * Memoized version of UsersList component.
 * Only re-renders when props change.
 */
export default memo(UsersList);
