package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Union extends ExpresionRegular {

    private final List<ExpresionRegular> operandos;

    /** Se construye desde Expresion.union, que ya aplana y simplifica. */
    Union(List<ExpresionRegular> operandos) {
        if (operandos.size() < 2)
            throw new IllegalArgumentException("Una union necesita al menos dos operandos.");
        this.operandos = Collections.unmodifiableList(new ArrayList<ExpresionRegular>(operandos));
    }

    public List<ExpresionRegular> getOperandos() { return operandos; }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verUnion(this); }

    @Override public int precedencia() { return PREC_UNION; }

    @Override public boolean equals(Object o) {
        return o instanceof Union && ((Union) o).operandos.equals(operandos);
    }

    @Override public int hashCode() { return 31 * operandos.hashCode() + 1; }
}
