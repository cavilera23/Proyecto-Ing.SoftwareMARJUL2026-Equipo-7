<template>
  <div class="solicitudes-view">
    <h1>Gestión de Intercambio</h1>
    <p>Publica y sigue tus solicitudes de cuidado.</p>

    <div class="nueva-solicitud">
      <h2>Nueva Solicitud</h2>
      <select v-model="tipo">
        <option value="NINO">Niño</option>
        <option value="ADULTO_MAYOR">Adulto Mayor</option>
        <option value="MASCOTA">Mascota</option>
      </select>
      <input type="text" v-model="descripcion" placeholder="Descripción breve" />
      <button @click="crearSolicitud">Publicar Solicitud</button>
      <div v-if="error" class="error">{{ error }}</div>
    </div>

    <div class="lista-solicitudes">
      <h2>Mis Solicitudes</h2>
      <ul>
        <li v-for="solicitud in solicitudes" :key="solicitud.id">
          {{ solicitud.tipo }} - {{ solicitud.estado }} - {{ solicitud.descripcion }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { crearSolicitudApi, listarSolicitudesApi } from '../services/solicitudService'

const tipo = ref('NINO')
const descripcion = ref('')
const error = ref('')
const solicitudes = ref([])

const cargarSolicitudes = async () => {
  try {
    solicitudes.value = await listarSolicitudesApi()
  } catch (e) {
    console.error("Error cargando solicitudes", e)
  }
}

const crearSolicitud = async () => {
  error.value = ''
  try {
    // Padre quemado para el esqueleto
    const padreId = "padre-123"
    await crearSolicitudApi({
      padreId,
      tipo: tipo.value,
      descripcion: descripcion.value,
      horario: {
        fechaInicio: new Date().toISOString(),
        fechaFin: new Date(Date.now() + 3600000).toISOString()
      }
    })
    descripcion.value = ''
    await cargarSolicitudes()
  } catch (e) {
    error.value = e.message || "Error al crear la solicitud (máximo 3 activas)."
  }
}

onMounted(() => {
  cargarSolicitudes()
})
</script>

<style scoped>
.solicitudes-view {
  padding: 20px;
}
.nueva-solicitud {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 300px;
  margin-bottom: 20px;
}
.error {
  color: red;
}
</style>
