package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Paso {

    private int numero;
    private final String titulo;
    private final String explicacion;
    private String regla = "";
    private String estadoResaltado;

    private final List<String> desarrollo = new ArrayList<String>();
    private final List<String> situacion = new ArrayList<String>();

    public Paso(String titulo, String explicacion) {
        this.titulo = titulo;
        this.explicacion = explicacion;
    }

    public Paso conRegla(String r) { regla = r; return this; }

    public Paso resaltando(String estado) { estadoResaltado = estado; return this; }

    /** Linea que cambia en este paso: se pinta con el color del paso a paso. */
    public Paso desarrollo(String linea) { desarrollo.add(linea); return this; }

    /** Linea de contexto: como queda el sistema o el diagrama tras el paso. */
    public Paso situacion(String linea) { situacion.add(linea); return this; }

    void setNumero(int n) { numero = n; }

    public int getNumero() { return numero; }

    public String getTitulo() { return titulo; }

    public String getExplicacion() { return explicacion; }

    public String getRegla() { return regla; }

    public String getEstadoResaltado() { return estadoResaltado; }

    public List<String> getDesarrollo() { return Collections.unmodifiableList(desarrollo); }

    public List<String> getSituacion() { return Collections.unmodifiableList(situacion); }

    @Override public String toString() { return numero + ". " + titulo; }
}
