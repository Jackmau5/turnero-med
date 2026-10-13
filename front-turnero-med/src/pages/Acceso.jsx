import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { api } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function Acceso({ registro = false }) {
  const { guardarSesion } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [datos, setDatos] = useState({ nombre: "", apellido: "", email: "", dni: "", password: "" });
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);
  const cambiar = (e) => setDatos((actual) => ({ ...actual, [e.target.name]: e.target.value }));

  async function enviar(e) {
    e.preventDefault(); setEnviando(true); setError("");
    try {
      const cuerpo = registro ? datos : { email: datos.email, password: datos.password };
      const usuario = await api(registro ? "/auth/register" : "/auth/login", { method: "POST", body: cuerpo });
      guardarSesion(usuario);
      const destino = location.state?.from;
      navigate(destino && destino.startsWith("/") && !destino.startsWith("//") ? destino :
        usuario.rol === "PACIENTE" ? "/" : usuario.rol === "ADMIN" ? "/admin/medicos" : "/agenda", { replace: true });
    } catch (e) { setError(e.message); }
    finally { setEnviando(false); }
  }

  return (
    <section className="formulario-pagina">
      <p className="etiqueta">TU CUENTA</p>
      <h1>{registro ? "Crear cuenta" : "Ingresar"}</h1>
      <p className="descripcion-portada">{registro ? "Registrate como paciente para reservar un turno." : "Ingresá para reservar o gestionar turnos."}</p>
      {location.state?.aviso && <p className="exito" role="status">{location.state.aviso}</p>}
      <form className="formulario panel" onSubmit={enviar}>
        {registro && <div className="dos-columnas">
          <label>Nombre<input name="nombre" required maxLength="255" autoComplete="given-name" value={datos.nombre} onChange={cambiar} /></label>
          <label>Apellido<input name="apellido" required maxLength="255" autoComplete="family-name" value={datos.apellido} onChange={cambiar} /></label>
        </div>}
        <label>Email<input name="email" type="email" required autoComplete="email" value={datos.email} onChange={cambiar} /></label>
        {registro && <label>DNI<input name="dni" inputMode="numeric" pattern="[0-9]{7,10}" title="Entre 7 y 10 dígitos" required value={datos.dni} onChange={cambiar} /></label>}
        <label>Contraseña<input name="password" type="password" minLength="8" required autoComplete={registro ? "new-password" : "current-password"} value={datos.password} onChange={cambiar} /></label>
        {error && <p className="error" role="alert">{error}</p>}
        <button className="boton" disabled={enviando}>{enviando ? "Enviando…" : registro ? "Registrarme" : "Ingresar"}</button>
      </form>
      <p className="mensaje">{registro ? <>¿Ya tenés cuenta? <Link to="/ingresar" state={location.state}>Ingresá</Link></> : <>¿Sos paciente nuevo? <Link to="/registro" state={location.state}>Registrate</Link></>}</p>
    </section>
  );
}
