package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class Alfabeto {

    private final Set<String> simbolos = new LinkedHashSet<String>();

    public void agregar(String s) { simbolos.add(s); }

    public boolean contiene(String s) { return simbolos.contains(s); }

    public int tamano() { return simbolos.size(); }

    public boolean estaVacio() { return simbolos.isEmpty(); }

    public List<String> comoLista() {
        List<String> l = new ArrayList<String>(simbolos);
        Collections.sort(l);
        return l;
    }

    /** Representacion habitual del alfabeto. */
    @Override public String toString() {
        return "{" + String.join(", ", comoLista()) + "}";
    }
}
