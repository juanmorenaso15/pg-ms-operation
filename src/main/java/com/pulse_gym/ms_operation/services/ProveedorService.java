// ProveedorService.java
package com.pulse_gym.ms_operation.services;

import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.ProveedorRequestDTO;
import com.pulse_gym.lb_common.dto.ProveedorResponseDTO;
import com.pulse_gym.lb_common.entity.operation.Proveedor;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_operation.repository.ProveedorRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    /**
     * Inyeccion de ProveedorRepository para manejar la lógica de negocio relacionada con
     * los proveedores, como el registro y la obtención de proveedores.
     */
    private final ProveedorRepository proveedorRepository;

    /**
     * Registra un nuevo proveedor en la base de datos.
     * 
     * Se valida que la peticion solo la puede hacer un entrenador, recepcionista o admin
     * 
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)}
     * @return MessegeGlobalDTO con un mensaje de éxito si el proveedor se registró correctamente
     */
    @Transactional
    public MessegeGlobalDTO registrarProveedor(ProveedorRequestDTO request, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        // Validar si ya existe un proveedor con el mismo email
        if (request.getEmail() != null && proveedorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un proveedor registrado con el email: " + request.getEmail());
        }
        
        // Crear nuevo proveedor
        Proveedor proveedor = new Proveedor();
        proveedor.setNombreEmpresa(request.getNombreEmpresa());
        proveedor.setContactoNombre(request.getContactoNombre());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        
        Proveedor saved = proveedorRepository.save(proveedor);
        
        return new MessegeGlobalDTO("Proveedor registrado exitosamente con ID: " + saved.getIdProveedor());
    }
    
    /**
     * Consulta todos los proveedores registrados en la base de datos.
     * 
     * Se valida que la peticion solo la puede hacer un Entrenador, recepcionista o admin
     * 
     * @param userRol Rol del usuario
     * @return Lista de proveedores registrados
     */
    public List<ProveedorResponseDTO> consultarTodosProveedores(String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);


        List<Proveedor> proveedores = proveedorRepository.findAll(Sort.by(Sort.Direction.ASC, "nombreEmpresa"));
        
        if (proveedores.isEmpty()) {
            throw new RuntimeException("No hay proveedores registrados");
        }
        
        return proveedores.stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Consulta un proveedor por su ID.
     * 
     * Se valida que la peticion solo la puede hacer un Entrenador, recepcionista o admin
     * 
     * @param id
     * @param userRol Rol del usuario
     * @return ProveedorResponseDTO con los datos del proveedor
     */
    public ProveedorResponseDTO consultarProveedorPorId(Long id, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        
        return convertirAResponseDTO(proveedor);
    }
    
    
    /**
     * Busca proveedores por su nombre.
     * 
     * Se valida que la peticion solo la puede hacer un Entrenador, recepcionista o admin
     * 
     * @param nombre
     * @param userRol Rol del usuario
     * @return Lista de proveedores que coinciden con el nombre
     */
    public List<ProveedorResponseDTO> buscarProveedoresPorNombre(String nombre, String userRol) {

        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);

        List<Proveedor> proveedores = proveedorRepository.findByNombreEmpresaContainingIgnoreCase(nombre);
        
        if (proveedores.isEmpty()) {
            throw new RuntimeException("No se encontraron proveedores con el nombre: " + nombre);
        }
        
        return proveedores.stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte un objeto Proveedor a un objeto ProveedorResponseDTO.
     * @param proveedor
     * @return ProveedorResponseDTO con los datos del proveedor
     */
    private ProveedorResponseDTO convertirAResponseDTO(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setIdProveedor(proveedor.getIdProveedor());
        dto.setNombreEmpresa(proveedor.getNombreEmpresa());
        dto.setContactoNombre(proveedor.getContactoNombre());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        
        // Contar equipos asociados (opcional)
        if (proveedor.getEquipos() != null) {
            dto.setCantidadEquipos(proveedor.getEquipos().size());
        } else {
            dto.setCantidadEquipos(0);
        }
        
        return dto;
    }
}