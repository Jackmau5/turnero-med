import { useCallback, useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { api, especialidades, fechaLegible, pesos } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function Carrito() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [carrito, setCarrito] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [ocupado, setOcupado] = useState(false);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState(location.state?.aviso || "");
  const cargar = useCallback(async () => {
    const data = await api("/carritos", { token: usuario.token, method: "POST" });
    setCarrito(data);
  }, [usuario.token]);
  useEffect(() => {
    cargar().catch((e) => setError(e.message)).finally(() => setCargando(false));
  }, [cargar]);

  async function accion(method, path, mensaje) {
    setOcupado(true); setError(""); setAviso("");
    try {
      const data = await api(path, { token: usuario.token, method });
      if (method === "POST" && path.endsWith("/confirmar")) {
        navigate("/mis-turnos", { state: { aviso: `Reserva confirmada. Total: ${pesos.format(data.total)}.` } });
      } else {
        await cargar();
        setAviso(mensaje);
      }
    } catch (e) {
      setError(e.message);
      if (e.status === 409 && e.message.includes("venció")) await cargar().catch(() => {});
    } finally { setOcupado(false); }
  }

  if (cargando) return <p className="mensaje">Cargando carrito…</p>;
  return <section className="pagina">
    <p className="etiqueta">TU SELECCIÓN</p><h1>Carrito de turnos</h1>
    {error && <p className="error" role="alert">{error}</p>}
    {aviso && <p className="exito" role="status">{aviso}</p>}
    {carrito && <>
      <p className="aviso">Los horarios quedan reservados hasta {new Date(carrito.venceEn).toLocaleString("es-AR", { dateStyle: "short", timeStyle: "short" })}. Confirmá antes de ese momento.</p>
      {carrito.turnos.length === 0 ? <div className="panel"><p>Tu carrito está vacío.</p><Link to="/">Buscar horarios</Link></div> : <>
        <div className="lista-paneles">{carrito.turnos.map((turno) => <article className="panel fila" key={turno.id}>
          <div><span className="especialidad">{especialidades[turno.especialidad]}</span><h2>{turno.medicoNombre}</h2>
            <p>{fechaLegible(turno)} · {turno.direccion.calle} {turno.direccion.altura}</p>
            <strong>{pesos.format(turno.precio)}</strong></div>
          <button className="boton-secundario" disabled={ocupado} onClick={() => accion("DELETE", `/carritos/${carrito.id}/turnos/${turno.id}`, "Turno quitado del carrito.")}>Quitar</button>
        </article>)}</div>
        <div className="panel resumen"><span>Total de la reserva</span><strong>{pesos.format(carrito.total)}</strong></div>
        <div className="acciones">
          <button className="boton" disabled={ocupado} onClick={() => accion("POST", `/carritos/${carrito.id}/confirmar`)}>{ocupado ? "Procesando…" : "Confirmar turnos"}</button>
          <button className="boton-secundario" disabled={ocupado} onClick={() => accion("DELETE", `/carritos/${carrito.id}/turnos`, "Carrito vaciado.")}>Vaciar carrito</button>
          <button className="boton-secundario" disabled={ocupado} onClick={() => accion("DELETE", `/carritos/${carrito.id}`, "Carrito cancelado.")}>Cancelar carrito</button>
        </div>
      </>}
    </>}
  </section>;
}
