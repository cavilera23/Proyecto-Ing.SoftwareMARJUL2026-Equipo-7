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
      <template v-else-if="esHistorial">
        <button v-if="puedeCalificar" class="btn-calificar" @click="$emit('calificar', solicitud.id)">Calificar Servicio</button>
        <div v-else-if="yaCalificada" class="ya-calificado">
          <span class="ya-calificado-estrellas">{{ estrellas(solicitud.calificacion.puntuacion) }}</span>
          <span class="ya-calificado-texto">Ya calificaste este servicio</span>
          <button class="btn-editar-calificacion" @click="$emit('editar-calificacion', solicitud)">✏️ Editar</button>
        </div>
      </template>
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

defineEmits(['cancelar', 'calificar', 'editar-calificacion'])

const esFinalizada = computed(
  () => (props.solicitud.estado || '').toUpperCase() === 'FINALIZADA'
)

// El servicio ya tiene una calificación asociada (anotada por la vista padre).
const yaCalificada = computed(() => !!props.solicitud.calificacion)

// Solo se puede calificar un servicio finalizado que aún no se haya calificado.
const puedeCalificar = computed(() => esFinalizada.value && !yaCalificada.value)

const mostrarAcciones = computed(
  () => props.esFutura || (props.esHistorial && esFinalizada.value)
)

const estrellas = (puntuacion) => '⭐'.repeat(puntuacion || 0)

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
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 22px;
  margin-bottom: 0;
  color: var(--color-text);
  transition: transform var(--t), box-shadow var(--t), border-color var(--t);
  box-shadow: var(--shadow-sm);
}

.solicitud-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-soft);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 12px;
}

.fecha {
  font-weight: 700;
  font-size: 1.05rem;
  font-family: var(--font-display);
  color: var(--color-heading);
}

.badge {
  padding: 5px 12px;
  border-radius: var(--radius-full);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.3px;
}

.badge-warning { background-color: var(--color-warning-soft); color: var(--color-warning); }
.badge-success { background-color: var(--color-success-soft); color: var(--color-success); }
.badge-info { background-color: var(--color-info-soft); color: var(--color-info); }
.badge-danger { background-color: var(--color-danger-soft); color: var(--color-danger); }
.badge-secondary { background-color: var(--color-background-mute); color: var(--color-muted); }

.tipo-asistencia {
  margin: 0 0 14px 0;
  font-size: 1.2rem;
  color: var(--color-heading);
  text-transform: capitalize;
}

.detalles p {
  margin: 7px 0;
  color: var(--color-text);
  font-size: 0.92rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detalles strong {
  color: var(--color-heading);
}

.card-footer {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
}

button {
  padding: 9px 18px;
  border: none;
  border-radius: var(--radius-sm);
  font-weight: 600;
  cursor: pointer;
  transition: background-color var(--t-fast), transform var(--t-fast);
}

.btn-cancelar {
  background-color: var(--color-danger-soft);
  color: var(--color-danger);
}

.btn-cancelar:hover {
  background-color: #fca5a5;
  transform: translateY(-1px);
}

.btn-calificar {
  background: var(--grad-primary);
  color: #fff;
  box-shadow: var(--shadow-primary);
}

.btn-calificar:hover {
  transform: translateY(-1px);
}

.ya-calificado {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ya-calificado-estrellas {
  font-size: 0.95rem;
  letter-spacing: 1px;
}

.ya-calificado-texto {
  color: var(--color-success);
  font-size: 0.82rem;
  font-weight: 600;
  background: var(--color-success-soft);
  padding: 4px 12px;
  border-radius: var(--radius-full);
}

.btn-editar-calificacion {
  background: var(--color-info-soft, #e0f2fe);
  color: var(--color-info, #0284c7);
  padding: 5px 14px;
  border-radius: var(--radius-sm);
  font-size: 0.82rem;
  font-weight: 600;
  border: 1px solid transparent;
  cursor: pointer;
  transition: background-color var(--t-fast), transform var(--t-fast), border-color var(--t-fast);
}

.btn-editar-calificacion:hover {
  background: var(--color-info, #0284c7);
  color: #fff;
  transform: translateY(-1px);
}
</style>
