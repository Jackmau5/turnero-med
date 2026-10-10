function Tarjeta({ producto }) {
  return (
    <article className="tarjeta">
      <h3>{producto.nombre}</h3>
      <p>{producto.descripcion}</p>
      <p>Precio: ${producto.precio}</p>
    </article>
  );
}

export default Tarjeta;
