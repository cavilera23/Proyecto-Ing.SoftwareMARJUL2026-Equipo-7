<script setup>
import { ref, computed } from "vue";
import { useRouter, useRoute, RouterLink } from "vue-router";
import { loginApi } from "@/services/authService";
import { auth } from "@/stores/auth";

const router = useRouter();
const route = useRoute();

// Si el usuario llegó aquí por intentar entrar a una ruta protegida sin sesión,
// el router agrega ?auth=required y mostramos el aviso.
const avisoSesion = computed(() => route.query.auth === "required");

const correo = ref("");
const contrasena = ref("");
const cargando = ref(false);
const error = ref("");

const iniciarSesion = async () => {
  error.value = "";

  if (!correo.value.trim() || !contrasena.value) {
    error.value = "Ingresa tu correo y tu contraseña.";
    return;
  }

  cargando.value = true;
  try {
    const { token, usuario } = await loginApi(correo.value.trim(), contrasena.value);
    auth.iniciarSesion(token, usuario);
    // Dirige al dashboard según el rol (CUIDADOR o PADRE).
    router.push(auth.rutaDashboard());
  } catch (e) {
    // El backend responde { "error": "..." }
    let msg = "No se pudo iniciar sesión. Inténtalo de nuevo.";
    try {
      msg = JSON.parse(e.message).error || msg;
    } catch {
      /* el cuerpo no era JSON; dejamos el mensaje genérico */
    }
    error.value = msg;
  } finally {
    cargando.value = false;
  }
};
</script>

<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-logo">
        <span class="logo-icono">🏡</span>
        <span class="logo-texto">Cuida<span class="green-text">Red</span></span>
      </div>

      <h1>Iniciar sesión</h1>
      <p class="subtitulo">Accede para gestionar tus cuidados.</p>

      <div v-if="avisoSesion" class="login-aviso">
        Debes iniciar sesión para acceder a esa página.
      </div>

      <form @submit.prevent="iniciarSesion" class="login-form">
        <div class="campo">
          <label>Correo electrónico</label>
          <input
            type="email"
            v-model="correo"
            placeholder="tucorreo@ejemplo.com"
            autocomplete="email"
          />
        </div>

        <div class="campo">
          <label>Contraseña</label>
          <input
            type="password"
            v-model="contrasena"
            placeholder="••••••••"
            autocomplete="current-password"
          />
        </div>

        <div v-if="error" class="login-error">{{ error }}</div>

        <button type="submit" class="btn-login" :disabled="cargando">
          {{ cargando ? "Ingresando…" : "Entrar" }}
        </button>
      </form>

      <p class="registro-link">
        ¿No tienes cuenta?
        <RouterLink to="/registro">Regístrate aquí</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 35px 30px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.login-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-heading);
  justify-content: center;
  margin-bottom: 10px;
}

.green-text {
  color: var(--color-primary);
}

.login-card h1 {
  color: var(--color-heading);
  font-size: 22px;
  text-align: center;
}

.subtitulo {
  color: var(--color-text);
  text-align: center;
  font-size: 14px;
  margin-bottom: 15px;
}

.login-form {
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
}

.campo input {
  padding: 11px;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background-color: var(--color-background);
  color: var(--color-heading);
}

.btn-login {
  margin-top: 4px;
  padding: 12px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-login:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-error {
  background-color: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
}

.login-aviso {
  background-color: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 15px;
}

.registro-link {
  text-align: center;
  color: var(--color-text);
  font-size: 14px;
  margin-top: 18px;
}

.registro-link a {
  color: var(--color-primary);
  font-weight: 600;
}
</style>
