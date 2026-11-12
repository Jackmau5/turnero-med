import { createContext, useContext, useEffect, useState } from "react";
import { api } from "../lib/api.js";

const AuthContext = createContext(null);

// El token JWT vive en una cookie HttpOnly: el front nunca lo ve ni lo guarda.
// Acá solo se conservan los datos públicos del usuario para mostrar en pantalla.
function datosPublicos(data) {
  return { id: data.id, nombre: data.nombre, apellido: data.apellido, email: data.email, rol: data.rol };
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(true);

  // Al abrir la página se le pregunta al back si la cookie corresponde a una sesión válida.
  useEffect(() => {
    let activo = true;
    api("/auth/me")
      .then((data) => { if (activo) setUsuario(datosPublicos(data)); })
      .catch(() => {})
      .finally(() => { if (activo) setCargando(false); });
    return () => { activo = false; };
  }, []);

  function guardarSesion(data) {
    setUsuario(datosPublicos(data));
  }
  function cerrarSesion() {
    // El back borra la cookie; el front solo olvida al usuario.
    api("/auth/logout", { method: "POST" }).catch(() => {});
    setUsuario(null);
  }
  useEffect(() => {
    const vencida = () => setUsuario(null);
    window.addEventListener("turnero:sesion-vencida", vencida);
    return () => window.removeEventListener("turnero:sesion-vencida", vencida);
  }, []);
  return <AuthContext.Provider value={{ usuario, cargando, guardarSesion, cerrarSesion }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("AuthProvider no está disponible");
  return value;
}
