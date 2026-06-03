import { apiFetch } from './api'

export const crearCalificacionApi = async (calificacion) => {
  return await apiFetch(`/calificaciones`, {
    method: 'POST',
    body: JSON.stringify(calificacion)
  })
}

export const listarCalificacionesPorCuidadorApi = async (cuidadorId) => {
  return await apiFetch(`/calificaciones/cuidador/${cuidadorId}`, {
    method: 'GET'
  })
}

export const obtenerCalificacionPorSolicitudApi = async (solicitudId) => {
  return await apiFetch(`/calificaciones/solicitud/${solicitudId}`, {
    method: 'GET'
  })
}
