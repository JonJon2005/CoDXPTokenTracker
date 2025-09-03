# CoDXPTokenTracker

CoDXPTokenTracker is a cross-language playground for managing Call of Duty double XP tokens.
It includes:

- **Python CLI** for direct manipulation of token data.
- **Java REST server** that exposes token data over HTTP.
- **React front end** (Vite) that consumes the REST API and displays current totals.

All components share a simple text file, `tokens.txt`, located in the repository root.
The file contains 12 lines representing counts of 15, 30, 45, and 60 minute tokens for
each category: Regular, Weapon, and Battle Pass.

## Prerequisites

| Component | Requirements |
|-----------|-------------|
| Python    | Python 3.8+, optionally `colorama` for colored output (\`pip install colorama\`). |
| Java      | JDK 17+, Apache Maven 3.8+. |
| Frontend  | Node.js 20+, npm 10+. |

Ensure `tokens.txt` exists in the repository root. If it does not, the utilities will
create one with zeros.

## Python Command-Line Tool

1. Navigate to the Python directory:
   ```bash
   cd python
   ```
2. (Optional) Install the color library for better Windows support:
   ```bash
   pip install colorama
   ```
3. Run the CLI:
   ```bash
   python main.py
   ```
   The tool lets you view, edit, and export token totals interactively. Edits are
   saved back to `../tokens.txt`.

## Java REST Backend

The Java implementation exposes token data as JSON and is used by the frontend.

1. Move into the Java project:
   ```bash
   cd java
   ```
2. Start the server with Maven:
   ```bash
   mvn exec:java
   ```
   The server listens on `http://localhost:7001` and uses `../tokens.txt` for
   storage.

### Available Endpoints

| Method | Path      | Description |
|--------|-----------|-------------|
| GET    | `/tokens` | Returns token counts for each category. |
| PUT    | `/tokens` | Replaces token counts using a JSON body matching the GET structure. |
| GET    | `/totals` | Returns computed minute and hour totals per category and overall. |

Example request to view tokens:
```bash
curl http://localhost:7001/tokens
```

## React Frontend

A Vite-powered React app displays token counts and automatically refreshes every
five seconds.

1. Enter the frontend folder and install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Start the development server:
   ```bash
   npm run dev
   ```
   By default, Vite serves on `http://localhost:5173` and proxies any request that
   begins with `/api` to `http://localhost:7001`. The proxy configuration lives in
   `vite.config.js`.

3. In a browser, open the printed URL (usually `http://localhost:5173`) to view the
   token dashboard.

## Using Python or Java Backend Interchangeably

The Python CLI and Java server share the same business logic and file format. You can
choose whichever implementation fits your workflow:

- Use the **Python CLI** for quick interactive edits directly from the terminal.
- Run the **Java server** when you need an HTTP API (required for the React frontend).

Both implementations operate on `tokens.txt`, so changes made by one are immediately
visible to the other.

## Development Tips

- Keep `tokens.txt` under version control if you want to track history of your token
  counts.
- The frontend's `npm run lint` script checks React source files with ESLint.
- The Java project uses Maven; run `mvn test` or `mvn package` for additional
  validation or to produce a runnable JAR.

## License

This project is provided for instructional purposes. Adjust and extend it as needed
for personal use.
