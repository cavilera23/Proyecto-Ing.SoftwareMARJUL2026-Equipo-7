import { apiFetch } from './api'

export const crearSolicitudApi = async (solicitud) => {
  return await apiFetch(`/intercambio/solicitudes`, {
    method: 'POST',
    body: JSON.stringify(solicitud)
  })
}

export const listarSolicitudesApi = async () => {
  return await apiFetch(`/intercambio/solicitudes`, {
    method: 'GET'
  })
}

export const modificarSolicitudApi = async (id, cambios) => {
  return await apiFetch(`/intercambio/solicitudes/${id}`, {
    method: 'PUT',
    body: JSON.stringify(cambios)
  })
}

export const cancelarSolicitudApi = async (id) => {
  return await apiFetch(`/intercambio/solicitudes/${id}/cancelar`, {
    method: 'PATCH'
  })
}
