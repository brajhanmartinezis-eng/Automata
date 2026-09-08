package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class Concatenacion extends ExpresionRegular {

    private final List<ExpresionRegular> factores;

    Concatenacion(List<ExpresionRegular> factores) {
        if (factores.size() < 2)
            throw new IllegalArgumentException("Una concatenacion necesita al menos dos factores.");
        this.factores = Collections.unmodifiableList(new ArrayList<ExpresionRegular>(factores));
    }

    public List<ExpresionRegular> getFactores() { return factores; }

    @Override public <T> T aceptar(VisitanteExpresion<T> v) { return v.verConcatenacion(this); }

    @Override public int precedencia() { return PREC_CONCAT; }

    @Override public boolean equals(Object o) {
        return o instanceof Concatenacion && ((Concatenacion) o).factores.equals(factores);
    }

    @Override public int hashCode() { return 31 * factores.hashCode() + 2; }
}
