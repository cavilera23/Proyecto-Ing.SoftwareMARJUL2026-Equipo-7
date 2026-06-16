import { apiFetch } from "./api";

// Inicia sesión. Devuelve { token, usuario }. auth:false → no manda token.
export const loginApi = async (correo, contrasena) => {
  return await apiFetch("/auth/login", {
    method: "POST",
    body: JSON.stringify({ correo, contrasena }),
    auth: false,
  });
};
