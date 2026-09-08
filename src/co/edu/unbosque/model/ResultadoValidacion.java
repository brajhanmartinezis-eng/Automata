package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ResultadoValidacion {

    private final List<String> errores = new ArrayList<String>();
    private final List<String> avisos = new ArrayList<String>();

    public void agregarError(String s) { errores.add(s); }

    public void agregarAviso(String s) { avisos.add(s); }

    public List<String> getErrores() { return Collections.unmodifiableList(errores); }

    public List<String> getAvisos() { return Collections.unmodifiableList(avisos); }

    public boolean esValido() { return errores.isEmpty(); }

    public boolean tieneAvisos() { return !avisos.isEmpty(); }

    public String erroresComoTexto() { return String.join("\n", errores); }

    public String avisosComoTexto() { return String.join("\n", avisos); }
}
