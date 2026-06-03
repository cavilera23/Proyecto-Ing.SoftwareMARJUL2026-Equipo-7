<template>
  <div class="notificaciones-container">
    <header class="dashboard-header">
      <h1>Notificaciones</h1>
      <p>Revisa los avisos del sistema y silencia los que no quieras seguir viendo.</p>
    </header>

    <div class="toolbar">
      <label class="switch-label">
        <input type="checkbox" v-model="incluirSilenciadas" @change="cargarNotificaciones" />
        Mostrar silenciadas
      </label>
    </div>

    <div v-if="cargando" class="loading-state">
      <div class="spinner"></div>
      <p>Cargando notificaciones...</p>
    </div>

    <div v-else class="lista">
      <div v-if="notificaciones.length === 0" class="empty-state">
        <p>No tienes notificaciones por ahora.</p>
      </div>

      <div
        v-for="n in notificaciones"
        :key="n.id"
        :class="['notif-card', { silenciada: n.silenciada }]"
      >
        <div class="notif-icono">{{ iconoTipo(n.tipo) }}</div>
        <div class="notif-body">
          <div class="notif-top">
            <h3>{{ n.titulo || 'Notificación' }}</h3>
            <span class="notif-fecha">{{ formatearFecha(n.fechaCreacion) }}</span>
          </div>
          <p class="notif-mensaje">{{ n.mensaje }}</p>
        </div>
        <div class="notif-acciones">
          <button v-if="!n.silenciada" class="btn-silenciar" @click="silenciar(n.id)">🔕 Silenciar</button>
          <button v-else class="btn-activar" @click="activar(n.id)">🔔 Activar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import {
  listarNotificacionesApi,
  silenciarNotificacionApi,
  activarNotificacionApi
} from '../services/notificacionService'

// ID hardcoded igual que en el resto de vistas de prueba
const USUARIO_TEST_ID = 'test-padre-123'

const notificaciones = ref([])
const cargando = ref(true)
const incluirSilenciadas = ref(false)

const cargarNotificaciones = async () => {
  cargando.value = true
  try {
    const data = await listarNotificacionesApi(USUARIO_TEST_ID, incluirSilenciadas.value)
    notificaciones.value = data || []
  } catch (error) {
    console.error('Error al cargar notificaciones:', error)
    Swal.fire({
      icon: 'error',
      title: 'Oops...',
      text: 'No se pudieron cargar las notificaciones. ' + error.message,
      background: '#1f2937',
      color: '#fff'
    })
  } finally {
    cargando.value = false
  }
}

const silenciar = async (id) => {
  try {
    await silenciarNotificacionApi(id)
    cargarNotificaciones()
  } catch (error) {
    Swal.fire({ icon: 'error', title: 'Error', text: error.message, background: '#1f2937', color: '#fff' })
  }
}

const activar = async (id) => {
  try {
    await activarNotificacionApi(id)
    cargarNotificaciones()
  } catch (error) {
    Swal.fire({ icon: 'error', title: 'Error', text: error.message, background: '#1f2937', color: '#fff' })
  }
}

const iconoTipo = (tipo) => {
  switch (tipo) {
    case 'SOLICITUD': return '📋'
    case 'CALIFICACION': return '⭐'
    default: return '🔔'
  }
}

const formatearFecha = (fecha) => {
  if (!fecha) return ''
  if (Array.isArray(fecha)) {
    const [y, m, d, h = 0, min = 0] = fecha
    return new Date(y, m - 1, d, h, min).toLocaleString('es-ES', {
      day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
    })
  }
  return new Date(fecha).toLocaleString('es-ES', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
  })
}

onMounted(() => {
  cargarNotificaciones()
})
</script>

<style scoped>
.notificaciones-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  color: #fff;
}

.dashboard-header {
  margin-bottom: 1.5rem;
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

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1.5rem;
}

.switch-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
  font-size: 0.95rem;
  cursor: pointer;
}

.lista {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.notif-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 1rem 1.25rem;
  transition: transform 0.2s ease;
}

.notif-card:hover {
  transform: translateY(-2px);
}

.notif-card.silenciada {
  opacity: 0.55;
}

.notif-icono {
  font-size: 1.5rem;
}

.notif-body {
  flex: 1;
}

.notif-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.25rem;
}

.notif-top h3 {
  margin: 0;
  font-size: 1.05rem;
  color: #fff;
}

.notif-fecha {
  color: #9ca3af;
  font-size: 0.8rem;
  white-space: nowrap;
}

.notif-mensaje {
  margin: 0;
  color: #ccc;
  font-size: 0.95rem;
}

.notif-acciones {
  display: flex;
  align-items: center;
}

button {
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.btn-silenciar {
  background-color: rgba(156, 163, 175, 0.1);
  color: #9ca3af;
  border: 1px solid #9ca3af;
}

.btn-silenciar:hover {
  background-color: rgba(156, 163, 175, 0.2);
}

.btn-activar {
  background-color: rgba(59, 130, 246, 0.1);
  color: #60a5fa;
  border: 1px solid #60a5fa;
}

.btn-activar:hover {
  background-color: rgba(59, 130, 246, 0.2);
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  color: #9ca3af;
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

@media (max-width: 768px) {
  .notif-card {
    flex-direction: column;
  }
  .notif-acciones {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
