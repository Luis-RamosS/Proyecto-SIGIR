package sigir.modelo;

import java.time.LocalDate;

public class ActividadDiariaInicio {

    private LocalDate fecha;
    private int operaciones;

    public ActividadDiariaInicio() {
    }

    public ActividadDiariaInicio(
            LocalDate fecha,
            int operaciones) {

        this.fecha = fecha;
        this.operaciones = operaciones;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getOperaciones() {
        return operaciones;
    }

    public void setOperaciones(int operaciones) {
        this.operaciones = operaciones;
    }
}
