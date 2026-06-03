import { apiFetch } from './api'

export const listarNotificacionesApi = async (usuarioId, incluirSilenciadas = false) => {
  return await apiFetch(`/notificaciones/${usuarioId}?incluirSilenciadas=${incluirSilenciadas}`, {
    method: 'GET'
  })
}

export const registrarNotificacionApi = async (notificacion) => {
  return await apiFetch(`/notificaciones`, {
    method: 'POST',
    body: JSON.stringify(notificacion)
  })
}

export const silenciarNotificacionApi = async (id) => {
  return await apiFetch(`/notificaciones/${id}/silenciar`, {
    method: 'PATCH'
  })
}

export const activarNotificacionApi = async (id) => {
  return await apiFetch(`/notificaciones/${id}/activar`, {
    method: 'PATCH'
  })
}
