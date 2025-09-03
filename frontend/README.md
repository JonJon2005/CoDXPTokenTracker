# Frontend

React app built with Vite that displays and updates XP token counts through the Java API.

## Development

1. Ensure the backend server is running on `http://localhost:7001`.
2. Install dependencies and start the dev server:
   ```bash
   npm install
   npm run dev
   ```
   The site is served on `http://localhost:5173`. Requests beginning with `/api` are proxied to the backend.

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start Vite in development mode. |
| `npm run build` | Create a production build in `dist/`. |
| `npm run preview` | Preview the production build locally. |
| `npm run lint` | Run ESLint on the source files. |

Use the `+`/`-` buttons in the UI to change counts and **Save** to persist them to `tokens.txt`.
