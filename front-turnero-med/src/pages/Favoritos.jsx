import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { especialidades } from "../lib/api.js";
import { quitarFavorito, selectFavoritos, vaciarFavoritos } from "../store/favoritosSlice.js";

export default function Favoritos() {
  const medicos = useSelector(selectFavoritos);
  const dispatch = useDispatch();
  return (
    <section className="pagina">
      <p className="etiqueta">MIS FAVORITOS</p>
      <h1>Médicos favoritos</h1>
      {medicos.length === 0 ? (
        <p className="mensaje">Todavía no marcaste ningún médico. Tocá la ☆ en un horario para guardarlo. <Link to="/">Ver horarios</Link></p>
      ) : <>
        <div className="resumen">
          <span>{medicos.length} {medicos.length === 1 ? "médico" : "médicos"}</span>
          <button type="button" className="boton-secundario boton-chico" onClick={() => dispatch(vaciarFavoritos())}>Vaciar favoritos</button>
        </div>
        <div className="lista-paneles">
          {medicos.map((m) => (
            <article key={m.id} className="panel fila">
              <div>
                <span className="especialidad">{especialidades[m.especialidad] || m.especialidad}</span>
                <h3>{m.nombre}</h3>
              </div>
              <button type="button" className="boton-secundario boton-chico" onClick={() => dispatch(quitarFavorito(m.id))}>Quitar</button>
            </article>
          ))}
        </div>
      </>}
    </section>
  );
}
