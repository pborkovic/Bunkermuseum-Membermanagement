import { createElement } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router';
import { router } from 'Frontend/routes';
import './styles/globals.css';

/**
 * Main application entry point.
 *
 * @description
 * Initializes the React application with:
 * - React Router for navigation
 * - Custom route configuration with error handling
 * - Global Shadcn UI styles and Tailwind CSS
 * - Theme management via ThemeProvider (defaults to light mode)
 *
 * This file customizes the default Vaadin Hilla entry point to:
 * 1. Import global styles (globals.css) with Shadcn UI theme variables
 * 2. Use custom routes.tsx for enhanced error handling (404, 500)
 * 3. Maintain compatibility with Vaadin's hot module replacement
 *
 * @author Philipp Borkovic
 */
function App() {
    return <RouterProvider router={router} />;
}

const outlet = document.getElementById('outlet')!;
let root = (outlet as any)._root ?? createRoot(outlet);
(outlet as any)._root = root;
root.render(createElement(App));
