package co.edu.unbosque.model;


public interface MetodoDerivacion {

    String getNombre();

    String getDescripcion();

    Derivacion derivar(Automata automata);
}
