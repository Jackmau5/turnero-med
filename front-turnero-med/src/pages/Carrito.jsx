import { useCallback, useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { api, especialidades, fechaLegible, pesos } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { fetchCart, removeFromCart } from "../features/cart/cartSlice.js";

export default function Carrito() {
  const { usuario } = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { carrito, fetchStatus, error: errorRedux } = useSelector((state) => state.cart);
  const [ocupado, setOcupado] = useState(false);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState(location.state?.aviso || "");
  const cargar = useCallback(() => dispatch(fetchCart({ token: usuario.token })).unwrap(), [dispatch, usuario.token]);
  useEffect(() => {
    cargar().catch(() => {});
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

  async function quitar(turnoId) {
    setOcupado(true); setError(""); setAviso("");
    try {
      await dispatch(removeFromCart({ carritoId: carrito.id, turnoId, token: usuario.token })).unwrap();
      setAviso("Turno quitado del carrito.");
    } catch (e) {
      setError(e.message);
      if (e.status === 409 && e.message.includes("venció")) await cargar().catch(() => {});
    } finally { setOcupado(false); }
  }

  if (fetchStatus === "loading" && !carrito) return <p className="mensaje">Cargando carrito…</p>;
  return <section className="pagina">
    <p className="etiqueta">TU SELECCIÓN</p><h1>Carrito de turnos</h1>
    {(error || errorRedux) && <p className="error" role="alert">{error || errorRedux}</p>}
    {aviso && <p className="exito" role="status">{aviso}</p>}
    {carrito && <>
      <p className="aviso">Los horarios quedan reservados hasta {new Date(carrito.venceEn).toLocaleString("es-AR", { dateStyle: "short", timeStyle: "short" })}. Confirmá antes de ese momento.</p>
      {carrito.turnos.length === 0 ? <div className="panel"><p>Tu carrito está vacío.</p><Link to="/">Buscar horarios</Link></div> : <>
        <div className="lista-paneles">{carrito.turnos.map((turno) => <article className="panel fila" key={turno.id}>
          <div><span className="especialidad">{especialidades[turno.especialidad]}</span><h2>{turno.medicoNombre}</h2>
            <p>{fechaLegible(turno)} · {turno.direccion.calle} {turno.direccion.altura}</p>
            <strong>{pesos.format(turno.precio)}</strong></div>
          <button className="boton-secundario" disabled={ocupado} onClick={() => quitar(turno.id)}>Quitar</button>
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
