// Persistencia del carrito en el navegador.
// El carrito de verdad vive en la base de datos del servidor (vence a los 15 minutos); esta copia local solo
// sirve para mostrarlo al instante al recargar la página y se revalida contra el servidor apenas arranca.
// No guarda credenciales: la sesión va en una cookie HttpOnly.

const clave = (usuarioId) => `turnero-med-carrito-${usuarioId}`;
const PENDIENTE = "turnero-med-reserva-pendiente";
const VIGENCIA_PENDIENTE_MS = 30 * 60 * 1000;

export function leerCarrito(usuarioId) {
  try {
    const carrito = JSON.parse(localStorage.getItem(clave(usuarioId)));
    if (!carrito || carrito.pacienteId !== usuarioId || !Array.isArray(carrito.turnos)) return null;
    if (!carrito.venceEn || new Date(carrito.venceEn) <= new Date()) return null; // un carrito vencido no se restaura
    return carrito;
  } catch {
    return null;
  }
}

export function guardarCarrito(usuarioId, carrito) {
  try { localStorage.setItem(clave(usuarioId), JSON.stringify(carrito)); } catch { /* almacenamiento lleno o bloqueado */ }
}

export function borrarCarrito(usuarioId) {
  try { localStorage.removeItem(clave(usuarioId)); } catch { /* nada que hacer */ }
}

// Reserva pendiente: el visitante apretó «Agregar al carrito» sin tener sesión. Se recuerda el horario
// para agregarlo solo cuando ingrese o se registre (válido 30 minutos).
export function guardarPendiente(turnoId) {
  try { localStorage.setItem(PENDIENTE, JSON.stringify({ turnoId, ts: Date.now() })); } catch { /* nada que hacer */ }
}

export function tomarPendiente() {
  try {
    const valor = JSON.parse(localStorage.getItem(PENDIENTE));
    localStorage.removeItem(PENDIENTE);
    if (!valor?.turnoId || Date.now() - valor.ts > VIGENCIA_PENDIENTE_MS) return null;
    return valor.turnoId;
  } catch {
    return null;
  }
}
