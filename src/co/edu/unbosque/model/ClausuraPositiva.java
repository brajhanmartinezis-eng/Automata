package co.edu.unbosque.model;


public final class ClausuraPositiva extends ExpresionRegular {

    private final ExpresionRegular base;

    ClausuraPositiva(ExpresionRegular base) { this.base = base; }

    public ExpresionRegular getBase() { return base; }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verClausuraPositiva(this); }

    @Override public int precedencia() { return PREC_UNARIO; }

    @Override public boolean equals(Object o) {
        return o instanceof ClausuraPositiva && ((ClausuraPositiva) o).base.equals(base);
    }

    @Override public int hashCode() { return 31 * base.hashCode() + 4; }
}
