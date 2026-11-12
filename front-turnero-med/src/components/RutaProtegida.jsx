import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function RutaProtegida({ roles }) {
  const { usuario, cargando } = useAuth();
  const location = useLocation();
  if (cargando) return <p className="mensaje" role="status">Cargando…</p>;
  if (!usuario) return <Navigate to="/ingresar" replace state={{ from: location.pathname }} />;
  if (roles && !roles.includes(usuario.rol)) return <Navigate to="/" replace />;
  return <Outlet />;
}
