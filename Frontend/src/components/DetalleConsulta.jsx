import { Link, useParams } from "react-router-dom";
import products from "../data/products.json";

const formatoPrecio = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  maximumFractionDigits: 0,
});

export default function DetalleConsulta() {
  const { id } = useParams();
  const consulta = products.find((producto) => String(producto.id) === id);

  if (!consulta) {
    return (
      <section className="detalle-consulta">
        <Link className="enlace-volver" to="/">← Volver a las consultas</Link>
        <h1>Consulta no encontrada</h1>
        <p>No existe una consulta con ese número.</p>
      </section>
    );
  }

  return (
    <article className="detalle-consulta">
      <Link className="enlace-volver" to="/">← Volver a las consultas</Link>
      <span className="especialidad">{consulta.especialidad}</span>
      <h1>{consulta.nombre}</h1>
      <p>{consulta.descripcion}</p>
      <div className="detalle-precio">
        <span>Valor de la consulta</span>
        <strong>{formatoPrecio.format(consulta.precio)}</strong>
      </div>
    </article>
  );
}
