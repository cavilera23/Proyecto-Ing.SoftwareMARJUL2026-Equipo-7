<script setup>
import { ref, computed } from "vue";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";

import {
  showSuccessAlert,
  showErrorAlert,
  showWarningAlert,
} from "@/components/modals/alerts";

const API_URL = "http://localhost:8080";

const CUIDADOR_ID = "faf49510-c57a-4d68-aa84-2027c0c20515";

const fechaSeleccionada = ref("");
const horaInicio = ref("");
const horaFin = ref("");
const mensaje = ref("");
const tipoMensaje = ref("");

const calendarEvents = ref([]);

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
    fechaSeleccionada.value = info.dateStr;
    mensaje.value = "";
    tipoMensaje.value = "";
  },
}));

const registrarHorario = async () => {
  mensaje.value = "";
  tipoMensaje.value = "";

  if (!fechaSeleccionada.value || !horaInicio.value || !horaFin.value) {
    await showWarningAlert(
      "Campos incompletos",
      "Debes seleccionar una fecha, una hora de inicio y una hora de fin.",
    );
    return;
  }

  if (horaInicio.value >= horaFin.value) {
    await showWarningAlert(
      "Horario inválido",
      "La hora de inicio debe ser anterior a la hora de fin.",
    );
    return;
  }

  const startIso = `${fechaSeleccionada.value}T${horaInicio.value}:00`;
  const endIso = `${fechaSeleccionada.value}T${horaFin.value}:00`;

  const horario = {
    fechaInicio: startIso,
    fechaFin: endIso,
  };

  console.log("Enviando horario al backend:", horario);

  try {
    const respuesta = await fetch(
      `${API_URL}/api/v1/agenda/horarios/${CUIDADOR_ID}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(horario),
      },
    );

    if (!respuesta.ok) {
      const errorTexto = await respuesta.text();
      throw new Error(errorTexto || "Error al registrar el horario.");
    }

    const data = await respuesta.json();

    calendarEvents.value = [
      ...calendarEvents.value,
      {
        title: "Disponible",
        start: data.fechaInicio,
        end: data.fechaFin,
        color: "#10b981",
      },
    ];

    mensaje.value = "¡Bloque de horario registrado con éxito!";
    tipoMensaje.value = "exito";

    await showSuccessAlert(
      "Disponibilidad registrada",
      "El bloque de horario fue guardado exitosamente.",
    );

    horaInicio.value = "";
    horaFin.value = "";
  } catch (error) {
    console.error("Error registrando horario:", error);

    mensaje.value = `Error: ${error.message}`;
    tipoMensaje.value = "error";

    await showErrorAlert("No se pudo registrar el horario", error.message);
  }
};
</script>

<template>
  <div class="agenda-layout">
    <div class="header-section">
      <h1>Gestión de Agenda</h1>
      <p>
        Selecciona un día en el calendario interactivo para declarar tus bloques
        de disponibilidad como cuidador. Asegúrate de que los horarios no se
        solapen con tus bloques previamente registrados. El sistema validará
        automáticamente y te notificará si hay algún conflicto.
      </p>
    </div>

    <div class="grid-agenda">
      <div class="panel-control">
        <h3>Declarar Disponibilidad</h3>

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

        <button @click="registrarHorario" class="btn-guardar">
          Guardar Bloque Disponible
        </button>

        <div
          v-if="mensaje"
          :class="[
            'alert-box',
            tipoMensaje === 'error' ? 'alert-error' : 'alert-exito',
          ]"
        >
          {{ mensaje }}
        </div>
      </div>

      <div class="calendar-wrapper">
        <FullCalendar :options="calendarOptions" />
      </div>
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
  border-radius: 12px;
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
  padding: 12px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-guardar:hover {
  background-color: var(--color-primary-hover);
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

.calendar-wrapper {
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 20px;
  border-radius: 12px;
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

@media (max-width: 968px) {
  .grid-agenda {
    grid-template-columns: 1fr;
  }
}
</style>
