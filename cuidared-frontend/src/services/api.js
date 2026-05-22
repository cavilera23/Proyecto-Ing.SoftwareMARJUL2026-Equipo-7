const API_URL = 'http://localhost:8080/api/v1'

export const apiFetch = async (endpoint, options = {}) => {
  const url = `${API_URL}${endpoint}`
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  }
  
  const response = await fetch(url, { ...options, headers })
  
  if (!response.ok) {
    const errorData = await response.text()
    throw new Error(errorData || 'Error en la petición API')
  }
  
  // Try to parse json, if not possible return as text or empty
  try {
    return await response.json()
  } catch (e) {
    return null
  }
}
