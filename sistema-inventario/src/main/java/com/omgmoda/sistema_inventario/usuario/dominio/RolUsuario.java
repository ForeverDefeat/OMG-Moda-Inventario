package com.omgmoda.sistema_inventario.usuario.dominio;

/**
 * Enum puro de dominio que define los roles de acceso al sistema (RF07, RNF01).
 *
 * ADMIN   : acceso completo — gestión de usuarios, productos, inventario,
 *           ventas, reportes, backups y ajustes de configuración.
 * VENDEDOR: acceso restringido — registro de ventas, consulta de stock
 *           y búsqueda en el catálogo de productos.
 */
public enum RolUsuario {
    ADMIN,
    VENDEDOR;

    /**
     * Retorna el nombre del rol con el prefijo requerido por Spring Security
     * para la evaluación de @PreAuthorize("hasRole('ADMIN')").
     */
    public String toSpringRole() {
        return "ROLE_" + this.name();
    }
}
