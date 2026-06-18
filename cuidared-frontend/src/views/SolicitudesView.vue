<!-- src/views/SolicitudesView.vue -->
<template>
  <div class="solicitudes-view">
    <!-- ============================================================= -->
    <!--  VISTA PADRE: publicar solicitudes                            -->
    <!-- ============================================================= -->
    <template v-if="!esCuidador">
      <div class="header-section">
        <h1 class="page-title">Gestión de Intercambio</h1>
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
              <select v-model="form.horaInicio" required>
                <option value="" disabled>--:--</option>
                <option v-for="slot in bloquesHorarios" :key="slot.value" :value="slot.value">
                  {{ slot.label }}
                </option>
              </select>
              <small>Bloques de 30 minutos</small>
            </div>

            <div class="campo">
              <label>Duración (horas):</label>
              <input type="number" v-model.number="form.duracionHoras" min="1" max="12" step="0.5" required />
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
                <span>📅 {{ formatearFecha(sol.fecha) }} {{ formatearHora(sol.horaInicio) }} ({{ sol.duracionHoras }}h)</span>
                <p v-if="sol.descripcion" class="descripcion">{{ sol.descripcion }}</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </template>

    <!-- ============================================================= -->
    <!--  VISTA CUIDADOR: mercado de solicitudes abiertas              -->
    <!-- ============================================================= -->
    <template v-else>
      <div class="header-section">
        <h1 class="page-title">Solicitudes de Cuidado</h1>
        <p>Revisa las solicitudes abiertas que coinciden con tus habilidades y decide cuáles aceptar.</p>
      </div>

      <div class="grid-layout">
        <!-- Disponibles para aceptar -->
        <div class="form-card">
          <h2>Disponibles para ti</h2>
          <div v-if="cargando" class="vacio"><p>Cargando solicitudes...</p></div>
          <div v-else-if="solicitudesDisponibles.length === 0" class="vacio">
            <p>✅ No hay solicitudes abiertas por ahora</p>
            <small>Cuando un cliente publique una solicitud de tu especialidad, aparecerá aquí.</small>
          </div>
          <ul v-else class="solicitudes-lista">
            <li v-for="sol in solicitudesDisponibles" :key="sol.id" class="solicitud-item">
              <div class="solicitud-header">
                <span class="tipo">{{ formatearTipo(sol.tipo) }}</span>
                <span class="estado estado-pendiente">PENDIENTE</span>
              </div>
              <div class="solicitud-detalle">
                <span>📅 {{ formatearFecha(sol.fecha) }} {{ formatearHora(sol.horaInicio) }} ({{ sol.duracionHoras }}h)</span>
                <span v-if="sol.padreNombre" class="creador">👤 Solicitado por: {{ sol.padreNombre }}</span>
                <p v-if="sol.descripcion" class="descripcion">{{ sol.descripcion }}</p>
              </div>
              <button class="btn-aceptar" :disabled="aceptandoId === sol.id" @click="aceptar(sol)">
                {{ aceptandoId === sol.id ? "Aceptando..." : "Aceptar solicitud" }}
              </button>
            </li>
          </ul>
        </div>

        <!-- Servicios ya aceptados -->
        <div class="list-card">
          <h2>Mis servicios aceptados</h2>
          <div v-if="misServiciosActivos.length === 0" class="vacio">
            <p>📋 Aún no has aceptado solicitudes</p>
            <small>Acepta una solicitud de la lista para que aparezca aquí.</small>
          </div>
          <ul v-else class="solicitudes-lista">
            <li v-for="sol in misServiciosActivos" :key="sol.id" class="solicitud-item">
              <div class="solicitud-header">
                <span class="tipo">{{ formatearTipo(sol.tipo) }}</span>
                <span :class="['estado', getEstadoClass(sol.estado)]">{{ sol.estado }}</span>
              </div>
              <div class="solicitud-detalle">
                <span>📅 {{ formatearFecha(sol.fecha) }} {{ formatearHora(sol.horaInicio) }} ({{ sol.duracionHoras }}h)</span>
                <span v-if="sol.padreNombre" class="creador">👤 Cliente: {{ sol.padreNombre }}</span>
                <p v-if="sol.descripcion" class="descripcion">{{ sol.descripcion }}</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </template>

    <!-- Mensajes toast -->
    <div v-if="mensaje" :class="['toast', tipoMensaje === 'error' ? 'toast-error' : 'toast-exito']">
      {{ mensaje }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  listarSolicitudesApi,
  crearSolicitudApi,
  listarSolicitudesDisponiblesApi,
  listarMisSolicitudesCuidadorApi,
  aceptarSolicitudApi
} from '../services/solicitudService'
import { showSuccessAlert, showErrorAlert, showWarningAlert } from '@/components/modals/alerts'
import { auth } from '@/stores/auth'

