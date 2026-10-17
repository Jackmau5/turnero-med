import { configureStore } from "@reduxjs/toolkit";
import favoritosReducer from "./favoritosSlice.js";

const KEY = "turnero-med-favoritos";

function leerFavoritos() {
  try {
    const medicos = JSON.parse(localStorage.getItem(KEY));
    return Array.isArray(medicos) ? { favoritos: { medicos } } : undefined;
  } catch {
    return undefined;
  }
}

export const store = configureStore({
  reducer: { favoritos: favoritosReducer },
  preloadedState: leerFavoritos(),
});

// Persistimos los favoritos para que sobrevivan a un refresh.
store.subscribe(() => {
  localStorage.setItem(KEY, JSON.stringify(store.getState().favoritos.medicos));
});
