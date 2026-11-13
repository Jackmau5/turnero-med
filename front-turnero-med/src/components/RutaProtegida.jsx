import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function RutaProtegida({ roles }) {
  const { usuario, cargandoSesion } = useAuth();
  const location = useLocation();
  if (cargandoSesion) return <p className="mensaje">Verificando sesión…</p>;
  if (!usuario) return <Navigate to="/ingresar" replace state={{ from: location.pathname }} />;
  if (roles && !roles.includes(usuario.rol)) return <Navigate to="/" replace />;
  return <Outlet />;
}
