import { Link } from "react-router-dom";
import { especialidades, fechaLegible, pesos } from "../lib/api.js";

export default function Tarjeta({ turno }) {
  return (
    <article className="tarjeta">
      <div className="tarjeta-superior">
        <span className="especialidad">{especialidades[turno.especialidad] || turno.especialidad}</span>
        <span className="numero">#{turno.id}</span>
      </div>
      <h3>{turno.medicoNombre}</h3>
      <p className="tarjeta-descripcion">{fechaLegible(turno)}</p>
      <p className="tarjeta-descripcion">{turno.direccion.calle} {turno.direccion.altura}, {turno.direccion.ciudad}</p>
      <div className="tarjeta-precio">
        <span>Valor de la consulta</span>
        <strong>{pesos.format(turno.precio)}</strong>
      </div>
      <Link className="enlace-detalle" to={`/consultas/${turno.id}`}>Ver horario y reservar →</Link>
    </article>
  );
}
