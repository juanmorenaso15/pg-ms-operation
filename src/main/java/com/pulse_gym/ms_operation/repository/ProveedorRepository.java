package com.pulse_gym.ms_operation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.pulse_gym.lb_common.entity.operation.Proveedor;
import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long>, 
                                            JpaSpecificationExecutor<Proveedor> {
    
    /**
     * Busca un proveedor por el nombre de empresa (parcial, case insensitive)
     * @param nombreEmpresa
     * @return List<Proveedor> con los proveedores encontrados
     */
    List<Proveedor> findByNombreEmpresaContainingIgnoreCase(String nombreEmpresa);
    
    /**
     * Busca un proveedor por el email
     * @param email
     * @return List<Proveedor> con los proveedores encontrados
     */
    List<Proveedor> findByEmail(String email);
    
    /**
     * Busca un proveedor por el teléfono
     * @param telefono
     * @return List<Proveedor> con los proveedores encontrados
     */
    List<Proveedor> findByTelefono(String telefono);
    
    /**
     * Verifica si existe un proveedor por el email
     * @param email
     * @return boolean true si existe, false si no existe
     */
    boolean existsByEmail(String email);
    
}