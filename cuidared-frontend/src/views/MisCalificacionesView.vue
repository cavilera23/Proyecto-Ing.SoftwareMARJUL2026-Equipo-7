<template>
  <div class="mis-calificaciones-container">
    <!-- ====================== VISTA CUIDADOR ====================== -->
    <template v-if="esCuidador">
      <header class="dashboard-header">
        <h1>Mis Calificaciones</h1>
        <p>Estas son las valoraciones que tus clientes te han dado.</p>
      </header>

      <div v-if="cargando" class="loading-state">
        <div class="spinner"></div>
        <p>Cargando información...</p>
      </div>

      <div v-else class="content-area">
        <div class="resumen-cuidador">
          <div class="promedio-grande">
            <span class="estrella-grande">⭐</span>
            <span class="promedio-num">{{ promedioCuidador }}</span>
          </div>
          <p class="promedio-label">
            Promedio sobre {{ calificacionesRecibidas.length }}
            {{ calificacionesRecibidas.length === 1 ? 'valoración' : 'valoraciones' }}
          </p>
        </div>

        <div v-if="calificacionesRecibidas.length === 0" class="empty-state">
          <p>Aún no has recibido calificaciones.</p>
        </div>
        <div v-else class="cards-grid">
          <div v-for="cal in calificacionesRecibidas" :key="cal.id" class="calif-card">
            <div class="calif-header">
              <span class="estrellas">{{ renderEstrellas(cal.puntuacion) }}</span>
              <span class="calif-fecha">{{ formatearFechaCal(cal.fecha) }}</span>
            </div>
            <p class="calif-comentario">
              {{ cal.comentario || 'Sin comentario.' }}
            </p>
          </div>
        </div>
      </div>
    </template>

    <!-- ====================== VISTA PADRE ====================== -->
    <template v-else>
      <header class="dashboard-header">
        <h1>Mis Solicitudes y Calificaciones</h1>
        <p>Administra tus próximas citas y revisa el historial de atenciones pasadas.</p>
      </header>

      <div class="tabs-container">
        <div class="tabs">
          <button
            :class="['tab-btn', { active: tabActiva === 'futuras' }]"
            @click="tabActiva = 'futuras'"
          >
            📅 Próximas Citas
          </button>
          <button
            :class="['tab-btn', { active: tabActiva === 'historial' }]"
            @click="tabActiva = 'historial'"
          >
            🕰️ Historial de Servicios
          </button>
        </div>
      </div>

      <div v-if="cargando" class="loading-state">
        <div class="spinner"></div>
        <p>Cargando información...</p>
      </div>

      <div v-else class="content-area">
        <!-- Sección Próximas Citas -->
        <div v-if="tabActiva === 'futuras'" class="tab-content">
          <div v-if="solicitudesFuturas.length === 0" class="empty-state">
            <p>No tienes citas programadas para el futuro.</p>
          </div>
          <div class="cards-grid">
            <SolicitudCard
              v-for="solicitud in solicitudesFuturas"
              :key="solicitud.id"
              :solicitud="solicitud"
              :esFutura="true"
              @cancelar="handleCancelar"
            />
          </div>
        </div>

        <!-- Sección Historial -->
        <div v-if="tabActiva === 'historial'" class="tab-content">
          <div v-if="solicitudesHistorial.length === 0" class="empty-state">
            <p>Aún no tienes atenciones pasadas en tu historial.</p>
          </div>
          <div class="cards-grid">
            <SolicitudCard
              v-for="solicitud in solicitudesHistorial"
              :key="solicitud.id"
              :solicitud="solicitud"
              :esHistorial="true"
              @calificar="handleCalificar"
              @editar-calificacion="handleEditarCalificacion"
              @eliminar-calificacion="handleEliminarCalificacion"
            />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { apiFetch } from '../services/api'
import { crearCalificacionApi, listarCalificacionesPorCuidadorApi, modificarCalificacionApi, eliminarCalificacionApi } from '../services/calificacionService'
import { cancelarSolicitudApi } from '../services/solicitudService'
import { auth } from '@/stores/auth'
import SolicitudCard from '../components/SolicitudCard.vue'

