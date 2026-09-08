package co.edu.unbosque.model;

import java.util.List;


public class RenderizadorTexto implements VisitanteExpresion<String> {

    private final Notacion notacion;

    public RenderizadorTexto(Notacion notacion) { this.notacion = notacion; }

    public String render(ExpresionRegular e) { return e.aceptar(this); }

    /** Envuelve al hijo en parentesis si su precedencia es menor que la exigida. */
    private String envolver(ExpresionRegular hijo, int precedenciaMinima) {
        String s = hijo.aceptar(this);
        return hijo.precedencia() < precedenciaMinima ? "(" + s + ")" : s;
    }

    private String unir(List<ExpresionRegular> partes, String separador, int precedenciaMinima) {
        StringBuilder sb = new StringBuilder();
        for (ExpresionRegular p : partes) {
            if (sb.length() > 0) sb.append(separador);
            sb.append(envolver(p, precedenciaMinima));
        }
        return sb.toString();
    }

    @Override public String verVacio(Vacio e) { return notacion.getVacio(); }

    @Override public String verCadenaVacia(CadenaVacia e) { return notacion.getCadenaVacia(); }

    @Override public String verSimbolo(Simbolo e) { return e.getValor(); }

    @Override public String verUnion(Union e) {
        return unir(e.getOperandos(), notacion.getUnion(), ExpresionRegular.PREC_UNION);
    }

    @Override public String verConcatenacion(Concatenacion e) {
        return unir(e.getFactores(), notacion.getConcatenacion(), ExpresionRegular.PREC_CONCAT);
    }

    @Override public String verEstrella(Estrella e) {
        return envolver(e.getBase(), ExpresionRegular.PREC_UNARIO) + notacion.getEstrella();
    }

    @Override public String verClausuraPositiva(ClausuraPositiva e) {
        return envolver(e.getBase(), ExpresionRegular.PREC_UNARIO) + notacion.getClausuraPositiva();
    }

    @Override public String verPotencia(Potencia e) {
        return envolver(e.getBase(), ExpresionRegular.PREC_UNARIO)
                + notacion.exponente(e.getExponente());
    }
}
