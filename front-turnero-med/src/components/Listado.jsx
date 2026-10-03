import products from "../data/products.json";
import Tarjeta from "./Tarjeta.jsx";

export default function Listado() {
  return (
    <section aria-labelledby="titulo-catalogo">
      <div className="titulo-listado">
        <h2 id="titulo-catalogo">Consultas médicas</h2>
        <span>{products.length} especialidades</span>
      </div>
      <div className="listado">
        {products.map((producto) => (
          <Tarjeta key={producto.id} producto={producto} />
        ))}
      </div>
    </section>
  );
}
