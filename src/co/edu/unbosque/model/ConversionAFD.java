package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/** Resultado de convertir un AFN a AFD: la traza paso a paso y el automata resultante. */
public class ConversionAFD {

    private final List<Paso> pasos = new ArrayList<Paso>();
    private Automata resultado;

    public Paso agregar(Paso p) {
        p.setNumero(pasos.size() + 1);
        pasos.add(p);
        return p;
    }

    public List<Paso> getPasos() { return Collections.unmodifiableList(pasos); }

    public Automata getResultado() { return resultado; }

    public void setResultado(Automata a) { resultado = a; }
}
