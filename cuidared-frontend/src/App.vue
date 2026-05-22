<script setup>
import { RouterLink, RouterView } from "vue-router";
</script>

<template>
  <div class="app-layout">
    <header class="navbar">
      <div class="nav-container">
        <div class="logo-area">
          <span class="logo-icono">🏡</span>
          <span class="logo-texto"
            >Cuida<span class="green-text">Red</span></span
          >
        </div>

        <input type="checkbox" id="menu-toggle" class="menu-toggle" />
        <label for="menu-toggle" class="hamburger">
          <span></span>
          <span></span>
          <span></span>
        </label>

        <nav class="nav-links">
          <RouterLink to="/agenda">Agenda</RouterLink>
          <RouterLink to="/solicitudes">Solicitudes</RouterLink>
          <RouterLink to="/perfil">Mi Perfil</RouterLink>
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
