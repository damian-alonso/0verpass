package com.overpass.dao;

import com.overpass.db.DataSource;
import com.overpass.model.Pago;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PagoDao {

    public List<Pago> findAllWithPersona() {
        String sql = "SELECT p.id, p.persona_id, p.tipo, p.monto, p.fecha_pago, p.vigencia_desde, p.vigencia_hasta, p.medio_pago, p.created_at, " +
                     "CONCAT(per.nombre, ' ', per.apellido) AS nombre_persona " +
                     "FROM pago p JOIN persona per ON p.persona_id = per.id ORDER BY p.fecha_pago DESC";
        List<Pago> list = new ArrayList<>();
        try (Connection c = DataSource.get().getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) list.add(mapRowWithPersona(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listando pagos", e);
        }
        return list;
    }

    public List<Pago> findByTipo(Pago.Tipo tipo) {
        String sql = "SELECT p.id, p.persona_id, p.tipo, p.monto, p.fecha_pago, p.vigencia_desde, p.vigencia_hasta, p.medio_pago, p.created_at, " +
                     "CONCAT(per.nombre, ' ', per.apellido) AS nombre_persona FROM pago p JOIN persona per ON p.persona_id = per.id WHERE p.tipo = ? ORDER BY p.fecha_pago DESC";
        List<Pago> list = new ArrayList<>();
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowWithPersona(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando pagos por tipo", e);
        }
        return list;
    }

    public List<Pago> findByPersonaId(int personaId) {
        String sql = "SELECT id, persona_id, tipo, monto, fecha_pago, vigencia_desde, vigencia_hasta, medio_pago, created_at FROM pago WHERE persona_id = ? ORDER BY fecha_pago DESC";
        List<Pago> list = new ArrayList<>();
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando pagos de persona", e);
        }
        return list;
    }

    public Optional<Pago> findById(int id) {
        String sql = "SELECT id, persona_id, tipo, monto, fecha_pago, vigencia_desde, vigencia_hasta, medio_pago, created_at FROM pago WHERE id = ?";
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando pago " + id, e);
        }
        return Optional.empty();
    }

    public int insert(Pago p) {
        String sql = "INSERT INTO pago (persona_id, tipo, monto, fecha_pago, vigencia_desde, vigencia_hasta, medio_pago) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getPersonaId());
            ps.setString(2, p.getTipo().name());
            ps.setBigDecimal(3, p.getMonto());
            ps.setDate(4, Date.valueOf(p.getFechaPago()));
            ps.setDate(5, Date.valueOf(p.getVigenciaDesde()));
            ps.setDate(6, Date.valueOf(p.getVigenciaHasta()));
            ps.setString(7, p.getMedioPago());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando pago", e);
        }
        throw new RuntimeException("No se obtuvo id al insertar pago");
    }

    /** Resumen de cobros: total mensuales y diarios en un rango de fechas (para el empleador). */
    public ResumenPagos resumenEntre(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT tipo, COUNT(*) AS cantidad, COALESCE(SUM(monto), 0) AS total FROM pago WHERE fecha_pago BETWEEN ? AND ? GROUP BY tipo";
        long totalMensual = 0;
        long totalDiario = 0;
        int countMensual = 0;
        int countDiario = 0;
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    int cant = rs.getInt("cantidad");
                    BigDecimal total = rs.getBigDecimal("total");
                    if ("MENSUAL".equals(tipo)) {
                        countMensual = cant;
                        totalMensual = total.longValue();
                    } else {
                        countDiario = cant;
                        totalDiario = total.longValue();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en resumen de pagos", e);
        }
        return new ResumenPagos(desde, hasta, countMensual, totalMensual, countDiario, totalDiario);
    }

    public static class ResumenPagos {
        public final LocalDate desde, hasta;
        public final int cantidadMensuales;
        public final long totalMensuales;
        public final int cantidadDiarios;
        public final long totalDiarios;

        public ResumenPagos(LocalDate desde, LocalDate hasta, int cantidadMensuales, long totalMensuales, int cantidadDiarios, long totalDiarios) {
            this.desde = desde;
            this.hasta = hasta;
            this.cantidadMensuales = cantidadMensuales;
            this.totalMensuales = totalMensuales;
            this.cantidadDiarios = cantidadDiarios;
            this.totalDiarios = totalDiarios;
        }
    }

    private static Pago mapRow(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setId(rs.getInt("id"));
        p.setPersonaId(rs.getInt("persona_id"));
        p.setTipo(Pago.Tipo.valueOf(rs.getString("tipo")));
        p.setMonto(rs.getBigDecimal("monto"));
        p.setFechaPago(rs.getDate("fecha_pago").toLocalDate());
        p.setVigenciaDesde(rs.getDate("vigencia_desde").toLocalDate());
        p.setVigenciaHasta(rs.getDate("vigencia_hasta").toLocalDate());
        p.setMedioPago(rs.getString("medio_pago"));
        Timestamp ts = rs.getTimestamp("created_at");
        p.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return p;
    }

    private static Pago mapRowWithPersona(ResultSet rs) throws SQLException {
        Pago p = mapRow(rs);
        p.setNombrePersona(rs.getString("nombre_persona"));
        return p;
    }
}
