<!-- src/views/SolicitudesView.vue -->
<template>
  <div class="solicitudes-view">
    <div class="header-section">
      <h1>Gestión de Intercambio</h1>
      <p>Publica una solicitud de cuidado y encuentra al cuidador ideal.</p>
    </div>

    <div class="grid-layout">
      <!-- Formulario de publicación -->
      <div class="form-card">
        <h2>Publicar nueva solicitud</h2>
        <form @submit.prevent="publicarSolicitud">
          <div class="campo">
            <label>Tipo de cuidador:</label>
            <select v-model="form.tipo" required>
              <option value="" disabled>Seleccione...</option>
              <option value="NINO">Niños</option>
              <option value="ADULTO_MAYOR">Adultos mayores</option>
              <option value="MASCOTA">Mascotas</option>
            </select>
          </div>

          <div class="campo">
            <label>Fecha:</label>
            <input type="date" v-model="form.fecha" :min="fechaMinima" required />
          </div>

          <div class="campo">
            <label>Hora de inicio:</label>
            <input type="time" v-model="form.horaInicio" step="1800" required />
            <small>(Se redondeará a 30 minutos)</small>
          </div>

          <div class="campo">
            <label>Duración (horas):</label>
            <input type="number" v-model="form.duracionHoras" min="1" max="12" step="0.5" required />
            <small>Mínimo 1 hora, máximo 12 horas</small>
          </div>

          <div class="campo">
            <label>Descripción adicional (opcional):</label>
            <textarea v-model="form.descripcion" rows="3" placeholder="Ej: Mi hijo tiene 5 años, necesita atención especial..."></textarea>
          </div>

          <button type="submit" :disabled="cargando">
            {{ cargando ? "Publicando..." : "Publicar solicitud" }}
          </button>
        </form>
      </div>

      <!-- Lista de solicitudes -->
      <div class="list-card">
        <h2>Mis solicitudes activas</h2>
        <div v-if="solicitudesActivas.length === 0" class="vacio">
          <p>📭 Aún no tienes solicitudes activas</p>
          <small>Completa el formulario para publicar tu primera solicitud</small>
        </div>
        <ul v-else class="solicitudes-lista">
          <li v-for="sol in solicitudesActivas" :key="sol.id" class="solicitud-item">
            <div class="solicitud-header">
              <span class="tipo">{{ formatearTipo(sol.tipo) }}</span>
              <span :class="['estado', getEstadoClass(sol.estado)]">{{ sol.estado }}</span>
            </div>
            <div class="solicitud-detalle">
              <span>📅 {{ sol.fecha }} {{ sol.horaInicio }} ({{ sol.duracionHoras }}h)</span>
              <p v-if="sol.descripcion" class="descripcion">{{ sol.descripcion }}</p>
            </div>
          </li>
        </ul>
      </div>
    </div>

    <!-- Mensajes toast -->
    <div v-if="mensaje" :class="['toast', tipoMensaje === 'error' ? 'toast-error' : 'toast-exito']">
      {{ mensaje }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listarSolicitudesApi, crearSolicitudApi } from '../services/solicitudService'
import { showSuccessAlert, showErrorAlert, showWarningAlert } from '@/components/modals/alerts'

// Estado
const form = ref({
  tipo: '',
  fecha: '',
  horaInicio: '',
  duracionHoras: 1,
  descripcion: ''
})

const solicitudes = ref([])
const cargando = ref(false)
const mensaje = ref('')
const tipoMensaje = ref('')

// ID del padre (temporal - en producción vendría de autenticación)
const PADRE_ID = "padre-123" // Cambia esto por el ID real del usuario logueado

const fechaMinima = computed(() => {
  const hoy = new Date()
  return hoy.toISOString().split('T')[0]
})

const solicitudesActivas = computed(() => {
  return solicitudes.value.filter(s => 
    s.estado === 'PENDIENTE' || s.estado === 'ACEPTADA'
  )
})

// Métodos
const formatearTipo = (tipo) => {
  const tipos = {
    'NINO': '👶 Niños',
    'ADULTO_MAYOR': '👴 Adultos mayores',
    'MASCOTA': '🐾 Mascotas'
  }
  return tipos[tipo] || tipo
}

const getEstadoClass = (estado) => {
  const clases = {
    'PENDIENTE': 'estado-pendiente',
    'ACEPTADA': 'estado-aceptada',
    'FINALIZADA': 'estado-finalizada',
    'CANCELADA': 'estado-cancelada'
  }
  return clases[estado] || ''
}

const cargarSolicitudes = async () => {
  try {
    const todas = await listarSolicitudesApi()
    // Filtrar solo las del padre actual
    solicitudes.value = todas.filter(s => s.padreId === PADRE_ID)
  } catch (error) {
    console.error("Error cargando solicitudes", error)
  }
}

const redondearHora = (horaStr) => {
  if (!horaStr) return horaStr
  const [h, m] = horaStr.split(':').map(Number)
  let minutosRedondeados = Math.round(m / 30) * 30
  let horaFinal = h
  if (minutosRedondeados === 60) {
    horaFinal = h + 1
    minutosRedondeados = 0
  }
  return `${String(horaFinal).padStart(2, '0')}:${String(minutosRedondeados).padStart(2, '0')}`
}

