import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../lib/api.js";

function cartRequest(name, request) {
  return createAsyncThunk(`cart/${name}`, async (args, { signal, rejectWithValue }) => {
    try {
      return await api(request.path(args), {
        protegida: true,
        method: request.method,
        body: request.body?.(args),
        signal,
      });
    } catch (error) {
      if (error.name === "AbortError") throw error;
      return rejectWithValue({ message: error.message, status: error.status });
    }
  });
}

export const fetchCart = cartRequest("fetchCart", {
  method: "POST",
  path: () => "/carritos",
});
export const addToCart = cartRequest("addToCart", {
  method: "POST",
  path: () => "/carritos/add",
  body: ({ turnoId }) => ({ id: turnoId }),
});
export const removeFromCart = cartRequest("removeFromCart", {
  method: "DELETE",
  path: ({ carritoId, turnoId }) => `/carritos/${carritoId}/turnos/${turnoId}`,
});
export const emptyCart = cartRequest("emptyCart", {
  method: "DELETE",
  path: ({ carritoId }) => `/carritos/${carritoId}/turnos`,
});
export const cancelCart = cartRequest("cancelCart", {
  method: "DELETE",
  path: ({ carritoId }) => `/carritos/${carritoId}`,
});
export const confirmCart = cartRequest("confirmCart", {
  method: "POST",
  path: ({ carritoId }) => `/carritos/${carritoId}/confirmar`,
});

const initialState = {
  carrito: null,
  status: "idle",
  requestId: null,
  error: null,
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    resetCart: () => initialState,
  },
  extraReducers(builder) {
    const operations = [fetchCart, addToCart, removeFromCart, emptyCart, cancelCart, confirmCart];
    for (const operation of operations) {
      builder
        .addCase(operation.pending, (state, { meta }) => {
          state.status = "loading";
          state.requestId = meta.requestId;
          state.error = null;
        })
        .addCase(operation.fulfilled, (state, { meta, payload }) => {
          if (state.requestId !== meta.requestId) return;
          state.status = "succeeded";
          state.requestId = null;
          state.carrito = payload;
        })
        .addCase(operation.rejected, (state, { meta, payload, error }) => {
          if (state.requestId !== meta.requestId) return;
          state.requestId = null;
          state.status = meta.aborted ? "idle" : "failed";
          if (!meta.aborted) state.error = payload?.message || error.message;
        });
    }
  },
});

export const { resetCart } = cartSlice.actions;
export default cartSlice.reducer;
