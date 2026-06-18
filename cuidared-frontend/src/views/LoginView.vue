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
  min-height: 74vh;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 42px 36px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: var(--shadow-lg);
  animation: fadeUp 0.45s ease both;
}

.login-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 800;
  font-family: var(--font-display);
  color: var(--color-heading);
  justify-content: center;
  margin-bottom: 14px;
}

.login-logo .logo-icono {
  display: inline-grid;
  place-items: center;
  width: 42px;
  height: 42px;
  font-size: 21px;
  background: var(--grad-primary);
  border-radius: 12px;
  box-shadow: var(--shadow-primary);
}

.green-text {
  color: var(--color-primary);
}

.login-card h1 {
  color: var(--color-heading);
  font-size: 24px;
  text-align: center;
}

.subtitulo {
  color: var(--color-text);
  text-align: center;
  font-size: 14px;
  margin-bottom: 18px;
}

.login-form {
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

.campo input {
  width: 100%;
}

.btn-login {
  margin-top: 6px;
  padding: 13px;
  background: var(--grad-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  box-shadow: var(--shadow-primary);
  transition: transform var(--t-fast), box-shadow var(--t-fast);
}

.btn-login:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 185, 129, 0.34);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.login-error {
  background-color: var(--color-danger-soft);
  color: var(--color-danger);
  border: 1px solid #fca5a5;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.login-aviso {
  background-color: var(--color-warning-soft);
  color: var(--color-warning);
  border: 1px solid #fcd34d;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  margin-bottom: 15px;
}

.registro-link {
  text-align: center;
  color: var(--color-text);
  font-size: 14px;
  margin-top: 20px;
}

.registro-link a {
  color: var(--color-primary);
  font-weight: 600;
}
</style>
