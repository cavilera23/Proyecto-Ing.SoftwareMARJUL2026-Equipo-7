import { createRouter, createWebHistory } from "vue-router";
import AgendaView from "../views/AgendaView.vue";
import SolicitudesView from "../views/SolicitudesView.vue";
import PerfilView from "../views/PerfilView.vue";
import MisCalificacionesView from "../views/MisCalificacionesView.vue"; // Lógica de Jesús recuperada
import NotificacionesView from "../views/NotificacionesView.vue";
import BuscarCuidadoresView from "../views/BuscarCuidadoresView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      redirect: "/agenda",
    },
    {
      path: "/agenda",
      name: "agenda",
      component: AgendaView,
    },
    {
      path: "/solicitudes",
      name: "solicitudes",
      component: SolicitudesView,
    },
    {
      path: "/perfil",
      name: "perfil",
      component: PerfilView,
    },
    {
      // Lógica de Jesús recuperada
      path: "/mis-calificaciones",
      name: "mis-calificaciones",
      component: MisCalificacionesView,
    },
    {
      path: "/notificaciones",
      name: "notificaciones",
      component: NotificacionesView,
    },
    {
      path: "/buscar-cuidadores",
      name: "buscar-cuidadores",
      component: BuscarCuidadoresView,
    },
    {
      path: "/",
      name: "home",
      component: () => import("../views/HomeView.vue"), // <-- Quitas el redirect y pones esto
    },
  ],
});

export default router;
