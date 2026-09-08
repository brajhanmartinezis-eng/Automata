package co.edu.unbosque.model;


public final class Simbolo extends ExpresionRegular {

    private final String valor;

    public Simbolo(String valor) {
        if (valor == null || valor.isEmpty())
            throw new IllegalArgumentException("El simbolo no puede estar vacio.");
        this.valor = valor;
    }

    public String getValor() { return valor; }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verSimbolo(this); }

    /** Un simbolo de varios caracteres se comporta como una concatenacion al imprimir. */
    @Override public int precedencia() {
        return valor.length() == 1 ? PREC_ATOMO : PREC_CONCAT;
    }

    @Override public boolean equals(Object o) {
        return o instanceof Simbolo && ((Simbolo) o).valor.equals(valor);
    }

    @Override public int hashCode() { return valor.hashCode(); }
}
