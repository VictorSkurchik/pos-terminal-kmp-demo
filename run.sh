#!/usr/bin/env bash
#
# Launch the whole stack with one command: backend (Ktor) + web admin (Compose/JS) + Android client.
#
#   ./run.sh                 — backend + web + android (if a device/emulator is connected)
#   ./run.sh --no-android    — backend + web only
#   ./run.sh --no-web        — without the web admin
#
# Ctrl+C stops everything.

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

SERVER_PORT=8080
WEB_PORT=5173
PKG="by.vsdev.posterminal.demo"
GRADLE_FLAGS="--no-configuration-cache --console=plain"
LOG_DIR="$ROOT/.run-logs"
mkdir -p "$LOG_DIR"

RUN_ANDROID=1
RUN_WEB=1
for arg in "$@"; do
  case "$arg" in
    --no-android) RUN_ANDROID=0 ;;
    --no-web)     RUN_WEB=0 ;;
    -h|--help)    grep '^#' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "Unknown option: $arg" >&2; exit 2 ;;
  esac
done

# adb: from PATH or from the standard SDK location
ADB="$(command -v adb || true)"
[ -z "$ADB" ] && [ -x "$HOME/Library/Android/sdk/platform-tools/adb" ] && ADB="$HOME/Library/Android/sdk/platform-tools/adb"

PIDS=()
CLEANED=0
cleanup() {
  [ "$CLEANED" = 1 ] && return; CLEANED=1
  echo; echo "Stopping..."
  for pid in "${PIDS[@]:-}"; do kill "$pid" 2>/dev/null; done
  # reliably kill whatever is listening on our ports (server JVM, webpack-dev-server)
  for port in "$SERVER_PORT" "$WEB_PORT"; do
    local held; held="$(lsof -ti "tcp:$port" 2>/dev/null)"
    [ -n "$held" ] && kill $held 2>/dev/null
  done
  [ -n "$ADB" ] && "$ADB" reverse --remove "tcp:$SERVER_PORT" >/dev/null 2>&1
  wait 2>/dev/null
  echo "Done."
}
trap cleanup INT TERM EXIT

wait_for_url() { # $1 url  $2 timeout_sec  $3 label
  printf "  waiting for %s" "$3"
  for _ in $(seq 1 "$2"); do
    curl -sf -o /dev/null "$1" && { echo " ok"; return 0; }
    printf "."; sleep 1
  done
  echo " failed (timeout)"; return 1
}

# -- backend ------------------------------------------------------------------
echo "backend  :server:run            -> http://localhost:$SERVER_PORT   (log: .run-logs/server.log)"
./gradlew :server:run $GRADLE_FLAGS > "$LOG_DIR/server.log" 2>&1 &
PIDS+=("$!")
wait_for_url "http://localhost:$SERVER_PORT/devices" 120 "backend" \
  || { echo "backend did not start, see .run-logs/server.log"; exit 1; }

# -- web admin (Vite dev, pointed at the local backend) -----------------------
if [ "$RUN_WEB" = 1 ]; then
  echo "web      web-admin (Vite dev)     -> http://localhost:$WEB_PORT   (log: .run-logs/web.log)"
  if [ ! -d web-admin/node_modules ]; then
    echo "  installing web-admin deps (first run)..."
    (cd web-admin && npm install) >> "$LOG_DIR/web.log" 2>&1
  fi
  VITE_SERVER_URL="http://localhost:$SERVER_PORT" \
    npm --prefix web-admin run dev -- --port "$WEB_PORT" --strictPort >> "$LOG_DIR/web.log" 2>&1 &
  PIDS+=("$!")
fi

# -- android client -----------------------------------------------------------
if [ "$RUN_ANDROID" = 1 ]; then
  if [ -n "$ADB" ] && "$ADB" devices | awk 'NR>1 && $2=="device"{f=1} END{exit !f}'; then
    echo "android  installDebug + launch  (log: .run-logs/android.log)"
    "$ADB" reverse "tcp:$SERVER_PORT" "tcp:$SERVER_PORT" >/dev/null 2>&1  # device localhost -> backend
    if ./gradlew :app:androidApp:installDebug $GRADLE_FLAGS > "$LOG_DIR/android.log" 2>&1; then
      "$ADB" shell am start -n "$PKG/.MainActivity" >/dev/null 2>&1
      echo "  installed and launched ok"
    else
      echo "  install failed, see .run-logs/android.log"
    fi
  else
    echo "android  SKIPPED — no device/emulator."
    echo "         Start an emulator and re-run (adb reverse is set automatically)."
  fi
fi

# -- wait for the web bundle and keep the stack up ----------------------------
[ "$RUN_WEB" = 1 ] && wait_for_url "http://localhost:$WEB_PORT/" 180 "web bundle"

echo
echo "--------------------------------------------------------------"
echo "  backend : http://localhost:$SERVER_PORT"
[ "$RUN_WEB" = 1 ] && echo "  web     : http://localhost:$WEB_PORT"
echo "  logs    : $LOG_DIR"
echo "  Ctrl+C  : stop everything."
echo "--------------------------------------------------------------"

# keep the script in the foreground and stream backend/web logs
TAIL_FILES=("$LOG_DIR/server.log")
[ "$RUN_WEB" = 1 ] && TAIL_FILES+=("$LOG_DIR/web.log")
tail -n 0 -f "${TAIL_FILES[@]}"
