export const API_URL = (import.meta.env.VITE_API_URL || "/api").replace(/\/$/, "");

export async function api(path, { token, method = "GET", body, signal } = {}) {
  let response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      signal,
      headers: {
        ...(body === undefined ? {} : { "Content-Type": "application/json" }),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
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
    if (response.status === 401 && token) window.dispatchEvent(new Event("turnero:sesion-vencida"));
    const error = new Error(data?.mensaje || (typeof data === "string" && data) || `Error HTTP ${response.status}`);
    error.status = response.status;
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

// Nombre para mostrar. Una especialidad nueva del back sin nombre cargado se muestra tal cual llega.
export function nombreEspecialidad(clave) {
  return especialidades[clave] || clave;
}

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
