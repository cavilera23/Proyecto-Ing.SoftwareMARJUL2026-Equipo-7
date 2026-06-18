package com.cuidared;

import com.cuidared.models.*;
import com.cuidared.repositories.CalificacionRepository;
import com.cuidared.repositories.SolicitudRepository;
import com.cuidared.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SolicitudRepository solicitudRepository;
    private final CalificacionRepository calificacionRepository;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      SolicitudRepository solicitudRepository,
                      CalificacionRepository calificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.calificacionRepository = calificacionRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        // ─────────── CUIDADORES ───────────

        Cuidador maria = cuidador("María González", "maria.gonzalez@cuidared.com", "0412-5551001", 15.0,
                List.of(TipoAsistencia.NINO, TipoAsistencia.ADULTO_MAYOR),
                List.of(
                        horario(2026, 6, 23, 8, 16),
                        horario(2026, 6, 24, 8, 16),
                        horario(2026, 6, 25, 9, 14),
                        horario(2026, 6, 26, 8, 16)
                ));

        Cuidador carlos = cuidador("Carlos Rodríguez", "carlos.rodriguez@cuidared.com", "0424-5552002", 12.0,
                List.of(TipoAsistencia.MASCOTA, TipoAsistencia.NINO),
                List.of(
                        horario(2026, 6, 23, 9, 17),
                        horario(2026, 6, 25, 9, 17),
                        horario(2026, 6, 27, 10, 15)
                ));

        Cuidador ana = cuidador("Ana Martínez", "ana.martinez@cuidared.com", "0416-5553003", 18.0,
                List.of(TipoAsistencia.ADULTO_MAYOR),
                List.of(
                        horario(2026, 6, 24, 7, 15),
                        horario(2026, 6, 25, 7, 15),
                        horario(2026, 6, 28, 8, 13)
                ));

        Cuidador luis = cuidador("Luis Pérez", "luis.perez@cuidared.com", "0414-5554004", 10.0,
                List.of(TipoAsistencia.NINO, TipoAsistencia.ADULTO_MAYOR, TipoAsistencia.MASCOTA),
                List.of(
                        horario(2026, 6, 23, 14, 20),
                        horario(2026, 6, 24, 14, 20),
                        horario(2026, 6, 26, 12, 18)
                ));

        usuarioRepository.saveAll(List.of(maria, carlos, ana, luis));

        // ─────────── PADRES ───────────

        Padre carmen = padre("Carmen Torres", "carmen.torres@gmail.com", "0412-6661001");
        Padre roberto = padre("Roberto Méndez", "roberto.mendez@gmail.com", "0424-6662002");
        Padre patricia = padre("Patricia Silva", "patricia.silva@gmail.com", "0416-6663003");
        Padre jose = padre("José Hernández", "jose.hernandez@gmail.com", "0414-6664004");

        usuarioRepository.saveAll(List.of(carmen, roberto, patricia, jose));

        // ─────────── SOLICITUDES FINALIZADAS ───────────

        Solicitud s1 = solicitud(carmen, maria, TipoAsistencia.NINO, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 5, 10), LocalTime.of(9, 0), 4,
                "Cuidado de mi hijo de 5 años mientras trabajo desde casa.");

        Solicitud s2 = solicitud(carmen, maria, TipoAsistencia.NINO, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 5, 20), LocalTime.of(8, 30), 3,
                "Supervisión para mi niña mientras tengo reunión médica.");

        Solicitud s3 = solicitud(roberto, maria, TipoAsistencia.ADULTO_MAYOR, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), 5,
                "Acompañamiento para mi mamá de 78 años, requiere asistencia para movilizarse.");

        Solicitud s4 = solicitud(roberto, carlos, TipoAsistencia.MASCOTA, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 5, 15), LocalTime.of(7, 0), 6,
                "Cuidado de mis dos perros (Golden y Labrador) durante un viaje corto.");

        Solicitud s5 = solicitud(patricia, ana, TipoAsistencia.ADULTO_MAYOR, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 5, 25), LocalTime.of(8, 0), 8,
                "Mi padre necesita cuidados postoperatorios y fisioterapia básica en casa.");

        Solicitud s6 = solicitud(carmen, ana, TipoAsistencia.ADULTO_MAYOR, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 6, 5), LocalTime.of(9, 0), 4,
                "Asistencia a mi abuela durante una cita médica y regreso a casa.");

        Solicitud s7 = solicitud(jose, luis, TipoAsistencia.NINO, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 5, 18), LocalTime.of(14, 0), 3,
                "Cuidado de mis dos hijos de 3 y 7 años por la tarde.");

        Solicitud s8 = solicitud(patricia, carlos, TipoAsistencia.NINO, EstadoSolicitud.FINALIZADA,
                LocalDate.of(2026, 6, 8), LocalTime.of(9, 0), 4,
                "Cuidado para mis gemelos de 4 años durante mi clase de yoga.");

        // ─────────── SOLICITUDES ACTIVAS ───────────

        Solicitud s9 = solicitud(jose, maria, TipoAsistencia.NINO, EstadoSolicitud.ACEPTADA,
                LocalDate.of(2026, 6, 23), LocalTime.of(8, 0), 4,
                "Cuidado de mi hijo durante jornada de trabajo presencial.");

        Solicitud s10 = solicitud(roberto, ana, TipoAsistencia.ADULTO_MAYOR, EstadoSolicitud.ACEPTADA,
                LocalDate.of(2026, 6, 24), LocalTime.of(7, 0), 6,
                "Asistencia a mi suegra para terapia física y preparación de medicamentos.");

        // PENDIENTES abiertas: sin cuidador asignado. Cualquier cuidador con la
        // habilidad correspondiente puede verlas y decidir si las acepta.
        Solicitud s11 = solicitud(carmen, null, TipoAsistencia.MASCOTA, EstadoSolicitud.PENDIENTE,
                LocalDate.of(2026, 6, 25), LocalTime.of(9, 0), 5,
                "Necesito alguien que cuide a mi gato siamés mientras viajo a Mérida.");

        Solicitud s12 = solicitud(patricia, null, TipoAsistencia.ADULTO_MAYOR, EstadoSolicitud.PENDIENTE,
                LocalDate.of(2026, 6, 26), LocalTime.of(12, 0), 4,
                "Acompañamiento a mi abuelo de 85 años a consulta médica y regreso.");

        Solicitud s13 = solicitud(jose, null, TipoAsistencia.NINO, EstadoSolicitud.PENDIENTE,
                LocalDate.of(2026, 6, 27), LocalTime.of(8, 0), 6,
                "Busco niñera para mi hija de 6 años durante mi turno de trabajo.");

        solicitudRepository.saveAll(List.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13));

        // Registrar solicitudes en el historial de cada padre
        carmen.addSolicitudId(s1.getId());
        carmen.addSolicitudId(s2.getId());
        carmen.addSolicitudId(s6.getId());
        carmen.addSolicitudId(s11.getId());

        roberto.addSolicitudId(s3.getId());
        roberto.addSolicitudId(s4.getId());
        roberto.addSolicitudId(s10.getId());

        patricia.addSolicitudId(s5.getId());
        patricia.addSolicitudId(s8.getId());
        patricia.addSolicitudId(s12.getId());

        jose.addSolicitudId(s7.getId());
        jose.addSolicitudId(s9.getId());
        jose.addSolicitudId(s13.getId());

        usuarioRepository.saveAll(List.of(carmen, roberto, patricia, jose));

        // ─────────── CALIFICACIONES ───────────
        // María: 5 + 4 + 5 = promedio 4.7
        List<Calificacion> cals = new ArrayList<>();
        cals.add(cal(s1, carmen, maria, 5,
                "¡Excelente! María es muy cariñosa con los niños, mi hijo quedó feliz.",
                LocalDateTime.of(2026, 5, 10, 14, 0)));
        cals.add(cal(s2, carmen, maria, 4,
                "Muy buena atención, puntual y responsable. La recomiendo.",
                LocalDateTime.of(2026, 5, 20, 12, 30)));
        cals.add(cal(s3, roberto, maria, 5,
                "Mi mamá la adoró. Fue muy atenta y paciente con ella. Volveremos a contratar.",
                LocalDateTime.of(2026, 6, 1, 15, 30)));

        // Carlos: 5 + 4 = promedio 4.5
        cals.add(cal(s4, roberto, carlos, 5,
                "Mis perros estuvieron muy bien cuidados, los dejó cansados de tanto jugar.",
                LocalDateTime.of(2026, 5, 15, 14, 0)));
        cals.add(cal(s8, patricia, carlos, 4,
                "Buen trato con los niños, aunque llegó 10 minutos tarde.",
                LocalDateTime.of(2026, 6, 8, 14, 0)));

        // Ana: 5 + 4 = promedio 4.5
        cals.add(cal(s5, patricia, ana, 5,
                "Profesional y comprometida. Conoce mucho sobre cuidado de adultos mayores.",
                LocalDateTime.of(2026, 5, 25, 17, 0)));
        cals.add(cal(s6, carmen, ana, 4,
                "Mi abuela quedó muy contenta. Ana es muy amable y paciente.",
                LocalDateTime.of(2026, 6, 5, 14, 0)));

        // Luis: 4 = promedio 4.0
        cals.add(cal(s7, jose, luis, 4,
                "Los niños se divirtieron mucho. Cumplió con todo lo acordado.",
                LocalDateTime.of(2026, 5, 18, 18, 0)));

        calificacionRepository.saveAll(cals);

        // ─────────── ACTUALIZAR PROMEDIOS ───────────
        recalcularPromedio(maria, cals);
        recalcularPromedio(carlos, cals);
        recalcularPromedio(ana, cals);
        recalcularPromedio(luis, cals);
        usuarioRepository.saveAll(List.of(maria, carlos, ana, luis));
    }

    // ─────────── HELPERS ───────────

    private void recalcularPromedio(Cuidador cuidador, List<Calificacion> todas) {
        double promedio = todas.stream()
                .filter(c -> cuidador.getId().equals(c.getCuidadorId()))
                .mapToInt(Calificacion::getPuntuacion)
                .average()
                .orElse(0.0);
        cuidador.setCalificacionPromedio(Math.round(promedio * 10.0) / 10.0);
    }

    private Cuidador cuidador(String nombre, String correo, String telefono, double tarifa,
                               List<TipoAsistencia> habilidades, List<Horario> horarios) {
        Cuidador c = new Cuidador(nombre, correo, telefono, tarifa);
        c.setContrasena("cuidared123");
        c.setHabilidades(new ArrayList<>(habilidades));
        c.setHorariosDisponibles(new ArrayList<>(horarios));
        return c;
    }

    private Padre padre(String nombre, String correo, String telefono) {
        Padre p = new Padre(nombre, correo, telefono);
        p.setContrasena("cuidared123");
        return p;
    }

    private Horario horario(int year, int month, int day, int horaInicio, int horaFin) {
        return new Horario(
                LocalDateTime.of(year, month, day, horaInicio, 0),
                LocalDateTime.of(year, month, day, horaFin, 0));
    }

    private Solicitud solicitud(Padre padre, Cuidador cuidador, TipoAsistencia tipo,
                                EstadoSolicitud estado, LocalDate fecha, LocalTime hora,
                                int duracion, String descripcion) {
        Solicitud s = new Solicitud();
        s.setPadreId(padre.getId());
        s.setPadreNombre(padre.getNombre());
        if (cuidador != null) {
            s.setCuidadorId(cuidador.getId());
        }
        s.setTipo(tipo);
        s.setEstado(estado);
        s.setFecha(fecha);
        s.setHoraInicio(hora);
        s.setDuracionHoras(duracion);
        s.setDescripcion(descripcion);
        s.setHorario(new Horario(
                LocalDateTime.of(fecha, hora),
                LocalDateTime.of(fecha, hora.plusHours(duracion))));
        return s;
    }

    private Calificacion cal(Solicitud solicitud, Padre autor, Cuidador cuidador,
                             int puntuacion, String comentario, LocalDateTime fecha) {
        Calificacion c = new Calificacion();
        c.setSolicitudId(solicitud.getId());
        c.setAutorId(autor.getId());
        c.setCuidadorId(cuidador.getId());
        c.setPuntuacion(puntuacion);
        c.setComentario(comentario);
        c.setFecha(fecha);
        return c;
    }
}
