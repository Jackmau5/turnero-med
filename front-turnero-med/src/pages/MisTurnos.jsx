import { useCallback, useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { api, esFuturo, especialidades, fechaLegible, pesos } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function MisTurnos() {
  const { usuario } = useAuth();
  const location = useLocation();
  const [turnos, setTurnos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [ocupado, setOcupado] = useState(null);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState(location.state?.aviso || "");
  const cargar = useCallback(() => api("/turnos/mis-turnos", { token: usuario.token }).then(setTurnos), [usuario.token]);
  useEffect(() => { cargar().catch((e) => setError(e.message)).finally(() => setCargando(false)); }, [cargar]);
  async function cancelar(turno) {
    if (!window.confirm(`¿Cancelar el turno con ${turno.medicoNombre} del ${fechaLegible(turno)}?`)) return;
    setOcupado(turno.id); setError(""); setAviso("");
    try {
      await api(`/turnos/${turno.id}/cancelar`, { token: usuario.token, method: "POST" });
      await cargar(); setAviso("Turno cancelado. El horario volvió a estar disponible.");
    } catch (e) { setError(e.message); }
    finally { setOcupado(null); }
  }
  return <section className="pagina">
    <p className="etiqueta">TU HISTORIAL</p><h1>Mis turnos</h1>
    {aviso && <p className="exito" role="status">{aviso}</p>}{error && <p className="error" role="alert">{error}</p>}
    {cargando ? <p className="mensaje">Cargando turnos…</p> : turnos.length === 0 ? <div className="panel"><p>Todavía no tenés reservas confirmadas.</p><Link to="/">Buscar un turno</Link></div> :
      <div className="lista-paneles">{turnos.map((turno) => <article className="panel fila" key={turno.id}>
        <div><span className={`estado estado-${turno.estado.toLowerCase()}`}>{turno.estado === "TOMADO" ? "Confirmado" : "Cancelado"}</span>
          <h2>{turno.medicoNombre}</h2><p>{especialidades[turno.especialidad]} · {fechaLegible(turno)}</p>
          <p>{turno.direccion.calle} {turno.direccion.altura}, {turno.direccion.ciudad}</p><strong>{pesos.format(turno.precio)}</strong></div>
        {turno.estado === "TOMADO" && esFuturo(turno) && <button className="boton-secundario" disabled={ocupado === turno.id} onClick={() => cancelar(turno)}>Cancelar turno</button>}
      </article>)}</div>}
  </section>;
}
