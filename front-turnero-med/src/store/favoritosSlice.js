import { createSlice } from "@reduxjs/toolkit";

// Médicos favoritos del paciente. Guardamos sólo lo necesario para mostrarlos
// sin tener que volver a pedirlos al back.
const initialState = { medicos: [] };

const favoritosSlice = createSlice({
  name: "favoritos",
  initialState,
  reducers: {
    agregarFavorito(state, action) {
      const medico = action.payload;
      if (!state.medicos.some((m) => m.id === medico.id)) state.medicos.push(medico);
    },
    quitarFavorito(state, action) {
      state.medicos = state.medicos.filter((m) => m.id !== action.payload);
    },
    alternarFavorito(state, action) {
      const medico = action.payload;
      const indice = state.medicos.findIndex((m) => m.id === medico.id);
      if (indice === -1) state.medicos.push(medico);
      else state.medicos.splice(indice, 1);
    },
    vaciarFavoritos(state) {
      state.medicos = [];
    },
  },
});

export const { agregarFavorito, quitarFavorito, alternarFavorito, vaciarFavoritos } = favoritosSlice.actions;

export const selectFavoritos = (state) => state.favoritos.medicos;
export const selectCantidadFavoritos = (state) => state.favoritos.medicos.length;
export const selectEsFavorito = (id) => (state) => state.favoritos.medicos.some((m) => m.id === id);

export default favoritosSlice.reducer;
