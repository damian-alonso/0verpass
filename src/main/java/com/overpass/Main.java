package com.overpass;

import com.overpass.dao.PersonaDao;
import com.overpass.dao.PagoDao;
import com.overpass.model.Pago;
import com.overpass.model.Persona;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Demo de uso del registro de pagos del muro 0verpass.
 * Asegúrate de tener MySQL corriendo con Docker: docker-compose up -d
 */
public class Main {

    public static void main(String[] args) {
        PersonaDao personaDao = new PersonaDao();
        PagoDao pagoDao = new PagoDao();

        System.out.println("=== 0verpass — Registro de pagos (mensual / diario) ===\n");

        // Listar personas
        List<Persona> personas = personaDao.findAll();
        System.out.println("Personas registradas: " + personas.size());
        personas.forEach(p -> System.out.println("  " + p.getNombreCompleto() + " — " + p.getEmail()));

        // Listar todos los pagos (con nombre de persona)
        System.out.println("\n--- Últimos pagos ---");
        List<Pago> pagos = pagoDao.findAllWithPersona();
        for (Pago p : pagos) {
            String nombre = p.getNombrePersona() != null ? p.getNombrePersona() : "id=" + p.getPersonaId();
            System.out.printf("  %s | %s | $%.2f | %s | %s%n", nombre, p.getTipo(), p.getMonto(), p.getFechaPago(), p.getMedioPago());
        }

        // Resumen para el empleador (este mes)
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate hoy = LocalDate.now();
        PagoDao.ResumenPagos resumen = pagoDao.resumenEntre(inicioMes, hoy);
        System.out.println("\n--- Resumen " + inicioMes + " a " + hoy + " ---");
        System.out.printf("  Mensuales: %d pagos, total $%d%n", resumen.cantidadMensuales, resumen.totalMensuales);
        System.out.printf("  Diarios:   %d pagos, total $%d%n", resumen.cantidadDiarios, resumen.totalDiarios);
        System.out.printf("  Total:     $%d%n", resumen.totalMensuales + resumen.totalDiarios);

        System.out.println("\n--- Fin ---");
    }
}
