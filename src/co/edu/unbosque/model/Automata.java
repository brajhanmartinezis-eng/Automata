package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class Automata {

    private final List<Estado> estados = new ArrayList<Estado>();
    private final List<Transicion> transiciones = new ArrayList<Transicion>();
    private final List<OyenteAutomata> oyentes = new ArrayList<OyenteAutomata>();

    private String nombre = "Aut\u00F3mata sin t\u00EDtulo";

    // ------------------------------------------------------------------
    // Observadores
    // ------------------------------------------------------------------

    public void agregarOyente(OyenteAutomata o) { oyentes.add(o); }

    public void quitarOyente(OyenteAutomata o) { oyentes.remove(o); }

    private void notificar() {
        for (OyenteAutomata o : new ArrayList<OyenteAutomata>(oyentes))
            o.automataCambio(this);
    }

    public void avisarCambio() { notificar(); }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public List<Estado> getEstados() { return Collections.unmodifiableList(estados); }

    public List<Transicion> getTransiciones() {
        return Collections.unmodifiableList(transiciones);
    }

    public String getNombre() { return nombre; }

    public void setNombre(String n) { nombre = n; notificar(); }

    public boolean estaVacio() { return estados.isEmpty(); }

    public Estado getInicial() {
        for (Estado e : estados) if (e.esInicial()) return e;
        return null;
    }

    public List<Estado> getEstadosAceptacion() {
        List<Estado> l = new ArrayList<Estado>();
        for (Estado e : estados) if (e.esAceptacion()) l.add(e);
        return l;
    }

    public Estado buscarEstado(String nombre) {
        for (Estado e : estados) if (e.getNombre().equals(nombre)) return e;
        return null;
    }

    public Alfabeto getAlfabeto() {
        Alfabeto a = new Alfabeto();
        for (Transicion t : transiciones) a.agregar(t.getSimbolo());
        return a;
    }

    /** Transicion definida para (estado, simbolo), o null si no existe. */
    public Transicion transicionDe(Estado origen, String simbolo) {
        for (Transicion t : transiciones)
            if (t.getOrigen() == origen && t.getSimbolo().equals(simbolo)) return t;
        return null;
    }

    public List<Transicion> transicionesEntre(Estado origen, Estado destino) {
        List<Transicion> l = new ArrayList<Transicion>();
        for (Transicion t : transiciones)
            if (t.getOrigen() == origen && t.getDestino() == destino) l.add(t);
        return l;
    }

    /** Simula el automata sobre una cadena, tratandolo como AFD posiblemente incompleto. */
    public boolean acepta(List<String> cadena) {
        Estado actual = getInicial();
        if (actual == null) return false;
        for (String s : cadena) {
            Transicion t = transicionDe(actual, s);
            if (t == null) return false;
            actual = t.getDestino();
        }
        return actual.esAceptacion();
    }

    public Set<Estado> estadosAlcanzables() {
        Set<Estado> vistos = new LinkedHashSet<Estado>();
        Estado ini = getInicial();
        if (ini == null) return vistos;
        List<Estado> cola = new ArrayList<Estado>();
        cola.add(ini);
        vistos.add(ini);
        while (!cola.isEmpty()) {
            Estado c = cola.remove(0);
            for (Transicion t : transiciones)
                if (t.getOrigen() == c && vistos.add(t.getDestino())) cola.add(t.getDestino());
        }
        return vistos;
    }

    public String proponerNombre() {
        for (int i = 0; i < 1000; i++)
            if (buscarEstado("q" + i) == null) return "q" + i;
        return "q" + System.currentTimeMillis();
    }

    // ------------------------------------------------------------------
    // Modificaciones
    // ------------------------------------------------------------------

    public Estado agregarEstado(String nombre, int x, int y) {
        Estado e = new Estado(nombre, x, y);
        if (estados.isEmpty()) e.setInicial(true);
        estados.add(e);
        notificar();
        return e;
    }

    public void eliminarEstado(Estado e) {
        estados.remove(e);
        List<Transicion> fuera = new ArrayList<Transicion>();
        for (Transicion t : transiciones) if (t.toca(e)) fuera.add(t);
        transiciones.removeAll(fuera);
        notificar();
    }

    public void renombrarEstado(Estado e, String nuevo) {
        e.setNombre(nuevo);
        notificar();
    }

    public void moverEstado(Estado e, int x, int y) {
        e.mover(x, y);
        notificar();
    }

    public void marcarInicial(Estado e) {
        for (Estado o : estados) o.setInicial(false);
        e.setInicial(true);
        notificar();
    }

    public void alternarAceptacion(Estado e) {
        e.setAceptacion(!e.esAceptacion());
        notificar();
    }

    /**
     * Agrega una transicion. Devuelve false si romperia el determinismo,
     * es decir si ya existe otra transicion desde el mismo estado con el
     * mismo simbolo.
     */
    public boolean agregarTransicion(Estado origen, String simbolo, Estado destino) {
        if (transicionDe(origen, simbolo) != null) return false;
        transiciones.add(new Transicion(origen, simbolo, destino));
        notificar();
        return true;
    }

    public void eliminarTransiciones(List<Transicion> ts) {
        transiciones.removeAll(ts);
        notificar();
    }

    /** Reemplaza el contenido conservando la identidad del objeto y los oyentes. */
    public void reemplazarPor(Automata otro) {
        estados.clear();
        transiciones.clear();
        estados.addAll(otro.estados);
        transiciones.addAll(otro.transiciones);
        nombre = otro.nombre;
        notificar();
    }

    public void limpiar() {
        estados.clear();
        transiciones.clear();
        nombre = "Aut\u00F3mata sin t\u00EDtulo";
        notificar();
    }

    /** Version sin notificaciones, para construir automatas en memoria. */
    public Estado agregarEstadoSilencioso(String nombre, int x, int y,
                                          boolean inicial, boolean aceptacion) {
        Estado e = new Estado(nombre, x, y);
        e.setInicial(inicial);
        e.setAceptacion(aceptacion);
        estados.add(e);
        return e;
    }

    public void agregarTransicionSilenciosa(Estado o, String s, Estado d) {
        transiciones.add(new Transicion(o, s, d));
    }
}
