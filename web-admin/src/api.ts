// Backend URL. Defaults to Render (production); override for local dev with VITE_SERVER_URL.
// Types mirror the Kotlin DTOs in :core.
export const SERVER_URL =
  import.meta.env.VITE_SERVER_URL ?? "https://pos-terminal-kmp-demo.onrender.com";

export type CommandType =
  | "LOCK"
  | "KIOSK_ON"
  | "KIOSK_OFF"
  | "SHOW_MESSAGE"
  | "RESTRICT_APP"
  | "WIPE";

export type DeviceStatus = "ONLINE" | "OFFLINE" | "LOCKED" | "KIOSK";

export interface Device {
  id: string;
  name: string;
  model: string | null;
  lastSeenAt: number;
  status: DeviceStatus;
  batteryLevel: number | null;
  enrollmentToken: string | null;
  kioskActive: boolean;
  restrictPayment: boolean;
}

/** Payload encoded into the enrollment QR (matches EnrollmentToken in :core). */
export interface EnrollmentToken {
  token: string;
  serverUrl: string;
}

export async function listDevices(): Promise<Device[]> {
  const res = await fetch(`${SERVER_URL}/devices`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function postCommand(
  deviceId: string,
  type: CommandType,
  payload: string | null = null,
): Promise<void> {
  const res = await fetch(`${SERVER_URL}/devices/${deviceId}/commands`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ type, payload }),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
}
