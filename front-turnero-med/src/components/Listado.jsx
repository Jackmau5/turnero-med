import { useEffect, useState } from "react";
import { api, especialidades, hoyISO } from "../lib/api.js";
import Tarjeta from "./Tarjeta.jsx";

export default function Listado() {
  const [turnos, setTurnos] = useState([]);
  const [medicos, setMedicos] = useState([]);
  const [filtros, setFiltros] = useState({ especialidad: "", medicoId: "", dia: "" });
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api("/medicos").then(setMedicos).catch((e) => setError(e.message));
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    const params = new URLSearchParams();
    Object.entries(filtros).forEach(([key, value]) => { if (value) params.set(key, value); });
    setCargando(true);
    setError("");
    api(`/turnos${params.size ? `?${params}` : ""}`, { signal: controller.signal })
      .then(setTurnos)
      .catch((e) => { if (e.name !== "AbortError") setError(e.message); })
      .finally(() => { if (!controller.signal.aborted) setCargando(false); });
    return () => controller.abort();
  }, [filtros]);

  function cambiar(e) { setFiltros((prev) => ({ ...prev, [e.target.name]: e.target.value })); }
  return (
    <section aria-labelledby="titulo-catalogo">
      <div className="titulo-listado">
        <div><p className="etiqueta">AGENDA ABIERTA</p><h2 id="titulo-catalogo">Horarios disponibles</h2></div>
        {!cargando && !error && <span>{turnos.length} {turnos.length === 1 ? "horario" : "horarios"}</span>}
      </div>
      <div className="filtros" aria-label="Filtrar horarios">
        <label>Especialidad
          <select name="especialidad" value={filtros.especialidad} onChange={cambiar}>
            <option value="">Todas</option>
            {Object.entries(especialidades).map(([clave, nombre]) => <option key={clave} value={clave}>{nombre}</option>)}
          </select>
        </label>
        <label>Médico
          <select name="medicoId" value={filtros.medicoId} onChange={cambiar}>
            <option value="">Todos</option>
            {medicos.map((m) => <option key={m.id} value={m.id}>{m.nombre} {m.apellido}</option>)}
          </select>
        </label>
        <label>Día
          <input type="date" name="dia" min={hoyISO()} value={filtros.dia} onChange={cambiar} />
        </label>
        <button type="button" className="boton-secundario" onClick={() => setFiltros({ especialidad: "", medicoId: "", dia: "" })}>Limpiar filtros</button>
      </div>
      {cargando ? <p className="mensaje">Cargando horarios…</p> : error ? <p className="error" role="alert">{error}</p> :
        turnos.length ? <div className="listado">{turnos.map((turno) => <Tarjeta key={turno.id} turno={turno} />)}</div> :
          <p className="mensaje">No hay horarios disponibles para esos filtros. Probá otro día o especialidad.</p>}
    </section>
  );
}
