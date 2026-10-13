import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function RutaProtegida({ roles, avisoSinPermiso }) {
  const { usuario } = useAuth();
  const location = useLocation();
  if (!usuario) return <Navigate to="/ingresar" replace state={{ from: location.pathname }} />;
  if (roles && !roles.includes(usuario.rol))
    return <Navigate to="/" replace state={avisoSinPermiso ? { aviso: avisoSinPermiso } : null} />;
  return <Outlet />;
}
