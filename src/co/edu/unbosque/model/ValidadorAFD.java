package co.edu.unbosque.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ValidadorAFD {

    private static final String VINETA = "\u2022 ";

    public ResultadoValidacion validar(Automata a) {
        ResultadoValidacion r = new ResultadoValidacion();

        if (a.estaVacio()) {
            r.agregarError(VINETA + "El aut\u00F3mata no tiene estados.");
            return r;
        }

        int iniciales = 0;
        for (Estado e : a.getEstados()) if (e.esInicial()) iniciales++;
        if (iniciales == 0)
            r.agregarError(VINETA + "Falta marcar el estado inicial "
                    + "(clic derecho sobre un estado).");
        if (iniciales > 1)
            r.agregarError(VINETA + "Hay " + iniciales + " estados iniciales; "
                    + "un AFD admite exactamente uno.");

        if (a.getEstadosAceptacion().isEmpty())
            r.agregarError(VINETA + "No hay estados de aceptaci\u00F3n "
                    + "(doble clic sobre un estado para marcarlo).");

        for (Estado e : a.getEstados()) {
            Set<String> vistos = new LinkedHashSet<String>();
            for (Transicion t : a.getTransiciones()) {
                if (t.getOrigen() != e) continue;
                if (!vistos.add(t.getSimbolo()))
                    r.agregarError(VINETA + "No es determinista: el estado " + e.getNombre()
                            + " define dos veces el s\u00EDmbolo '" + t.getSimbolo() + "'.");
            }
        }

        List<String> sigma = a.getAlfabeto().comoLista();
        for (Estado e : a.getEstados()) {
            StringBuilder faltan = new StringBuilder();
            for (String s : sigma)
                if (a.transicionDe(e, s) == null) {
                    if (faltan.length() > 0) faltan.append(", ");
                    faltan.append(s);
                }
            if (faltan.length() > 0)
                r.agregarAviso(VINETA + e.getNombre() + " no define transici\u00F3n para: "
                        + faltan + " (AFD incompleto: se toma como transici\u00F3n al vac\u00EDo).");
        }

        Set<Estado> alcanzables = a.estadosAlcanzables();
        for (Estado e : a.getEstados())
            if (!alcanzables.contains(e))
                r.agregarAviso(VINETA + e.getNombre()
                        + " no es alcanzable desde el estado inicial.");

        return r;
    }
}
