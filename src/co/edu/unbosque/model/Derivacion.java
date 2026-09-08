package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Derivacion {

    private final String nombreMetodo;
    private final List<Paso> pasos = new ArrayList<Paso>();
    private ExpresionRegular expresion;
    private ExpresionRegular expresionSimplificada;

    public Derivacion(String nombreMetodo) { this.nombreMetodo = nombreMetodo; }

    public Paso agregar(Paso p) {
        p.setNumero(pasos.size() + 1);
        pasos.add(p);
        return p;
    }

    public String getNombreMetodo() { return nombreMetodo; }

    public List<Paso> getPasos() { return Collections.unmodifiableList(pasos); }

    public ExpresionRegular getExpresion() { return expresion; }

    public void setExpresion(ExpresionRegular e) { expresion = e; }

    /** Expresion tras aplicar clausura positiva y potencias; puede ser la misma. */
    public ExpresionRegular getExpresionSimplificada() {
        return expresionSimplificada == null ? expresion : expresionSimplificada;
    }

    public void setExpresionSimplificada(ExpresionRegular e) { expresionSimplificada = e; }

    public boolean huboSimplificacion() {
        return expresionSimplificada != null && !expresionSimplificada.equals(expresion);
    }
}
