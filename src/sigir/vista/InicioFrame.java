package sigir.vista;

import sigir.vista.paneles.InicioPanel;

public class InicioFrame extends BaseFrame {
    public InicioFrame(String usuarioActual){super("Inicio",usuarioActual,"Inicio");establecerContenido(new InicioPanel());}
}
