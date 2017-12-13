# Tiling Puzzle


## Requirements
- Kotlin 1.2
- Gradle
- Node.js 9.x

#### Gradle Dependencies
- spek + Kluent (unit tests)
- ktor (server)
- moshi (JSON parser)
- rxjava (observables)

## Running (Unix)
### Server
From project root directory:

```bash
./gradlew run
```

This installs gradle dependencies and starts the "api" server on
[localhost:8080](http://localhost:8080), listening to websocket requests
at [localhost:8080/ws](http://localhost:8080/ws).

### Client
From `ui/` directory:

```bash
npm install
npm start
```

This installs node dependencies and starts the node server that serves
the react.js frontend at [localhost:3000](http://localhost:3000).

## Benchmarking

Run `src/main/kotlin/Main.kt`
