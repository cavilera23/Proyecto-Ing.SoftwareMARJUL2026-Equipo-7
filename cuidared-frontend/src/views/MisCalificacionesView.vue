<template>
  <div class="mis-calificaciones-container">
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
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { apiFetch } from '../services/api'
import SolicitudCard from '../components/SolicitudCard.vue'

// ID hardcoded según lo solicitado para pruebas visuales
const PADRE_TEST_ID = 'test-padre-123' 

const solicitudesFuturas = ref([])
const solicitudesHistorial = ref([])
const cargando = ref(true)
const tabActiva = ref('futuras') // 'futuras' o 'historial'

const cargarSolicitudes = async () => {
  cargando.value = true
  try {
    const data = await apiFetch(`/intercambio/solicitudes/padre/${PADRE_TEST_ID}`)
    if (data) {
      solicitudesFuturas.value = data.futuras || []
      solicitudesHistorial.value = data.historial || []
    }
  } catch (error) {
    console.error('Error al cargar solicitudes:', error)
    Swal.fire({
      icon: 'error',
      title: 'Oops...',
      text: 'Hubo un problema al cargar tus solicitudes. ' + error.message,
      background: '#1f2937',
      color: '#fff'
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
    background: '#1f2937',
    color: '#fff'
  }).then((result) => {
    if (result.isConfirmed) {
      // Aquí iría la llamada al backend para cancelar:
      // apiFetch(`/intercambio/solicitudes/${id}/cancelar`, { method: 'POST' })
      Swal.fire({
        title: 'Cancelada',
        text: 'La cita ha sido cancelada exitosamente.',
        icon: 'success',
        background: '#1f2937',
        color: '#fff'
      })
      // Simular actualización
      solicitudesFuturas.value = solicitudesFuturas.value.filter(s => s.id !== id)
    }
  })
}

const handleCalificar = (id) => {
  Swal.fire({
    title: 'Calificar Servicio',
    input: 'select',
    inputOptions: {
      '5': '⭐⭐⭐⭐⭐ - Excelente',
      '4': '⭐⭐⭐⭐ - Muy Bueno',
      '3': '⭐⭐⭐ - Bueno',
      '2': '⭐⭐ - Regular',
      '1': '⭐ - Malo'
    },
    inputPlaceholder: 'Selecciona una calificación',
    showCancelButton: true,
    confirmButtonText: 'Enviar',
    cancelButtonText: 'Cancelar',
    background: '#1f2937',
    color: '#fff'
  }).then((result) => {
    if (result.isConfirmed && result.value) {
      Swal.fire({
        title: '¡Gracias!',
        text: `Has calificado este servicio con ${result.value} estrellas.`,
        icon: 'success',
        background: '#1f2937',
        color: '#fff'
      })
    }
  })
}

onMounted(() => {
  cargarSolicitudes()
})
</script>

<style scoped>
.mis-calificaciones-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  color: #fff;
}

.dashboard-header {
  margin-bottom: 2rem;
  text-align: center;
}

.dashboard-header h1 {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.dashboard-header p {
  color: #9ca3af;
  font-size: 1.1rem;
}

/* Tabs */
.tabs-container {
  display: flex;
  justify-content: center;
  margin-bottom: 2.5rem;
}

.tabs {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.05);
  padding: 0.5rem;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.tab-btn {
  background: transparent;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  color: #9ca3af;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-btn:hover {
  color: #fff;
}

.tab-btn.active {
  background: rgba(96, 165, 250, 0.15);
  color: #60a5fa;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Content Area */
.content-area {
  min-height: 400px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

/* States */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  color: #9ca3af;
  font-size: 1.1rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #9ca3af;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-left-color: #60a5fa;
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
