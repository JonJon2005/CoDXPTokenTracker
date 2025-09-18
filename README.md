# CoDXPTokenTracker

A small playground for tracking Call of Duty Double XP tokens. The project demonstrates the same token logic implemented in multiple languages:

- **Python CLI** – interactive terminal editor.
- **Java REST API** – HTTP service used by the frontend.
- **React frontend** – dashboard that consumes the API and lets you adjust counts.

The frontend includes an account menu (top-right icon) where users can log out.

Token data and account metadata now live in MongoDB (collection defaults to `codxp_tokens.users`). Every user document stores a bcrypt password hash, XP token counts and profile details.

## Setup

1. **Clone the repository** and open a terminal in its root directory.
2. **Prerequisites**
   - Python 3.8+
   - JDK 17+ and Apache Maven 3.8+
   - Node.js 20+ and npm 10+
   - MongoDB 6+ (local or hosted instance)
   - (Optional) `pip install colorama` for better Windows terminal colours.

   Set the following environment variables if you are not using the defaults:

   - `MONGODB_URI` (default `mongodb://localhost:27017`)
   - `MONGODB_DATABASE` (default `codxp_tokens`)
   - `MONGODB_USERS_COLLECTION` (default `users`)

For a one-command startup, use the provided scripts to launch both the backend and frontend:

- macOS: `./start-mac.sh`
- Windows: `start-windows.bat`

3. **Start the Java backend**
   ```bash
   cd java
   mvn exec:java
   ```
   The server runs on `http://localhost:7001` and reads/writes user data via MongoDB.
4. **Start the React frontend** in a second terminal
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Vite serves the app on `http://localhost:5173` and proxies `/api/*` requests to the backend.
5. **Use the Python CLI** (optional) for direct editing
   ```bash
   cd python
   python main.py
   ```
   Any changes are written back to MongoDB.

## Repository Layout

| Path       | Description                     |
|------------|---------------------------------|
| `python/`  | Command-line interface.         |
| `java/`    | REST API server (Javalin).      |
| `frontend/`| React dashboard powered by Vite.|

The database is external; no user data lives inside the repository anymore.

## License

Provided for educational use. Modify and extend as desired.
