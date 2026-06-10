<template>
  <div class="notificaciones-container">
    <header class="dashboard-header">
      <h1>Notificaciones</h1>
      <p>Revisa los avisos del sistema y silencia los que no quieras seguir viendo.</p>
    </header>

    <div class="toolbar">
      <button class="btn-programar" @click="mostrarFormulario = !mostrarFormulario">
        {{ mostrarFormulario ? '✕ Cerrar' : '➕ Programar notificación' }}
      </button>
      <label class="switch-label">
        <input type="checkbox" v-model="incluirSilenciadas" @change="cargarNotificaciones" />
        Mostrar silenciadas
      </label>
    </div>

    <transition name="slide">
      <form v-if="mostrarFormulario" class="form-programar" @submit.prevent="programar">
        <h2>Programar un recordatorio</h2>
        <p class="form-ayuda">Crea un aviso para una cita o evento próximo y el sistema te lo recordará.</p>

        <label class="campo">
          <span>Título</span>
          <input v-model="nuevo.titulo" type="text" placeholder="Ej: Cita con el pediatra" maxlength="80" />
        </label>

        <label class="campo">
          <span>Mensaje *</span>
          <textarea v-model="nuevo.mensaje" rows="2" placeholder="Ej: Llevar a Sofía a la consulta de las 10:00" required></textarea>
        </label>

        <label class="campo">
          <span>Fecha y hora del aviso *</span>
          <input v-model="nuevo.fechaProgramada" type="datetime-local" :min="minFechaProgramada" required />
        </label>

        <div class="form-acciones">
          <button type="button" class="btn-cancelar" @click="cerrarFormulario">Cancelar</button>
          <button type="submit" class="btn-guardar" :disabled="guardando">
            {{ guardando ? 'Programando...' : 'Programar' }}
          </button>
        </div>
      </form>
    </transition>

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
          <span v-if="esPendiente(n)" class="badge-programada">
            ⏰ Programada para {{ formatearFecha(n.fechaProgramada) }}
          </span>
        </div>
        <div class="notif-acciones">
          <button v-if="!n.silenciada" class="btn-silenciar" @click="silenciar(n.id)">🔕 Silenciar</button>
          <button v-else class="btn-activar" @click="activar(n.id)">🔔 Activar</button>
          <button class="btn-eliminar" @click="eliminar(n)">🗑️ Eliminar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Swal from 'sweetalert2'
import {
  listarNotificacionesApi,
  silenciarNotificacionApi,
  activarNotificacionApi,
  programarNotificacionApi,
  eliminarNotificacionApi
} from '../services/notificacionService'

// ID hardcoded igual que en el resto de vistas de prueba
const USUARIO_TEST_ID = 'test-padre-123'

const notificaciones = ref([])
const cargando = ref(true)
const incluirSilenciadas = ref(false)

// --- Programar notificación ---
const mostrarFormulario = ref(false)
const guardando = ref(false)
const nuevo = ref({ titulo: '', mensaje: '', fechaProgramada: '' })

// Mínimo seleccionable: ahora mismo (formato datetime-local: yyyy-MM-ddTHH:mm)
const minFechaProgramada = computed(() => {
  const ahora = new Date()
  ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset())
  return ahora.toISOString().slice(0, 16)
})

const cerrarFormulario = () => {
  mostrarFormulario.value = false
  nuevo.value = { titulo: '', mensaje: '', fechaProgramada: '' }
}

const programar = async () => {
  if (!nuevo.value.mensaje.trim() || !nuevo.value.fechaProgramada) {
    Swal.fire({ icon: 'warning', title: 'Faltan datos', text: 'Indica un mensaje y la fecha del aviso.', background: '#1f2937', color: '#fff' })
    return
  }

  // datetime-local entrega "yyyy-MM-ddTHH:mm"; el backend espera segundos.
  const fechaConSegundos = nuevo.value.fechaProgramada.length === 16
    ? `${nuevo.value.fechaProgramada}:00`
    : nuevo.value.fechaProgramada

  guardando.value = true
  try {
    await programarNotificacionApi({
      usuarioId: USUARIO_TEST_ID,
      titulo: nuevo.value.titulo.trim() || 'Recordatorio',
      mensaje: nuevo.value.mensaje.trim(),
      tipo: 'RECORDATORIO',
      fechaProgramada: fechaConSegundos
    })
    cerrarFormulario()
    await cargarNotificaciones()
    Swal.fire({ icon: 'success', title: '¡Listo!', text: 'Tu recordatorio fue programado.', background: '#1f2937', color: '#fff', timer: 1800, showConfirmButton: false })
  } catch (error) {
    Swal.fire({ icon: 'error', title: 'No se pudo programar', text: error.message, background: '#1f2937', color: '#fff' })
  } finally {
    guardando.value = false
  }
}

