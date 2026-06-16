<script setup>
import { RouterLink } from "vue-router";
import { auth } from "@/stores/auth";

const accesos = [
  { to: "/agenda", icono: "📅", titulo: "Mi Agenda", desc: "Declara y gestiona tus bloques de disponibilidad." },
  { to: "/solicitudes", icono: "📨", titulo: "Solicitudes", desc: "Revisa y responde las solicitudes de cuidado." },
  { to: "/mis-calificaciones", icono: "⭐", titulo: "Mis Calificaciones", desc: "Consulta las valoraciones que has recibido." },
  { to: "/notificaciones", icono: "🔔", titulo: "Notificaciones", desc: "Recordatorios y avisos de tus servicios." },
  { to: "/perfil", icono: "👤", titulo: "Mi Perfil", desc: "Actualiza tus datos, tarifa y habilidades." },
];
</script>

<template>
  <div class="dashboard">
    <header class="dash-header">
      <h1>Hola, {{ auth.usuario?.nombre || "Cuidador" }} 👋</h1>
      <p>Panel del <strong>cuidador</strong>. Gestiona tu disponibilidad y tus servicios.</p>
    </header>

    <div class="dash-grid">
      <RouterLink v-for="a in accesos" :key="a.to" :to="a.to" class="dash-card">
        <span class="dash-icono">{{ a.icono }}</span>
        <span class="dash-titulo">{{ a.titulo }}</span>
        <span class="dash-desc">{{ a.desc }}</span>
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

.dash-header h1 {
  color: var(--color-heading);
}

.dash-header p {
  color: var(--color-text);
  margin-top: 6px;
}

.dash-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.dash-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 22px;
  transition: transform 0.15s ease, border-color 0.15s ease;
}

.dash-card:hover {
  transform: translateY(-3px);
  border-color: var(--color-primary);
}

.dash-icono {
  font-size: 30px;
}

.dash-titulo {
  color: var(--color-heading);
  font-weight: 600;
  font-size: 17px;
}

.dash-desc {
  color: var(--color-text);
  font-size: 13px;
  line-height: 1.4;
}
</style>
