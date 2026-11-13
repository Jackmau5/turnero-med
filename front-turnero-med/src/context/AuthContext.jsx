import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { resetCart } from "../features/cart/cartSlice.js";
import { api, cambiarSesionApi } from "../lib/api.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const dispatch = useDispatch();
  const [usuario, setUsuario] = useState(null);
  const [cargandoSesion, setCargandoSesion] = useState(true);

  const guardarSesion = useCallback((perfil) => {
    cambiarSesionApi();
    dispatch(resetCart());
    setUsuario(perfil);
    setCargandoSesion(false);
  }, [dispatch]);

  const cerrarSesion = useCallback(async () => {
    await api("/auth/logout", { method: "POST" });
    guardarSesion(null);
  }, [guardarSesion]);

  useEffect(() => {
    try {
      localStorage.removeItem("turnero-med-sesion");
    } catch {
      // La restauración por cookie también funciona con el almacenamiento bloqueado.
    }
    const controller = new AbortController();
    api("/auth/me", { signal: controller.signal })
      .then((perfil) => { if (!controller.signal.aborted) guardarSesion(perfil); })
      .catch(() => { if (!controller.signal.aborted) setCargandoSesion(false); });
    const sesionVencida = () => guardarSesion(null);
    window.addEventListener("turnero:sesion-vencida", sesionVencida);
    return () => {
      controller.abort();
      window.removeEventListener("turnero:sesion-vencida", sesionVencida);
    };
  }, [guardarSesion]);

  return <AuthContext.Provider value={{ usuario, cargandoSesion, guardarSesion, cerrarSesion }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("AuthProvider no está disponible");
  return value;
}
