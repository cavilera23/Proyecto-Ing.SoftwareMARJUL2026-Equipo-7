<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";

import {
  showSuccessAlert,
  showErrorAlert,
  showWarningAlert,
  showConfirmAlert,
} from "@/components/modals/alerts";
import {
  listarHorariosApi,
  agregarHorarioApi,
  modificarHorarioApi,
  eliminarHorarioApi,
} from "@/services/agendaService";
import { auth } from "@/stores/auth";

// El cuidador en sesión: ya no hace falta pegar el ID a mano, se toma del token.
const cuidadorId = ref(auth.usuario?.id || localStorage.getItem('cuidadorId') || '')

watch(cuidadorId, async (nuevoId) => {
  localStorage.setItem('cuidadorId', nuevoId)
  if (nuevoId.trim()) {
    await cargarHorarios(nuevoId)
  } else {
    calendarEvents.value = []
  }
})

const fechaSeleccionada = ref("");
const horaInicio = ref("");
const horaFin = ref("");
const mensaje = ref("");
const tipoMensaje = ref("");

const calendarEvents = ref([]);

// Lista cruda de bloques registrados, cada uno con su índice (el backend
// identifica cada bloque por su posición en la lista del cuidador).
const horariosRegistrados = ref([]);

// Estado de edición: cuando es true, el formulario actualiza en vez de crear.
const modoEdicion = ref(false);
const indiceEditando = ref(null);

// Referencia al panel del formulario, para llevar la vista hasta él al editar.
const panelRef = ref(null);

const horasInicio = [
  { value: "07:00", label: "07:00 a.m." },
  { value: "07:30", label: "07:30 a.m." },
  { value: "08:00", label: "08:00 a.m." },
  { value: "08:30", label: "08:30 a.m." },
  { value: "09:00", label: "09:00 a.m." },
  { value: "09:30", label: "09:30 a.m." },
  { value: "10:00", label: "10:00 a.m." },
  { value: "10:30", label: "10:30 a.m." },
  { value: "11:00", label: "11:00 a.m." },
  { value: "11:30", label: "11:30 a.m." },
  { value: "12:00", label: "12:00 p.m." },
  { value: "12:30", label: "12:30 p.m." },
  { value: "13:00", label: "01:00 p.m." },
  { value: "13:30", label: "01:30 p.m." },
  { value: "14:00", label: "02:00 p.m." },
  { value: "14:30", label: "02:30 p.m." },
  { value: "15:00", label: "03:00 p.m." },
  { value: "15:30", label: "03:30 p.m." },
  { value: "16:00", label: "04:00 p.m." },
  { value: "16:30", label: "04:30 p.m." },
  { value: "17:00", label: "05:00 p.m." },
  { value: "17:30", label: "05:30 p.m." },
  { value: "18:00", label: "06:00 p.m." },
  { value: "18:30", label: "06:30 p.m." },
  { value: "19:00", label: "07:00 p.m." },
];

const horasFin = [
  { value: "07:30", label: "07:30 a.m." },
  { value: "08:00", label: "08:00 a.m." },
  { value: "08:30", label: "08:30 a.m." },
  { value: "09:00", label: "09:00 a.m." },
  { value: "09:30", label: "09:30 a.m." },
  { value: "10:00", label: "10:00 a.m." },
  { value: "10:30", label: "10:30 a.m." },
  { value: "11:00", label: "11:00 a.m." },
  { value: "11:30", label: "11:30 a.m." },
  { value: "12:00", label: "12:00 p.m." },
  { value: "12:30", label: "12:30 p.m." },
  { value: "13:00", label: "01:00 p.m." },
  { value: "13:30", label: "01:30 p.m." },
  { value: "14:00", label: "02:00 p.m." },
  { value: "14:30", label: "02:30 p.m." },
  { value: "15:00", label: "03:00 p.m." },
  { value: "15:30", label: "03:30 p.m." },
  { value: "16:00", label: "04:00 p.m." },
  { value: "16:30", label: "04:30 p.m." },
  { value: "17:00", label: "05:00 p.m." },
  { value: "17:30", label: "05:30 p.m." },
  { value: "18:00", label: "06:00 p.m." },
  { value: "18:30", label: "06:30 p.m." },
  { value: "19:00", label: "07:00 p.m." },
  { value: "19:30", label: "07:30 p.m." },
];

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: "dayGridMonth",
  locale: "es",
  editable: false,
  selectable: true,
  events: calendarEvents.value,
  headerToolbar: {
    left: "prev,next today",
    center: "title",
    right: "dayGridMonth",
  },
  dateClick: (info) => {
    seleccionarFecha(info.dateStr);
  },
}));

