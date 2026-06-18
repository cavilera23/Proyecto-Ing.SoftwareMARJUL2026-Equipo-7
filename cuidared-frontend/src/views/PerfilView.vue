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
      <h1 class="page-title">Mi Perfil</h1>
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
        <div class="perfil-hero">
          <span class="perfil-avatar">{{ (perfil.nombre || "?").trim().charAt(0).toUpperCase() }}</span>
          <div class="perfil-hero-info">
            <h2>{{ perfil.nombre }}</h2>
            <span class="rol-badge">{{ esCuidador ? "Cuidador" : "Padre / Tutor" }}</span>
          </div>
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
  max-width: 580px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  margin-bottom: 24px;
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

.estado {
  color: var(--color-text);
  padding: 16px 0;
  text-align: center;
}

.tarjeta {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  padding: 30px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  animation: fadeUp 0.4s ease both;
}

/* Cabecera del perfil con avatar */
.perfil-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-border);
}

.perfil-avatar {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--grad-primary);
  color: #fff;
  font-size: 28px;
  font-weight: 800;
  font-family: var(--font-display);
  box-shadow: var(--shadow-primary);
}

.perfil-hero-info h2 {
  margin: 0 0 6px;
  font-size: 1.4rem;
  color: var(--color-heading);
}

.rol-badge {
  align-self: flex-start;
  background-color: var(--color-primary-tint);
  color: var(--color-primary-hover);
  border: 1px solid var(--color-primary-soft);
  border-radius: var(--radius-full);
  padding: 4px 14px;
  font-size: 13px;
  font-weight: 600;
}

.dato {
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 14px;
}

.dato:last-of-type {
  border-bottom: none;
}

.etiqueta {
  color: var(--color-muted);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.4px;
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
  background-color: var(--color-primary-tint);
  color: var(--color-primary-hover);
  border: 1px solid var(--color-primary-soft);
  border-radius: var(--radius-full);
  padding: 3px 12px;
  font-size: 13px;
  font-weight: 600;
}

.formulario {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.seccion-cuidador {
  background-color: var(--color-primary-tint);
  padding: 20px;
  border-radius: var(--radius);
  border: 1px solid var(--color-primary-soft);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.seccion-cuidador h3 {
  color: var(--color-heading);
  margin: 0;
  font-size: 1.05rem;
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
  transition: border-color var(--t-fast);
}

.checkbox-item:hover {
  border-color: var(--color-primary);
}

.checkbox-item input {
  accent-color: var(--color-primary);
  width: 16px;
  height: 16px;
}

input {
  width: 100%;
}

.acciones {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
}

button {
  padding: 12px 20px;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-weight: 700;
  transition: transform var(--t-fast), box-shadow var(--t-fast), background var(--t-fast);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-primario {
  background: var(--grad-primary);
  color: white;
  box-shadow: var(--shadow-primary);
}

.btn-primario:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.32);
}

.btn-secundario {
  background-color: var(--color-background-mute);
  color: var(--color-heading);
  border: 1px solid var(--color-border);
}

.btn-secundario:hover:not(:disabled) {
  border-color: var(--color-border-hover);
}

.btn-peligro {
  background-color: transparent;
  color: var(--color-danger);
  border: 1px solid var(--color-danger-soft);
}

.btn-peligro:hover:not(:disabled) {
  background-color: var(--color-danger-soft);
}

.mensaje {
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

  .tarjeta {
    padding: 18px;
  }

  .checkbox-group {
    flex-direction: column;
  }
}
</style>
