package com.pulse_gym.ms_operation.repository;

import com.pulse_gym.lb_common.dto.HistorialAccesoDTO;
import com.pulse_gym.lb_common.dto.HistorialAccesoFiltroDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HistorialAccesoRepositoryImpl implements HistorialAccesoRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Page<HistorialAccesoDTO> consultarHistorialAccesos(HistorialAccesoFiltroDTO filtro, Pageable pageable) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("fechaInicio", filtro.getFechaInicio() != null ? filtro.getFechaInicio() : LocalDateTime.MIN);
        params.addValue("fechaFin", filtro.getFechaFin() != null ? filtro.getFechaFin() : LocalDateTime.MAX);
        params.addValue("offset", pageable.getOffset());
        params.addValue("limit", pageable.getPageSize());

        String unionQuery = buildUnionQuery(filtro, params);
        String countQuery = buildCountQuery(filtro, params);

        List<HistorialAccesoDTO> content = jdbcTemplate.query(unionQuery, params, new BeanPropertyRowMapper<>(HistorialAccesoDTO.class));

        Long total = jdbcTemplate.queryForObject(countQuery, params, Long.class);

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    /**
     * Construye la consulta UNION completa con paginación.
     */
    private String buildUnionQuery(HistorialAccesoFiltroDTO filtro, MapSqlParameterSource params) {
        String baseQuery = buildBaseUnionQuery(filtro, params);
        return "SELECT * FROM (" + baseQuery + ") AS historial ORDER BY fechaHora DESC OFFSET :offset LIMIT :limit";
    }

    /**
     * Construye la consulta de conteo.
     */
    private String buildCountQuery(HistorialAccesoFiltroDTO filtro, MapSqlParameterSource params) {
        String baseQuery = buildBaseUnionQuery(filtro, params);
        return "SELECT COUNT(*) FROM (" + baseQuery + ") AS historial";
    }

    /**
     * Construye la parte central de la consulta (UNION ALL) sin ORDER BY ni paginación.
     */
    private String buildBaseUnionQuery(HistorialAccesoFiltroDTO filtro, MapSqlParameterSource params) {
        String asistenciaQuery = buildAsistenciaQuery(filtro, params);
        String biometricaQuery = buildBiometricaQuery(filtro, params);

        return asistenciaQuery + " UNION ALL " + biometricaQuery;
    }

    /**
     * Construye la consulta para la tabla asistencia.
     */
    private String buildAsistenciaQuery(HistorialAccesoFiltroDTO filtro, MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    a.id_usuario AS usuarioId, ");
        sql.append("    CONCAT(u.nombre, ' ', u.apellido) AS nombreUsuario, ");
        sql.append("    a.fecha_hora_entrada AS fechaHora, ");
        sql.append("    a.tipo_acceso AS tipoAcceso, ");
        sql.append("    CASE ");
        sql.append("        WHEN a.estado_acceso = 'PERMITIDO' THEN 'EXITOSO' ");
        sql.append("        WHEN a.estado_acceso = 'DENEGADO' THEN 'FALLIDO' ");
        sql.append("        ELSE 'DESCONOCIDO' ");
        sql.append("    END AS resultado, ");
        sql.append("    a.motivo_denegacion AS motivo, ");
        sql.append("    a.id_sede AS sedeId, ");
        sql.append("    s.nombre_sede AS nombreSede ");
        sql.append("FROM operations_schema.asistencia a ");
        sql.append("LEFT JOIN users_schema.usuario_perfil u ON a.id_usuario = u.id_usuario ");
        sql.append("LEFT JOIN operations_schema.sede s ON a.id_sede = s.id_sede ");
        sql.append("WHERE 1=1 ");
        sql.append(buildWhereAsistencia(filtro, params));
        return sql.toString();
    }

    /**
     * Construye la consulta para la tabla auditoria_biometrica.
     */
    private String buildBiometricaQuery(HistorialAccesoFiltroDTO filtro, MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    ab.id_usuario AS usuarioId, ");
        sql.append("    CONCAT(u.nombre, ' ', u.apellido) AS nombreUsuario, ");
        sql.append("    ab.fecha_hora AS fechaHora, ");
        sql.append("    'BIOMETRICO' AS tipoAcceso, ");
        sql.append("    CASE ");
        sql.append("        WHEN ab.exitoso = true THEN 'EXITOSO' ");
        sql.append("        WHEN ab.exitoso = false AND ab.mensaje LIKE '%bloqueado%' THEN 'BLOQUEADO' ");
        sql.append("        ELSE 'FALLIDO' ");
        sql.append("    END AS resultado, ");
        sql.append("    ab.mensaje AS motivo, ");
        sql.append("    ab.id_sede AS sedeId, ");
        sql.append("    s.nombre_sede AS nombreSede ");
        sql.append("FROM operations_schema.auditoria_biometrica ab ");
        sql.append("LEFT JOIN users_schema.usuario_perfil u ON ab.id_usuario = u.id_usuario ");
        sql.append("LEFT JOIN operations_schema.sede s ON ab.id_sede = s.id_sede ");
        sql.append("WHERE 1=1 ");
        sql.append(buildWhereBiometrica(filtro, params));
        return sql.toString();
    }

    /**
     * Construye las condiciones WHERE para asistencias.
     */
    private String buildWhereAsistencia(HistorialAccesoFiltroDTO filtro, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();

        if (filtro.getUsuarioId() != null) {
            conditions.add("a.id_usuario = :usuarioId");
            params.addValue("usuarioId", filtro.getUsuarioId());
        }

        conditions.add("a.fecha_hora_entrada BETWEEN :fechaInicio AND :fechaFin");

        if (filtro.getTipoAcceso() != null) {
            if (filtro.getTipoAcceso().equalsIgnoreCase("WEB")) {
                conditions.add("a.tipo_acceso IN ('WEB', 'APP')");
            } else if (filtro.getTipoAcceso().equalsIgnoreCase("HUELLA")) {
                conditions.add("a.tipo_acceso = 'BIOMETRICO'");
            }
        }

        if (filtro.getResultado() != null) {
            String res = filtro.getResultado().toUpperCase();
            if (res.equals("EXITOSO")) {
                conditions.add("a.estado_acceso = 'PERMITIDO'");
            } else if (res.equals("FALLIDO")) {
                conditions.add("a.estado_acceso = 'DENEGADO'");
            } else if (res.equals("BLOQUEADO")) {
                conditions.add("1=0");
            }
        }

        return conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions);
    }

    /**
     * Construye las condiciones WHERE para auditoría biométrica.
     */
    private String buildWhereBiometrica(HistorialAccesoFiltroDTO filtro, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();

        if (filtro.getUsuarioId() != null) {
            conditions.add("ab.id_usuario = :usuarioId");
        }

        conditions.add("ab.fecha_hora BETWEEN :fechaInicio AND :fechaFin");

        if (filtro.getTipoAcceso() != null) {
            if (filtro.getTipoAcceso().equalsIgnoreCase("WEB")) {
                conditions.add("1=0");
            }
        }

        if (filtro.getResultado() != null) {
            String res = filtro.getResultado().toUpperCase();
            if (res.equals("EXITOSO")) {
                conditions.add("ab.exitoso = true");
            } else if (res.equals("FALLIDO")) {
                conditions.add("ab.exitoso = false AND ab.mensaje NOT LIKE '%bloqueado%'");
            } else if (res.equals("BLOQUEADO")) {
                conditions.add("ab.exitoso = false AND ab.mensaje LIKE '%bloqueado%'");
            }
        }

        return conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions);
    }
}