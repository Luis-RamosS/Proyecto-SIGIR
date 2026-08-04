package sigir.controlador;

import sigir.modelo.ModuloInicio;

@FunctionalInterface
public interface NavegacionInicioListener {

    void abrirModulo(ModuloInicio modulo);
}