const cargarHorarios = async (id) => {
  try {
    const horarios = await listarHorariosApi(id);

    // Guardamos la lista con el índice de cada bloque (su posición en el array).
    // Ese índice es el que el backend espera en PUT/DELETE /horarios/{id}/{indice}.
    horariosRegistrados.value = horarios.map((h, indice) => ({
      indice,
      fechaInicio: h.fechaInicio,
      fechaFin: h.fechaFin,
    }));

    // El calendario refleja exactamente los mismos bloques.
    calendarEvents.value = horariosRegistrados.value.map((h) => ({
      title: "Disponible",
      start: h.fechaInicio,
      end: h.fechaFin,
      color: "#10b981",
    }));
  } catch (error) {
    console.error("Error cargando horarios:", error);
  }
};

onMounted(async () => {
  if (cuidadorId.value.trim()) {
    await cargarHorarios(cuidadorId.value);
  }
});

// Valida el formulario y construye el objeto Horario que entiende el backend.
// Devuelve null (y muestra el aviso) si algo está incompleto.
const construirHorarioDesdeFormulario = async () => {
  if (!cuidadorId.value.trim()) {
    await showWarningAlert(
      "ID requerido",
      "Debes ingresar tu ID de cuidador antes de gestionar tus horarios.",
    );
    return null;
  }

  if (!fechaSeleccionada.value || !horaInicio.value || !horaFin.value) {
    await showWarningAlert(
      "Campos incompletos",
      "Debes seleccionar una fecha, una hora de inicio y una hora de fin.",
    );
    return null;
  }

  if (horaInicio.value >= horaFin.value) {
    await showWarningAlert(
      "Horario inválido",
      "La hora de inicio debe ser anterior a la hora de fin.",
    );
    return null;
  }

  return {
    fechaInicio: `${fechaSeleccionada.value}T${horaInicio.value}:00`,
    fechaFin: `${fechaSeleccionada.value}T${horaFin.value}:00`,
  };
};

// Botón principal del formulario: crea un bloque nuevo o actualiza el que se
// está editando, según el modoEdicion.
const guardarHorario = async () => {
  mensaje.value = "";
  tipoMensaje.value = "";

  const horario = await construirHorarioDesdeFormulario();
  if (!horario) return;

  const editando = modoEdicion.value;

  try {
    if (editando) {
      await modificarHorarioApi(cuidadorId.value, indiceEditando.value, horario);
    } else {
      await agregarHorarioApi(cuidadorId.value, horario);
    }

    // Recargamos desde el backend para que la lista y los índices queden
    // alineados con lo que realmente quedó guardado.
    await cargarHorarios(cuidadorId.value);
    cancelarEdicion();

    mensaje.value = editando
      ? "¡Bloque de horario actualizado!"
      : "¡Bloque de horario registrado con éxito!";
    tipoMensaje.value = "exito";

    await showSuccessAlert(
      editando ? "Horario actualizado" : "Disponibilidad registrada",
      editando
        ? "El bloque de disponibilidad fue modificado correctamente."
        : "El bloque de horario fue guardado exitosamente.",
    );
  } catch (error) {
    mensaje.value = `Error: ${error.message}`;
    tipoMensaje.value = "error";

    await showErrorAlert(
      editando ? "No se pudo actualizar el horario" : "No se pudo registrar el horario",
      error.message,
    );
  }
};

// Carga un bloque existente en el formulario y entra en modo edición.
const iniciarEdicion = async (horario) => {
  modoEdicion.value = true;
  indiceEditando.value = horario.indice;

  // El ISO viene como "2026-06-20T08:00:00": cortamos fecha y horas.
  fechaSeleccionada.value = horario.fechaInicio.substring(0, 10); // YYYY-MM-DD
  horaInicio.value = horario.fechaInicio.substring(11, 16); // HH:MM
  horaFin.value = horario.fechaFin.substring(11, 16);

  mensaje.value = "";
  tipoMensaje.value = "";

  // Llevamos la vista hasta el formulario para que se note que entró en edición.
  await nextTick();
  panelRef.value?.scrollIntoView({ behavior: "smooth", block: "center" });
};

// Sale del modo edición y limpia el formulario.
const cancelarEdicion = () => {
  modoEdicion.value = false;
  indiceEditando.value = null;
  horaInicio.value = "";
  horaFin.value = "";
};

