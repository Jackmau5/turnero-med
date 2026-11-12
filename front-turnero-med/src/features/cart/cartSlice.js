import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../lib/api.js";

export const fetchCart = createAsyncThunk(
  "cart/fetchCart",
  async (_, { signal }) => api("/carritos", { method: "POST", signal }),
);

export const addToCart = createAsyncThunk(
  "cart/addToCart",
  async ({ turnoId }, { signal }) => {
    const carrito = await api("/carritos", { method: "POST", signal });
    return api(`/carritos/${carrito.id}/turnos/${turnoId}`, {
      method: "POST",
      signal,
    });
  },
);

const initialState = {
  carrito: null,
  fetchStatus: "idle",
  addStatus: "idle",
  error: null,
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    resetCart: () => initialState,
    restaurarCarrito(state, action) {
      if (action.payload && !state.carrito) state.carrito = action.payload; // copia local; el servidor la confirma o corrige enseguida
    },
    clearCartError(state) {
      state.error = null;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(fetchCart.pending, (state) => {
        state.fetchStatus = "loading";
        state.error = null;
      })
      .addCase(fetchCart.fulfilled, (state, action) => {
        state.fetchStatus = "succeeded";
        state.carrito = action.payload;
      })
      .addCase(fetchCart.rejected, (state, action) => {
        state.fetchStatus = "failed";
        state.error = action.error.message || "No se pudo cargar el carrito.";
      })
      .addCase(addToCart.pending, (state) => {
        state.addStatus = "loading";
        state.error = null;
      })
      .addCase(addToCart.fulfilled, (state, action) => {
        state.addStatus = "succeeded";
        state.carrito = action.payload;
      })
      .addCase(addToCart.rejected, (state, action) => {
        state.addStatus = "failed";
        state.error = action.error.message || "No se pudo agregar el turno al carrito.";
      });
  },
});

export const { resetCart, restaurarCarrito, clearCartError } = cartSlice.actions;

// Cantidad de turnos del carrito abierto y vigente (para la insignia de la barra).
export const cantidadCarrito = (state) => {
  const carrito = state.cart.carrito;
  if (!carrito || carrito.estado !== "ABIERTO" || new Date(carrito.venceEn) <= new Date()) return 0;
  return carrito.turnos.length;
};
export default cartSlice.reducer;
