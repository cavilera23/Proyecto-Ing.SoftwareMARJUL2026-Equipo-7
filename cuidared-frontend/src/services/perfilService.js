import { apiFetch } from './api'

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
