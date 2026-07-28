package com.pulse_gym.ms_operation.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.dto.ActualizarEstadoReporteDTO;
import com.pulse_gym.lb_common.dto.ConsultaEquipoRequestDTO;
import com.pulse_gym.lb_common.dto.EquipoRequestDTO;
import com.pulse_gym.lb_common.dto.EstadoEquipoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.ReporteFallaDTO;
import com.pulse_gym.lb_common.entity.operation.Equipo;
import com.pulse_gym.lb_common.entity.operation.Proveedor;
import com.pulse_gym.lb_common.entity.operation.Sede;
import com.pulse_gym.lb_common.enums.EnumEstado;
import com.pulse_gym.lb_common.enums.EnumEstadoReporte;
import com.pulse_gym.lb_common.enums.EnumUrgencia;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_operation.repository.EquipoRepository;
import com.pulse_gym.ms_operation.repository.ProveedorRepository;
import com.pulse_gym.ms_operation.repository.SedeRepository;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipoService {
    /**
     * Inyeccion de EquipoRepository
     */
    private final EquipoRepository equipoRepository;

    /**
     * Inyeccion de ProveedorRepository para manejar las operaciones de base de
     * datos relacionadas con los proveedores
     */
    private final ProveedorRepository proveedorRepository;

    /**
     * Inyeccion de SedeRepository para manejar las operaciones de base de datos
     * relacionadas con las sedes
     */
    private final SedeRepository sedeRepository;

    /**
     * Registra un nuevo equipo en el sistema. Primero verifica que el número de
     * serie del equipo no exista ya en la base de datos
     * 
     * Se valida que solo pueda hacer la peticion un Admin, un Entrenador o un Recepcionista
     * 
     * @param equipoRequestDTO
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return MessegeGlobalDTO con un mensaje de éxito si el equipo se registró
     *         correctamente
     */
    public MessegeGlobalDTO registrarEquipo(EquipoRequestDTO equipoRequestDTO, String userRol) {
        
        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        if (equipoRepository.findByNumeroSerie(equipoRequestDTO.getNumeroSerie()).isPresent()) {

            MessegeGlobalDTO response = new MessegeGlobalDTO("El número de serie ya existe");
            return response;
        }

        Proveedor proveedor = proveedorRepository
                .findById(equipoRequestDTO.getIdProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        Sede sede = sedeRepository
                .findById(equipoRequestDTO.getIdSede())
                .orElseThrow(() -> new RuntimeException("Sede no encontrada"));

        Equipo equipo = new Equipo();

        equipo.setProveedor(proveedor);
        equipo.setSede(sede);

        equipo.setNombre(equipoRequestDTO.getNombre());
        equipo.setMarca(equipoRequestDTO.getMarca());
        equipo.setModelo(equipoRequestDTO.getModelo());
        equipo.setNumeroSerie(equipoRequestDTO.getNumeroSerie());
        equipo.setFechaAdquisicion(equipoRequestDTO.getFechaAdquisicion());
        equipo.setFechaGarantia(equipoRequestDTO.getFechaGarantia());
        equipo.setUbicacion(equipoRequestDTO.getUbicacion());
        equipo.setEstado(equipoRequestDTO.getEstado());

        equipoRepository.save(equipo);

        MessegeGlobalDTO response = new MessegeGlobalDTO("Equipo registrado correctamente");
        return response;
    }

    /**
     * Obtiene una lista de equipos que coinciden con los criterios de búsqueda
     * especificados en el objeto ConsultaEquipoRequestDTO.
     * Utiliza Specification para construir una consulta dinámica basada en los
     * criterios de búsqueda proporcionados.
     * 
     * Se valida que solo pueda hacer la peticion un Cualquier Rol
     * 
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return Lista de equipos que coinciden con los criterios de búsqueda
     *         especificados en el objeto ConsultaEquipoRequestDTO
     */
    public List<Equipo> obtenerEquipos(ConsultaEquipoRequestDTO request, String userRol) {

        ValidacionDeRoles.validarCualquierRol(userRol);

        Specification<Equipo> spec = buildSpecification(request);

        return equipoRepository.findAll(spec);
    }

    /**
     * Construye una especificación de búsqueda para la consulta de equipos
     * 
     * @param request
     * @return
     */
    private Specification<Equipo> buildSpecification(ConsultaEquipoRequestDTO request) {
        // root -> Entidad, query -> modificar consulta , cd - > construye WHERE
        return (root, query, cb) -> {
            // Especifica el tipo jakarta.persistence.criteria.Predicate
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Búsqueda por nombre
            if (StringUtils.isNotBlank(request.getNombre())) {
                predicates.add(cb.like(cb.lower(root.get("nombre")),
                        "%" + request.getNombre().toLowerCase() + "%")); // % comodin ->
                                                                         
            }

            // Búsqueda por marca
            if (StringUtils.isNotBlank(request.getMarca())) {
                predicates.add(cb.like(cb.lower(root.get("marca")),
                        "%" + request.getMarca().toLowerCase() + "%"));
            }

            // Búsqueda por ubicación
            if (StringUtils.isNotBlank(request.getUbicacion())) {
                predicates.add(cb.like(cb.lower(root.get("ubicacion")),
                        "%" + request.getUbicacion().toLowerCase() + "%"));
            }

            // Búsqueda por estado
            if (StringUtils.isNotBlank(request.getEstado())) {
                try {
                    EnumEstado estadoEnum = EnumEstado.valueOf(request.getEstado().toUpperCase());
                    predicates.add(cb.equal(root.get("estado"), estadoEnum));
                } catch (IllegalArgumentException e) {
                    // Estado no válido
                }
            }

            // Búsqueda por sede
            if (request.getIdSede() != null) {
                predicates.add(cb.equal(root.get("sede").get("idSede"), request.getIdSede()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Actualiza la información de un equipo existente en la base de datos. El
     * método
     * primero verifica que el equipo con el ID proporcionado exista, luego
     * actualiza
     * 
     * Se valida que solo pueda hacer la peticion un Admin, un Entrenador o un Recepcionista
     * 
     * @param id
     * @param equipoRequestDTO
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return MessegeGlobalDTO con un mensaje de éxito si el equipo se actualizó
     *         correctamente
     */
    public MessegeGlobalDTO actualizarEquipo(Long id, EquipoRequestDTO equipoRequestDTO, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Proveedor proveedor = proveedorRepository
                .findById(equipoRequestDTO.getIdProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        Sede sede = sedeRepository
                .findById(equipoRequestDTO.getIdSede())
                .orElseThrow(() -> new RuntimeException("Sede no encontrada"));

        equipo.setProveedor(proveedor);
        equipo.setSede(sede);

        equipo.setNombre(equipoRequestDTO.getNombre());
        equipo.setMarca(equipoRequestDTO.getMarca());
        equipo.setModelo(equipoRequestDTO.getModelo());
        equipo.setNumeroSerie(equipoRequestDTO.getNumeroSerie());
        equipo.setFechaAdquisicion(equipoRequestDTO.getFechaAdquisicion());
        equipo.setFechaGarantia(equipoRequestDTO.getFechaGarantia());
        equipo.setUbicacion(equipoRequestDTO.getUbicacion());
        equipo.setEstado(equipoRequestDTO.getEstado());

        equipoRepository.save(equipo);

        MessegeGlobalDTO response = new MessegeGlobalDTO("Equipo actualizado correctamente");
        return response;
    }

    /**
     * Cambia el estado de un equipo existente en la base de datos.
     * 
     * Se valida que la ruta solo pueda hacer la peticion un Admin, un Entrenador o un Recepcionista
     * 
     * @param id
     * @param estadoRequestDTO
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return MessegeGlobalDTO con un mensaje de éxito si el estado del equipo se
     *         actualizó correctamente
     */
    public MessegeGlobalDTO cambiarEstadoEquipo(Long id, EstadoEquipoRequestDTO estadoRequestDTO, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        try {
            String nuevoEstado = estadoRequestDTO.getEstado().toUpperCase().trim();

            EnumEstado estadoEnum;
            try {
                estadoEnum = EnumEstado.valueOf(nuevoEstado);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado no válido: '" + estadoRequestDTO.getEstado() +
                        "'. Los valores válidos son: OPERATIVO, EN_MANTENIMIENTO, FUERA_DE_SERVICIO, RETIRADO");
            }

            // Guardar estado anterior para el mensaje
            String estadoAnterior = equipo.getEstado() != null ? equipo.getEstado().name() : "SIN_ESTADO";

            // Actualizar el estado
            equipo.setEstado(estadoEnum);
            equipoRepository.save(equipo);

            // Retornar respuesta exitosa
            return new MessegeGlobalDTO(String.format(
                    "Estado del equipo actualizado correctamente de %s a %s",
                    estadoAnterior,
                    estadoEnum.name()));

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el estado del equipo: " + e.getMessage());
        }
    }

    /**
     * Reporta una falla en un equipo existente en la base de datos.
     * 
     * Se valida que la ruta solo pueda hacer la peticion un Admin, un Entrenador o un Recepcionista
     * 
     * @param idEquipo
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return MessegeGlobalDTO con un mensaje de éxito si la falla se reportó
     *         correctamente
     */
    @Transactional
    public MessegeGlobalDTO reportarFalla(Long idEquipo, ReporteFallaDTO request, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + idEquipo));

        // Validar urgencia
        EnumUrgencia urgencia;
        try {
            urgencia = EnumUrgencia.valueOf(request.getUrgencia().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Urgencia no válida. Valores: BAJA, MEDIA, ALTA, CRITICA");
        }

        // Actualizar equipo con la falla
        equipo.setUrgenciaFalla(urgencia);
        equipo.setDescripcionFalla(request.getDescripcion());
        equipo.setEstadoReporte(EnumEstadoReporte.PENDIENTE);

        // Si la urgencia es CRITICA, cambiar estado del equipo
        if (urgencia == EnumUrgencia.CRITICA) {
            equipo.setEstado(EnumEstado.MANTENIMIENTO);
        }

        equipoRepository.save(equipo);

        return new MessegeGlobalDTO("Falla reportada exitosamente para el equipo: " + equipo.getNombre());
    }

    /**
     * Actualiza el estado de un reporte de falla existente en la base de datos.
     * 
     * Se valida que la ruta solo pueda hacer la peticion un Admin, un Entrenador o un Recepcionista
     * 
     * @param idEquipo
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return MessegeGlobalDTO con un mensaje de éxito si el estado del reporte de
     *         falla se actualizó correctamente
     */
    @Transactional
    public MessegeGlobalDTO actualizarEstadoReporte(Long idEquipo, ActualizarEstadoReporteDTO request, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + idEquipo));

        // Validar estado
        EnumEstadoReporte nuevoEstado;
        try {
            nuevoEstado = EnumEstadoReporte.valueOf(request.getEstado().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Estado no válido. Valores: PENDIENTE, EN_REVISION, EN_REPARACION, RESUELTO");
        }

        String estadoAnterior = equipo.getEstadoReporte() != null ? equipo.getEstadoReporte().name()
                : "NINGUNO";
        equipo.setEstadoReporte(nuevoEstado);

        // Si se resuelve, limpiar la falla
        if (nuevoEstado == EnumEstadoReporte.RESUELTO) {
            equipo.setUrgenciaFalla(EnumUrgencia.NINGUNA);
            equipo.setDescripcionFalla(null);
            // Si el equipo estaba en mantenimiento por esta falla, volver a operativo
            if (equipo.getEstado() == EnumEstado.MANTENIMIENTO) {
                equipo.setEstado(EnumEstado.OPERATIVO);
            }
        }

        equipoRepository.save(equipo);

        return new MessegeGlobalDTO(String.format(
                "Estado del reporte actualizado de %s a %s para el equipo: %s",
                estadoAnterior, nuevoEstado.name(), equipo.getNombre()));
    }

    /**
     * Consulta los reportes de falla de los equipos que coinciden con los criterios
     * de búsqueda especificados.
     * 
     * 
     * 
     * @param idEquipo
     * @param estado
     * @param urgencia
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return Lista de equipos que coinciden con los criterios de búsqueda
     */
    public List<Equipo> consultarReportesFalla(Long idEquipo, String estado, String urgencia, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Specification<Equipo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Solo equipos que tienen reportes de falla (excluir NINGUNA)
            predicates.add(cb.notEqual(root.get("urgenciaFalla"), EnumUrgencia.NINGUNA));

            // Filtro por ID de equipo
            if (idEquipo != null) {
                predicates.add(cb.equal(root.get("idEquipo"), idEquipo));
            }

            // Filtro por estado
            if (estado != null && !estado.isEmpty()) {
                try {
                    EnumEstadoReporte estadoEnum = EnumEstadoReporte.valueOf(estado.toUpperCase());
                    predicates.add(cb.equal(root.get("estadoReporte"), estadoEnum));
                } catch (IllegalArgumentException e) {
                    // Ignorar
                }
            }

            // Filtro por urgencia
            if (urgencia != null && !urgencia.isEmpty()) {
                try {
                    EnumUrgencia urgenciaEnum = EnumUrgencia.valueOf(urgencia.toUpperCase());
                    predicates.add(cb.equal(root.get("urgenciaFalla"), urgenciaEnum));
                } catch (IllegalArgumentException e) {
                    // Ignorar
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return equipoRepository.findAll(spec);
    }

}