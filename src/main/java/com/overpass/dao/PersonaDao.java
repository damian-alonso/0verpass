package com.overpass.dao;

import com.overpass.db.DataSource;
import com.overpass.model.Persona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonaDao {

    public List<Persona> findAll() {
        String sql = "SELECT id, nombre, apellido, email, telefono, documento, fecha_alta, activo, observaciones FROM persona WHERE activo = 1 ORDER BY apellido, nombre";
        List<Persona> list = new ArrayList<>();
        try (Connection c = DataSource.get().getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listando personas", e);
        }
        return list;
    }

    public Optional<Persona> findById(int id) {
        String sql = "SELECT id, nombre, apellido, email, telefono, documento, fecha_alta, activo, observaciones FROM persona WHERE id = ?";
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando persona " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Persona> findByEmail(String email) {
        String sql = "SELECT id, nombre, apellido, email, telefono, documento, fecha_alta, activo, observaciones FROM persona WHERE email = ?";
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando persona por email", e);
        }
        return Optional.empty();
    }

    public int insert(Persona p) {
        String sql = "INSERT INTO persona (nombre, apellido, email, telefono, documento, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getDocumento());
            ps.setString(6, p.getObservaciones());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando persona", e);
        }
        throw new RuntimeException("No se obtuvo id al insertar persona");
    }

    public void update(Persona p) {
        String sql = "UPDATE persona SET nombre=?, apellido=?, email=?, telefono=?, documento=?, activo=?, observaciones=? WHERE id=?";
        try (Connection c = DataSource.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getDocumento());
            ps.setBoolean(6, p.isActivo());
            ps.setString(7, p.getObservaciones());
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando persona", e);
        }
    }

    private static Persona mapRow(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        p.setEmail(rs.getString("email"));
        p.setTelefono(rs.getString("telefono"));
        p.setDocumento(rs.getString("documento"));
        Timestamp ts = rs.getTimestamp("fecha_alta");
        p.setFechaAlta(ts != null ? ts.toLocalDateTime() : null);
        p.setActivo(rs.getBoolean("activo"));
        p.setObservaciones(rs.getString("observaciones"));
        return p;
    }
}
