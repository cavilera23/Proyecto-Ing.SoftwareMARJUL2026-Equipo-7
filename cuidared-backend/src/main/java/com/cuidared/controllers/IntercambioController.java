@PostMapping("/solicitudes")
public ResponseEntity<?> crearSolicitud(@RequestBody Solicitud solicitud) {
    try {
        Solicitud nueva = solicitudService.crearSolicitud(solicitud);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    } catch (ReglaNegocioException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (SolapamientoHorarioException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", e.getMessage(), "solicitudExistenteId", ""));
    }
}
