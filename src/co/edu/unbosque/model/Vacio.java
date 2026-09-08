package co.edu.unbosque.model;

public final class Vacio extends ExpresionRegular {

    public static final Vacio INSTANCIA = new Vacio();

    private Vacio() { }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verVacio(this); }

    @Override public int precedencia() { return PREC_ATOMO; }

    @Override public boolean esVacio() { return true; }

    @Override public boolean equals(Object o) { return o instanceof Vacio; }

    @Override public int hashCode() { return 17; }
}
