import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function Perfil() {
  const { usuario, cerrarSesion } = useAuth();
  const navigate = useNavigate();
  const [datos, setDatos] = useState({ nombre: "", apellido: "", email: "", dni: "", password: "" });
  const [cargando, setCargando] = useState(true);
  const [ocupado, setOcupado] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => {
    api(`/pacientes/${usuario.id}`, { token: usuario.token }).then((p) => setDatos({ ...p, password: "" }))
      .catch((e) => setError(e.message)).finally(() => setCargando(false));
  }, [usuario]);
  async function guardar(e) {
    e.preventDefault(); setOcupado(true); setError("");
    try {
      await api(`/pacientes/${usuario.id}`, { token: usuario.token, method: "PUT", body: datos });
      cerrarSesion();
      navigate("/ingresar", { replace: true, state: { aviso: "Perfil actualizado. Ingresá nuevamente para continuar." } });
    } catch (e) { setError(e.message); }
    finally { setOcupado(false); }
  }
  return <section className="formulario-pagina"><p className="etiqueta">TU CUENTA</p><h1>Mi perfil</h1>
    {cargando ? <p className="mensaje">Cargando perfil…</p> : <form className="formulario panel" onSubmit={guardar}>
      {[ ["nombre", "Nombre"], ["apellido", "Apellido"], ["email", "Email"], ["dni", "DNI"] ].map(([campo, titulo]) =>
        <label key={campo}>{titulo}<input name={campo} type={campo === "email" ? "email" : "text"} required value={datos[campo] || ""} onChange={(e) => setDatos({ ...datos, [campo]: e.target.value })} /></label>)}
      <label>Contraseña nueva (opcional)<input name="password" type="password" minLength="8" value={datos.password} onChange={(e) => setDatos({ ...datos, password: e.target.value })} /></label>
      <p className="aviso">Después de guardar tendrás que ingresar nuevamente.</p>
      {error && <p className="error" role="alert">{error}</p>}
      <button className="boton" disabled={ocupado}>{ocupado ? "Guardando…" : "Guardar cambios"}</button>
    </form>}
  </section>;
}
