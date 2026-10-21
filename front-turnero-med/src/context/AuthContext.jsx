import { createContext, useContext, useEffect, useState } from "react";

const KEY = "turnero-med-sesion";
const AuthContext = createContext(null);

function leerSesion() {
  try {
    const value = JSON.parse(localStorage.getItem(KEY));
    if (!value?.token || !value?.rol || !value?.id) return null;
    const payload = JSON.parse(atob(value.token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    if (!payload.exp || payload.exp * 1000 <= Date.now()) {
      localStorage.removeItem(KEY);
      return null;
    }
    return value;
  } catch {
    localStorage.removeItem(KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerSesion);
  function guardarSesion(data) {
    const value = { id: data.id, nombre: data.nombre, apellido: data.apellido, email: data.email, rol: data.rol, token: data.token };
    localStorage.setItem(KEY, JSON.stringify(value));
    setUsuario(value);
  }
  function cerrarSesion() {
    localStorage.removeItem(KEY);
    setUsuario(null);
  }
  useEffect(() => {
    window.addEventListener("turnero:sesion-vencida", cerrarSesion);
    return () => window.removeEventListener("turnero:sesion-vencida", cerrarSesion);
  }, []);
  return <AuthContext.Provider value={{ usuario, guardarSesion, cerrarSesion }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("AuthProvider no está disponible");
  return value;
}
