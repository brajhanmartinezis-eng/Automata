package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.List;


public class Reflejador implements VisitanteExpresion<ExpresionRegular> {

    public ExpresionRegular reflejar(ExpresionRegular e) { return e.aceptar(this); }

    @Override public ExpresionRegular verVacio(Vacio e) { return e; }

    @Override public ExpresionRegular verCadenaVacia(CadenaVacia e) { return e; }

    @Override public ExpresionRegular verSimbolo(Simbolo e) { return e; }

    @Override public ExpresionRegular verUnion(Union e) {
        List<ExpresionRegular> partes = new ArrayList<ExpresionRegular>();
        for (ExpresionRegular o : e.getOperandos()) partes.add(o.aceptar(this));
        return Expresion.union(partes);
    }

    @Override public ExpresionRegular verConcatenacion(Concatenacion e) {
        List<ExpresionRegular> factores = new ArrayList<ExpresionRegular>();
        List<ExpresionRegular> originales = e.getFactores();
        for (int i = originales.size() - 1; i >= 0; i--)
            factores.add(originales.get(i).aceptar(this));
        return Expresion.concatenacion(factores);
    }

    @Override public ExpresionRegular verEstrella(Estrella e) {
        return Expresion.estrella(e.getBase().aceptar(this));
    }

    @Override public ExpresionRegular verClausuraPositiva(ClausuraPositiva e) {
        return Expresion.clausuraPositiva(e.getBase().aceptar(this));
    }

    @Override public ExpresionRegular verPotencia(Potencia e) {
        return Expresion.potencia(e.getBase().aceptar(this), e.getExponente());
    }
}
