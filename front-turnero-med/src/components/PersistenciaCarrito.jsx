import { useEffect, useRef } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { addToCart, fetchCart, resetCart, restaurarCarrito } from "../features/cart/cartSlice.js";
import { borrarCarrito, guardarCarrito, leerCarrito, tomarPendiente } from "../features/cart/persistencia.js";

// No dibuja nada: mantiene el carrito sincronizado con el navegador y con el servidor.
export default function PersistenciaCarrito() {
  const { usuario } = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const carrito = useSelector((state) => state.cart.carrito);
  const iniciadoPara = useRef(null);
  const usuarioPrevio = useRef(null);

  // 1) Cuando se identifica un paciente: mostrar la copia local, completar la reserva pendiente (si la hay)
  //    y pedirle al servidor el carrito real.
  useEffect(() => {
    if (!usuario) return;
    if (usuario.rol !== "PACIENTE") { tomarPendiente(); return; }
    if (iniciadoPara.current === usuario.id) return; // evita repetir el arranque (StrictMode / re-render)
    iniciadoPara.current = usuario.id;
    dispatch(restaurarCarrito(leerCarrito(usuario.id)));
    const pendiente = tomarPendiente();
    if (pendiente) {
      dispatch(addToCart({ turnoId: pendiente })).unwrap()
        .then(() => navigate("/carrito", { state: { aviso: "El turno que elegiste se agregó al carrito." } }))
        .catch(() => dispatch(fetchCart())); // por ejemplo, el horario ya lo tomó otro paciente
    } else {
      dispatch(fetchCart());
    }
  }, [usuario, dispatch, navigate]);

  // 2) Guardar cada cambio del carrito.
  useEffect(() => {
    if (usuario?.rol === "PACIENTE" && carrito) guardarCarrito(usuario.id, carrito);
  }, [usuario, carrito]);

  // 3) Al salir (o si la sesión vence): borrar la copia local y limpiar el estado.
  useEffect(() => {
    if (usuarioPrevio.current && !usuario) {
      borrarCarrito(usuarioPrevio.current);
      iniciadoPara.current = null;
      dispatch(resetCart());
    }
    usuarioPrevio.current = usuario?.id ?? null;
  }, [usuario, dispatch]);

  return null;
}
