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

export const buscarCuidadoresApi = async (horario) => {
  return await apiFetch(`/agenda/buscar-cuidadores`, {
    method: 'POST',
    body: JSON.stringify(horario)
  })
}

export const agregarHorarioApi = async (cuidadorId, horario) => {
  return await apiFetch(`/agenda/horarios/${cuidadorId}`, {
    method: 'POST',
    body: JSON.stringify(horario)
  })
}

export const modificarHorarioApi = async (cuidadorId, indice, horario) => {
  return await apiFetch(`/agenda/horarios/${cuidadorId}/${indice}`, {
    method: 'PUT',
    body: JSON.stringify(horario)
  })
}

export const eliminarHorarioApi = async (cuidadorId, indice) => {
  return await apiFetch(`/agenda/horarios/${cuidadorId}/${indice}`, {
    method: 'DELETE'
  })
}
