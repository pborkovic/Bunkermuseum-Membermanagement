import { Icon } from '@vaadin/react-components/Icon.js';

interface SettingsTabProps {
  onProfileUpdate: () => Promise<void>;
}

export default function SettingsTab({ onProfileUpdate }: SettingsTabProps) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex flex-col items-center justify-center min-h-[400px] text-center">
        <Icon
          icon="vaadin:cog"
          className="w-16 h-16 text-gray-400 mb-4"
        />
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          Einstellungen
        </h2>
        <p className="text-gray-500 max-w-md">
          Hier werden Sie Ihre persönlichen Einstellungen verwalten können. Diese Funktion wird in Kürze verfügbar sein.
        </p>
      </div>
    </div>
  );
}
