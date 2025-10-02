import { createElement } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router';
import { router } from 'Frontend/generated/routes.js';
import './styles/globals.css';

/**
 * Main application entry point.
 *
 * @description
 * Initializes the React application with:
 * - React Router for navigation
 * - Generated route configuration from Vaadin
 * - Global Shadcn UI styles and Tailwind CSS
 * - Dark mode support via CSS variables
 *
 * This file customizes the default Vaadin Hilla entry point to:
 * 1. Import global styles (globals.css) with Shadcn UI theme variables
 * 2. Maintain compatibility with Vaadin's hot module replacement
 */
function App() {
    return <RouterProvider router={router} />;
}

const outlet = document.getElementById('outlet')!;
let root = (outlet as any)._root ?? createRoot(outlet);
(outlet as any)._root = root;
root.render(createElement(App));
