package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ServicioDerivacion {

    private final List<MetodoDerivacion> metodos;
    private final ValidadorAutomata validador = new ValidadorAutomata();
    private boolean usarClausuraYPotencias = true;

    public ServicioDerivacion() {
        this.metodos = Collections.unmodifiableList(
                Arrays.<MetodoDerivacion>asList(new MetodoEliminacionEstados()));
    }

    public List<MetodoDerivacion> getMetodos() { return metodos; }

    public boolean isUsarClausuraYPotencias() { return usarClausuraYPotencias; }

    public void setUsarClausuraYPotencias(boolean v) { usarClausuraYPotencias = v; }

    public ResultadoValidacion validar(Automata a) { return validador.validar(a); }

    /**
     * Ejecuta el metodo indicado (funciona con AFD o AFN). Devuelve null si el
     * automata no es valido; en ese caso el controlador debe consultar validar().
     */
    public Derivacion derivar(Automata automata, MetodoDerivacion metodo) {
        if (!validador.validar(automata).esValido()) return null;

        Derivacion d = metodo.derivar(automata);

        if (usarClausuraYPotencias) {
            Simplificador s = new Simplificador();
            ExpresionRegular simple = s.simplificar(d.getExpresion());
            d.setExpresionSimplificada(simple);
            if (!simple.equals(d.getExpresion()))
                d.agregar(pasoReescritura(d.getExpresion(), simple, s.getBitacora()));
        }
        return d;
    }

    private Paso pasoReescritura(ExpresionRegular antes, ExpresionRegular despues,
                                 List<String> bitacora) {
        Paso p = new Paso("Reescritura con clausura positiva y potencias",
                "La expresi\u00F3n ya es correcta, pero se puede escribir m\u00E1s corta "
              + "usando los otros dos operadores del lenguaje regular: la clausura "
              + "positiva, que abrevia una repetici\u00F3n de una o m\u00E1s veces, y la "
              + "potencia, que abrevia un mismo factor repetido. Las dos formas "
              + "describen exactamente el mismo lenguaje.");
        p.conRegla("L L* = L* L = L\u207A          L L \u2026 L (n veces) = L\u207F");
        p.desarrollo("antes:    L = " + antes);
        for (String linea : bitacora) p.desarrollo("   " + linea);
        p.desarrollo("despu\u00E9s:  L = " + despues);
        return p;
    }

    // ------------------------------------------------------------------
    // Operaciones sobre el lenguaje obtenido
    // ------------------------------------------------------------------

    /** Reflexion del lenguaje: todas sus cadenas escritas al reves. */
    public ExpresionRegular reflexion(ExpresionRegular e) {
        return new Reflejador().reflejar(e);
    }

    /** Potencia del lenguaje: L concatenado consigo mismo n veces. */
    public ExpresionRegular potencia(ExpresionRegular e, int n) {
        return Expresion.potencia(e, n);
    }

    /** Todas las cadenas del lenguaje hasta cierta longitud, para comprobar a mano. */
    public List<String> cadenasDeEjemplo(Automata a, int longitudMaxima, int cantidad) {
        List<String> sigma = a.getAlfabeto().comoLista();
        List<String> resultado = new ArrayList<String>();
        List<List<String>> nivel = new ArrayList<List<String>>();
        nivel.add(new ArrayList<String>());

        for (int len = 0; len <= longitudMaxima && resultado.size() < cantidad; len++) {
            List<List<String>> siguiente = new ArrayList<List<String>>();
            for (List<String> cadena : nivel) {
                if (a.acepta(cadena) && resultado.size() < cantidad)
                    resultado.add(cadena.isEmpty() ? "\u03B5" : String.join("", cadena));
                for (String s : sigma) {
                    List<String> nueva = new ArrayList<String>(cadena);
                    nueva.add(s);
                    siguiente.add(nueva);
                }
            }
            nivel = siguiente;
            if (nivel.size() > 20000) break;
        }
        return resultado;
    }
}
