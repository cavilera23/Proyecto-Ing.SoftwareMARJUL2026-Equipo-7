<script setup>
import { ref } from "vue";

import {
  showSuccessAlert,
  showErrorAlert,
  showWarningAlert,
} from "@/components/modals/alerts";

const API_URL = "http://localhost:8080";

// Estado del formulario
const formData = ref({
  tipoUsuario: "PADRE",
  nombre: "",
  correo: "",
  telefono: "",

  // Campos exclusivos del cuidador
  tarifaHora: 0,
  habilidades: [],
  rutaDocumentoAntecedentes: "",
});

const mensaje = ref("");
const tipoMensaje = ref("");
const cargando = ref(false);

// Manejo del archivo simulado para cumplir con PDF/JPG/PNG
const handleFileUpload = (event) => {
  const file = event.target.files[0];

  if (!file) {
    formData.value.rutaDocumentoAntecedentes = "";
    return;
  }

  const tiposPermitidos = ["application/pdf", "image/jpeg", "image/png"];

  if (!tiposPermitidos.includes(file.type)) {
    formData.value.rutaDocumentoAntecedentes = "";

    showWarningAlert(
      "Archivo no válido",
      "El documento de antecedentes debe ser PDF, JPG o PNG.",
    );

    event.target.value = "";
    return;
  }

  // Como estamos guardando en JSON, solo enviamos el nombre del archivo
  formData.value.rutaDocumentoAntecedentes = file.name;
};

const limpiarFormulario = () => {
  formData.value = {
    tipoUsuario: "PADRE",
    nombre: "",
    correo: "",
    telefono: "",
    tarifaHora: 0,
    habilidades: [],
    rutaDocumentoAntecedentes: "",
  };
};

