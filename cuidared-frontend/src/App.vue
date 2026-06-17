<script setup>
import { computed } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { auth } from "@/stores/auth";

const route = useRoute();
const router = useRouter();

// El navbar de la app solo se muestra con sesión iniciada.
const mostrarNav = computed(() => auth.autenticado);
// El header público (logo + Iniciar sesión / Registrarse) se muestra sin sesión,
// salvo en la propia pantalla de login (que ya tiene su propio acceso).
const mostrarHeaderPublico = computed(
  () => !auth.autenticado && route.path !== "/login",
);
const esCuidador = computed(() => auth.tipoUsuario === "CUIDADOR");
const esPadre = computed(() => auth.tipoUsuario === "PADRE");

const cerrarSesion = () => {
  auth.cerrarSesion();
  router.push("/login");
};
</script>

<template>
  <div class="app-layout">
    <!-- Header público: visible sin sesión (landing y registro) -->
    <header class="navbar" v-if="mostrarHeaderPublico">
      <div class="nav-container">
        <RouterLink to="/" class="logo-area">
          <span class="logo-icono">🏡</span>
          <span class="logo-texto"
            >Cuida<span class="green-text">Red</span></span
          >
        </RouterLink>

        <div class="auth-acciones">
          <RouterLink to="/login" class="btn-auth-secundario">Iniciar sesión</RouterLink>
          <RouterLink to="/registro" class="btn-auth-primario">Registrarse</RouterLink>
        </div>
      </div>
    </header>

    <header class="navbar" v-if="mostrarNav">
      <div class="nav-container">
        <RouterLink :to="auth.rutaDashboard()" class="logo-area">
          <span class="logo-icono">🏡</span>
          <span class="logo-texto"
            >Cuida<span class="green-text">Red</span></span
          >
        </RouterLink>

        <input type="checkbox" id="menu-toggle" class="menu-toggle" />
        <label for="menu-toggle" class="hamburger">
          <span></span>
          <span></span>
          <span></span>
        </label>

        <nav class="nav-links">
          <RouterLink :to="auth.rutaDashboard()">Inicio</RouterLink>

          <!-- Enlaces del cuidador -->
          <template v-if="esCuidador">
            <RouterLink to="/agenda">Agenda</RouterLink>
          </template>

          <!-- Enlaces del padre -->
          <template v-if="esPadre">
            <RouterLink to="/buscar-cuidadores">Buscar Cuidadores</RouterLink>
          </template>

          <!-- Comunes a ambos roles -->
          <RouterLink to="/solicitudes">Solicitudes</RouterLink>
          <RouterLink to="/mis-calificaciones">Calificaciones</RouterLink>
          <RouterLink to="/notificaciones">Notificaciones</RouterLink>
          <RouterLink to="/perfil">Perfil</RouterLink>

          <span class="usuario-chip">{{ auth.usuario?.nombre }}</span>
          <button class="btn-logout" @click="cerrarSesion">Salir</button>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <div class="container">
        <RouterView />
      </div>
    </main>

    <footer class="footer">
      <div class="footer-container">
        <p>
          &copy; 2026 CuidaRed - Planificación de cuidados P2P. Caracas,
          Venezuela.
        </p>
        <small class="footer-sub"
          >Proyecto Académico • Ingeniería de Software • UCAB</small
        >
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* Contenedor principal en malla/flex para empujar el footer al fondo si hay poco contenido */
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-background);
}

/* --- NAVBAR (ANCHO COMPLETO) --- */
.navbar {
  background-color: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
  width: 100%;
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 15px 0;
}

/* Alineación interna del navbar respetando márgenes estables */
.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-heading);
  letter-spacing: -0.5px;
}

.green-text {
  color: var(--color-primary);
}

.nav-links {
  display: flex;
  gap: 15px;
}

.nav-links a {
  color: var(--color-text);
  font-weight: 500;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.25s ease;
}

.nav-links a:hover {
  color: var(--color-heading);
  background-color: rgba(255, 255, 255, 0.05);
}

/* Enlace activo con los colores de tu maqueta */
.router-link-active {
  background-color: rgba(16, 185, 129, 0.15) !important;
  color: var(--color-primary) !important;
  font-weight: 600 !important;
}

/* El logo no debe pintarse como "enlace activo" aunque apunte al dashboard */
.logo-area {
  cursor: pointer;
}
.logo-area.router-link-active {
  background-color: transparent !important;
}

/* Nombre del usuario y botón de cerrar sesión */
.usuario-chip {
  display: flex;
  align-items: center;
  color: var(--color-text);
  font-size: 13px;
  font-weight: 600;
  padding: 8px 12px;
  border-left: 1px solid var(--color-border);
  margin-left: 6px;
}

.btn-logout {
  background-color: transparent;
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.4);
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-logout:hover {
  background-color: rgba(239, 68, 68, 0.12);
}

/* Botones del header público (landing) */
.auth-acciones {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-auth-secundario {
  color: var(--color-heading);
  font-weight: 600;
  font-size: 14px;
  padding: 9px 16px;
  border-radius: 8px;
}

.btn-auth-secundario:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.btn-auth-primario {
  background-color: var(--color-primary);
  color: white;
  font-weight: 600;
  font-size: 14px;
  padding: 9px 18px;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.btn-auth-primario:hover {
  background-color: var(--color-primary-hover);
}

/* --- MENÚ HAMBURGUESA (RESPONSIVE) --- */
.menu-toggle {
  display: none;
}

.hamburger {
  display: none;
  flex-direction: column;
  gap: 5px;
  cursor: pointer;
}

.hamburger span {
  width: 25px;
  height: 3px;
  background-color: var(--color-heading);
  border-radius: 2px;
  transition: 0.3s;
}

/* --- CONTENIDO CENTRAL --- */
.main-content {
  flex: 1;
  width: 100%;
  padding: 40px 0;
}

/* Contenedor que limita el ancho del formulario en PC para que no se estire feo */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* --- FOOTER (ANCHO COMPLETO) --- */
.footer {
  background-color: var(--color-background-mute);
  border-top: 1px solid var(--color-border);
  width: 100%;
  padding: 25px 0;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
  color: var(--color-text);
  font-size: 14px;
}

.footer-sub {
  display: block;
  margin-top: 5px;
  opacity: 0.6;
  font-size: 12px;
}

/* --- MEDIA QUERIES (RESPONSIVIDAD TOTAL) --- */
@media (max-width: 768px) {
  .hamburger {
    display: flex;
  }

  /* Menú colapsable en móvil */
  .nav-links {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    background-color: var(--color-background-soft);
    flex-direction: column;
    padding: 20px;
    gap: 10px;
    border-bottom: 1px solid var(--color-border);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3);
  }

  .nav-links a {
    width: 100%;
    text-align: center;
  }

  /* Si el checkbox está marcado, mostramos el menú en móvil */
  .menu-toggle:checked ~ .nav-links {
    display: flex;
  }
}
</style>
