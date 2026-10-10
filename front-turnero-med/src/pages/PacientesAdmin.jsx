import { useCallback, useEffect, useState } from "react";
import { api } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

const vacio = { nombre: "", apellido: "", email: "", dni: "", password: "" };
export default function PacientesAdmin() {
  const { usuario } = useAuth();
  const [pacientes, setPacientes] = useState([]);
  const [form, setForm] = useState(vacio);
  const [editando, setEditando] = useState(null);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const cargar = useCallback(() => api("/pacientes", { token: usuario.token }).then(setPacientes).catch((e) => setError(e.message)), [usuario.token]);
  useEffect(() => { cargar(); }, [cargar]);
  async function guardar(e) {
    e.preventDefault(); setOcupado(true); setError(""); setAviso("");
    try {
      await api(editando ? `/pacientes/${editando}` : "/pacientes", { token: usuario.token, method: editando ? "PUT" : "POST", body: form });
      await cargar(); setEditando(null); setForm(vacio); setAviso(editando ? "Paciente actualizado." : "Paciente creado.");
    } catch (e) { setError(e.message); } finally { setOcupado(false); }
  }
  async function eliminar(p) {
    if (!window.confirm(`¿Eliminar a ${p.nombre} ${p.apellido}? Solo se podrá si no tiene turnos o carritos relacionados.`)) return;
    setOcupado(true); setError(""); setAviso("");
    try { await api(`/pacientes/${p.id}`, { token: usuario.token, method: "DELETE" }); await cargar(); setAviso("Paciente eliminado."); }
    catch (e) { setError(e.message); } finally { setOcupado(false); }
  }
  return <section className="pagina"><p className="etiqueta">ADMINISTRACIÓN</p><h1>Pacientes</h1>
    {error && <p className="error" role="alert">{error}</p>}{aviso && <p className="exito" role="status">{aviso}</p>}
    <form className="formulario panel" onSubmit={guardar}><h2>{editando ? "Editar paciente" : "Dar de alta un paciente"}</h2>
      <div className="dos-columnas"><label>Nombre<input required value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} /></label><label>Apellido<input required value={form.apellido} onChange={(e) => setForm({ ...form, apellido: e.target.value })} /></label></div>
      <label>Email<input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
      <label>DNI<input required inputMode="numeric" pattern="[0-9]{7,10}" value={form.dni} onChange={(e) => setForm({ ...form, dni: e.target.value })} /></label>
      <label>Contraseña {editando && "(dejar vacía para conservarla)"}<input type="password" minLength="8" required={!editando} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></label>
      <div className="acciones"><button className="boton" disabled={ocupado}>{ocupado ? "Guardando…" : editando ? "Guardar cambios" : "Crear paciente"}</button>
        {editando && <button type="button" className="boton-secundario" onClick={() => { setEditando(null); setForm(vacio); }}>Cancelar edición</button>}</div>
    </form>
    <h2>Pacientes registrados</h2><div className="lista-paneles">{pacientes.map((p) => <article className="panel fila" key={p.id}><div><h3>{p.nombre} {p.apellido}</h3><p>DNI {p.dni} · {p.email}</p></div>
      <div className="acciones"><button className="boton-secundario" onClick={() => { setEditando(p.id); setForm({ ...p, password: "" }); window.scrollTo({ top: 0, behavior: "smooth" }); }}>Editar</button>
        <button className="boton-secundario" disabled={ocupado} onClick={() => eliminar(p)}>Eliminar</button></div></article>)}</div>
  </section>;
}