const esCuidador = computed(() => auth.tipoUsuario === 'CUIDADOR')

// --- Estado vista PADRE ---
const solicitudesFuturas = ref([])
const solicitudesHistorial = ref([])
const tabActiva = ref('futuras') // 'futuras' o 'historial'

// --- Estado vista CUIDADOR ---
const calificacionesRecibidas = ref([])

const cargando = ref(true)

const promedioCuidador = computed(() => {
  if (calificacionesRecibidas.value.length === 0) return '0.0'
  const suma = calificacionesRecibidas.value.reduce((acc, c) => acc + c.puntuacion, 0)
  return (suma / calificacionesRecibidas.value.length).toFixed(1)
})

const renderEstrellas = (puntuacion) => {
  const llenas = '⭐'.repeat(puntuacion)
  return llenas || '—'
}

const formatearFechaCal = (fecha) => {
  if (!fecha) return ''
  return new Date(fecha).toLocaleDateString('es-VE', { day: 'numeric', month: 'long', year: 'numeric' })
}

const cargarCalificacionesCuidador = async () => {
  cargando.value = true
  try {
    const data = await listarCalificacionesPorCuidadorApi(auth.usuario?.id)
    // Más recientes primero
    calificacionesRecibidas.value = (data || []).sort(
      (a, b) => new Date(b.fecha) - new Date(a.fecha)
    )
  } catch (error) {
    console.error('Error al cargar calificaciones:', error)
    Swal.fire({
      icon: 'error',
      title: 'Oops...',
      text: 'Hubo un problema al cargar tus calificaciones. ' + error.message,
      background: '#ffffff',
      color: '#5a6675'
    })
  } finally {
    cargando.value = false
  }
}

const cargarSolicitudes = async () => {
  cargando.value = true
  try {
    // Traemos las solicitudes del padre y, en paralelo, todas las calificaciones
    // para saber cuáles servicios ya fueron calificados (y mostrar la nota dejada).
    const [data, calificaciones] = await Promise.all([
      apiFetch(`/intercambio/solicitudes/padre/${auth.usuario?.id}`),
      apiFetch('/calificaciones')
    ])

    const califPorSolicitud = {}
    ;(calificaciones || []).forEach((c) => { califPorSolicitud[c.solicitudId] = c })
    const anotar = (s) => ({ ...s, calificacion: califPorSolicitud[s.id] || null })

    if (data) {
      solicitudesFuturas.value = (data.futuras || []).map(anotar)
      solicitudesHistorial.value = (data.historial || []).map(anotar)
    }
  } catch (error) {
    console.error('Error al cargar solicitudes:', error)
    Swal.fire({
      icon: 'error',
      title: 'Oops...',
      text: 'Hubo un problema al cargar tus solicitudes. ' + error.message,
      background: '#ffffff',
      color: '#5a6675'
    })
  } finally {
    cargando.value = false
  }
}

const handleCancelar = (id) => {
  Swal.fire({
    title: '¿Cancelar Cita?',
    text: "Esta acción no se puede deshacer.",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ef4444',
    cancelButtonColor: '#4b5563',
    confirmButtonText: 'Sí, cancelar',
    cancelButtonText: 'No, mantener',
    background: '#ffffff',
    color: '#5a6675'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await cancelarSolicitudApi(id)
        await Swal.fire({
          title: 'Cancelada',
          text: 'La cita ha sido cancelada exitosamente.',
          icon: 'success',
          background: '#ffffff',
          color: '#5a6675'
        })
        cargarSolicitudes()
      } catch (error) {
        Swal.fire({
          icon: 'error',
          title: 'No se pudo cancelar',
          text: error.message,
          background: '#ffffff',
          color: '#5a6675'
        })
      }
    }
  })
}

