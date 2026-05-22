<template>
  <div class="solicitud-card">
    <div class="card-header">
      <span class="fecha">{{ formatearFecha(solicitud.fecha) }}</span>
      <span :class="['badge', estadoClase(solicitud.estado)]">{{ solicitud.estado }}</span>
    </div>
    <div class="card-body">
      <h3 class="tipo-asistencia">{{ formatTipoAsistencia(solicitud.tipo) }}</h3>
      <div class="detalles">
        <p><i class="icon">🕒</i> <strong>Hora:</strong> {{ formatearHora(solicitud.horaInicio) }} ({{ solicitud.duracionHoras }} horas)</p>
        <p><i class="icon">📝</i> <strong>Descripción:</strong> {{ solicitud.descripcion || 'Sin descripción' }}</p>
      </div>
    </div>
    <div class="card-footer" v-if="mostrarAcciones">
      <button v-if="esFutura" class="btn-cancelar" @click="$emit('cancelar', solicitud.id)">Cancelar Cita</button>
      <button v-if="esHistorial" class="btn-calificar" @click="$emit('calificar', solicitud.id)">Calificar Servicio</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  solicitud: {
    type: Object,
    required: true
  },
  esFutura: {
    type: Boolean,
    default: false
  },
  esHistorial: {
    type: Boolean,
    default: false
  }
})

defineEmits(['cancelar', 'calificar'])

const mostrarAcciones = computed(() => props.esFutura || props.esHistorial)

const estadoClase = (estado) => {
  if (!estado) return 'badge-secondary'
  const est = estado.toUpperCase()
  if (est === 'PENDIENTE') return 'badge-warning'
  if (est === 'ACEPTADA') return 'badge-success'
  if (est === 'FINALIZADA') return 'badge-info'
  if (est === 'CANCELADA') return 'badge-danger'
  return 'badge-secondary'
}

const formatTipoAsistencia = (tipo) => {
  if (!tipo) return 'Asistencia General'
  return tipo.replace(/_/g, ' ')
}

const formatearFecha = (fechaArray) => {
  if (!fechaArray) return 'Fecha por definir'
  // Si viene como array [2026, 5, 21] o string
  if (Array.isArray(fechaArray)) {
    const [year, month, day] = fechaArray
    const date = new Date(year, month - 1, day)
    return date.toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })
  }
  return new Date(fechaArray).toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })
}

const formatearHora = (horaArray) => {
  if (!horaArray) return '--:--'
  if (Array.isArray(horaArray)) {
    let [hour, minute] = horaArray
    minute = minute !== undefined ? minute : 0
    return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
  }
  if (typeof horaArray === 'string') {
    return horaArray.substring(0, 5) // "14:00:00" -> "14:00"
  }
  return horaArray
}
</script>

<style scoped>
.solicitud-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  color: #fff;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.solicitud-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 10px;
}

.fecha {
  font-weight: 600;
  font-size: 1.1rem;
  color: #e0e0e0;
}

.badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: bold;
  letter-spacing: 0.5px;
}

.badge-warning { background-color: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid #fbbf24; }
.badge-success { background-color: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid #34d399; }
.badge-info { background-color: rgba(59, 130, 246, 0.2); color: #60a5fa; border: 1px solid #60a5fa; }
.badge-danger { background-color: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid #f87171; }
.badge-secondary { background-color: rgba(156, 163, 175, 0.2); color: #9ca3af; border: 1px solid #9ca3af; }

.tipo-asistencia {
  margin: 0 0 15px 0;
  font-size: 1.3rem;
  color: #fff;
  text-transform: capitalize;
}

.detalles p {
  margin: 5px 0;
  color: #ccc;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-footer {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
}

button {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancelar {
  background-color: rgba(239, 68, 68, 0.1);
  color: #f87171;
  border: 1px solid #f87171;
}

.btn-cancelar:hover {
  background-color: rgba(239, 68, 68, 0.2);
}

.btn-calificar {
  background-color: rgba(59, 130, 246, 0.1);
  color: #60a5fa;
  border: 1px solid #60a5fa;
}

.btn-calificar:hover {
  background-color: rgba(59, 130, 246, 0.2);
}
</style>
