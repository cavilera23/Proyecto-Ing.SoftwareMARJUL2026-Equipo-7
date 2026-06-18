<template>
  <div class="buscar-container">
    <header class="dashboard-header">
      <h1 class="page-title">Buscar Cuidadores</h1>
      <p>Indica la fecha y el horario que necesitas y te mostramos quién puede atenderte sin conflictos.</p>
    </header>

    <form class="busqueda-form" @submit.prevent="buscar">
      <div class="campo">
        <label for="fecha">Fecha</label>
        <input id="fecha" type="date" v-model="fecha" :min="hoy" required />
      </div>
      <div class="campo">
        <label for="horaInicio">Hora inicio</label>
        <select id="horaInicio" v-model="horaInicio" required>
          <option value="" disabled>--:--</option>
          <option v-for="slot in bloquesHorarios" :key="'i' + slot.value" :value="slot.value">{{ slot.label }}</option>
        </select>
      </div>
      <div class="campo">
        <label for="horaFin">Hora fin</label>
        <select id="horaFin" v-model="horaFin" required>
          <option value="" disabled>--:--</option>
          <option v-for="slot in bloquesHorarios" :key="'f' + slot.value" :value="slot.value">{{ slot.label }}</option>
        </select>
      </div>
      <button type="submit" class="btn-buscar" :disabled="cargando">
        {{ cargando ? 'Buscando...' : '🔍 Buscar' }}
      </button>
    </form>

    <div v-if="cargando" class="loading-state">
      <div class="spinner"></div>
      <p>Buscando cuidadores disponibles...</p>
    </div>

    <div v-else-if="busquedaRealizada" class="resultados">
      <div v-if="cuidadores.length === 0" class="empty-state">
        <span class="empty-emoji">🔍</span>
        <p>No hay cuidadores disponibles para esa franja horaria. Prueba con otro horario.</p>
      </div>

      <div v-else>
        <h2 class="resultados-titulo">{{ cuidadores.length }} cuidador(es) disponible(s)</h2>
        <div class="cards-grid">
          <div v-for="c in cuidadores" :key="c.id" class="cuidador-card">
            <div class="card-top">
              <div class="cuidador-id">
                <span class="cuidador-avatar">{{ inicial(c.nombre) }}</span>
                <h3>{{ c.nombre }}</h3>
              </div>
              <span class="rating">⭐ {{ (c.calificacionPromedio || 0).toFixed(1) }}</span>
            </div>
            <p class="tarifa">💲 {{ c.tarifaHora }} <span>/ hora</span></p>
            <div class="habilidades" v-if="c.habilidades && c.habilidades.length">
              <span v-for="h in c.habilidades" :key="h" class="chip">{{ formatHabilidad(h) }}</span>
            </div>
            <p class="contacto">📧 {{ c.correo }}<br />📞 {{ c.telefono }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Swal from 'sweetalert2'
import { buscarCuidadoresApi } from '../services/agendaService'

const hoy = new Date().toISOString().split('T')[0]

const fecha = ref('')
const horaInicio = ref('')
const horaFin = ref('')

const cuidadores = ref([])
const cargando = ref(false)
const busquedaRealizada = ref(false)

// Bloques de 30 minutos de 06:00 a 22:00.
const bloquesHorarios = computed(() => {
  const slots = []
  for (let h = 6; h <= 22; h++) {
    for (const m of [0, 30]) {
      const hh = String(h).padStart(2, '0')
      const mm = String(m).padStart(2, '0')
      const ampm = h < 12 ? 'a.m.' : 'p.m.'
      const h12 = h % 12 === 0 ? 12 : h % 12
      slots.push({ value: `${hh}:${mm}`, label: `${String(h12).padStart(2, '0')}:${mm} ${ampm}` })
    }
  }
  return slots
})

const swalBase = { background: '#ffffff', color: '#5a6675', customClass: { popup: 'cr-swal', title: 'cr-swal-title', confirmButton: 'cr-swal-confirm' } }

const buscar = async () => {
  if (horaInicio.value >= horaFin.value) {
    Swal.fire({ ...swalBase, icon: 'warning', title: 'Horario inválido', text: 'La hora de inicio debe ser anterior a la hora de fin.' })
    return
  }

  cargando.value = true
  busquedaRealizada.value = false
  try {
    const horario = {
      fechaInicio: `${fecha.value}T${horaInicio.value}:00`,
      fechaFin: `${fecha.value}T${horaFin.value}:00`
    }
    const data = await buscarCuidadoresApi(horario)
    cuidadores.value = data || []
    busquedaRealizada.value = true
  } catch (error) {
    console.error('Error al buscar cuidadores:', error)
    Swal.fire({ ...swalBase, icon: 'error', title: 'Oops...', text: 'No se pudo realizar la búsqueda. ' + error.message })
  } finally {
    cargando.value = false
  }
}

const formatHabilidad = (h) => (h ? h.replace(/_/g, ' ') : h)
const inicial = (nombre) => (nombre || '?').trim().charAt(0).toUpperCase()
</script>

<style scoped>
.buscar-container {
  max-width: 1000px;
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
  margin-bottom: 0.5rem;
}

.dashboard-header p {
  color: var(--color-text);
  font-size: 1.05rem;
}

.busqueda-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 1rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-sm);
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
  flex: 1;
  min-width: 150px;
}

.campo label {
  font-size: 0.8rem;
  color: var(--color-heading);
  font-weight: 600;
}

.campo input,
.campo select {
  width: 100%;
}

.btn-buscar {
  background: var(--grad-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  padding: 12px 24px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-primary);
  transition: transform var(--t-fast), box-shadow var(--t-fast);
  height: fit-content;
}

.btn-buscar:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.32);
}

.btn-buscar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.resultados-titulo {
  font-size: 1.3rem;
  margin-bottom: 1.2rem;
  color: var(--color-heading);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.4rem;
}

.cuidador-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1.4rem;
  box-shadow: var(--shadow-sm);
  transition: transform var(--t), box-shadow var(--t), border-color var(--t);
}

.cuidador-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-soft);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.85rem;
}

.cuidador-id {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cuidador-avatar {
  display: inline-grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--grad-primary);
  color: #fff;
  font-weight: 700;
  font-size: 15px;
}

.card-top h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--color-heading);
}

.rating {
  color: var(--color-star);
  font-weight: 700;
  font-size: 0.95rem;
  background: var(--color-warning-soft);
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.tarifa {
  color: var(--color-primary-hover);
  font-weight: 700;
  font-size: 1.1rem;
  margin: 0.25rem 0 0.85rem;
}

.tarifa span {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-muted);
}

.habilidades {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 0.85rem;
}

.chip {
  background: var(--color-primary-tint);
  color: var(--color-primary-hover);
  border: 1px solid var(--color-primary-soft);
  border-radius: var(--radius-full);
  padding: 3px 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: capitalize;
}

.contacto {
  color: var(--color-text);
  font-size: 0.85rem;
  margin: 0;
  line-height: 1.7;
  border-top: 1px solid var(--color-border);
  padding-top: 0.75rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--color-background-mute);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--color-border-hover);
  color: var(--color-text);
  font-size: 1.05rem;
}

.empty-emoji {
  font-size: 2.5rem;
  display: block;
  margin-bottom: 1rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 250px;
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
  .busqueda-form {
    flex-direction: column;
    align-items: stretch;
  }
  .btn-buscar {
    width: 100%;
  }
}
</style>
