<template>
  <div class="buscar-container">
    <header class="dashboard-header">
      <h1>Buscar Cuidadores Disponibles</h1>
      <p>Indica la fecha y el horario que necesitas y te mostramos quién puede atenderte sin conflictos.</p>
    </header>

    <form class="busqueda-form" @submit.prevent="buscar">
      <div class="campo">
        <label for="fecha">Fecha</label>
        <input id="fecha" type="date" v-model="fecha" :min="hoy" required />
      </div>
      <div class="campo">
        <label for="horaInicio">Hora inicio</label>
        <input id="horaInicio" type="time" v-model="horaInicio" required />
      </div>
      <div class="campo">
        <label for="horaFin">Hora fin</label>
        <input id="horaFin" type="time" v-model="horaFin" required />
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
        <p>No hay cuidadores disponibles para esa franja horaria. Prueba con otro horario.</p>
      </div>

      <div v-else>
        <h2 class="resultados-titulo">{{ cuidadores.length }} cuidador(es) disponible(s)</h2>
        <div class="cards-grid">
          <div v-for="c in cuidadores" :key="c.id" class="cuidador-card">
            <div class="card-top">
              <h3>{{ c.nombre }}</h3>
              <span class="rating">⭐ {{ (c.calificacionPromedio || 0).toFixed(1) }}</span>
            </div>
            <p class="tarifa">💲 {{ c.tarifaHora }} / hora</p>
            <div class="habilidades" v-if="c.habilidades && c.habilidades.length">
              <span v-for="h in c.habilidades" :key="h" class="chip">{{ formatHabilidad(h) }}</span>
            </div>
            <p class="contacto">📧 {{ c.correo }} · 📞 {{ c.telefono }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Swal from 'sweetalert2'
import { buscarCuidadoresApi } from '../services/agendaService'

const hoy = new Date().toISOString().split('T')[0]

const fecha = ref('')
const horaInicio = ref('')
const horaFin = ref('')

const cuidadores = ref([])
const cargando = ref(false)
const busquedaRealizada = ref(false)

const buscar = async () => {
  if (horaInicio.value >= horaFin.value) {
    Swal.fire({
      icon: 'warning',
      title: 'Horario inválido',
      text: 'La hora de inicio debe ser anterior a la hora de fin.',
      background: '#1f2937',
      color: '#fff'
    })
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
    Swal.fire({
      icon: 'error',
      title: 'Oops...',
      text: 'No se pudo realizar la búsqueda. ' + error.message,
      background: '#1f2937',
      color: '#fff'
    })
  } finally {
    cargando.value = false
  }
}

const formatHabilidad = (h) => (h ? h.replace(/_/g, ' ') : h)
</script>

<style scoped>
.buscar-container {
  max-width: 1000px;
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

.busqueda-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  flex: 1;
  min-width: 140px;
}

.campo label {
  font-size: 0.85rem;
  color: #9ca3af;
  font-weight: 600;
}

.campo input {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 0.6rem 0.8rem;
  color: #fff;
  font-size: 0.95rem;
  color-scheme: dark;
}

.btn-buscar {
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0.7rem 1.5rem;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s;
  height: fit-content;
}

.btn-buscar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.resultados-titulo {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  color: #e0e0e0;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.cuidador-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.25rem;
  transition: transform 0.2s ease;
}

.cuidador-card:hover {
  transform: translateY(-4px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.card-top h3 {
  margin: 0;
  font-size: 1.2rem;
}

.rating {
  color: #fbbf24;
  font-weight: 600;
  font-size: 0.95rem;
}

.tarifa {
  color: #34d399;
  font-weight: 600;
  margin: 0.25rem 0 0.75rem;
}

.habilidades {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 0.75rem;
}

.chip {
  background: rgba(96, 165, 250, 0.15);
  color: #60a5fa;
  border: 1px solid rgba(96, 165, 250, 0.4);
  border-radius: 20px;
  padding: 2px 10px;
  font-size: 0.75rem;
  text-transform: capitalize;
}

.contacto {
  color: #9ca3af;
  font-size: 0.85rem;
  margin: 0;
}

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
  height: 250px;
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
  .busqueda-form {
    flex-direction: column;
    align-items: stretch;
  }
  .btn-buscar {
    width: 100%;
  }
}
</style>
