import { useDispatch, useSelector } from "react-redux";
import { alternarFavorito, selectEsFavorito } from "../store/favoritosSlice.js";

export default function BotonFavorito({ medico }) {
  const dispatch = useDispatch();
  const esFavorito = useSelector(selectEsFavorito(medico.id));
  return (
    <button
      type="button"
      className={esFavorito ? "favorito activo" : "favorito"}
      aria-pressed={esFavorito}
      aria-label={esFavorito ? `Quitar a ${medico.nombre} de favoritos` : `Agregar a ${medico.nombre} a favoritos`}
      onClick={() => dispatch(alternarFavorito(medico))}
    >
      {esFavorito ? "★" : "☆"}
    </button>
  );
}
