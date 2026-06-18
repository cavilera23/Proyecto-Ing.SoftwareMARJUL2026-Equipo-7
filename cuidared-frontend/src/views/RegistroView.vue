<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

import {
  showSuccessAlert,
  showErrorAlert,
  showWarningAlert,
} from "@/components/modals/alerts";

const router = useRouter();

const API_URL = "http://localhost:8080";

// Estado del formulario
const formData = ref({
  tipoUsuario: "PADRE",
  nombre: "",
  correo: "",
  telefono: "",
  contrasena: "",

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
    contrasena: "",
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

  if (!formData.value.contrasena || formData.value.contrasena.length < 4) {
    await showWarningAlert(
      "Contraseña requerida",
      "Debes ingresar una contraseña de al menos 4 caracteres.",
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
      `Bienvenido al ecosistema CuidaRed, ${data.nombre}. Ya puedes iniciar sesión.`,
    );

    limpiarFormulario();
    // Tras registrarse, lo llevamos al login para que entre con su correo y clave.
    router.push("/login");
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
      <h1 class="page-title">Crea tu cuenta</h1>
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

      <div class="campo">
        <label>Contraseña:</label>
        <input
          type="password"
          v-model="formData.contrasena"
          placeholder="Mínimo 4 caracteres"
          autocomplete="new-password"
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
  max-width: 580px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  margin-bottom: 26px;
  text-align: center;
}

.header-section h1 {
  font-size: 2.4rem;
  margin-bottom: 8px;
}

.header-section p {
  color: var(--color-text);
  line-height: 1.5;
}

.formulario {
  display: flex;
  flex-direction: column;
  gap: 18px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 30px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  animation: fadeUp 0.4s ease both;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.campo label {
  color: var(--color-heading);
  font-size: 13px;
  font-weight: 600;
}

.campo small {
  color: var(--color-primary-hover);
  font-weight: 500;
  font-size: 12px;
}

.seccion-cuidador {
  background-color: var(--color-primary-tint);
  padding: 22px;
  border-radius: var(--radius);
  border: 1px solid var(--color-primary-soft);
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: fadeUp 0.35s ease both;
}

.seccion-cuidador h3 {
  color: var(--color-heading);
  margin: 0;
  font-size: 1.1rem;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-heading);
  font-size: 14px;
  font-weight: 500;
  background: #fff;
  border: 1px solid var(--color-border);
  padding: 9px 14px;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: border-color var(--t-fast), box-shadow var(--t-fast);
}

.checkbox-item:hover {
  border-color: var(--color-primary);
}

.checkbox-item input {
  accent-color: var(--color-primary);
  width: 16px;
  height: 16px;
}

input,
select {
  width: 100%;
}

input[type="file"] {
  padding: 9px;
  background: #fff;
}

button[type="submit"] {
  padding: 14px;
  background: var(--grad-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-weight: 700;
  font-size: 15px;
  box-shadow: var(--shadow-primary);
  transition: transform var(--t-fast), box-shadow var(--t-fast), opacity var(--t-fast);
}

button[type="submit"]:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.34);
}

button[type="submit"]:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  box-shadow: none;
}

.mensaje {
  margin-top: 18px;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 14px;
}

.mensaje-exito {
  background-color: var(--color-success-soft);
  color: var(--color-success);
  border: 1px solid #6ee7b7;
}

.mensaje-error {
  background-color: var(--color-danger-soft);
  color: var(--color-danger);
  border: 1px solid #fca5a5;
}

@media (max-width: 640px) {
  .perfil-view {
    padding: 12px;
  }

  .formulario {
    padding: 20px;
  }
}
</style>
