<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";

import { auth } from "@/stores/auth";
import {
  obtenerMiPerfilApi,
  modificarMiPerfilApi,
  eliminarMiPerfilApi,
} from "@/services/perfilService";
import {
  showSuccessAlert,
  showErrorAlert,
  showWarningAlert,
  showConfirmAlert,
} from "@/components/modals/alerts";

const router = useRouter();

// Etiquetas legibles para las habilidades del cuidador.
const HABILIDADES_DISPONIBLES = [
  { valor: "NINO", etiqueta: "Niños" },
  { valor: "ADULTO_MAYOR", etiqueta: "Adultos Mayores" },
  { valor: "MASCOTA", etiqueta: "Mascotas" },
];

const etiquetaHabilidad = (valor) =>
  HABILIDADES_DISPONIBLES.find((h) => h.valor === valor)?.etiqueta || valor;

const perfil = ref(null);
const cargando = ref(true);
const guardando = ref(false);
const eliminando = ref(false);
const error = ref("");
const editando = ref(false);

const esCuidador = computed(() => perfil.value?.tipoUsuario === "CUIDADOR");

// Borrador editable independiente del perfil cargado, para poder cancelar cambios.
const formulario = reactive({
  nombre: "",
  correo: "",
  telefono: "",
  tarifaHora: 0,
  habilidades: [],
});

const cargarPerfil = async () => {
  cargando.value = true;
  error.value = "";
  try {
    perfil.value = await obtenerMiPerfilApi();
  } catch (e) {
    error.value =
      "No se pudo cargar tu perfil. Inténtalo de nuevo más tarde.";
    console.error("Error al cargar el perfil:", e);
  } finally {
    cargando.value = false;
  }
};

const iniciarEdicion = () => {
  formulario.nombre = perfil.value.nombre || "";
  formulario.correo = perfil.value.correo || "";
  formulario.telefono = perfil.value.telefono || "";
  formulario.tarifaHora = perfil.value.tarifaHora || 0;
  formulario.habilidades = Array.isArray(perfil.value.habilidades)
    ? [...perfil.value.habilidades]
    : [];
  editando.value = true;
};

const cancelarEdicion = () => {
  editando.value = false;
};

const guardarCambios = async () => {
  if (!formulario.nombre.trim()) {
    await showWarningAlert("Nombre requerido", "Debes ingresar tu nombre.");
    return;
  }
  if (!formulario.correo.trim()) {
    await showWarningAlert(
      "Correo requerido",
      "Debes ingresar un correo electrónico.",
    );
    return;
  }
  if (!formulario.telefono.trim()) {
    await showWarningAlert(
      "Teléfono requerido",
      "Debes ingresar un número de teléfono.",
    );
    return;
  }

  // Solo enviamos los campos editables; el backend resuelve el usuario por el token.
  const cambios = {
    tipoUsuario: perfil.value.tipoUsuario,
    nombre: formulario.nombre.trim(),
    correo: formulario.correo.trim(),
    telefono: formulario.telefono.trim(),
  };

  if (esCuidador.value) {
    if (Number(formulario.tarifaHora) <= 0) {
      await showWarningAlert(
        "Tarifa inválida",
        "La tarifa por hora debe ser mayor a 0.",
      );
      return;
    }
    if (formulario.habilidades.length === 0) {
      await showWarningAlert(
        "Habilidades requeridas",
        "Debes seleccionar al menos una habilidad de cuidado.",
      );
      return;
    }
    cambios.tarifaHora = Number(formulario.tarifaHora);
    cambios.habilidades = [...formulario.habilidades];
  }

  guardando.value = true;
  try {
    const actualizado = await modificarMiPerfilApi(cambios);
    perfil.value = actualizado;
    // Mantenemos el nombre/rol que muestra el navbar en sincronía con la sesión.
    auth.actualizarUsuario(actualizado);
    editando.value = false;
    await showSuccessAlert(
      "Perfil actualizado",
      "Tus datos se guardaron correctamente.",
    );
  } catch (e) {
    let msg = "No se pudieron guardar los cambios.";
    try {
      msg = JSON.parse(e.message).error || msg;
    } catch {
      /* el cuerpo no era JSON; dejamos el mensaje genérico */
    }
    await showErrorAlert("Error al guardar", msg);
  } finally {
    guardando.value = false;
  }
};

const eliminarPerfil = async () => {
  const { isConfirmed } = await showConfirmAlert(
    "Eliminar perfil",
    "Esta acción es permanente y cerrará tu sesión. ¿Deseas continuar?",
  );
  if (!isConfirmed) return;

  eliminando.value = true;
  try {
    await eliminarMiPerfilApi();
    auth.cerrarSesion();
    await showSuccessAlert(
      "Perfil eliminado",
      "Tu perfil se eliminó correctamente.",
    );
    router.push("/login");
  } catch (e) {
    let msg = "No se pudo eliminar el perfil.";
    try {
      msg = JSON.parse(e.message).error || msg;
    } catch {
      /* el cuerpo no era JSON; dejamos el mensaje genérico */
    }
    await showErrorAlert("Error al eliminar", msg);
  } finally {
    eliminando.value = false;
  }
};

onMounted(cargarPerfil);
</script>