const esCuidador = computed(() => auth.tipoUsuario === 'CUIDADOR')

// Estado común
const cargando = ref(false)
const mensaje = ref('')
const tipoMensaje = ref('')

// --- Estado PADRE ---
const form = ref({
  tipo: '',
  fecha: '',
  horaInicio: '',
  duracionHoras: 1,
  descripcion: ''
})
const solicitudes = ref([])

// --- Estado CUIDADOR ---
const solicitudesDisponibles = ref([])
const misServiciosActivos = ref([])
const aceptandoId = ref(null)

// Genera los bloques de 30 minutos de 06:00 a 22:00.
const bloquesHorarios = computed(() => {
  const slots = []
  for (let h = 6; h <= 22; h++) {
    for (const m of [0, 30]) {
      const hh = String(h).padStart(2, '0')
      const mm = String(m).padStart(2, '0')
      const value = `${hh}:${mm}`
      const ampm = h < 12 ? 'a.m.' : 'p.m.'
      const h12 = h % 12 === 0 ? 12 : h % 12
      slots.push({ value, label: `${String(h12).padStart(2, '0')}:${mm} ${ampm}` })
    }
  }
  return slots
})

const fechaMinima = computed(() => new Date().toISOString().split('T')[0])

const solicitudesActivas = computed(() =>
  solicitudes.value.filter(s => s.estado === 'PENDIENTE' || s.estado === 'ACEPTADA')
)

// --- Helpers de formato ---
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

const formatearFecha = (fecha) => {
  if (!fecha) return 'Fecha por definir'
  if (Array.isArray(fecha)) {
    const [y, mo, d] = fecha
    return new Date(y, mo - 1, d).toLocaleDateString('es-VE', { day: 'numeric', month: 'short', year: 'numeric' })
  }
  return new Date(fecha).toLocaleDateString('es-VE', { day: 'numeric', month: 'short', year: 'numeric' })
}

const formatearHora = (hora) => {
  if (!hora) return ''
  if (Array.isArray(hora)) {
    const [h, m = 0] = hora
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }
  return typeof hora === 'string' ? hora.substring(0, 5) : hora
}

// ============================== PADRE ==============================

const cargarSolicitudes = async () => {
  try {
    const todas = await listarSolicitudesApi()
    solicitudes.value = todas.filter(s => s.padreId === auth.usuario?.id)
  } catch (error) {
    console.error("Error cargando solicitudes", error)
  }
}

