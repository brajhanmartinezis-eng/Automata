package co.edu.unbosque.model;


public abstract class ExpresionRegular {

    public static final int PREC_UNION  = 1;
    public static final int PREC_CONCAT = 2;
    public static final int PREC_UNARIO = 3;
    public static final int PREC_ATOMO  = 4;

    /** Punto de entrada del patron Visitante. */
    public abstract <T> T aceptar(VisitanteExpresion<T> visitante);

    /** Precedencia del operador, para decidir parentesis al imprimir. */
    public abstract int precedencia();

    public boolean esVacio()       { return false; }

    public boolean esCadenaVacia() { return false; }

    @Override
    public String toString() {
        return new RenderizadorTexto(Notacion.actual()).render(this);
    }

    @Override public abstract boolean equals(Object otro);

    @Override public abstract int hashCode();
}
