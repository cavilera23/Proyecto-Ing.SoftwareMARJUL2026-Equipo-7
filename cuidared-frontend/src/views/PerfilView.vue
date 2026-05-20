<template>
  <div class="perfil-view">
    <h1>Mi Perfil y Calificaciones</h1>
    
    <div v-if="usuario">
      <p><strong>Nombre:</strong> {{ usuario.nombre }}</p>
      <p><strong>Correo:</strong> {{ usuario.correo }}</p>
      <p><strong>Teléfono:</strong> {{ usuario.telefono }}</p>
      <p><strong>Calificación Promedio:</strong> {{ usuario.calificacionPromedio }} / 5.0</p>
      <p><strong>Tipo de Usuario:</strong> {{ usuario.tipoUsuario }}</p>
    </div>
    <div v-else>
      <p>Cargando perfil...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const usuario = ref(null)

const cargarPerfil = async () => {
  try {
    // Ejemplo de ID quemado
    const userId = "usuario-123"
    const response = await fetch(`http://localhost:8080/api/v1/perfiles/${userId}`)
    if (response.ok) {
      usuario.value = await response.json()
    } else {
      // Simular un perfil por defecto si no hay base de datos con ese ID
      usuario.value = {
        nombre: "Juan Pérez",
        correo: "juan@cuidared.com",
        telefono: "0414-1234567",
        calificacionPromedio: 4.8,
        tipoUsuario: "PADRE"
      }
    }
  } catch (e) {
    console.error("Error cargando perfil", e)
  }
}

onMounted(() => {
  cargarPerfil()
})
</script>

<style scoped>
.perfil-view {
  padding: 20px;
}
</style>
