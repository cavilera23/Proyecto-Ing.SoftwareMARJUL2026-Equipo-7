import { createRouter, createWebHistory } from 'vue-router'
import AgendaView from '../views/AgendaView.vue'
import SolicitudesView from '../views/SolicitudesView.vue'
import PerfilView from '../views/PerfilView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/agenda'
    },
    {
      path: '/agenda',
      name: 'agenda',
      component: AgendaView
    },
    {
      path: '/solicitudes',
      name: 'solicitudes',
      component: SolicitudesView
    },
    {
      path: '/perfil',
      name: 'perfil',
      component: PerfilView
    }
  ]
})

export default router
