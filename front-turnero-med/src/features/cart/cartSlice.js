import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../lib/api.js";

export const fetchCart = createAsyncThunk(
  "cart/fetchCart",
  async ({ token }, { signal }) => api("/carritos", { token, method: "POST", signal }),
);

export const addToCart = createAsyncThunk(
  "cart/addToCart",
  async ({ turnoId, token }, { signal }) => {
    const carrito = await api("/carritos", { token, method: "POST", signal });
    return api(`/carritos/${carrito.id}/turnos/${turnoId}`, {
      token,
      method: "POST",
      signal,
    });
  },
);

export const removeFromCart = createAsyncThunk(
  "cart/removeFromCart",
  async ({ carritoId, turnoId, token }, { signal }) =>
    api(`/carritos/${carritoId}/turnos/${turnoId}`, {
      token,
      method: "DELETE",
      signal,
    }),
);

const initialState = {
  carrito: null,
  fetchStatus: "idle",
  addStatus: "idle",
  removeStatus: "idle",
  error: null,
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    resetCart: () => initialState,
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
      })
      .addCase(removeFromCart.pending, (state) => {
        state.removeStatus = "loading";
        state.error = null;
      })
      .addCase(removeFromCart.fulfilled, (state, action) => {
        state.removeStatus = "succeeded";
        state.carrito = action.payload;
      })
      .addCase(removeFromCart.rejected, (state, action) => {
        state.removeStatus = "failed";
        state.error = action.error.message || "No se pudo quitar el turno del carrito.";
      });
  },
});

export const { resetCart, clearCartError } = cartSlice.actions;
export default cartSlice.reducer;
