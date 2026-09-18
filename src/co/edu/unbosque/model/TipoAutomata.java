package co.edu.unbosque.model;


public enum TipoAutomata {

    AFD("AFD"),
    AFN("AFN");

    private final String etiqueta;

    TipoAutomata(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }

    public static TipoAutomata de(Automata a) {
        return a.esDeterminista() ? AFD : AFN;
    }
}
