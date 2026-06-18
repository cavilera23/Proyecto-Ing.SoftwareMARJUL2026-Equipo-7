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

// --- Lado del cuidador (mercado de solicitudes abiertas) ---

// Solicitudes PENDIENTES y abiertas que el cuidador en sesión puede tomar.
export const listarSolicitudesDisponiblesApi = async () => {
  return await apiFetch(`/intercambio/solicitudes/disponibles`, {
    method: 'GET'
  })
}

// Solicitudes que el cuidador en sesión ya aceptó (activas + historial).
export const listarMisSolicitudesCuidadorApi = async () => {
  return await apiFetch(`/intercambio/solicitudes/cuidador/mias`, {
    method: 'GET'
  })
}

// El cuidador en sesión acepta una solicitud pendiente.
export const aceptarSolicitudApi = async (id) => {
  return await apiFetch(`/intercambio/solicitudes/${id}/aceptar`, {
    method: 'PATCH'
  })
}
