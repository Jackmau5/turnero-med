import { useCallback, useEffect, useState } from "react";
import { api, especialidades } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

const vacio = { nombre: "", apellido: "", email: "", password: "", matricula: "", especialidad: "CARDIOLOGIA" };
export default function MedicosAdmin() {
  const { usuario } = useAuth();
  const [medicos, setMedicos] = useState([]);
  const [form, setForm] = useState(vacio);
  const [editando, setEditando] = useState(null);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const cargar = useCallback(() => api("/medicos").then(setMedicos).catch((e) => setError(e.message)), []);
  useEffect(() => { cargar(); }, [cargar]);
  async function guardar(e) {
    e.preventDefault(); setOcupado(true); setError(""); setAviso("");
    try {
      await api(editando ? `/medicos/${editando}` : "/medicos", { token: usuario.token, method: editando ? "PUT" : "POST", body: form });
      await cargar(); setForm(vacio); setEditando(null);
      setAviso(editando ? "Médico actualizado." : "Médico creado. Ya puede iniciar sesión y publicar horarios.");
    } catch (e) { setError(e.message); }
    finally { setOcupado(false); }
  }
  async function eliminar(m) {
    if (!window.confirm(`¿Eliminar a ${m.nombre} ${m.apellido}? Solo se podrá si no tiene turnos relacionados.`)) return;
    setOcupado(true); setError(""); setAviso("");
    try { await api(`/medicos/${m.id}`, { token: usuario.token, method: "DELETE" }); await cargar(); setAviso("Médico eliminado."); }
    catch (e) { setError(e.message); }
    finally { setOcupado(false); }
  }
  return <section className="pagina"><p className="etiqueta">ADMINISTRACIÓN</p><h1>Médicos</h1>
    {error && <p className="error" role="alert">{error}</p>}{aviso && <p className="exito" role="status">{aviso}</p>}
    <form className="formulario panel" onSubmit={guardar}><h2>{editando ? "Editar médico" : "Dar de alta un médico"}</h2>
      <div className="dos-columnas"><label>Nombre<input required value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} /></label>
      <label>Apellido<input required value={form.apellido} onChange={(e) => setForm({ ...form, apellido: e.target.value })} /></label></div>
      <label>Email<input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
      <div className="dos-columnas"><label>Matrícula<input required value={form.matricula} onChange={(e) => setForm({ ...form, matricula: e.target.value })} /></label>
      <label>Especialidad<select value={form.especialidad} onChange={(e) => setForm({ ...form, especialidad: e.target.value })}>{Object.entries(especialidades).map(([k, v]) => <option key={k} value={k}>{v}</option>)}</select></label></div>
      <label>Contraseña {editando && "(dejar vacía para conservarla)"}<input type="password" minLength="8" required={!editando} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></label>
      <div className="acciones"><button className="boton" disabled={ocupado}>{ocupado ? "Guardando…" : editando ? "Guardar cambios" : "Crear médico"}</button>
        {editando && <button type="button" className="boton-secundario" onClick={() => { setEditando(null); setForm(vacio); }}>Cancelar edición</button>}</div>
    </form>
    <h2>Profesionales registrados</h2>
    <div className="lista-paneles">{medicos.map((m) => <article className="panel fila" key={m.id}><div><h3>{m.nombre} {m.apellido}</h3><p>{especialidades[m.especialidad]} · Matrícula {m.matricula}</p><p>{m.email}</p></div>
      <div className="acciones"><button className="boton-secundario" onClick={() => { setEditando(m.id); setForm({ ...m, password: "" }); window.scrollTo({ top: 0, behavior: "smooth" }); }}>Editar</button>
        <button className="boton-secundario" disabled={ocupado} onClick={() => eliminar(m)}>Eliminar</button></div></article>)}</div>
  </section>;
}