const handleCalificar = (id) => {
  Swal.fire({
    title: 'Calificar Servicio',
    html: `
      <select id="swal-puntuacion" class="swal2-select" style="display:block;margin:0 auto 1rem;width:80%">
        <option value="" disabled selected>Selecciona una calificación</option>
        <option value="5">⭐⭐⭐⭐⭐ - Excelente</option>
        <option value="4">⭐⭐⭐⭐ - Muy Bueno</option>
        <option value="3">⭐⭐⭐ - Bueno</option>
        <option value="2">⭐⭐ - Regular</option>
        <option value="1">⭐ - Malo</option>
      </select>
      <textarea id="swal-comentario" class="swal2-textarea" placeholder="Comentario (opcional)" style="width:80%"></textarea>
    `,
    showCancelButton: true,
    confirmButtonText: 'Enviar',
    cancelButtonText: 'Cancelar',
    background: '#ffffff',
    color: '#5a6675',
    customClass: { popup: 'cr-swal', title: 'cr-swal-title', confirmButton: 'cr-swal-confirm', cancelButton: 'cr-swal-cancel' },
    preConfirm: () => {
      const puntuacion = document.getElementById('swal-puntuacion').value
      if (!puntuacion) {
        Swal.showValidationMessage('Debes seleccionar una calificación')
        return false
      }
      return {
        puntuacion: parseInt(puntuacion, 10),
        comentario: document.getElementById('swal-comentario').value
      }
    }
  }).then(async (result) => {
    if (result.isConfirmed && result.value) {
      try {
        await crearCalificacionApi({
          solicitudId: id,
          puntuacion: result.value.puntuacion,
          comentario: result.value.comentario
        })
        await Swal.fire({
          title: '¡Gracias!',
          text: `Has calificado este servicio con ${result.value.puntuacion} estrellas.`,
          icon: 'success',
          background: '#ffffff',
          color: '#5a6675'
        })
        // Recargar para reflejar el estado actualizado
        cargarSolicitudes()
      } catch (error) {
        Swal.fire({
          icon: 'error',
          title: 'No se pudo calificar',
          text: error.message,
          background: '#ffffff',
          color: '#5a6675'
        })
      }
    }
  })
}

const handleEditarCalificacion = (solicitud) => {
  const cal = solicitud.calificacion
  if (!cal) return

  Swal.fire({
    title: 'Editar Calificación',
    html: `
      <select id="swal-puntuacion" class="swal2-select" style="display:block;margin:0 auto 1rem;width:80%">
        <option value="5" ${cal.puntuacion === 5 ? 'selected' : ''}>⭐⭐⭐⭐⭐ - Excelente</option>
        <option value="4" ${cal.puntuacion === 4 ? 'selected' : ''}>⭐⭐⭐⭐ - Muy Bueno</option>
        <option value="3" ${cal.puntuacion === 3 ? 'selected' : ''}>⭐⭐⭐ - Bueno</option>
        <option value="2" ${cal.puntuacion === 2 ? 'selected' : ''}>⭐⭐ - Regular</option>
        <option value="1" ${cal.puntuacion === 1 ? 'selected' : ''}>⭐ - Malo</option>
      </select>
      <textarea id="swal-comentario" class="swal2-textarea" placeholder="Comentario (opcional)" style="width:80%">${cal.comentario || ''}</textarea>
    `,
    showCancelButton: true,
    confirmButtonText: 'Guardar Cambios',
    cancelButtonText: 'Cancelar',
    background: '#ffffff',
    color: '#5a6675',
    customClass: { popup: 'cr-swal', title: 'cr-swal-title', confirmButton: 'cr-swal-confirm', cancelButton: 'cr-swal-cancel' },
    preConfirm: () => {
      const puntuacion = document.getElementById('swal-puntuacion').value
      if (!puntuacion) {
        Swal.showValidationMessage('Debes seleccionar una calificación')
        return false
      }
      return {
        puntuacion: parseInt(puntuacion, 10),
        comentario: document.getElementById('swal-comentario').value
      }
    }
  }).then(async (result) => {
    if (result.isConfirmed && result.value) {
      try {
        await modificarCalificacionApi(cal.id, {
          puntuacion: result.value.puntuacion,
          comentario: result.value.comentario
        })
        await Swal.fire({
          title: '¡Actualizada!',
          text: `Tu calificación ha sido actualizada a ${result.value.puntuacion} estrellas.`,
          icon: 'success',
          background: '#ffffff',
          color: '#5a6675'
        })
        cargarSolicitudes()
      } catch (error) {
        Swal.fire({
          icon: 'error',
          title: 'No se pudo actualizar',
          text: error.message,
          background: '#ffffff',
          color: '#5a6675'
        })
      }
    }
  })
}

