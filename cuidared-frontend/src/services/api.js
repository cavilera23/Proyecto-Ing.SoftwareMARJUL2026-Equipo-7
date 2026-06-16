import { auth } from "../stores/auth";

const API_URL = "http://localhost:8080/api/v1";

export const apiFetch = async (endpoint, options = {}) => {
  const url = `${API_URL}${endpoint}`;
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  // Adjunta el token JWT salvo que la llamada pida explícitamente auth: false
  // (por ejemplo el propio login).
  if (options.auth !== false && auth.token) {
    headers["Authorization"] = `Bearer ${auth.token}`;
  }

  const response = await fetch(url, { ...options, headers });

  // Si teníamos sesión y el backend la rechaza (token expirado/ inválido),
  // cerramos sesión y mandamos al login.
  if (response.status === 401 && auth.token) {
    auth.cerrarSesion();
    if (window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
  }

  if (!response.ok) {
    const errorData = await response.text();
    throw new Error(errorData || "Error en la petición API");
  }

  // Intenta parsear json; si no puede (ej. 204 sin cuerpo), devuelve null.
  try {
    return await response.json();
  } catch (e) {
    return null;
  }
};
