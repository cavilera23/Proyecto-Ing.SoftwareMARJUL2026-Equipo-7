import { reactive } from "vue";

// Store de sesión simple (sin Pinia): estado reactivo + persistencia en localStorage.
const STORAGE_KEY = "cuidared_auth";

function cargarInicial() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : { token: null, usuario: null };
  } catch {
    return { token: null, usuario: null };
  }
}

const estado = reactive(cargarInicial());

function persistir() {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({ token: estado.token, usuario: estado.usuario }),
  );
}

export const auth = {
  estado,

  get token() {
    return estado.token;
  },
  get usuario() {
    return estado.usuario;
  },
  get tipoUsuario() {
    return estado.usuario ? estado.usuario.tipoUsuario : null;
  },
  get autenticado() {
    return !!estado.token;
  },

  iniciarSesion(token, usuario) {
    estado.token = token;
    estado.usuario = usuario;
    persistir();
  },

  // Refresca los datos del usuario en sesión (p. ej. tras editar el perfil),
  // manteniendo el mismo token.
  actualizarUsuario(usuario) {
    estado.usuario = usuario;
    persistir();
  },

  cerrarSesion() {
    estado.token = null;
    estado.usuario = null;
    localStorage.removeItem(STORAGE_KEY);
  },

  // Ruta de inicio según el rol del usuario.
  rutaDashboard() {
    return estado.usuario && estado.usuario.tipoUsuario === "CUIDADOR"
      ? "/dashboard-cuidador"
      : "/dashboard-padre";
  },
};
