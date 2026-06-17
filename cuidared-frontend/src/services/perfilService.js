import { apiFetch } from './api'

// --- Perfil propio (ligado al token de la sesión) ---
// El backend resuelve el id a partir del JWT, así que el cliente no envía ningún id.

export const obtenerMiPerfilApi = async () => {
  return await apiFetch('/perfiles/me', {
    method: 'GET'
  })
}

export const modificarMiPerfilApi = async (cambios) => {
  return await apiFetch('/perfiles/me', {
    method: 'PUT',
    body: JSON.stringify(cambios)
  })
}

export const eliminarMiPerfilApi = async () => {
  return await apiFetch('/perfiles/me', {
    method: 'DELETE'
  })
}

// --- Operaciones por id (uso administrativo / interno) ---

export const obtenerPerfilApi = async (id) => {
  return await apiFetch(`/perfiles/${id}`, {
    method: 'GET'
  })
}

export const modificarInformacionApi = async (id, cambios) => {
  return await apiFetch(`/perfiles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(cambios)
  })
}

export const eliminarPerfilApi = async (id) => {
  return await apiFetch(`/perfiles/${id}`, {
    method: 'DELETE'
  })
}
