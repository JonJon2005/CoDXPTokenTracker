# CoDXPTokenTracker

CoDXPTokenTracker is a playground that keeps track of Call of Duty® Double XP token balances. The same token rules are implemented in three different layers so you can experiment with them however you like:

- **Java REST API** (Javalin) – authoritative service that reads and writes token data in MongoDB.
- **React frontend** (Vite) – dashboard for editing counts via the REST API.
- **Python CLI** – quick terminal editor that talks directly to the same MongoDB collection.

The application persists everything in MongoDB. Each user document stores a bcrypt password hash, profile metadata, and the current token inventory. No credentials or data files are checked into the repository.

---

## Prerequisites

Install the following on your development machine before you get started:

- **Git** for cloning the project.
- **MongoDB 6.0 or newer** (Community Edition is fine).
- **Java 17+** and **Apache Maven 3.8+**.
- **Node.js 20+** and the matching **npm 10+**.
- **Python 3.8+** (plus `pip`). Optional but recommended: `pip install colorama` for richer colours on Windows terminals.

The services read the following environment variables. You only need to set them if you want values other than the defaults:

| Variable | Default | Purpose |
|----------|---------|---------|
| `MONGODB_URI` | `mongodb://localhost:27017` | Connection string for your MongoDB deployment. |
| `MONGODB_DATABASE` | `codxp_tokens` | Database used by all components. |
| `MONGODB_USERS_COLLECTION` | `users` | Collection that stores token documents. |

Create a `.env` file or export the variables in each terminal if you need to override them.

---

## Setting up MongoDB locally

If you already have MongoDB available, skip to the next section. Otherwise, pick the option that fits your platform.

### macOS (Homebrew)

```bash
brew tap mongodb/brew
brew install mongodb-community@7.0
brew services start mongodb-community@7.0
```

This installs MongoDB 7.0 as a macOS service and starts it automatically. Logs are written to `/usr/local/var/log/mongodb`. Stop it later with `brew services stop mongodb-community@7.0`.

### Ubuntu / Debian

```bash
# Import MongoDB's public key and repository once
sudo apt-get install -y wget gnupg
wget -qO - https://pgp.mongodb.com/server-7.0.asc | sudo gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg
echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu $(lsb_release -sc)/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list

# Install and start the server
sudo apt-get update
sudo apt-get install -y mongodb-org
sudo systemctl enable --now mongod
```

Check the status with `sudo systemctl status mongod`. Logs live under `/var/log/mongodb/mongod.log`.

### Windows

1. Download the MongoDB Community MSI installer from [mongodb.com/try/download/community](https://www.mongodb.com/try/download/community).
2. Run the installer, choose **Complete**, and keep the **MongoDB as a Service** option enabled.
3. After installation, MongoDB starts automatically. Use *MongoDB Compass* or open **Command Prompt** and run `"C:\Program Files\MongoDB\Server\7.0\bin\mongosh.exe"` to connect.

### Docker (any platform)

```bash
docker run --name codxp-mongo -p 27017:27017 -d mongo:7
```

The container keeps data in an ephemeral volume. Add `-v $(pwd)/mongo-data:/data/db` if you want it persisted relative to the repository.

### Seed the database (optional)

Once MongoDB is running, you can create the database and collection explicitly:

```bash
mongosh "${MONGODB_URI:-mongodb://localhost:27017}"
> use codxp_tokens
> db.createCollection("users")
> db.users.insertOne({
    username: "demo",
    passwordHash: "<bcrypt hash>",
    doubleXpTokens: { regular: 0, weapon: 0, battlePass: 0 },
    profile: { clanTag: "COD", platform: "pc" }
  })
```

The application will also create the database and collection lazily the first time you save data, so seeding is optional.

---

## Step-by-step project setup (from the repository root)

All commands below assume you are at the project root directory: `CoDXPTokenTracker/`.

### 1. Clone and enter the project

```bash
git clone <your-fork-or-the-upstream-url>
cd CoDXPTokenTracker
```

### 2. Verify MongoDB connectivity

```bash
mongosh "${MONGODB_URI:-mongodb://localhost:27017}" --eval 'db.runCommand({ ping: 1 })'
```

A response containing `{ ok: 1 }` confirms MongoDB is reachable.

### 3. Install dependencies per component

**Backend API (Java + Maven)**
```bash
cd java
mvn clean install
cd ..
```

**Frontend (Node + npm)**
```bash
cd frontend
npm install
cd ..
```

**Python CLI**
```bash
cd python
python -m venv .venv
source .venv/bin/activate   # On Windows use: .venv\Scripts\activate
pip install -r requirements.txt
deactivate
cd ..
```

### 4. Run the services

Open separate terminals for each long-running process so logs stay readable.

**Terminal A – Java REST API**
```bash
cd CoDXPTokenTracker/java
export MONGODB_URI="mongodb://localhost:27017"          # Optional if using defaults
export MONGODB_DATABASE="codxp_tokens"
export MONGODB_USERS_COLLECTION="users"
mvn exec:java
```
The API listens on `http://localhost:7001` and talks to MongoDB using the configured URI.

**Terminal B – React frontend**
```bash
cd CoDXPTokenTracker/frontend
npm run dev
```
Vite serves the dashboard on `http://localhost:5173` and proxies `/api/*` requests to the backend running on port 7001.

**Terminal C (optional) – Python CLI**
```bash
cd CoDXPTokenTracker/python
source .venv/bin/activate   # On Windows use: .venv\Scripts\activate
python main.py
```
Any changes you make here are immediately persisted to MongoDB.

> **Tip:** On macOS you can run `./start-mac.sh`, and on Windows you can run `start-windows.bat` to spin up the backend and frontend together with sensible defaults.

### 5. Stopping everything

- Press `Ctrl+C` in each terminal window to stop the running service.
- If you started MongoDB with Homebrew, Systemd, or Windows Services, stop it using the corresponding tool when you are finished.

---

## Repository layout

| Path | Description |
|------|-------------|
| `frontend/` | React + Vite single-page application.
| `java/` | Javalin-based REST API that the frontend calls.
| `python/` | Command-line interface for quick manual edits.
| `start-mac.sh`, `start-windows.bat` | Helper scripts for launching the API and frontend together.

---

## License

This project is provided for educational purposes. Modify it freely to suit your needs.