<template>
  <div class="perfil-view">
    <div class="header-section">
      <h1>Mi Perfil</h1>
      <p>Consulta, actualiza o elimina la información de tu cuenta.</p>
    </div>

    <div v-if="cargando" class="estado">Cargando tu perfil…</div>

    <div v-else-if="error" class="mensaje mensaje-error">
      {{ error }}
      <button class="btn-secundario" @click="cargarPerfil">Reintentar</button>
    </div>

    <div v-else-if="perfil" class="tarjeta">
      <!-- MODO LECTURA -->
      <template v-if="!editando">
        <div class="rol-badge">
          {{ esCuidador ? "Cuidador" : "Padre / Tutor" }}
        </div>

        <div class="dato">
          <span class="etiqueta">Nombre</span>
          <span class="valor">{{ perfil.nombre }}</span>
        </div>

        <div class="dato">
          <span class="etiqueta">Correo</span>
          <span class="valor">{{ perfil.correo }}</span>
        </div>

        <div class="dato">
          <span class="etiqueta">Teléfono</span>
          <span class="valor">{{ perfil.telefono }}</span>
        </div>

        <div class="dato">
          <span class="etiqueta">Calificación promedio</span>
          <span class="valor"
            >⭐ {{ (perfil.calificacionPromedio ?? 0).toFixed(1) }}</span
          >
        </div>

        <template v-if="esCuidador">
          <div class="dato">
            <span class="etiqueta">Tarifa por hora</span>
            <span class="valor">${{ perfil.tarifaHora }}</span>
          </div>

          <div class="dato">
            <span class="etiqueta">Disponible</span>
            <span class="valor">{{ perfil.disponible ? "Sí" : "No" }}</span>
          </div>

          <div class="dato">
            <span class="etiqueta">Habilidades</span>
            <span class="valor">
              <span
                v-for="h in perfil.habilidades"
                :key="h"
                class="chip"
                >{{ etiquetaHabilidad(h) }}</span
              >
              <span v-if="!perfil.habilidades?.length">—</span>
            </span>
          </div>

          <div class="dato" v-if="perfil.rutaDocumentoAntecedentes">
            <span class="etiqueta">Documento de antecedentes</span>
            <span class="valor">{{ perfil.rutaDocumentoAntecedentes }}</span>
          </div>
        </template>

        <div class="acciones">
          <button class="btn-primario" @click="iniciarEdicion">
            Editar perfil
          </button>
          <button
            class="btn-peligro"
            :disabled="eliminando"
            @click="eliminarPerfil"
          >
            {{ eliminando ? "Eliminando…" : "Eliminar perfil" }}
          </button>
        </div>
      </template>

      <!-- MODO EDICIÓN -->
      <form v-else class="formulario" @submit.prevent="guardarCambios">
        <div class="campo">
          <label>Nombre Completo</label>
          <input type="text" v-model="formulario.nombre" required />
        </div>

        <div class="campo">
          <label>Correo Electrónico</label>
          <input type="email" v-model="formulario.correo" required />
        </div>

        <div class="campo">
          <label>Teléfono</label>
          <input type="text" v-model="formulario.telefono" required />
        </div>

        <div v-if="esCuidador" class="seccion-cuidador">
          <h3>Datos de Cuidador</h3>

          <div class="campo">
            <label>Tarifa por hora ($)</label>
            <input
              type="number"
              v-model="formulario.tarifaHora"
              min="0"
              step="0.01"
            />
          </div>

          <div class="campo">
            <label>Habilidades de Cuidado</label>
            <div class="checkbox-group">
              <label
                v-for="h in HABILIDADES_DISPONIBLES"
                :key="h.valor"
                class="checkbox-item"
              >
                <input
                  type="checkbox"
                  :value="h.valor"
                  v-model="formulario.habilidades"
                />
                {{ h.etiqueta }}
              </label>
            </div>
          </div>
        </div>

        <div class="acciones">
          <button class="btn-primario" type="submit" :disabled="guardando">
            {{ guardando ? "Guardando…" : "Guardar cambios" }}
          </button>
          <button
            class="btn-secundario"
            type="button"
            :disabled="guardando"
            @click="cancelarEdicion"
          >
            Cancelar
          </button>
        </div>
      </form>
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

.estado {
  color: var(--color-text);
  padding: 16px 0;
}

.tarjeta {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 24px;
  border-radius: 12px;
}

.rol-badge {
  align-self: flex-start;
  background-color: rgba(16, 185, 129, 0.15);
  color: var(--color-primary);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 999px;
  padding: 4px 14px;
  font-size: 13px;
  font-weight: 600;
}

.dato {
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 12px;
}

.dato:last-of-type {
  border-bottom: none;
}

.etiqueta {
  color: var(--color-text);
  font-size: 13px;
  opacity: 0.8;
}

.valor {
  color: var(--color-heading);
  font-size: 16px;
  font-weight: 500;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 13px;
}

.formulario {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

input {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-heading);
}

.acciones {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
}

button {
  padding: 12px 18px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 700;
  transition:
    background 0.2s ease,
    opacity 0.2s ease;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-primario {
  background-color: var(--color-primary);
  color: white;
}

.btn-primario:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-secundario {
  background-color: transparent;
  color: var(--color-heading);
  border: 1px solid var(--color-border);
}

.btn-secundario:hover:not(:disabled) {
  background-color: rgba(255, 255, 255, 0.05);
}

.btn-peligro {
  background-color: transparent;
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.4);
}

.btn-peligro:hover:not(:disabled) {
  background-color: rgba(239, 68, 68, 0.12);
}

.mensaje {
  padding: 12px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

  .tarjeta {
    padding: 18px;
  }

  .checkbox-group {
    flex-direction: column;
  }
}
</style>
