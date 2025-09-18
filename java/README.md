# Java Token Server

REST API built with [Javalin](https://javalin.io/) that exposes XP token data stored in MongoDB.

## Run

From the repository root:
```bash
cd java
mvn exec:java
```
The server listens on `http://localhost:7001` and expects a reachable MongoDB instance (see repository README for configuration variables).

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/tokens` | Return token counts for each category. |
| PUT | `/tokens` | Replace token counts using a JSON body matching the GET format. |
| GET | `/totals` | Return total minutes and hours per category and overall. |

## Build

To create a runnable JAR:
```bash
mvn package
java -jar target/token-server-0.1.0.jar
```
