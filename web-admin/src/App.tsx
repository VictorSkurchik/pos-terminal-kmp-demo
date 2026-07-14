import { useEffect, useState } from "react";
import { QRCodeCanvas } from "qrcode.react";
import {
  SERVER_URL,
  deleteDevice,
  listDevices,
  postCommand,
  type CommandType,
  type Device,
} from "./api";

function newToken(): string {
  return "enr-" + Math.random().toString(16).slice(2, 8);
}

function ago(ts: number): string {
  const s = Math.max(0, Math.round((Date.now() - ts) / 1000));
  if (s < 60) return `${s}s ago`;
  return `${Math.round(s / 60)}m ago`;
}

export default function App() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState("Please restart the terminal");
  const [token, setToken] = useState(newToken);
  const [wipeTarget, setWipeTarget] = useState<Device | null>(null);

  useEffect(() => {
    let alive = true;
    const tick = async () => {
      try {
        const list = await listDevices();
        if (alive) {
          setDevices(list);
          setError(null);
        }
      } catch (e) {
        if (alive) setError(e instanceof Error ? e.message : "request failed");
      }
    };
    void tick();
    const id = setInterval(() => void tick(), 3000);
    return () => {
      alive = false;
      clearInterval(id);
    };
  }, []);

  const send = (id: string, type: CommandType, payload: string | null = null) => {
    postCommand(id, type, payload).catch((e) =>
      setError(e instanceof Error ? e.message : "request failed"),
    );
  };

  const confirmWipe = (device: Device) => {
    deleteDevice(device.id)
      .then(() => setDevices((prev) => prev.filter((d) => d.id !== device.id)))
      .catch((e) => setError(e instanceof Error ? e.message : "request failed"))
      .finally(() => setWipeTarget(null));
  };

  const qrPayload = JSON.stringify({ token, serverUrl: SERVER_URL });

  return (
    <main className="container">
      <h1>POS MDM — Admin Console</h1>
      <p className="muted">Backend: {SERVER_URL} · auto-refresh 3s</p>
      {error && <p className="error">Error: {error}</p>}

      <section className="card qr">
        <QRCodeCanvas value={qrPayload} size={160} />
        <div>
          <h2>QR enrollment</h2>
          <p className="muted">token: {token}</p>
          <p className="muted">Scan in the app: Manage → Scan QR</p>
          <button onClick={() => setToken(newToken())}>New token</button>
        </div>
      </section>

      <label className="field">
        SHOW_MESSAGE text
        <input value={message} onChange={(e) => setMessage(e.target.value)} />
      </label>

      {devices.length === 0 ? (
        <p>No devices enrolled yet.</p>
      ) : (
        devices.map((d) => (
          <section className="card" key={d.id}>
            <b>{d.name}</b>
            <p className="muted">
              id={d.id} · {d.status} · battery={d.batteryLevel ?? "?"}% · seen{" "}
              {ago(d.lastSeenAt)}
              {d.enrollmentToken ? ` · QR token=${d.enrollmentToken}` : ""}
            </p>
            <div className="buttons">
              <button onClick={() => send(d.id, "LOCK")}>Lock</button>
              <button onClick={() => send(d.id, "KIOSK_ON")}>Kiosk On</button>
              <button onClick={() => send(d.id, "KIOSK_OFF")}>Kiosk Off</button>
              <button onClick={() => send(d.id, "SHOW_MESSAGE", message)}>
                Message
              </button>
              <button onClick={() => send(d.id, "RESTRICT_APP", "on")}>
                Restrict payment
              </button>
              <button onClick={() => send(d.id, "RESTRICT_APP", "off")}>
                Allow payment
              </button>
              <button className="danger" onClick={() => setWipeTarget(d)}>
                Wipe
              </button>
            </div>
          </section>
        ))
      )}

      {wipeTarget && (
        <div className="modal-backdrop" onClick={() => setWipeTarget(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Wipe device?</h2>
            <p className="muted">
              “{wipeTarget.name}” ({wipeTarget.id}) will be removed from the console. The terminal
              detects this and resets to the registration screen.
            </p>
            <div className="buttons">
              <button onClick={() => setWipeTarget(null)}>Cancel</button>
              <button className="danger" onClick={() => confirmWipe(wipeTarget)}>
                Wipe
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