const publicarSolicitud = async () => {
  mensaje.value = ''
  tipoMensaje.value = ''

  // Validaciones
  if (!form.value.tipo) {
    await showWarningAlert("Campo requerido", "Debes seleccionar un tipo de cuidador")
    return
  }

  if (!form.value.fecha) {
    await showWarningAlert("Campo requerido", "Debes seleccionar una fecha")
    return
  }

  if (!form.value.horaInicio) {
    await showWarningAlert("Campo requerido", "Debes seleccionar una hora de inicio")
    return
  }

  if (form.value.duracionHoras < 1 || form.value.duracionHoras > 12) {
    await showWarningAlert("Duración inválida", "La duración debe ser entre 1 y 12 horas")
    return
  }

  cargando.value = true

  // Redondear hora
  const horaRedondeada = redondearHora(form.value.horaInicio)
  
  // Calcular hora fin
  const [h, m] = horaRedondeada.split(':').map(Number)
  const minutosTotales = h * 60 + m + (form.value.duracionHoras * 60)
  const horaFin = `${String(Math.floor(minutosTotales / 60)).padStart(2, '0')}:${String(minutosTotales % 60).padStart(2, '0')}`

  const payload = {
    padreId: PADRE_ID,
    tipo: form.value.tipo,
    descripcion: form.value.descripcion,
    fecha: form.value.fecha,
    horaInicio: horaRedondeada,
    duracionHoras: form.value.duracionHoras,
    horario: {
      fechaInicio: `${form.value.fecha}T${horaRedondeada}:00`,
      fechaFin: `${form.value.fecha}T${horaFin}:00`
    }
  }

  console.log("Enviando solicitud:", payload)

  try {
    const respuesta = await crearSolicitudApi(payload)
    
    // Mostrar éxito
    await showSuccessAlert("Solicitud publicada", "Tu solicitud ha sido publicada exitosamente")
    
    // Resetear formulario
    form.value = {
      tipo: '',
      fecha: '',
      horaInicio: '',
      duracionHoras: 1,
      descripcion: ''
    }
    
    // Recargar lista
    await cargarSolicitudes()
    
    mensaje.value = "✅ Solicitud publicada exitosamente"
    tipoMensaje.value = "exito"
    setTimeout(() => {
      mensaje.value = ''
    }, 3000)
    
  } catch (error) {
    console.error("Error al publicar:", error)
    let errorMsg = error.message || "Error al publicar la solicitud"
    
    // Manejar errores específicos
    if (errorMsg.includes("Límite de 3")) {
      errorMsg = "Límite de 3 solicitudes activas alcanzado"
    } else if (errorMsg.includes("Complete su perfil")) {
      errorMsg = "Complete su perfil primero antes de publicar solicitudes"
    } else if (errorMsg.includes("dirección") || errorMsg.includes("Caracas")) {
      errorMsg = "Servicio solo disponible en Caracas"
    } else if (errorMsg.includes("duración")) {
      errorMsg = "La duración debe ser entre 1 y 12 horas"
    } else if (errorMsg.includes("fecha no puede ser anterior")) {
      errorMsg = "La fecha no puede ser anterior a hoy"
    }
    
    await showErrorAlert("No se pudo publicar", errorMsg)
    
    mensaje.value = `❌ ${errorMsg}`
    tipoMensaje.value = "error"
    setTimeout(() => {
      mensaje.value = ''
    }, 3000)
  } finally {
    cargando.value = false
  }
}

// Cargar solicitudes al montar
onMounted(() => {
  cargarSolicitudes()
})
</script>

<style scoped>
.solicitudes-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  margin-bottom: 30px;
}

.header-section h1 {
  color: var(--color-heading);
  margin-bottom: 8px;
}

.header-section p {
  color: var(--color-text);
}

.grid-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.form-card, .list-card {
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 24px;
}

.form-card h2, .list-card h2 {
  color: var(--color-heading);
  margin-bottom: 20px;
  font-size: 1.3rem;
}

.campo {
  margin-bottom: 18px;
}

.campo label {
  display: block;
  color: var(--color-text);
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
}

.campo input, .campo select, .campo textarea {
  width: 100%;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-heading);
}

.campo small {
  display: block;
  color: var(--color-text);
  font-size: 11px;
  margin-top: 4px;
}

button {
  width: 100%;
  padding: 12px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

button:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.vacio {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text);
}

.solicitudes-lista {
  list-style: none;
  padding: 0;
}

.solicitud-item {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.solicitud-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.tipo {
  font-weight: 600;
  color: var(--color-heading);
}

.estado {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 20px;
}

.estado-pendiente {
  background-color: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.estado-aceptada {
  background-color: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.solicitud-detalle {
  font-size: 13px;
  color: var(--color-text);
}

.descripcion {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.8;
}

.toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

.toast-exito {
  background-color: #10b981;
  color: white;
}

.toast-error {
  background-color: #ef4444;
  color: white;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .grid-layout {
    grid-template-columns: 1fr;
  }
}
</style>
