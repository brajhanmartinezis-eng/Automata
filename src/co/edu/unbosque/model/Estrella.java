package co.edu.unbosque.model;


public final class Estrella extends ExpresionRegular {

    private final ExpresionRegular base;

    Estrella(ExpresionRegular base) { this.base = base; }

    public ExpresionRegular getBase() { return base; }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verEstrella(this); }

    @Override public int precedencia() { return PREC_UNARIO; }

    @Override public boolean equals(Object o) {
        return o instanceof Estrella && ((Estrella) o).base.equals(base);
    }

    @Override public int hashCode() { return 31 * base.hashCode() + 3; }
}