const handleEliminarCalificacion = (id) => {
  Swal.fire({
    title: '¿Eliminar Calificación?',
    text: "Esta acción no se puede deshacer y tu comentario será retirado.",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ef4444',
    cancelButtonColor: '#4b5563',
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar',
    background: '#ffffff',
    color: '#5a6675'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await eliminarCalificacionApi(id)
        await Swal.fire({
          title: 'Eliminada',
          text: 'Tu calificación ha sido eliminada exitosamente.',
          icon: 'success',
          background: '#ffffff',
          color: '#5a6675'
        })
        cargarSolicitudes()
      } catch (error) {
        Swal.fire({
          icon: 'error',
          title: 'No se pudo eliminar',
          text: error.message,
          background: '#ffffff',
          color: '#5a6675'
        })
      }
    }
  })
}

onMounted(() => {
  if (esCuidador.value) {
    cargarCalificacionesCuidador()
  } else {
    cargarSolicitudes()
  }
})
</script>

<style scoped>
.mis-calificaciones-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 0;
  color: var(--color-text);
}

.dashboard-header {
  margin-bottom: 2rem;
  text-align: center;
}

.dashboard-header h1 {
  font-size: 2.4rem;
  font-weight: 800;
  background: var(--grad-heading);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.dashboard-header p {
  color: var(--color-text);
  font-size: 1.05rem;
}

/* Tabs */
.tabs-container {
  display: flex;
  justify-content: center;
  margin-bottom: 2.5rem;
}

.tabs {
  display: inline-flex;
  background: var(--color-background-mute);
  padding: 0.4rem;
  border-radius: var(--radius);
  border: 1px solid var(--color-border);
}

.tab-btn {
  background: transparent;
  border: none;
  padding: 0.7rem 1.4rem;
  border-radius: var(--radius-sm);
  color: var(--color-text);
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--t);
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-btn:hover {
  color: var(--color-heading);
}

.tab-btn.active {
  background: var(--color-surface);
  color: var(--color-primary-hover);
  box-shadow: var(--shadow-sm);
}

/* Content Area */
.content-area {
  min-height: 400px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.4rem;
}

/* Resumen del cuidador */
.resumen-cuidador {
  text-align: center;
  margin-bottom: 2.5rem;
  padding: 2rem;
  background: var(--grad-hero);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.promedio-grande {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.estrella-grande {
  font-size: 2.4rem;
}

.promedio-num {
  font-size: 3rem;
  font-weight: 800;
  font-family: var(--font-display);
  color: var(--color-star);
}

.promedio-label {
  color: var(--color-text);
  margin-top: 0.5rem;
}

/* Tarjeta de calificación recibida */
.calif-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 22px;
  box-shadow: var(--shadow-sm);
  transition: transform var(--t), box-shadow var(--t);
}

.calif-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.calif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.estrellas {
  font-size: 1.05rem;
  letter-spacing: 2px;
}

.calif-fecha {
  font-size: 0.8rem;
  color: var(--color-muted);
}

.calif-comentario {
  color: var(--color-text);
  font-size: 0.95rem;
  line-height: 1.5;
  font-style: italic;
}

/* States */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--color-background-mute);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--color-border-hover);
  color: var(--color-text);
  font-size: 1.05rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--color-text);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-border);
  border-left-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .mis-calificaciones-container {
    padding: 1rem;
  }
  
  .dashboard-header h1 {
    font-size: 2rem;
  }
  
  .cards-grid {
    grid-template-columns: 1fr;
  }
  
  .tabs {
    flex-direction: column;
    width: 100%;
  }
  
  .tab-btn {
    justify-content: center;
  }
}
</style>
