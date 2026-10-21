import { Link } from "react-router-dom";

const formatoPrecio = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  maximumFractionDigits: 0,
});

export default function Tarjeta({ producto }) {
  return (
    <article className="tarjeta">
      <div className="tarjeta-superior">
        <span className="especialidad">{producto.especialidad}</span>
        <span className="numero" aria-hidden="true">
          {String(producto.id).padStart(2, "0")}
        </span>
      </div>
      <h3>{producto.nombre}</h3>
      <p className="tarjeta-descripcion">{producto.descripcion}</p>
      <div className="tarjeta-precio">
        <span>Valor de la consulta</span>
        <strong>{formatoPrecio.format(producto.precio)}</strong>
      </div>
      <Link className="enlace-detalle" to={`/consultas/${producto.id}`}>
        Ver detalle de {producto.especialidad}
      </Link>
    </article>
  );
}
