<template>
  <div class="agenda-view">
    <h1>Gestión de Agenda</h1>
    <p>Consulta y crea tus horarios aquí.</p>
    
    <div class="agenda-form">
      <label>Fecha Inicio:</label>
      <input type="datetime-local" v-model="fechaInicio" />
      
      <label>Fecha Fin:</label>
      <input type="datetime-local" v-model="fechaFin" />

      <button @click="verificarDisponibilidad">Verificar Disponibilidad</button>
    </div>

    <div v-if="mensaje" class="mensaje">
      {{ mensaje }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { verificarDisponibilidadApi } from '../services/agendaService'

const fechaInicio = ref('')
const fechaFin = ref('')
const mensaje = ref('')

const verificarDisponibilidad = async () => {
  if (!fechaInicio.value || !fechaFin.value) {
    mensaje.value = "Por favor seleccione ambas fechas."
    return
  }

  try {
    // Ejemplo con un ID de cuidador quemado para el esqueleto
    const cuidadorId = "cuidador-123" 
    const disponible = await verificarDisponibilidadApi(cuidadorId, {
      fechaInicio: new Date(fechaInicio.value).toISOString(),
      fechaFin: new Date(fechaFin.value).toISOString()
    })
    
    mensaje.value = disponible ? "¡Horario disponible!" : "El horario se solapa con una solicitud activa."
  } catch (error) {
    mensaje.value = "Error al verificar disponibilidad."
  }
}
</script>

<style scoped>
.agenda-view {
  padding: 20px;
}
.agenda-form {
  display: flex;
  flex-direction: column;
  max-width: 300px;
  gap: 10px;
}
.mensaje {
  margin-top: 15px;
  font-weight: bold;
}
</style>
