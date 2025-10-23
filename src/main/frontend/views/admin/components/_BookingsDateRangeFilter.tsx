import { Icon } from '@vaadin/react-components';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';

/**
 * Date range presets for quick filtering
 */
export const DATE_RANGE_PRESETS = [
  { value: '1week', label: '1 Woche', days: 7 },
  { value: '1month', label: '1 Monat', days: 30 },
  { value: '6months', label: '6 Monate', days: 180 },
  { value: '1year', label: '1 Jahr', days: 365 },
  { value: 'custom', label: 'Benutzerdefiniert', days: 0 },
] as const;

/**
 * BookingsDateRangeFilter component props.
 */
interface BookingsDateRangeFilterProps {
  dateRangePreset: string;
  startDate: Date | undefined;
  endDate: Date | undefined;
  onPresetChange: (preset: string) => void;
  onStartDateChange: (date: Date | undefined) => void;
  onEndDateChange: (date: Date | undefined) => void;
}

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

/**
 * BookingsDateRangeFilter component - Date range filtering UI.
 *
 * Provides predefined date ranges (1 week, 1 month, etc.) and custom
 * date range selection for filtering bookings.
 *
 * Features:
 * - Preset date ranges (1 week, 1 month, 6 months, 1 year)
 * - Custom date range selection
 * - Active filter display
 *
 * @component
 *
 * @author Philipp Borkovic
 */
export default function BookingsDateRangeFilter({
  dateRangePreset,
  startDate,
  endDate,
  onPresetChange,
  onStartDateChange,
  onEndDateChange,
}: BookingsDateRangeFilterProps): JSX.Element {
  return (
    <div className="flex-shrink-0 bg-white rounded-lg p-4 border border-gray-200">
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
        <div className="flex items-center gap-2">
          <Icon
            icon="vaadin:calendar"
            className="text-black"
            style={{ width: '20px', height: '20px' }}
          />
          <label className="text-sm font-medium text-black">Zeitraum:</label>
        </div>

        {/* Date Range Preset Selector */}
        <Select
          value={dateRangePreset}
          onValueChange={onPresetChange}
        >
          <SelectTrigger className="w-[180px] h-9 border-black text-black [&_svg]:text-black [&_svg]:opacity-100 [&_svg]:-mt-4">
            <SelectValue />
          </SelectTrigger>
          <SelectContent className="bg-white border-black">
            {DATE_RANGE_PRESETS.map(preset => (
              <SelectItem
                key={preset.value}
                value={preset.value}
                className="text-black hover:bg-gray-100 hover:text-black focus:bg-gray-100 focus:text-black"
              >
                {preset.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Custom Date Range Pickers */}
        {dateRangePreset === 'custom' && (
          <div className="flex flex-col sm:flex-row gap-2 items-start sm:items-center">
            <div className="flex items-center gap-2">
              <label className="text-sm text-gray-600">Von:</label>
              <DatePicker
                value={startDate}
                onChange={onStartDateChange}
              />
            </div>
            <div className="flex items-center gap-2">
              <label className="text-sm text-gray-600">Bis:</label>
              <DatePicker
                value={endDate}
                onChange={onEndDateChange}
              />
            </div>
          </div>
        )}

        {/* Active Filter Display */}
        {startDate && endDate && (
          <div className="text-sm text-gray-600">
            {formatDate(startDate.toISOString())} - {formatDate(endDate.toISOString())}
          </div>
        )}
      </div>
    </div>
  );
}
