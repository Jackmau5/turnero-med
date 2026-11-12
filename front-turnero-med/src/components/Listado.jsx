import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { api, especialidades, hoyISO } from "../lib/api.js";
import Tarjeta from "./Tarjeta.jsx";

export default function Listado() {
  const [turnos, setTurnos] = useState([]);
  const [medicos, setMedicos] = useState([]);
  // Los filtros viven en la URL (?q=...&especialidad=...): se conservan al recargar, al compartir el enlace y al volver desde un detalle.
  const [params, setParams] = useSearchParams();
  const filtros = { q: params.get("q") || "", especialidad: params.get("especialidad") || "", medicoId: params.get("medicoId") || "", dia: params.get("dia") || "" };
  const [texto, setTexto] = useState(filtros.q);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  function actualizar(cambios) {
    setParams((prev) => {
      const siguiente = new URLSearchParams(prev);
      Object.entries(cambios).forEach(([clave, valor]) => { if (valor) siguiente.set(clave, valor); else siguiente.delete(clave); });
      return siguiente;
    }, { replace: true });
  }
  // La búsqueda se hace solo al apretar Enter (o el botón Buscar), no en cada letra.
  function buscar(e) { e.preventDefault(); actualizar({ q: texto.trim() }); }
  // Si la URL cambia desde afuera (botón Atrás, enlace), el cuadro de búsqueda la acompaña.
  useEffect(() => { setTexto(filtros.q); }, [filtros.q]);
  useEffect(() => {
    api("/medicos").then(setMedicos).catch((e) => setError(e.message));
  }, []);
  useEffect(() => {
    const controller = new AbortController();
    const consulta = new URLSearchParams();
    Object.entries(filtros).forEach(([key, value]) => { if (value) consulta.set(key, value); });
    setCargando(true);
    setError("");
    api(`/turnos${consulta.size ? `?${consulta}` : ""}`, { signal: controller.signal })
      .then(setTurnos)
      .catch((e) => { if (e.name !== "AbortError") setError(e.message); })
      .finally(() => { if (!controller.signal.aborted) setCargando(false); });
    return () => controller.abort();
  }, [params]);

  function cambiar(e) { actualizar({ [e.target.name]: e.target.value }); }
  function limpiar() { setTexto(""); setParams({}, { replace: true }); }
  return (
    <section aria-labelledby="titulo-catalogo">
      <div className="titulo-listado">
        <div><p className="etiqueta">AGENDA ABIERTA</p><h2 id="titulo-catalogo">Horarios disponibles</h2></div>
        {!cargando && !error && <span>{turnos.length} {turnos.length === 1 ? "horario" : "horarios"}</span>}
      </div>
      <div className="filtros" role="search" aria-label="Buscar y filtrar horarios">
        <form className="filtro-busqueda buscador" onSubmit={buscar}>
          <label>Buscar
            <input type="search" name="q" maxLength="100" autoComplete="off" enterKeyHint="search" placeholder="Médico, especialidad o zona… y Enter" value={texto} onChange={(e) => setTexto(e.target.value)} />
          </label>
          <button type="submit" className="boton">Buscar</button>
        </form>
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
        <button type="button" className="boton-secundario" onClick={limpiar}>Limpiar filtros</button>
      </div>
      {cargando ? <p className="mensaje">Cargando horarios…</p> : error ? <p className="error" role="alert">{error}</p> :
        turnos.length ? <div className="listado">{turnos.map((turno) => <Tarjeta key={turno.id} turno={turno} />)}</div> :
          <p className="mensaje">{filtros.q ? <>No encontramos horarios para «{filtros.q}». Probá con otra palabra o quitá algún filtro.</> : "No hay horarios disponibles para esos filtros. Probá otro día o especialidad."}</p>}
    </section>
  );
}
