<script setup>
import { RouterLink } from "vue-router";
import { auth } from "@/stores/auth";

const accesos = [
  { to: "/buscar-cuidadores", icono: "🔍", titulo: "Buscar Cuidadores", desc: "Encuentra cuidadores disponibles para tu necesidad." },
  { to: "/solicitudes", icono: "📨", titulo: "Mis Solicitudes", desc: "Crea y da seguimiento a tus solicitudes de cuidado." },
  { to: "/mis-calificaciones", icono: "⭐", titulo: "Calificaciones", desc: "Califica los servicios que has recibido." },
  { to: "/notificaciones", icono: "🔔", titulo: "Notificaciones", desc: "Recordatorios y avisos de tus solicitudes." },
  { to: "/perfil", icono: "👤", titulo: "Mi Perfil", desc: "Actualiza tus datos de contacto." },
];
</script>

<template>
  <div class="dashboard">
    <header class="dash-header">
      <span class="dash-rol">Padre / Tutor</span>
      <h1>Hola, {{ auth.usuario?.nombre || "Usuario" }} 👋</h1>
      <p>Busca cuidadores verificados y gestiona tus solicitudes de cuidado.</p>
    </header>

    <div class="dash-grid">
      <RouterLink v-for="a in accesos" :key="a.to" :to="a.to" class="dash-card">
        <span class="dash-icono">{{ a.icono }}</span>
        <span class="dash-titulo">{{ a.titulo }}</span>
        <span class="dash-desc">{{ a.desc }}</span>
        <span class="dash-arrow">→</span>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.dash-header {
  background: var(--grad-hero);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 38px 40px;
  animation: fadeUp 0.4s ease both;
}

.dash-rol {
  display: inline-block;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--color-border);
  color: var(--color-primary-hover);
  font-weight: 600;
  font-size: 12px;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  margin-bottom: 14px;
}

.dash-header h1 {
  color: var(--color-heading);
  font-size: 2.3rem;
}

.dash-header p {
  color: var(--color-text);
  margin-top: 8px;
  font-size: 1.05rem;
}

.dash-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.dash-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 26px;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: transform var(--t), box-shadow var(--t), border-color var(--t);
}

.dash-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--grad-primary);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform var(--t);
}

.dash-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-soft);
}

.dash-card:hover::before {
  transform: scaleX(1);
}

.dash-icono {
  display: inline-grid;
  place-items: center;
  width: 56px;
  height: 56px;
  font-size: 27px;
  background: var(--color-primary-tint);
  border-radius: 16px;
  margin-bottom: 4px;
}

.dash-titulo {
  color: var(--color-heading);
  font-weight: 700;
  font-size: 17px;
  font-family: var(--font-display);
}

.dash-desc {
  color: var(--color-text);
  font-size: 13.5px;
  line-height: 1.5;
}

.dash-arrow {
  position: absolute;
  top: 26px;
  right: 24px;
  color: var(--color-primary);
  font-size: 20px;
  opacity: 0;
  transform: translateX(-6px);
  transition: opacity var(--t), transform var(--t);
}

.dash-card:hover .dash-arrow {
  opacity: 1;
  transform: translateX(0);
}

@media (max-width: 640px) {
  .dash-header {
    padding: 28px 22px;
  }
  .dash-header h1 {
    font-size: 1.8rem;
  }
}
</style>
