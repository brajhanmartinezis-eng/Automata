package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MetodoEliminacionEstados implements MetodoDerivacion {

    private final List<String> nodos = new ArrayList<String>();
    private final Map<String, Map<String, ExpresionRegular>> aristas =
            new LinkedHashMap<String, Map<String, ExpresionRegular>>();

    private String entrada, salida;
    private Derivacion derivacion;

    @Override public String getNombre() {
        return "Eliminaci\u00F3n de estados";
    }

    @Override public String getDescripcion() {
        return "Borra los estados intermedios uno a uno reconstruyendo los caminos.";
    }

    // ------------------------------------------------------------------

    @Override
    public Derivacion derivar(Automata automata) {
        derivacion = new Derivacion(getNombre());
        nodos.clear();
        aristas.clear();

        construirGrafo(automata);
        pasoConstruccion(automata);

        List<String> intermedios = new ArrayList<String>(nodos);
        intermedios.remove(entrada);
        intermedios.remove(salida);

        while (!intermedios.isEmpty()) {
            String q = elegirSiguiente(intermedios);
            pasoEliminacion(q);
            intermedios.remove(q);
        }

        ExpresionRegular resultado = etiqueta(entrada, salida);
        pasoFinal(resultado);
        derivacion.setExpresion(resultado);
        return derivacion;
    }

    // ------------------------------------------------------------------

    private void construirGrafo(Automata a) {
        Set<String> usados = new HashSet<String>();
        for (Estado e : a.getEstados()) usados.add(e.getNombre());
        entrada = nombreLibre("ENTRADA", usados);
        salida = nombreLibre("SALIDA", usados);

        nodos.add(entrada);
        for (Estado e : a.getEstados()) nodos.add(e.getNombre());
        nodos.add(salida);

        for (Transicion t : a.getTransiciones()) {
            String o = t.getOrigen().getNombre(), d = t.getDestino().getNombre();
            ponerEtiqueta(o, d, Expresion.union(etiqueta(o, d),
                    Expresion.simbolo(t.getSimbolo())));
        }
        ponerEtiqueta(entrada, a.getInicial().getNombre(), Expresion.cadenaVacia());
        for (Estado e : a.getEstadosAceptacion())
            ponerEtiqueta(e.getNombre(), salida,
                    Expresion.union(etiqueta(e.getNombre(), salida),
                            Expresion.cadenaVacia()));
    }

    private void pasoConstruccion(Automata a) {
        Paso p = new Paso("Preparaci\u00F3n del diagrama",
                "El alfabeto es \u03A3 = " + a.getAlfabeto() + ". Se agregan dos nodos "
              + "auxiliares: " + entrada + ", unido con \u03B5 al estado inicial, y "
              + salida + ", al que llega con \u03B5 cada estado de aceptaci\u00F3n. As\u00ED "
              + "el diagrama queda con una sola entrada y una sola salida, que es lo que "
              + "el m\u00E9todo necesita. A partir de aqu\u00ED las aristas dejan de estar "
              + "etiquetadas con s\u00EDmbolos sueltos y pasan a estarlo con expresiones "
              + "regulares completas.");
        p.conRegla("Una sola " + entrada + " sin aristas de llegada, "
                + "una sola " + salida + " sin aristas de salida");
        for (String i : nodos)
            for (String j : nodos)
                if (!etiqueta(i, j).esVacio())
                    p.desarrollo("R(" + i + ", " + j + ") = " + etiqueta(i, j));
        derivacion.agregar(p);
    }

    private void pasoEliminacion(String q) {
        String u = Notacion.actual().getUnion().trim();
        Paso p = new Paso("Eliminaci\u00F3n del estado " + q,
                "Se borra " + q + " y se reconstruye cada camino que pasaba por \u00E9l. "
              + "Para cada par de estados i, j que sobreviven, el camino i \u2192 " + q
              + " \u2192 j se arma por concatenaci\u00F3n, dejando en medio la estrella "
              + "del bucle de " + q + " para permitir cualquier n\u00FAmero de vueltas, y "
              + "el resultado se une con la arista i \u2192 j que ya exist\u00EDa.");
        p.conRegla("R(i,j) := R(i,j) " + u + " R(i," + q + ") R(" + q + "," + q
                + ")* R(" + q + ",j)");
        p.resaltando(q);

        ExpresionRegular bucle = etiqueta(q, q);
        ExpresionRegular estrella = Expresion.estrella(bucle);
        p.desarrollo("bucle:  R(" + q + "," + q + ") = " + bucle
                + "        R* = " + estrella);

        List<String> restantes = new ArrayList<String>();
        for (String x : nodos) if (!x.equals(q)) restantes.add(x);

        boolean hubo = false;
        for (String i : restantes) {
            ExpresionRegular llegada = etiqueta(i, q);
            if (llegada.esVacio()) continue;
            for (String j : restantes) {
                ExpresionRegular salidaQ = etiqueta(q, j);
                if (salidaQ.esVacio()) continue;
                ExpresionRegular previa = etiqueta(i, j);
                ExpresionRegular camino = Expresion.concatenacion(
                        java.util.Arrays.asList(llegada, estrella, salidaQ));
                ExpresionRegular nueva = Expresion.union(previa, camino);
                ponerEtiqueta(i, j, nueva);
                hubo = true;
                p.desarrollo("R(" + i + "," + j + ") = " + previa + " " + u + " "
                        + factor(llegada) + factor(estrella) + factor(salidaQ)
                        + "   =   " + nueva);
            }
        }
        if (!hubo) p.desarrollo("(ning\u00FAn camino pasaba por " + q + ")");

        nodos.remove(q);
        aristas.remove(q);
        for (Map<String, ExpresionRegular> m : aristas.values()) m.remove(q);

        p.situacion("Aristas que quedan:");
        for (String i : nodos)
            for (String j : nodos)
                if (!etiqueta(i, j).esVacio())
                    p.situacion("   R(" + i + ", " + j + ") = " + etiqueta(i, j));
        derivacion.agregar(p);
    }

    private void pasoFinal(ExpresionRegular resultado) {
        Paso p = new Paso("Expresi\u00F3n regular resultante",
                "Solo quedan " + entrada + " y " + salida + ". La etiqueta de la "
              + "\u00FAnica arista que los une es la expresi\u00F3n regular del lenguaje "
              + "que acepta el aut\u00F3mata.");
        p.conRegla("L = R(" + entrada + ", " + salida + ")");
        p.desarrollo("L = " + resultado);
        derivacion.agregar(p);
    }

    // ------------------------------------------------------------------
    // Grafo
    // ------------------------------------------------------------------

    private ExpresionRegular etiqueta(String i, String j) {
        Map<String, ExpresionRegular> m = aristas.get(i);
        if (m == null) return Expresion.vacio();
        ExpresionRegular e = m.get(j);
        return e == null ? Expresion.vacio() : e;
    }

    private void ponerEtiqueta(String i, String j, ExpresionRegular e) {
        Map<String, ExpresionRegular> m = aristas.get(i);
        if (m == null) {
            m = new LinkedHashMap<String, ExpresionRegular>();
            aristas.put(i, m);
        }
        m.put(j, e);
    }

    /** Heuristica: eliminar primero el estado menos conectado acorta la expresion. */
    private String elegirSiguiente(List<String> intermedios) {
        String mejor = intermedios.get(0);
        int mejorCosto = Integer.MAX_VALUE;
        for (String q : intermedios) {
            int entran = 0, salen = 0;
            for (String x : nodos) {
                if (x.equals(q)) continue;
                if (!etiqueta(x, q).esVacio()) entran++;
                if (!etiqueta(q, x).esVacio()) salen++;
            }
            int costo = entran * salen;
            if (costo < mejorCosto) { mejorCosto = costo; mejor = q; }
        }
        return mejor;
    }

    private String nombreLibre(String base, Set<String> usados) {
        String n = base;
        while (usados.contains(n)) n = n + "_";
        usados.add(n);
        return n;
    }

    private String factor(ExpresionRegular e) {
        String s = e.toString();
        return e.precedencia() < ExpresionRegular.PREC_CONCAT ? "(" + s + ")" : s;
    }
}