// Maneja el clic en un día del calendario.
// - Si ya estás editando, el clic solo cambia la fecha del bloque en edición.
// - Si no, decide según los bloques de ese día: con uno lo edita, con varios
//   te manda a la lista, y sin ninguno avisa que primero hay que registrarlo.
const seleccionarFecha = (fecha) => {
  // En modo edición el calendario solo sirve para mover la fecha del bloque.
  if (modoEdicion.value) {
    fechaSeleccionada.value = fecha;
    return;
  }

  const bloquesDelDia = horariosRegistrados.value.filter(
    (h) => h.fechaInicio.substring(0, 10) === fecha,
  );

  if (bloquesDelDia.length === 1) {
    // Hay exactamente un bloque ese día: lo cargamos para editar.
    iniciarEdicion(bloquesDelDia[0]);
    return;
  }

  // Sin bloque (o con varios) no se puede editar: preparamos para crear.
  fechaSeleccionada.value = fecha;
  horaInicio.value = "";
  horaFin.value = "";

  if (bloquesDelDia.length > 1) {
    mensaje.value =
      "Hay varios bloques en esa fecha. Elige cuál editar en la lista de abajo.";
  } else {
    mensaje.value =
      "No tienes un horario registrado en esta fecha. Completa las horas para crear uno nuevo.";
  }
  tipoMensaje.value = "info";
};

// Elimina un bloque previa confirmación del cuidador.
const eliminarHorario = async (horario) => {
  const resultado = await showConfirmAlert(
    "¿Eliminar este horario?",
    "Esta franja dejará de estar disponible y no podrán agendarte en ese momento.",
  );
  if (!resultado.isConfirmed) return;

  try {
    await eliminarHorarioApi(cuidadorId.value, horario.indice);
    await cargarHorarios(cuidadorId.value);

    // Si estábamos editando justo ese bloque, salimos del modo edición.
    if (modoEdicion.value) cancelarEdicion();

    await showSuccessAlert(
      "Horario eliminado",
      "La franja fue retirada de tu disponibilidad.",
    );
  } catch (error) {
    await showErrorAlert("No se pudo eliminar el horario", error.message);
  }
};

// Formatea un bloque para mostrarlo en la lista (ej: "vie 20 jun · 08:00 – 12:00").
const formatearRango = (horario) => {
  const inicio = new Date(horario.fechaInicio);
  const fin = new Date(horario.fechaFin);
  const fecha = inicio.toLocaleDateString("es-VE", {
    weekday: "short",
    day: "2-digit",
    month: "short",
  });
  const opcionesHora = { hour: "2-digit", minute: "2-digit" };
  const hi = inicio.toLocaleTimeString("es-VE", opcionesHora);
  const hf = fin.toLocaleTimeString("es-VE", opcionesHora);
  return `${fecha} · ${hi} – ${hf}`;
};

// Devuelve la duración del bloque en formato corto (ej: "2h 30m", "1h", "45m").
const formatearDuracion = (horario) => {
  const ms = new Date(horario.fechaFin) - new Date(horario.fechaInicio);
  const minutos = Math.round(ms / 60000);
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  if (horas && resto) return `${horas}h ${resto}m`;
  if (horas) return `${horas}h`;
  return `${resto}m`;
};
</script>

