export const API_URL = (import.meta.env?.VITE_API_URL || "/api").replace(/\/$/, "");

let csrfPendiente;
let versionSesion = 0;

export function cambiarSesionApi() {
  versionSesion += 1;
  csrfPendiente = undefined;
}

function obtenerCsrf() {
  if (!csrfPendiente) {
    const solicitud = enviar("/auth/csrf", {}).then(({ token }) => token);
    csrfPendiente = solicitud;
    solicitud.catch(() => {
      if (csrfPendiente === solicitud) csrfPendiente = undefined;
    });
  }
  return csrfPendiente;
}

export async function api(path, { method = "GET", body, signal, protegida = false } = {}) {
  const version = versionSesion;
  const modifica = !["GET", "HEAD", "OPTIONS"].includes(method);
  const csrf = modifica ? obtenerCsrf() : undefined;
  async function enviarEnSesion(tokenCsrf) {
    const valor = await tokenCsrf;
    if (version !== versionSesion) throw new DOMException("La sesión cambió", "AbortError");
    return enviar(path, { method, body, signal, csrf: valor });
  }
  try {
    try {
      return await enviarEnSesion(csrf);
    } catch (error) {
      if (!modifica || error.codigo !== "CSRF_INVALIDO" || version !== versionSesion || signal?.aborted) throw error;
      // Spring rechazó la solicitud antes de ejecutar el controlador.
      if (csrfPendiente === csrf) csrfPendiente = undefined;
      return await enviarEnSesion(obtenerCsrf());
    }
  } catch (error) {
    if (error.status === 401 && protegida && version === versionSesion && !signal?.aborted) {
      window.dispatchEvent(new Event("turnero:sesion-vencida"));
    }
    throw error;
  }
}

async function enviar(path, { method = "GET", body, signal, csrf }) {
  let response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      signal,
      credentials: "include",
      headers: {
        ...(body === undefined ? {} : { "Content-Type": "application/json" }),
        ...(csrf ? { "X-XSRF-TOKEN": csrf } : {}),
      },
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    });
  } catch (error) {
    if (error.name === "AbortError") throw error;
    throw new Error("No se pudo conectar con el servidor. Comprobá que el back esté iniciado.");
  }

  const text = await response.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }
  if (!response.ok) {
    const error = new Error(data?.mensaje || (typeof data === "string" && data) || `Error HTTP ${response.status}`);
    error.status = response.status;
    error.codigo = data?.codigo;
    throw error;
  }
  return data;
}

export const especialidades = {
  CARDIOLOGIA: "Cardiología",
  CLINICA_MEDICA: "Clínica médica",
  DERMATOLOGIA: "Dermatología",
  GINECOLOGIA: "Ginecología",
  PEDIATRIA: "Pediatría",
  TRAUMATOLOGIA: "Traumatología",
  OFTALMOLOGIA: "Oftalmología",
  PSIQUIATRIA: "Psiquiatría",
};

export const pesos = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS", maximumFractionDigits: 2 });

export function hoyISO() {
  const fecha = new Date();
  return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, "0")}-${String(fecha.getDate()).padStart(2, "0")}`;
}

export function fechaLegible(turno) {
  const { dia, hora } = turno.fecha;
  return new Intl.DateTimeFormat("es-AR", { dateStyle: "full", timeStyle: "short" })
    .format(new Date(`${dia}T${hora}`));
}

export function esFuturo(turno) {
  return new Date(`${turno.fecha.dia}T${turno.fecha.hora}`) > new Date();
}
