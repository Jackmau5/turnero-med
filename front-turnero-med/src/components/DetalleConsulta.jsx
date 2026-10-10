import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, especialidades, fechaLegible, pesos } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function DetalleConsulta() {
  const { id } = useParams();
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const [turno, setTurno] = useState(null);
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState("");

  useEffect(() => {
    let activo = true;
    setCargando(true); setError("");
    api(`/turnos/${id}`, { token: usuario?.token }).then((data) => { if (activo) setTurno(data); })
      .catch((e) => { if (activo) setError(e.message); })
      .finally(() => { if (activo) setCargando(false); });
    return () => { activo = false; };
  }, [id, usuario?.token]);

  async function reservar() {
    if (!usuario) {
      navigate("/ingresar", { state: { from: `/consultas/${id}` } });
      return;
    }
    if (usuario.rol !== "PACIENTE") return;
    setGuardando(true); setMensaje("");
    try {
      const carrito = await api("/carritos", { token: usuario.token, method: "POST" });
      await api(`/carritos/${carrito.id}/turnos/${turno.id}`, { token: usuario.token, method: "POST" });
      navigate("/carrito", { state: { aviso: "El turno se agregó al carrito." } });
    } catch (e) { setMensaje(e.message); }
    finally { setGuardando(false); }
  }
  if (cargando) return <p className="mensaje">Cargando consulta…</p>;
  if (error) return <div className="detalle-consulta"><Link to="/">← Volver</Link><p className="error" role="alert">{error}</p></div>;
  return (
    <article className="detalle-consulta panel">
      <Link className="enlace-volver" to="/">← Volver a los horarios</Link>
      <span className="especialidad">{especialidades[turno.especialidad] || turno.especialidad}</span>
      <h1>Consulta con {turno.medicoNombre}</h1>
      <p>{fechaLegible(turno)}</p>
      <p>{turno.direccion.calle} {turno.direccion.altura}, {turno.direccion.ciudad} {turno.direccion.codigoPostal || ""}</p>
      <div className="detalle-precio"><span>Valor de la consulta</span><strong>{pesos.format(turno.precio)}</strong></div>
      {turno.estado === "DISPONIBLE" && (
        usuario?.rol === "PACIENTE" || !usuario ?
          <button className="boton" disabled={guardando} onClick={reservar}>{guardando ? "Agregando…" : "Agregar al carrito"}</button> :
          <p className="aviso">Para reservar, ingresá con una cuenta de paciente.</p>
      )}
      {turno.estado !== "DISPONIBLE" && <p className="aviso">Este horario ya no está disponible.</p>}
      {mensaje && <p className="error" role="alert">{mensaje}</p>}
    </article>
  );
}
