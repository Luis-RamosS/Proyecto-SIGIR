package sigir.vista;

import sigir.vista.paneles.PanelModuloSimple;

public class ModuloFrame extends BaseFrame {
    public ModuloFrame(String modulo,String usuarioActual){super(modulo,usuarioActual,modulo);establecerContenido(new PanelModuloSimple(modulo));}
}
