<template>
  <div class="notificaciones-container">
    <header class="dashboard-header">
      <h1 class="page-title">Notificaciones</h1>
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
import { auth } from '@/stores/auth'

// El usuario en sesión (su id viene del token al iniciar sesión).
const usuarioId = computed(() => auth.usuario?.id)

// Base de estilo claro para los modales de esta vista.
const swalBase = { background: '#ffffff', color: '#5a6675', customClass: { popup: 'cr-swal', title: 'cr-swal-title', confirmButton: 'cr-swal-confirm', cancelButton: 'cr-swal-cancel' } }

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
    Swal.fire({ ...swalBase, icon: 'warning', title: 'Faltan datos', text: 'Indica un mensaje y la fecha del aviso.' })
    return
  }

  // datetime-local entrega "yyyy-MM-ddTHH:mm"; el backend espera segundos.
  const fechaConSegundos = nuevo.value.fechaProgramada.length === 16
    ? `${nuevo.value.fechaProgramada}:00`
    : nuevo.value.fechaProgramada

  guardando.value = true
  try {
    await programarNotificacionApi({
      usuarioId: usuarioId.value,
      titulo: nuevo.value.titulo.trim() || 'Recordatorio',
      mensaje: nuevo.value.mensaje.trim(),
      tipo: 'RECORDATORIO',
      fechaProgramada: fechaConSegundos
    })
    cerrarFormulario()
    await cargarNotificaciones()
    Swal.fire({ ...swalBase, icon: 'success', title: '¡Listo!', text: 'Tu recordatorio fue programado.', timer: 1800, showConfirmButton: false })
  } catch (error) {
    Swal.fire({ ...swalBase, icon: 'error', title: 'No se pudo programar', text: error.message })
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
    confirmButtonColor: '#dc2626',
    ...swalBase
  })

  if (!result.isConfirmed) return

  try {
    await eliminarNotificacionApi(notif.id)
    notificaciones.value = notificaciones.value.filter(n => n.id !== notif.id)
    Swal.fire({ ...swalBase, icon: 'success', title: 'Eliminada', text: 'La notificación fue eliminada.', timer: 1500, showConfirmButton: false })
  } catch (error) {
    Swal.fire({ ...swalBase, icon: 'error', title: 'Error', text: error.message })
  }
}

// Un recordatorio está pendiente mientras el backend no lo haya disparado.
const esPendiente = (n) => !n.disparada && !!n.fechaProgramada

const cargarNotificaciones = async (silencioso = false) => {
  if (!silencioso) cargando.value = true
  try {
    const data = await listarNotificacionesApi(usuarioId.value, incluirSilenciadas.value)
    notificaciones.value = data || []
  } catch (error) {
    console.error('Error al cargar notificaciones:', error)
    if (!silencioso) {
      Swal.fire({
        ...swalBase,
        icon: 'error',
        title: 'Oops...',
        text: 'No se pudieron cargar las notificaciones. ' + error.message
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
    Swal.fire({ ...swalBase, icon: 'error', title: 'Error', text: error.message })
  }
}

const activar = async (id) => {
  try {
    await activarNotificacionApi(id)
    cargarNotificaciones()
  } catch (error) {
    Swal.fire({ ...swalBase, icon: 'error', title: 'Error', text: error.message })
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
  padding: 1rem 0;
  color: var(--color-text);
}

.dashboard-header {
  margin-bottom: 1.5rem;
  text-align: center;
}

.dashboard-header h1 {
  font-size: 2.4rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
}

.dashboard-header p {
  color: var(--color-text);
  font-size: 1.05rem;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.btn-programar {
  background: var(--grad-primary);
  color: #fff;
  border: none;
  padding: 11px 20px;
  font-size: 0.95rem;
  box-shadow: var(--shadow-primary);
  transition: transform var(--t-fast), box-shadow var(--t-fast);
}

.btn-programar:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.3);
}

.form-programar {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  box-shadow: var(--shadow-md);
}

.form-programar h2 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--color-heading);
}

.form-ayuda {
  margin: -0.5rem 0 0;
  color: var(--color-text);
  font-size: 0.9rem;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.campo span {
  font-size: 0.8rem;
  color: var(--color-heading);
  font-weight: 600;
}

.campo input,
.campo textarea {
  width: 100%;
  font-family: inherit;
}

.form-acciones {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.btn-guardar {
  background: var(--grad-primary);
  color: #fff;
  border: none;
  padding: 10px 22px;
  box-shadow: var(--shadow-primary);
  transition: transform var(--t-fast);
}

.btn-guardar:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-guardar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-cancelar {
  background-color: var(--color-background-mute);
  color: var(--color-heading);
  border: 1px solid var(--color-border);
  padding: 10px 22px;
}

.btn-cancelar:hover {
  border-color: var(--color-border-hover);
}

.badge-programada {
  display: inline-block;
  margin-top: 0.5rem;
  background: var(--color-info-soft);
  color: var(--color-info);
  border: 1px solid #bfdbfe;
  padding: 3px 12px;
  border-radius: var(--radius-full);
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
  color: var(--color-text);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
}

.switch-label input {
  accent-color: var(--color-primary);
  width: 16px;
  height: 16px;
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
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1.1rem 1.35rem;
  box-shadow: var(--shadow-sm);
  transition: transform var(--t), box-shadow var(--t);
}

.notif-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.notif-card.silenciada {
  opacity: 0.6;
}

.notif-icono {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  font-size: 1.3rem;
  background: var(--color-primary-tint);
  border-radius: 12px;
  flex-shrink: 0;
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
  font-size: 1.02rem;
  color: var(--color-heading);
}

.notif-fecha {
  color: var(--color-muted);
  font-size: 0.8rem;
  white-space: nowrap;
}

.notif-mensaje {
  margin: 0;
  color: var(--color-text);
  font-size: 0.92rem;
}

.notif-acciones {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

button {
  padding: 7px 13px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 0.82rem;
  cursor: pointer;
  transition: background-color var(--t-fast), border-color var(--t-fast);
  white-space: nowrap;
}

.btn-silenciar {
  background-color: var(--color-background-mute);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.btn-silenciar:hover {
  border-color: var(--color-border-hover);
}

.btn-activar {
  background-color: var(--color-info-soft);
  color: var(--color-info);
  border: 1px solid #bfdbfe;
}

.btn-activar:hover {
  background-color: #bfdbfe;
}

.btn-eliminar {
  background-color: var(--color-danger-soft);
  color: var(--color-danger);
  border: 1px solid #fca5a5;
}

.btn-eliminar:hover {
  background-color: #fca5a5;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--color-background-mute);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--color-border-hover);
  color: var(--color-text);
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
