import { useCallback, useEffect, useState } from "react";
import { api, especialidades, fechaLegible, hoyISO, pesos } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

const vacio = { medicoId: "", dia: "", hora: "", precio: "", calle: "", altura: "", ciudad: "", codigoPostal: "" };
const estados = { DISPONIBLE: "Disponible", RESERVADO: "En carrito", TOMADO: "Confirmado", CANCELADO: "Cancelado" };

export default function Agenda() {
  const { usuario } = useAuth();
  const [medicos, setMedicos] = useState([]);
  const [medicoId, setMedicoId] = useState(usuario.rol === "MEDICO" ? String(usuario.id) : "");
  const [estado, setEstado] = useState("DISPONIBLE");
  const [turnos, setTurnos] = useState([]);
  const [form, setForm] = useState({ ...vacio, medicoId: usuario.rol === "MEDICO" ? String(usuario.id) : "" });
  const [editando, setEditando] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [ocupado, setOcupado] = useState(false);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");

  useEffect(() => { api("/medicos").then(setMedicos).catch((e) => setError(e.message)); }, []);
  const cargar = useCallback(async () => {
    setCargando(true); setError("");
    try {
      const params = new URLSearchParams({ estado });
      if (medicoId) params.set("medicoId", medicoId);
      const data = await api(`/turnos?${params}`, { token: usuario.token });
      setTurnos(data);
    } catch (e) { setError(e.message); }
    finally { setCargando(false); }
  }, [estado, medicoId, usuario.token]);
  useEffect(() => { cargar(); }, [cargar]);

  function cambiar(e) { setForm((prev) => ({ ...prev, [e.target.name]: e.target.value })); }
  function editar(turno) {
    setEditando(turno.id);
    setForm({ medicoId: String(turno.medicoId), dia: turno.fecha.dia, hora: turno.fecha.hora.slice(0, 5),
      precio: String(turno.precio), calle: turno.direccion.calle, altura: String(turno.direccion.altura),
      ciudad: turno.direccion.ciudad, codigoPostal: turno.direccion.codigoPostal || "" });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }
  function limpiar() { setEditando(null); setForm({ ...vacio, medicoId: usuario.rol === "MEDICO" ? String(usuario.id) : "" }); }
  async function enviar(e) {
    e.preventDefault(); setOcupado(true); setError(""); setAviso("");
    const body = { medicoId: Number(form.medicoId), fecha: { dia: form.dia, hora: form.hora },
      direccion: { calle: form.calle, altura: Number(form.altura), ciudad: form.ciudad, codigoPostal: form.codigoPostal },
      precio: Number(form.precio) };
    try {
      await api(editando ? `/turnos/${editando}` : "/turnos", { token: usuario.token, method: editando ? "PUT" : "POST", body });
      setAviso(editando ? "Horario actualizado." : "Horario publicado.");
      limpiar(); await cargar();
    } catch (e) { setError(e.message); }
    finally { setOcupado(false); }
  }
  async function retirar(turno) {
    if (!window.confirm(`¿Dar de baja el horario del ${fechaLegible(turno)}?`)) return;
    setOcupado(true); setError(""); setAviso("");
    try {
      await api(`/turnos/${turno.id}`, { token: usuario.token, method: "DELETE" });
      await cargar(); setAviso("Horario retirado de la oferta.");
    } catch (e) { setError(e.message); }
    finally { setOcupado(false); }
  }
  return <section className="pagina">
    <p className="etiqueta">GESTIÓN DE HORARIOS</p><h1>Agenda médica</h1>
    <p className="descripcion-portada">Publicá horarios futuros y consultá las reservas. Los horarios ocupados se conservan en el historial.</p>
    {error && <p className="error" role="alert">{error}</p>}{aviso && <p className="exito" role="status">{aviso}</p>}
    <form className="formulario panel" onSubmit={enviar}>
      <h2>{editando ? `Editar horario #${editando}` : "Publicar un horario"}</h2>
      <label>Médico
        <select name="medicoId" required disabled={usuario.rol === "MEDICO" || Boolean(editando)} value={form.medicoId} onChange={cambiar}>
          <option value="">Elegí un médico</option>
          {medicos.filter((m) => usuario.rol === "ADMIN" || m.id === usuario.id).map((m) => <option key={m.id} value={m.id}>{m.nombre} {m.apellido} — {especialidades[m.especialidad]}</option>)}
        </select>
      </label>
      <div className="dos-columnas">
        <label>Fecha<input type="date" name="dia" min={hoyISO()} required value={form.dia} onChange={cambiar} /></label>
        <label>Hora<input type="time" name="hora" step="60" required value={form.hora} onChange={cambiar} /></label>
      </div>
      <div className="dos-columnas">
        <label>Precio en pesos<input type="number" name="precio" min="0" step="0.01" required value={form.precio} onChange={cambiar} /></label>
        <label>Calle<input name="calle" required value={form.calle} onChange={cambiar} /></label>
      </div>
      <div className="dos-columnas">
        <label>Altura<input type="number" name="altura" min="0" required value={form.altura} onChange={cambiar} /></label>
        <label>Ciudad<input name="ciudad" required value={form.ciudad} onChange={cambiar} /></label>
      </div>
      <label>Código postal (opcional)<input name="codigoPostal" value={form.codigoPostal} onChange={cambiar} /></label>
      <div className="acciones"><button className="boton" disabled={ocupado}>{ocupado ? "Guardando…" : editando ? "Guardar horario" : "Publicar horario"}</button>
        {editando && <button type="button" className="boton-secundario" onClick={limpiar}>Cancelar edición</button>}</div>
    </form>
    <div className="titulo-listado"><h2>Turnos de la agenda</h2><div className="filtros-compactos">
      {usuario.rol === "ADMIN" && <select aria-label="Filtrar por médico" value={medicoId} onChange={(e) => setMedicoId(e.target.value)}><option value="">Todos los médicos</option>{medicos.map((m) => <option key={m.id} value={m.id}>{m.nombre} {m.apellido}</option>)}</select>}
      <select aria-label="Filtrar por estado" value={estado} onChange={(e) => setEstado(e.target.value)}>{Object.entries(estados).map(([clave, nombre]) => <option key={clave} value={clave}>{nombre}</option>)}</select>
    </div></div>
    {cargando ? <p className="mensaje">Cargando agenda…</p> : turnos.length === 0 ? <p className="mensaje">No hay turnos con ese estado.</p> :
      <div className="lista-paneles">{turnos.map((t) => <article className="panel fila" key={t.id}>
        <div><span className={`estado estado-${t.estado.toLowerCase()}`}>{estados[t.estado]}</span><h2>{t.medicoNombre}</h2>
          <p>{especialidades[t.especialidad]} · {fechaLegible(t)} · Turno #{t.id}</p>
          <p>{t.direccion.calle} {t.direccion.altura}, {t.direccion.ciudad}</p>
          {t.pacienteId && <p>Paciente #{t.pacienteId}</p>}<strong>{pesos.format(t.precio)}</strong></div>
        {t.estado === "DISPONIBLE" && <div className="acciones"><button className="boton-secundario" onClick={() => editar(t)}>Editar</button>
          <button className="boton-secundario" disabled={ocupado} onClick={() => retirar(t)}>Dar de baja</button></div>}
      </article>)}</div>}
  </section>;
}
