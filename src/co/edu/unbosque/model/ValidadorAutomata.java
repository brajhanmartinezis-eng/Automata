package co.edu.unbosque.model;

import java.util.List;
import java.util.Set;

public class ValidadorAutomata {

    private static final String VINETA = "• ";

    public ResultadoValidacion validar(Automata a) {
        ResultadoValidacion r = new ResultadoValidacion();

        if (a.estaVacio()) {
            r.agregarError(VINETA + "El autómata no tiene estados.");
            return r;
        }

        int iniciales = 0;
        for (Estado e : a.getEstados()) if (e.esInicial()) iniciales++;
        if (iniciales == 0)
            r.agregarError(VINETA + "Falta marcar el estado inicial "
                    + "(clic derecho sobre un estado).");
        if (iniciales > 1)
            r.agregarError(VINETA + "Hay " + iniciales + " estados iniciales; "
                    + "un autómata admite exactamente uno.");

        if (a.getEstadosAceptacion().isEmpty())
            r.agregarError(VINETA + "No hay estados de aceptación "
                    + "(doble clic sobre un estado para marcarlo).");

        boolean determinista = a.esDeterminista();

        if (determinista) {
            List<String> sigma = a.getAlfabeto().comoLista();
            for (Estado e : a.getEstados()) {
                StringBuilder faltan = new StringBuilder();
                for (String s : sigma)
                    if (a.transicionDe(e, s) == null) {
                        if (faltan.length() > 0) faltan.append(", ");
                        faltan.append(s);
                    }
                if (faltan.length() > 0)
                    r.agregarAviso(VINETA + e.getNombre() + " no define transición para: "
                            + faltan + " (AFD incompleto: se toma como transición al vacío).");
            }
        }

        Set<Estado> alcanzables = a.estadosAlcanzables();
        for (Estado e : a.getEstados())
            if (!alcanzables.contains(e))
                r.agregarAviso(VINETA + e.getNombre()
                        + " no es alcanzable desde el estado inicial.");

        return r;
    }
}
