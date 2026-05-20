<template>
  <header>
    <div class="wrapper">
      <nav>
        <RouterLink to="/agenda">Agenda</RouterLink>
        <RouterLink to="/solicitudes">Solicitudes</RouterLink>
        <RouterLink to="/perfil">Perfil</RouterLink>
      </nav>
    </div>
  </header>

  <main>
    <RouterView />
  </main>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<style scoped>
header {
  line-height: 1.5;
  background-color: #f8f9fa;
  padding: 1rem;
  border-bottom: 1px solid #dee2e6;
}

nav {
  width: 100%;
  font-size: 16px;
  text-align: center;
}

nav a.router-link-exact-active {
  color: var(--color-text);
  font-weight: bold;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  text-decoration: none;
  color: #007bff;
}

main {
  padding: 20px;
}
</style>
