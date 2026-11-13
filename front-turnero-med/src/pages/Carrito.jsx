import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { especialidades, fechaLegible, pesos } from "../lib/api.js";
import { useAuth } from "../context/AuthContext.jsx";
import { cancelCart, confirmCart, emptyCart, fetchCart, removeFromCart } from "../features/cart/cartSlice.js";

export default function Carrito() {
  const { usuario } = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { carrito, status, error } = useSelector((state) => state.cart);
  const [aviso, setAviso] = useState(location.state?.aviso || "");
  const pacienteId = usuario.id;
  const ocupado = status === "loading";

  useEffect(() => {
    const request = dispatch(fetchCart({ pacienteId }));
    return () => request.abort();
  }, [dispatch, pacienteId]);

  async function accion(operation, mensaje, turnoId) {
    setAviso("");
    try {
      const data = await dispatch(operation({ carritoId: carrito.id, turnoId })).unwrap();
      if (operation === confirmCart) {
        navigate("/mis-turnos", { state: { aviso: `Reserva confirmada. Total: ${pesos.format(data.total)}.` } });
      } else {
        setAviso(mensaje);
      }
    } catch (error) {
      if (error.status === 409 && error.message.includes("venció")) {
        const resultado = await dispatch(fetchCart({ pacienteId }));
        if (fetchCart.fulfilled.match(resultado)) {
          setAviso("El carrito venció y se cargó uno nuevo.");
        }
      }
    }
  }

  if (ocupado && !carrito) return <p className="mensaje">Cargando carrito…</p>;
  return <section className="pagina">
    <p className="etiqueta">TU SELECCIÓN</p><h1>Carrito de turnos</h1>
    {error && <p className="error" role="alert">{error}</p>}
    {aviso && <p className="exito" role="status">{aviso}</p>}
    {carrito && <>
      {carrito.estado === "ABIERTO" && <p className="aviso">Los horarios quedan reservados hasta {new Date(carrito.venceEn).toLocaleString("es-AR", { dateStyle: "short", timeStyle: "short" })}. Confirmá antes de ese momento.</p>}
      {carrito.turnos.length === 0 ? <div className="panel"><p>Tu carrito está vacío.</p><Link to="/">Buscar horarios</Link></div> : <>
        <div className="lista-paneles">{carrito.turnos.map((turno) => <article className="panel fila" key={turno.id}>
          <div><span className="especialidad">{especialidades[turno.especialidad]}</span><h2>{turno.medicoNombre}</h2>
            <p>{fechaLegible(turno)} · {turno.direccion.calle} {turno.direccion.altura}</p>
            <strong>{pesos.format(turno.precio)}</strong></div>
          <button className="boton-secundario" disabled={ocupado} onClick={() => accion(removeFromCart, "Turno quitado del carrito.", turno.id)}>Quitar</button>
        </article>)}</div>
        <div className="panel resumen"><span>Total de la reserva</span><strong>{pesos.format(carrito.total)}</strong></div>
        <div className="acciones">
          <button className="boton" disabled={ocupado} onClick={() => accion(confirmCart)}>{ocupado ? "Procesando…" : "Confirmar turnos"}</button>
          <button className="boton-secundario" disabled={ocupado} onClick={() => accion(emptyCart, "Carrito vaciado.")}>Vaciar carrito</button>
          <button className="boton-secundario" disabled={ocupado} onClick={() => accion(cancelCart, "Carrito cancelado.")}>Cancelar carrito</button>
        </div>
      </>}
    </>}
  </section>;
}
