package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class Expresion {

    private Expresion() { }

    // ------------------------------------------------------------------
    // Atomos
    // ------------------------------------------------------------------

    public static ExpresionRegular vacio() { return Vacio.INSTANCIA; }

    public static ExpresionRegular cadenaVacia() { return CadenaVacia.INSTANCIA; }

    public static ExpresionRegular simbolo(String s) { return new Simbolo(s); }

    // ------------------------------------------------------------------
    // Union
    // ------------------------------------------------------------------

    public static ExpresionRegular union(ExpresionRegular a, ExpresionRegular b) {
        return union(Arrays.asList(a, b));
    }

    public static ExpresionRegular union(List<ExpresionRegular> partes) {
        List<ExpresionRegular> planas = new ArrayList<ExpresionRegular>();
        for (ExpresionRegular e : partes) {
            if (e.esVacio()) continue;                       // L U vacio = L
            if (e instanceof Union) planas.addAll(((Union) e).getOperandos());
            else planas.add(e);
        }
        List<ExpresionRegular> unicas = new ArrayList<ExpresionRegular>();
        for (ExpresionRegular e : planas)
            if (!unicas.contains(e)) unicas.add(e);          // L U L = L
        if (unicas.isEmpty()) return vacio();
        if (unicas.size() == 1) return unicas.get(0);
        return new Union(unicas);
    }

    // ------------------------------------------------------------------
    // Concatenacion
    // ------------------------------------------------------------------

    public static ExpresionRegular concatenacion(ExpresionRegular a, ExpresionRegular b) {
        return concatenacion(Arrays.asList(a, b));
    }

    public static ExpresionRegular concatenacion(List<ExpresionRegular> partes) {
        List<ExpresionRegular> planas = new ArrayList<ExpresionRegular>();
        for (ExpresionRegular e : partes) {
            if (e.esVacio()) return vacio();                 // L . vacio = vacio
            if (e.esCadenaVacia()) continue;                 // L . cadenaVacia = L
            if (e instanceof Concatenacion) planas.addAll(((Concatenacion) e).getFactores());
            else planas.add(e);
        }
        if (planas.isEmpty()) return cadenaVacia();
        if (planas.size() == 1) return planas.get(0);
        return new Concatenacion(planas);
    }

    // ------------------------------------------------------------------
    // Operadores unarios
    // ------------------------------------------------------------------

    public static ExpresionRegular estrella(ExpresionRegular base) {
        if (base.esVacio() || base.esCadenaVacia()) return cadenaVacia();
        if (base instanceof Estrella) return base;                          // (L*)* = L*
        if (base instanceof ClausuraPositiva)                               // (L+)* = L*
            return new Estrella(((ClausuraPositiva) base).getBase());
        return new Estrella(base);
    }

    public static ExpresionRegular clausuraPositiva(ExpresionRegular base) {
        if (base.esVacio()) return vacio();                                 // vacio+ = vacio
        if (base.esCadenaVacia()) return cadenaVacia();
        if (base instanceof Estrella) return base;                          // (L*)+ = L*
        if (base instanceof ClausuraPositiva) return base;                  // (L+)+ = L+
        return new ClausuraPositiva(base);
    }

    public static ExpresionRegular potencia(ExpresionRegular base, int n) {
        if (n < 0) throw new IllegalArgumentException("Exponente negativo: " + n);
        if (n == 0) return cadenaVacia();
        if (n == 1) return base;
        if (base.esVacio()) return vacio();
        if (base.esCadenaVacia()) return cadenaVacia();
        if (base instanceof Potencia) {                                     // (L^m)^n = L^(m n)
            Potencia p = (Potencia) base;
            return new Potencia(p.getBase(), p.getExponente() * n);
        }
        return new Potencia(base, n);
    }

    /** Desarrolla una potencia como concatenacion explicita, para mostrarla paso a paso. */
    public static ExpresionRegular desarrollarPotencia(ExpresionRegular base, int n) {
        List<ExpresionRegular> factores = new ArrayList<ExpresionRegular>();
        for (int i = 0; i < n; i++) factores.add(base);
        return concatenacion(factores);
    }
}