<template>
  <div class="agenda-layout">
    <div class="header-section">
      <h1 class="page-title">Gestión de Agenda</h1>
      <p>
        Selecciona un día en el calendario interactivo para declarar tus bloques
        de disponibilidad como cuidador. Asegúrate de que los horarios no se
        solapen con tus bloques previamente registrados. El sistema validará
        automáticamente y te notificará si hay algún conflicto.
      </p>
    </div>

    <div class="grid-agenda">
      <div class="panel-control" ref="panelRef">
        <h3>{{ modoEdicion ? "Editar Bloque" : "Declarar Disponibilidad" }}</h3>

        <div v-if="modoEdicion" class="banner-edicion">
          ✏️ Estás editando un bloque existente. Ajusta las horas y pulsa
          <strong>Actualizar</strong>, o <strong>Cancelar</strong> para volver.
        </div>

        <div class="campo">
          <label>Fecha Elegida:</label>
          <input
            type="date"
            v-model="fechaSeleccionada"
            readonly
            class="input-disabled"
          />
        </div>

        <div class="campo-doble">
          <div class="campo">
            <label>Hora Inicio:</label>
            <select v-model="horaInicio">
              <option value="" disabled>--:--</option>
              <option
                v-for="hora in horasInicio"
                :key="hora.value"
                :value="hora.value"
              >
                {{ hora.label }}
              </option>
            </select>
          </div>

          <div class="campo">
            <label>Hora Fin:</label>
            <select v-model="horaFin">
              <option value="" disabled>--:--</option>
              <option
                v-for="hora in horasFin"
                :key="hora.value"
                :value="hora.value"
              >
                {{ hora.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="acciones-form">
          <button @click="guardarHorario" class="btn-guardar">
            {{ modoEdicion ? "Actualizar Bloque" : "Guardar Bloque Disponible" }}
          </button>
          <button
            v-if="modoEdicion"
            @click="cancelarEdicion"
            class="btn-cancelar"
          >
            Cancelar
          </button>
        </div>

        <div v-if="mensaje" :class="['alert-box', `alert-${tipoMensaje}`]">
          {{ mensaje }}
        </div>
      </div>

      <div class="calendar-wrapper">
        <FullCalendar :options="calendarOptions" />
      </div>
    </div>

    <div class="lista-horarios" v-if="cuidadorId">
      <h3>Mis Bloques Registrados</h3>

      <p v-if="horariosRegistrados.length === 0" class="vacio">
        Aún no tienes bloques de disponibilidad registrados.
      </p>

      <ul v-else>
        <li
          v-for="horario in horariosRegistrados"
          :key="horario.indice"
          class="horario-item"
          :class="{ 'horario-editando': modoEdicion && indiceEditando === horario.indice }"
        >
          <span class="horario-rango">
            {{ formatearRango(horario) }}
            <span class="horario-duracion">{{ formatearDuracion(horario) }}</span>
          </span>
          <div class="horario-acciones">
            <button class="btn-editar" @click="iniciarEdicion(horario)">
              Editar
            </button>
            <button class="btn-eliminar" @click="eliminarHorario(horario)">
              Eliminar
            </button>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.agenda-layout {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.header-section h1 {
  color: var(--color-heading);
}

.header-section p {
  color: var(--color-text);
  line-height: 1.6;
}

.grid-agenda {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
}

.panel-control {
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 25px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: fit-content;
}

.panel-control h3 {
  color: var(--color-heading);
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.campo label {
  color: var(--color-text);
  font-size: 14px;
}

.campo-doble {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

input,
select {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-heading);
}

.input-disabled {
  opacity: 0.8;
  cursor: not-allowed;
  background-color: var(--color-background-mute);
}

.btn-guardar {
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

.btn-guardar:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.32);
}

.acciones-form {
  display: flex;
  gap: 10px;
}

.acciones-form .btn-guardar {
  flex: 1;
}

.btn-cancelar {
  padding: 12px 18px;
  background-color: transparent;
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-cancelar:hover {
  background-color: var(--color-background-mute);
}

.lista-horarios {
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 25px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.lista-horarios h3 {
  color: var(--color-heading);
  margin-bottom: 15px;
}

.lista-horarios .vacio {
  color: var(--color-text);
  opacity: 0.7;
  font-size: 14px;
}

.lista-horarios ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.horario-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-background);
}

.horario-editando {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary);
}

.horario-rango {
  color: var(--color-heading);
  font-weight: 500;
  text-transform: capitalize;
  display: flex;
  align-items: center;
  gap: 10px;
}

.horario-duracion {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  background-color: rgba(16, 185, 129, 0.12);
  border: 1px solid rgba(16, 185, 129, 0.3);
  padding: 2px 8px;
  border-radius: 999px;
  text-transform: none;
}

.banner-edicion {
  background-color: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.35);
  color: var(--color-text);
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.4;
}

.horario-acciones {
  display: flex;
  gap: 8px;
}

.btn-editar,
.btn-eliminar {
  padding: 7px 14px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.btn-editar {
  background-color: #3b82f6;
  color: white;
}

.btn-eliminar {
  background-color: #ef4444;
  color: white;
}

.btn-editar:hover,
.btn-eliminar:hover {
  opacity: 0.85;
}

.alert-box {
  padding: 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
}

.alert-error {
  background-color: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.alert-exito {
  background-color: rgba(16, 185, 129, 0.15);
  color: var(--color-primary);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.alert-info {
  background-color: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.calendar-wrapper {
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 20px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

:deep(.fc) {
  --fc-border-color: var(--color-border);
  --fc-daygrid-event-dot-color: var(--color-primary);
  color: var(--color-heading);
}

:deep(.fc-col-header-cell-cushion),
:deep(.fc-daygrid-day-number) {
  color: var(--color-text);
  text-decoration: none;
}

:deep(.fc-toolbar-title) {
  font-size: 18px !important;
  color: var(--color-heading);
}

:deep(.fc-button-primary) {
  background-color: var(--color-background) !important;
  border-color: var(--color-border) !important;
}

:deep(.fc-button-active) {
  background-color: var(--color-primary) !important;
}

.id-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.id-section label {
  font-weight: 600;
  color: var(--color-text);
  white-space: nowrap;
}

.id-section small {
  color: #f59e0b;
  font-size: 12px;
}

.input-id {
  flex: 1;
  min-width: 260px;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-heading);
}

@media (max-width: 968px) {
  .grid-agenda {
    grid-template-columns: 1fr;
  }
}
</style>
