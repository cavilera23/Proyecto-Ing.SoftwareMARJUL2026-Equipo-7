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

// Inicial del usuario para el avatar del navbar.
const inicialUsuario = computed(() =>
  (auth.usuario?.nombre || "?").trim().charAt(0).toUpperCase(),
);

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

          <span class="usuario-chip">
            <span class="usuario-avatar">{{ inicialUsuario }}</span>
            <span class="usuario-nombre">{{ auth.usuario?.nombre }}</span>
          </span>
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
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-background);
}

/* --- NAVBAR --- */
.navbar {
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(12px);
  -webkit-backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--color-border);
  width: 100%;
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 12px 0;
}

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
  font-size: 21px;
  font-weight: 800;
  font-family: var(--font-display);
  color: var(--color-heading);
  letter-spacing: -0.03em;
  cursor: pointer;
}

.logo-icono {
  display: inline-grid;
  place-items: center;
  width: 38px;
  height: 38px;
  font-size: 19px;
  background: var(--grad-primary);
  border-radius: 11px;
  box-shadow: var(--shadow-primary);
}

.green-text {
  color: var(--color-primary);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-links a {
  color: var(--color-text);
  font-weight: 500;
  font-size: 14px;
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  transition: color var(--t-fast), background-color var(--t-fast);
}

.nav-links a:hover {
  color: var(--color-heading);
  background-color: var(--color-background-mute);
}

.router-link-active {
  background-color: var(--color-primary-tint) !important;
  color: var(--color-primary-hover) !important;
  font-weight: 600 !important;
}

.logo-area.router-link-active {
  background-color: transparent !important;
}

/* Chip de usuario con avatar */
.usuario-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-heading);
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px 4px 4px;
  margin-left: 8px;
  background: var(--color-background-mute);
  border-radius: var(--radius-full);
}

.usuario-avatar {
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--grad-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.btn-logout {
  background-color: transparent;
  color: var(--color-danger);
  border: 1px solid var(--color-danger-soft);
  border-radius: var(--radius-sm);
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--t-fast), border-color var(--t-fast);
}

.btn-logout:hover {
  background-color: var(--color-danger-soft);
  border-color: var(--color-danger);
}

/* Botones del header público (landing) */
.auth-acciones {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-auth-secundario {
  color: var(--color-heading);
  font-weight: 600;
  font-size: 14px;
  padding: 9px 16px;
  border-radius: var(--radius-sm);
  transition: background var(--t-fast);
}

.btn-auth-secundario:hover {
  background-color: var(--color-background-mute);
}

.btn-auth-primario {
  background: var(--grad-primary);
  color: white;
  font-weight: 600;
  font-size: 14px;
  padding: 9px 20px;
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-primary);
  transition: transform var(--t-fast), box-shadow var(--t-fast);
}

.btn-auth-primario:hover {
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 12px 28px rgba(16, 185, 129, 0.34);
}

/* --- MENÚ HAMBURGUESA --- */
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

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* --- FOOTER --- */
.footer {
  background-color: var(--color-background-soft);
  border-top: 1px solid var(--color-border);
  width: 100%;
  padding: 28px 0;
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
  opacity: 0.7;
  font-size: 12px;
}

/* --- RESPONSIVE --- */
@media (max-width: 860px) {
  .hamburger {
    display: flex;
  }

  .nav-links {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    background-color: var(--color-background-soft);
    flex-direction: column;
    align-items: stretch;
    padding: 18px;
    gap: 6px;
    border-bottom: 1px solid var(--color-border);
    box-shadow: var(--shadow-lg);
  }

  .nav-links a {
    width: 100%;
    text-align: center;
  }

  .usuario-chip {
    justify-content: center;
    margin: 8px 0 0;
  }

  .btn-logout {
    width: 100%;
  }

  .menu-toggle:checked ~ .nav-links {
    display: flex;
  }
}
</style>
