<script setup>
import { ref } from "vue";

// Estado del formulario
const formData = ref({
  tipoUsuario: "PADRE", // Opción por defecto
  nombre: "",
  correo: "",
  telefono: "",
  // Campos exclusivos del Cuidador
  tarifaHora: 0,
  habilidades: [],
  rutaDocumentoAntecedentes: "",
});

const mensaje = ref("");
const cargando = ref(false);

// Manejo del archivo simulado para cumplir con el formato PDF/JPG/PNG
const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    // Como estamos guardando en JSON, solo enviamos el nombre del archivo al backend
    formData.value.rutaDocumentoAntecedentes = file.name;
  }
};

// Función para enviar los datos al backend
const registrarPerfil = async () => {
  cargando.value = true;
  mensaje.value = "";

  try {
    const respuesta = await fetch("http://localhost:8080/api/v1/perfiles", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(formData.value),
    });

    if (!respuesta.ok) {
      // Si el backend lanza nuestra ReglaNegocioException, la atrapamos aquí
      const errorData = await respuesta.json().catch(() => ({}));
      throw new Error(
        errorData.message || "Error en las validaciones del registro.",
      );
    }

    const data = await respuesta.json();
    mensaje.value = `¡Registro exitoso! Bienvenido al ecosistema CuidaRed, ${data.nombre}.`;
  } catch (error) {
    mensaje.value = error.message;
    console.error("Error al registrar:", error);
  } finally {
    cargando.value = false;
  }
};
</script>

<template>
  <div class="perfil-view">
    <h1>Completar Perfil</h1>
    <p>Regístrate en la comunidad para solicitar u ofrecer asistencia.</p>

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
        <input type="text" v-model="formData.nombre" required />
      </div>

      <div class="campo">
        <label>Correo Electrónico:</label>
        <input type="email" v-model="formData.correo" required />
      </div>

      <div class="campo">
        <label>Teléfono:</label>
        <input type="text" v-model="formData.telefono" required />
      </div>

      <div v-if="formData.tipoUsuario === 'CUIDADOR'" class="seccion-cuidador">
        <h3>Datos de Cuidador</h3>

        <div class="campo">
          <label>Tarifa por hora ($):</label>
          <input type="number" v-model="formData.tarifaHora" min="0" required />
        </div>

        <div class="campo">
          <label>Habilidades de Cuidado (Selecciona al menos una):</label>
          <div class="checkbox-group">
            <label
              ><input
                type="checkbox"
                value="NINO"
                v-model="formData.habilidades"
              />
              Niños</label
            >
            <label
              ><input
                type="checkbox"
                value="ADULTO_MAYOR"
                v-model="formData.habilidades"
              />
              Adultos Mayores</label
            >
            <label
              ><input
                type="checkbox"
                value="MASCOTA"
                v-model="formData.habilidades"
              />
              Mascotas</label
            >
          </div>
        </div>

        <div class="campo">
          <label>Documento de Antecedentes (PDF, JPG, PNG):</label>
          <input
            type="file"
            accept=".pdf,.jpg,.png"
            @change="handleFileUpload"
            required
          />
        </div>
      </div>

      <button type="submit" :disabled="cargando">
        {{ cargando ? "Registrando..." : "Registrar Perfil" }}
      </button>
    </form>

    <div
      v-if="mensaje"
      :class="{
        'mensaje-exito': !mensaje.includes('Error'),
        'mensaje-error': mensaje.includes('Error'),
      }"
    >
      {{ mensaje }}
    </div>
  </div>
</template>

<style scoped>
.perfil-view {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}
.formulario {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.campo {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.seccion-cuidador {
  background-color: #2c2c2c;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #444;
}
.checkbox-group {
  display: flex;
  gap: 15px;
}
input,
select {
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
  background-color: #fff;
  color: #333;
}
button {
  padding: 12px;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
button:disabled {
  background-color: #2c7a56;
}
.mensaje-exito {
  margin-top: 20px;
  color: #42b883;
  font-weight: bold;
}
.mensaje-error {
  margin-top: 20px;
  color: #ff4d4d;
  font-weight: bold;
}
</style>
