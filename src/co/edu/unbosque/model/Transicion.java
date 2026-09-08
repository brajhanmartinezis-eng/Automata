package co.edu.unbosque.model;

public class Transicion {

    private final Estado origen;
    private final Estado destino;
    private final String simbolo;

    public Transicion(Estado origen, String simbolo, Estado destino) {
        this.origen = origen;
        this.simbolo = simbolo;
        this.destino = destino;
    }

    public Estado getOrigen()  { return origen; }

    public Estado getDestino() { return destino; }

    public String getSimbolo() { return simbolo; }

    public boolean parte(Estado e) { return origen == e; }

    public boolean toca(Estado e) { return origen == e || destino == e; }

    @Override public String toString() {
        return origen.getNombre() + " --" + simbolo + "--> " + destino.getNombre();
    }
}
