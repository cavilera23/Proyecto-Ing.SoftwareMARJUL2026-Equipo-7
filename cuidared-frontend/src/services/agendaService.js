import { apiFetch } from './api'

export const verificarDisponibilidadApi = async (cuidadorId, horario) => {
  return await apiFetch(`/agenda/disponibilidad/${cuidadorId}`, {
    method: 'POST',
    body: JSON.stringify(horario)
  })
}