// Función para enviar los datos al backend
const registrarPerfil = async () => {
  mensaje.value = "";
  tipoMensaje.value = "";

  if (!formData.value.nombre.trim()) {
    await showWarningAlert(
      "Nombre requerido",
      "Debes ingresar tu nombre completo.",
    );
    return;
  }

  if (!formData.value.correo.trim()) {
    await showWarningAlert(
      "Correo requerido",
      "Debes ingresar un correo electrónico.",
    );
    return;
  }

  if (!formData.value.telefono.trim()) {
    await showWarningAlert(
      "Teléfono requerido",
      "Debes ingresar un número de teléfono.",
    );
    return;
  }

  if (formData.value.tipoUsuario === "CUIDADOR") {
    if (Number(formData.value.tarifaHora) <= 0) {
      await showWarningAlert(
        "Tarifa inválida",
        "La tarifa por hora debe ser mayor a 0.",
      );
      return;
    }

    if (formData.value.habilidades.length === 0) {
      await showWarningAlert(
        "Habilidades requeridas",
        "Debes seleccionar al menos una habilidad de cuidado.",
      );
      return;
    }

    if (!formData.value.rutaDocumentoAntecedentes) {
      await showWarningAlert(
        "Documento requerido",
        "Debes cargar tu documento de antecedentes en formato PDF, JPG o PNG.",
      );
      return;
    }
  }

  cargando.value = true;

  const payload = {
    ...formData.value,
    tarifaHora: Number(formData.value.tarifaHora),
  };

  console.log("Enviando perfil al backend:", payload);

  try {
    const respuesta = await fetch(`${API_URL}/api/v1/perfiles`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    if (!respuesta.ok) {
      const errorTexto = await respuesta.text();
      throw new Error(errorTexto || "Error en las validaciones del registro.");
    }

    const data = await respuesta.json();

    mensaje.value = `¡Registro exitoso! Bienvenido al ecosistema CuidaRed, ${data.nombre}.`;
    tipoMensaje.value = "exito";

    await showSuccessAlert(
      "Registro exitoso",
      `Bienvenido al ecosistema CuidaRed, ${data.nombre}.`,
    );

    limpiarFormulario();
  } catch (error) {
    console.error("Error al registrar:", error);

    mensaje.value = error.message;
    tipoMensaje.value = "error";

    await showErrorAlert("No se pudo completar el registro", error.message);
  } finally {
    cargando.value = false;
  }
};
</script>

<template>
  <div class="perfil-view">
    <div class="header-section">
      <h1>Completar Perfil</h1>
      <p>Regístrate en la comunidad para solicitar u ofrecer asistencia.</p>
    </div>

    <form @submit.prevent="registrarPerfil" class="formulario">
      <div class="campo">
        <label>Quiero registrarme como:</label>
        <select v-model="formData.tipoUsuario" required>
          <option value="PADRE">Padre / Tutor (Buscar Cuidador)</option>
          <option value="CUIDADOR">Cuidador (Ofrecer Asistencia)</option>
        </select>
      </div>

      <div class="campo">
        <label>Nombre Completo:</label>
        <input
          type="text"
          v-model="formData.nombre"
          placeholder="Ej: Mauricio Caldera"
          required
        />
      </div>

      <div class="campo">
        <label>Correo Electrónico:</label>
        <input
          type="email"
          v-model="formData.correo"
          placeholder="Ej: correo@gmail.com"
          required
        />
      </div>

      <div class="campo">
        <label>Teléfono:</label>
        <input
          type="text"
          v-model="formData.telefono"
          placeholder="Ej: +584121234567"
          required
        />
      </div>

      <div v-if="formData.tipoUsuario === 'CUIDADOR'" class="seccion-cuidador">
        <h3>Datos de Cuidador</h3>

        <div class="campo">
          <label>Tarifa por hora ($):</label>
          <input
            type="number"
            v-model="formData.tarifaHora"
            min="0"
            step="0.01"
            required
          />
        </div>

        <div class="campo">
          <label>Habilidades de Cuidado:</label>

          <div class="checkbox-group">
            <label class="checkbox-item">
              <input
                type="checkbox"
                value="NINO"
                v-model="formData.habilidades"
              />
              Niños
            </label>

            <label class="checkbox-item">
              <input
                type="checkbox"
                value="ADULTO_MAYOR"
                v-model="formData.habilidades"
              />
              Adultos Mayores
            </label>

            <label class="checkbox-item">
              <input
                type="checkbox"
                value="MASCOTA"
                v-model="formData.habilidades"
              />
              Mascotas
            </label>
          </div>
        </div>

        <div class="campo">
          <label>Documento de Antecedentes (PDF, JPG, PNG):</label>
          <input
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            @change="handleFileUpload"
            required
          />

          <small v-if="formData.rutaDocumentoAntecedentes">
            Archivo seleccionado: {{ formData.rutaDocumentoAntecedentes }}
          </small>
        </div>
      </div>

      <button type="submit" :disabled="cargando">
        {{ cargando ? "Registrando..." : "Registrar Perfil" }}
      </button>
    </form>

    <div
      v-if="mensaje"
      :class="[
        'mensaje',
        tipoMensaje === 'error' ? 'mensaje-error' : 'mensaje-exito',
      ]"
    >
      {{ mensaje }}
    </div>
  </div>
</template>

<style scoped>
.perfil-view {
  max-width: 560px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  margin-bottom: 24px;
}

.header-section h1 {
  color: var(--color-heading);
  margin-bottom: 8px;
}

.header-section p {
  color: var(--color-text);
  line-height: 1.5;
}

.formulario {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 24px;
  border-radius: 12px;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.campo label {
  color: var(--color-text);
  font-size: 14px;
  font-weight: 500;
}

.campo small {
  color: var(--color-text);
  opacity: 0.85;
  font-size: 12px;
}

.seccion-cuidador {
  background-color: var(--color-background);
  padding: 16px;
  border-radius: 10px;
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.seccion-cuidador h3 {
  color: var(--color-heading);
  margin: 0;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text);
  font-size: 14px;
}

input,
select {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-heading);
}

input[type="file"] {
  padding: 8px;
}

button {
  padding: 12px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 700;
  transition:
    background 0.2s ease,
    opacity 0.2s ease;
}

button:hover {
  background-color: var(--color-primary-hover);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.mensaje {
  margin-top: 18px;
  padding: 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
}

.mensaje-exito {
  background-color: rgba(16, 185, 129, 0.15);
  color: var(--color-primary);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.mensaje-error {
  background-color: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

@media (max-width: 640px) {
  .perfil-view {
    padding: 12px;
  }

  .formulario {
    padding: 18px;
  }

  .checkbox-group {
    flex-direction: column;
  }
}
</style>
