#!/bin/bash
set -e

# Launch Java backend and React frontend for CoDXPTokenTracker on macOS.
# Usage: ./start-mac.sh

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Start the Java backend in the background
cd "$ROOT_DIR/java"
mvn exec:java &
BACK_PID=$!

# Start the frontend
cd "$ROOT_DIR/frontend"
npm install
npm run dev

# When frontend exits, terminate backend
kill $BACK_PID
