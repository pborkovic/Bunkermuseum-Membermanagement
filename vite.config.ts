import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';
import path from 'path';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src/main/frontend'),
    },
  },
});

export default overrideVaadinConfig(customConfig);
