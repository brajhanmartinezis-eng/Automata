package co.edu.unbosque.model;


public final class CadenaVacia extends ExpresionRegular {

    public static final CadenaVacia INSTANCIA = new CadenaVacia();

    private CadenaVacia() { }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verCadenaVacia(this); }

    @Override public int precedencia() { return PREC_ATOMO; }

    @Override public boolean esCadenaVacia() { return true; }

    @Override public boolean equals(Object o) { return o instanceof CadenaVacia; }

    @Override public int hashCode() { return 19; }
}
