package co.edu.unbosque.controller;


public enum Herramienta {

    MOVER("Mover", "Arrastra los estados para reubicarlos"),
    ESTADO("+ Estado", "Clic en el lienzo para crear un estado"),
    TRANSICION("+ Transici\u00F3n", "Clic en el estado origen y luego en el destino"),
    BORRAR("Borrar", "Clic sobre un estado o sobre la etiqueta de una transici\u00F3n");

    private final String etiqueta;
    private final String ayuda;

    Herramienta(String etiqueta, String ayuda) {
        this.etiqueta = etiqueta;
        this.ayuda = ayuda;
    }

    public String getEtiqueta() { return etiqueta; }

    public String getAyuda() { return ayuda; }
}
