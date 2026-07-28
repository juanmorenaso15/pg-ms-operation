package com.pulse_gym.ms_operation.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.dto.HistorialMantenimientoDTO;
import com.pulse_gym.lb_common.dto.MantenimientoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.entity.operation.Equipo;
import com.pulse_gym.lb_common.entity.operation.Mantenimiento;
import com.pulse_gym.lb_common.entity.operation.Proveedor;
import com.pulse_gym.lb_common.enums.EnumTipoMantenimiento;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_operation.repository.EquipoRepository;
import com.pulse_gym.ms_operation.repository.MantenimientoRepository;
import com.pulse_gym.ms_operation.repository.ProveedorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class MantenimientoService {

    /**
     * Inyeccion de ProveedorRepository para manejar las operaciones de base de
     * datos relacionadas con los proveedores
     */
    private final ProveedorRepository proveedorRepository;

    /**
     * Inyeccion de EquipoRepository para manejar las operaciones de base de datos
     * relacionadas con las sedes
     */ 
    private final EquipoRepository equipoRepository;

    /**
     * Inyeccion de MantenimientoRepository para manejar las operaciones de base de datos
     * relacionadas con los mantenimientos
     */
    private final MantenimientoRepository mantenimientoRepository;
    
    /**
     * Registra un nuevo mantenimiento en el sistema. Primero verifica que el proveedor y el equipo existan.
     * Luego, convierte el tipo de mantenimiento a su representación enum y crea un nuevo objeto Mantenimiento.
     * Finalmente, guarda el mantenimiento en la base de datos.
     * 
     * se valida que la peticion solo la puede hacer un Entrenador, recepcionista o admin
     * 
     * @param mantenimientoRequestDTO
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return MessegeGlobalDTO con el resultado del registro
     */
    public MessegeGlobalDTO registrarMantenimiento(MantenimientoRequestDTO mantenimientoRequestDTO, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Proveedor proveedor = null;
        if (mantenimientoRequestDTO.getIdProveedor() != null) {
            proveedor = proveedorRepository
                    .findById(mantenimientoRequestDTO.getIdProveedor())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        }

        Equipo equipo = equipoRepository
                .findById(mantenimientoRequestDTO.getIdEquipo())
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        EnumTipoMantenimiento tipoMantenimiento;

        try {
            tipoMantenimiento = EnumTipoMantenimiento.valueOf(mantenimientoRequestDTO.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de mantenimiento no válido. Debe ser PREVENTIVO o CORRECTIVO");
        }

        Mantenimiento mantenimiento = new Mantenimiento();

        mantenimiento.setProveedor(proveedor);
        mantenimiento.setEquipo(equipo);

        mantenimiento.setFechaServicio(mantenimientoRequestDTO.getFechaServicio());
        mantenimiento.setTipo(tipoMantenimiento);
        mantenimiento.setDescripcion(mantenimientoRequestDTO.getDescripcion());
        mantenimiento.setCosto(mantenimientoRequestDTO.getCosto());
        mantenimiento.setProximoMantenimiento(mantenimientoRequestDTO.getProximoMantenimiento());
        mantenimiento.setTecnicoResponsable(mantenimientoRequestDTO.getTecnicoResponsable());
        mantenimiento.setProximoMantenimiento(mantenimientoRequestDTO.getProximoMantenimiento());

        mantenimientoRepository.save(mantenimiento);

        return new MessegeGlobalDTO("Mantenimiento registrado exitosamente");
    }

    /**
     * Obtiene los registros de mantenimiento de un equipo.
     * 
     * Se valida que la peticion solo la puede hacer un Entrenador, recepcionista o admin
     * 
     * @param idEquipo
     * @param userRol Rol del usuario
     * @return List<HistorialMantenimientoDTO> con los registros de mantenimiento encontrados   
     */
    public List<HistorialMantenimientoDTO> obtenerHistorialPorEquipo(Long idEquipo, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado con ID: " + idEquipo));

        List<Mantenimiento> mantenimientos = mantenimientoRepository
                .findByEquipoIdEquipoOrderByFechaServicioDesc(idEquipo);

        if (mantenimientos.isEmpty()) {
            throw new RuntimeException("El equipo '" + equipo.getNombre() + "' no tiene registros de mantenimiento");
        }

        return mantenimientos.stream()
                .map(this::convertirAHistorialDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un objeto Mantenimiento a un objeto HistorialMantenimientoDTO.
     * @param mantenimiento
     * @return HistorialMantenimientoDTO con los datos del mantenimiento
     */
    private HistorialMantenimientoDTO convertirAHistorialDTO(Mantenimiento mantenimiento) {
        HistorialMantenimientoDTO dto = new HistorialMantenimientoDTO();

        // Mapeo básico (copiar campos simples)
        dto.setIdMantenimiento(mantenimiento.getIdMantenimiento());
        dto.setFechaServicio(mantenimiento.getFechaServicio());
        dto.setDescripcion(mantenimiento.getDescripcion());
        dto.setCosto(mantenimiento.getCosto());
        dto.setTecnicoResponsable(mantenimiento.getTecnicoResponsable());
        dto.setProximoMantenimiento(mantenimiento.getProximoMantenimiento());

        // Mapeo del tipo (Enum a String)
        if (mantenimiento.getTipo() != null) {
            dto.setTipo(mantenimiento.getTipo().name()); // CORRECTIVO, PREVENTIVO, etc.
        }

        // Mapeo de relaciones: Proveedor (solo el nombre)
        if (mantenimiento.getProveedor() != null) {
            dto.setProveedorNombre(mantenimiento.getProveedor().getNombreEmpresa());
        } else {
            dto.setProveedorNombre("Interno"); // Mantenimiento sin proveedor externo
        }

        return dto;
    }

}
