import { apiFetch } from './api'

export const verificarDisponibilidadApi = async (cuidadorId, horario) => {
  return await apiFetch(`/agenda/disponibilidad/${cuidadorId}`, {
    method: 'POST',
    body: JSON.stringify(horario)
  })
}

export const listarHorariosApi = async (cuidadorId) => {
  return await apiFetch(`/agenda/horarios/${cuidadorId}`, {
    method: 'GET'
  })
}
