package co.edu.unbosque.model;


public final class Potencia extends ExpresionRegular {

    private final ExpresionRegular base;
    private final int exponente;

    Potencia(ExpresionRegular base, int exponente) {
        if (exponente < 2)
            throw new IllegalArgumentException("Use Expresion.potencia para exponentes 0 y 1.");
        this.base = base;
        this.exponente = exponente;
    }

    public ExpresionRegular getBase() { return base; }

    public int getExponente() { return exponente; }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verPotencia(this); }

    @Override public int precedencia() { return PREC_UNARIO; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Potencia)) return false;
        Potencia p = (Potencia) o;
        return p.exponente == exponente && p.base.equals(base);
    }

    @Override public int hashCode() { return 31 * base.hashCode() + exponente; }
}