const eliminar = async (notif) => {
  const result = await Swal.fire({
    icon: 'warning',
    title: '¿Eliminar notificación?',
    html: `Se eliminará <b>"${notif.titulo || 'Notificación'}"</b> de forma permanente.`,
    showCancelButton: true,
    confirmButtonText: 'Sí, eliminar',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: '#ef4444',
    cancelButtonColor: '#6b7280',
    background: '#1f2937',
    color: '#fff'
  })

  if (!result.isConfirmed) return

  try {
    await eliminarNotificacionApi(notif.id)
    notificaciones.value = notificaciones.value.filter(n => n.id !== notif.id)
    Swal.fire({ icon: 'success', title: 'Eliminada', text: 'La notificación fue eliminada.', background: '#1f2937', color: '#fff', timer: 1500, showConfirmButton: false })
  } catch (error) {
    Swal.fire({ icon: 'error', title: 'Error', text: error.message, background: '#1f2937', color: '#fff' })
  }
}

// Un recordatorio está pendiente mientras el backend no lo haya disparado.
const esPendiente = (n) => !n.disparada && !!n.fechaProgramada

const cargarNotificaciones = async (silencioso = false) => {
  if (!silencioso) cargando.value = true
  try {
    const data = await listarNotificacionesApi(USUARIO_TEST_ID, incluirSilenciadas.value)
    notificaciones.value = data || []
  } catch (error) {
    console.error('Error al cargar notificaciones:', error)
    if (!silencioso) {
      Swal.fire({
        icon: 'error',
        title: 'Oops...',
        text: 'No se pudieron cargar las notificaciones. ' + error.message,
        background: '#1f2937',
        color: '#fff'
      })
    }
  } finally {
    if (!silencioso) cargando.value = false
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
    case 'RECORDATORIO': return '⏰'
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

// Auto-refresco: detecta los recordatorios que el backend dispara al llegar su minuto.
let intervalo = null

onMounted(() => {
  cargarNotificaciones()
  intervalo = setInterval(() => cargarNotificaciones(true), 30000)
})

onUnmounted(() => {
  if (intervalo) clearInterval(intervalo)
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
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn-programar {
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  color: #fff;
  border: none;
  padding: 10px 18px;
  font-size: 0.95rem;
}

.btn-programar:hover {
  opacity: 0.9;
}

.form-programar {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-programar h2 {
  margin: 0;
  font-size: 1.3rem;
  color: #fff;
}

.form-ayuda {
  margin: -0.5rem 0 0;
  color: #9ca3af;
  font-size: 0.9rem;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.campo span {
  font-size: 0.85rem;
  color: #cbd5e1;
  font-weight: 600;
}

.campo input,
.campo textarea {
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 10px 12px;
  color: #fff;
  font-size: 0.95rem;
  font-family: inherit;
}

.campo input:focus,
.campo textarea:focus {
  outline: none;
  border-color: #60a5fa;
}

.form-acciones {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.btn-guardar {
  background-color: #10b981;
  color: #fff;
  border: none;
  padding: 9px 20px;
}

.btn-guardar:hover {
  background-color: #059669;
}

.btn-guardar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancelar {
  background-color: transparent;
  color: #9ca3af;
  border: 1px solid #6b7280;
  padding: 9px 20px;
}

.btn-cancelar:hover {
  background-color: rgba(156, 163, 175, 0.1);
}

.badge-programada {
  display: inline-block;
  margin-top: 0.5rem;
  background: rgba(96, 165, 250, 0.15);
  color: #93c5fd;
  border: 1px solid rgba(96, 165, 250, 0.4);
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
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
  gap: 0.5rem;
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

.btn-eliminar {
  background-color: rgba(239, 68, 68, 0.1);
  color: #f87171;
  border: 1px solid #ef4444;
}

.btn-eliminar:hover {
  background-color: rgba(239, 68, 68, 0.2);
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
