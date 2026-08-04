package sigir.modelo;

public enum TipoReporte {
    VENTAS("VENTAS", "Ventas registradas",
            "Detalle de las ventas realizadas en el período.",
            true, true, true),
    COMPRAS("COMPRAS", "Compras registradas",
            "Detalle de compras realizadas a proveedores.",
            true, true, true),
    INVENTARIO("INVENTARIO", "Inventario general",
            "Existencias y valor actual del inventario.",
            false, false, false),
    STOCK_BAJO("STOCK_BAJO", "Productos con stock bajo",
            "Productos agotados o cercanos al mínimo.",
            false, false, false),
    PRODUCTOS_MAS_VENDIDOS(
            "PRODUCTOS_MAS_VENDIDOS",
            "Productos más vendidos",
            "Productos con mayor cantidad vendida.",
            true, false, true),
    MOVIMIENTOS("MOVIMIENTOS", "Movimientos de inventario",
            "Entradas, salidas y ajustes de existencias.",
            true, true, true),
    CREDITOS("CREDITOS", "Créditos",
            "Créditos pendientes, vencidos, pagados o anulados.",
            true, true, false),
    REPARACIONES("REPARACIONES", "Reparaciones",
            "Órdenes de servicio y su estado actual.",
            true, true, true),
    ACTIVIDAD_USUARIOS(
            "ACTIVIDAD_USUARIOS",
            "Actividad por usuario",
            "Operaciones realizadas por cada usuario.",
            true, false, true);

    private final String codigo;
    private final String nombreVisible;
    private final String descripcion;
    private final boolean usaFechas;
    private final boolean usaEstado;
    private final boolean usaUsuario;

    TipoReporte(
            String codigo,
            String nombreVisible,
            String descripcion,
            boolean usaFechas,
            boolean usaEstado,
            boolean usaUsuario) {
        this.codigo = codigo;
        this.nombreVisible = nombreVisible;
        this.descripcion = descripcion;
        this.usaFechas = usaFechas;
        this.usaEstado = usaEstado;
        this.usaUsuario = usaUsuario;
    }

    public String getCodigo() { return codigo; }
    public String getNombreVisible() { return nombreVisible; }
    public String getDescripcion() { return descripcion; }
    public boolean isUsaFechas() { return usaFechas; }
    public boolean isUsaEstado() { return usaEstado; }
    public boolean isUsaUsuario() { return usaUsuario; }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
