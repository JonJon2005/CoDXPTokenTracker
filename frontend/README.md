# CoDXPTokenTracker Frontend

This directory contains a minimal React application powered by Vite. It fetches
XP token data from the backend server and displays the totals for each category.
The app automatically refreshes token counts every 5 seconds when there are no
unsaved edits. Adjust token values with the `+`/`-` buttons and click **Save** to
persist changes.

## Development

1. Make sure the backend server is running on `http://localhost:7001`.
   If your backend uses a different port, update `vite.config.js` accordingly.
2. Install dependencies and start the Vite development server:

   ```bash
   npm install
   npm run dev
   ```

Requests beginning with `/api` are proxied to the backend, so the React app can
call endpoints such as `/api/tokens` without worrying about CORS during local
development.
