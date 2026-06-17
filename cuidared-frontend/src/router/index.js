import { createRouter, createWebHistory } from "vue-router";
import { auth } from "@/stores/auth";

import HomeView from "../views/HomeView.vue";
import LoginView from "../views/LoginView.vue";
import DashboardCuidadorView from "../views/DashboardCuidadorView.vue";
import DashboardPadreView from "../views/DashboardPadreView.vue";
import AgendaView from "../views/AgendaView.vue";
import SolicitudesView from "../views/SolicitudesView.vue";
import PerfilView from "../views/PerfilView.vue";
import RegistroView from "../views/RegistroView.vue";
import MisCalificacionesView from "../views/MisCalificacionesView.vue";
import NotificacionesView from "../views/NotificacionesView.vue";
import BuscarCuidadoresView from "../views/BuscarCuidadoresView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Raíz: landing page pública. El guard redirige a su dashboard si ya hay sesión.
    { path: "/", name: "home", component: HomeView, meta: { publica: true } },

    // Públicas (no requieren sesión)
    { path: "/login", name: "login", component: LoginView, meta: { publica: true } },
    // Registro de una cuenta nueva (público).
    { path: "/registro", name: "registro", component: RegistroView, meta: { publica: true } },

    // Dashboards por rol
    {
      path: "/dashboard-cuidador",
      name: "dashboard-cuidador",
      component: DashboardCuidadorView,
      meta: { roles: ["CUIDADOR"] },
    },
    {
      path: "/dashboard-padre",
      name: "dashboard-padre",
      component: DashboardPadreView,
      meta: { roles: ["PADRE"] },
    },

    // Acciones del cuidador
    { path: "/agenda", name: "agenda", component: AgendaView, meta: { roles: ["CUIDADOR"] } },

    // Acciones del padre
    {
      path: "/buscar-cuidadores",
      name: "buscar-cuidadores",
      component: BuscarCuidadoresView,
      meta: { roles: ["PADRE"] },
    },

    // Acciones de ambos roles
    // Perfil propio: requiere sesión (ver/editar/eliminar la cuenta del token).
    { path: "/perfil", name: "perfil", component: PerfilView, meta: { roles: ["CUIDADOR", "PADRE"] } },
    { path: "/solicitudes", name: "solicitudes", component: SolicitudesView, meta: { roles: ["CUIDADOR", "PADRE"] } },
    { path: "/mis-calificaciones", name: "mis-calificaciones", component: MisCalificacionesView, meta: { roles: ["CUIDADOR", "PADRE"] } },
    { path: "/notificaciones", name: "notificaciones", component: NotificacionesView, meta: { roles: ["CUIDADOR", "PADRE"] } },
  ],
});

// Guard global: controla sesión y permisos por rol.
router.beforeEach((to) => {
  // Rutas públicas: si ya hay sesión, la landing y el login no tienen sentido,
  // así que mandamos al usuario directo a su dashboard.
  if (to.meta.publica) {
    if (auth.autenticado && (to.path === "/" || to.path === "/login")) {
      return auth.rutaDashboard();
    }
    return true;
  }

  // Rutas protegidas: exigen sesión. Si no la hay, al login con un aviso
  // (auth=required) para que el usuario sepa por qué fue redirigido.
  if (!auth.autenticado) {
    return { path: "/login", query: { auth: "required" } };
  }

  // Control de rol: si la ruta restringe roles y el usuario no encaja,
  // lo enviamos a su propio dashboard.
  const roles = to.meta.roles;
  if (roles && !roles.includes(auth.tipoUsuario)) {
    return auth.rutaDashboard();
  }

  return true;
});

export default router;
