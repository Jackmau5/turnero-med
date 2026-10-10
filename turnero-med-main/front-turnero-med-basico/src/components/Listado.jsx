import products from "../data/products.json";
import Tarjeta from "./Tarjeta.jsx";

function Listado() {
  return (
    <section>
      <h2>Catálogo de consultas médicas</h2>
      <div className="listado">
        {products.map((producto) => (
          <Tarjeta key={producto.id} producto={producto} />
        ))}
      </div>
    </section>
  );
}

export default Listado;