const publicarSolicitud = async () => {
  mensaje.value = ''
  tipoMensaje.value = ''

  if (!form.value.tipo) return showWarningAlert("Campo requerido", "Debes seleccionar un tipo de cuidador")
  if (!form.value.fecha) return showWarningAlert("Campo requerido", "Debes seleccionar una fecha")
  if (!form.value.horaInicio) return showWarningAlert("Campo requerido", "Debes seleccionar una hora de inicio")
  if (form.value.duracionHoras < 1 || form.value.duracionHoras > 12) {
    return showWarningAlert("Duración inválida", "La duración debe ser entre 1 y 12 horas")
  }

  cargando.value = true

  const horaInicio = form.value.horaInicio
  const [h, m] = horaInicio.split(':').map(Number)
  const minutosTotales = h * 60 + m + (form.value.duracionHoras * 60)
  const horaFin = `${String(Math.floor(minutosTotales / 60)).padStart(2, '0')}:${String(minutosTotales % 60).padStart(2, '0')}`

  const payload = {
    tipo: form.value.tipo,
    descripcion: form.value.descripcion,
    fecha: form.value.fecha,
    horaInicio,
    duracionHoras: form.value.duracionHoras,
    horario: {
      fechaInicio: `${form.value.fecha}T${horaInicio}:00`,
      fechaFin: `${form.value.fecha}T${horaFin}:00`
    }
  }

  try {
    await crearSolicitudApi(payload)
    await showSuccessAlert("Solicitud publicada", "Tu solicitud ha sido publicada exitosamente")
    form.value = { tipo: '', fecha: '', horaInicio: '', duracionHoras: 1, descripcion: '' }
    await cargarSolicitudes()
    flash("✅ Solicitud publicada exitosamente", "exito")
  } catch (error) {
    console.error("Error al publicar:", error)
    let errorMsg = extraerError(error.message) || "Error al publicar la solicitud"
    await showErrorAlert("No se pudo publicar", errorMsg)
    flash(`❌ ${errorMsg}`, "error")
  } finally {
    cargando.value = false
  }
}

// ============================= CUIDADOR =============================

const cargarVistaCuidador = async () => {
  cargando.value = true
  try {
    const [disponibles, mias] = await Promise.all([
      listarSolicitudesDisponiblesApi(),
      listarMisSolicitudesCuidadorApi()
    ])
    solicitudesDisponibles.value = disponibles || []
    misServiciosActivos.value = (mias && mias.activas) || []
  } catch (error) {
    console.error("Error cargando vista de cuidador", error)
  } finally {
    cargando.value = false
  }
}

const aceptar = async (sol) => {
  aceptandoId.value = sol.id
  try {
    await aceptarSolicitudApi(sol.id)
    await showSuccessAlert("¡Solicitud aceptada!", "El servicio fue agregado a tus servicios aceptados.")
    await cargarVistaCuidador()
    flash("✅ Solicitud aceptada", "exito")
  } catch (error) {
    const errorMsg = extraerError(error.message) || "No se pudo aceptar la solicitud"
    await showErrorAlert("No se pudo aceptar", errorMsg)
    flash(`❌ ${errorMsg}`, "error")
    // Si ya la tomó otro, refrescar la lista para que desaparezca.
    await cargarVistaCuidador()
  } finally {
    aceptandoId.value = null
  }
}

// ============================== Util ==============================

// El backend devuelve errores como JSON {"error":"..."} o texto plano.
const extraerError = (raw) => {
  if (!raw) return null
  try {
    const obj = JSON.parse(raw)
    return obj.error || raw
  } catch {
    return raw
  }
}

const flash = (texto, tipo) => {
  mensaje.value = texto
  tipoMensaje.value = tipo
  setTimeout(() => { mensaje.value = '' }, 3000)
}

onMounted(() => {
  if (esCuidador.value) {
    cargarVistaCuidador()
  } else {
    cargarSolicitudes()
  }
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
  font-size: 2.3rem;
  margin-bottom: 8px;
}

.header-section p {
  color: var(--color-text);
  font-size: 1.05rem;
}

.grid-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 26px;
}

.form-card, .list-card {
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 28px;
  box-shadow: var(--shadow-sm);
  animation: fadeUp 0.4s ease both;
}

.form-card h2, .list-card h2 {
  color: var(--color-heading);
  margin-bottom: 22px;
  font-size: 1.25rem;
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
  padding: 13px;
  background: var(--grad-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-primary);
  transition: transform var(--t-fast), box-shadow var(--t-fast);
}

button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.32);
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  box-shadow: none;
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
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 14px;
  box-shadow: var(--shadow-xs);
  transition: transform var(--t-fast), box-shadow var(--t-fast);
}

.solicitud-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
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

.estado-finalizada {
  background-color: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

.estado-cancelada {
  background-color: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

.solicitud-detalle {
  font-size: 13px;
  color: var(--color-text);
}

.creador {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-heading);
}

.descripcion {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.8;
}

.btn-aceptar {
  margin-top: 12px;
  background-color: #10b981;
}

.btn-aceptar:hover:not(:disabled) {
  background-color: #059669;
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
