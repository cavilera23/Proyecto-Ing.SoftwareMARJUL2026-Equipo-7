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
