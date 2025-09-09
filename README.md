# CoDXPTokenTracker

A small playground for tracking Call of Duty Double XP tokens. The project demonstrates the same token logic implemented in multiple languages:

- **Python CLI** – interactive terminal editor.
- **Java REST API** – HTTP service used by the frontend.
- **React frontend** – dashboard that consumes the API and lets you adjust counts.

Tokens are stored per-user as JSON files under `data/users/<username>.json`. Each file contains a hashed password and token counts. If no user file exists, the tools fall back to the legacy `tokens.txt` in the repository root.

## Setup

1. **Clone the repository** and open a terminal in its root directory.
2. **Prerequisites**
   - Python 3.8+
   - JDK 17+ and Apache Maven 3.8+
   - Node.js 20+ and npm 10+
   - (Optional) `pip install colorama` for better Windows terminal colours.

For a one-command startup, use the provided scripts to launch both the backend and frontend:

- macOS: `./start-mac.sh`
- Windows: `start-windows.bat`

3. **Start the Java backend**
   ```bash
   cd java
   mvn exec:java
   ```
   The server runs on `http://localhost:7001` and reads user data from `../data/users/` (falling back to `../tokens.txt`).
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
   Any changes are written back to the user’s JSON file or, for the default account, `../tokens.txt`.

## Repository Layout

| Path       | Description                     |
|------------|---------------------------------|
| `data/users/` | Per-user JSON token storage. |
| `tokens.txt` | Legacy default token file.    |
| `python/`  | Command-line interface.         |
| `java/`    | REST API server (Javalin).      |
| `frontend/`| React dashboard powered by Vite.|

## License

Provided for educational use. Modify and extend as desired.
