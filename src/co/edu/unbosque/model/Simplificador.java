package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.List;


public class Simplificador implements VisitanteExpresion<ExpresionRegular> {

    private final List<String> bitacora = new ArrayList<String>();

    public ExpresionRegular simplificar(ExpresionRegular e) {
        bitacora.clear();
        return e.aceptar(this);
    }

    /** Reglas aplicadas durante la ultima simplificacion, en orden. */
    public List<String> getBitacora() { return bitacora; }

    // ------------------------------------------------------------------

    @Override public ExpresionRegular verVacio(Vacio e) { return e; }

    @Override public ExpresionRegular verCadenaVacia(CadenaVacia e) { return e; }

    @Override public ExpresionRegular verSimbolo(Simbolo e) { return e; }

    @Override public ExpresionRegular verEstrella(Estrella e) {
        return Expresion.estrella(e.getBase().aceptar(this));
    }

    @Override public ExpresionRegular verClausuraPositiva(ClausuraPositiva e) {
        return Expresion.clausuraPositiva(e.getBase().aceptar(this));
    }

    @Override public ExpresionRegular verPotencia(Potencia e) {
        return Expresion.potencia(e.getBase().aceptar(this), e.getExponente());
    }

    @Override public ExpresionRegular verUnion(Union e) {
        List<ExpresionRegular> partes = new ArrayList<ExpresionRegular>();
        for (ExpresionRegular o : e.getOperandos()) partes.add(o.aceptar(this));
        return Expresion.union(partes);
    }

    @Override public ExpresionRegular verConcatenacion(Concatenacion e) {
        List<ExpresionRegular> factores = new ArrayList<ExpresionRegular>();
        for (ExpresionRegular f : e.getFactores()) factores.add(f.aceptar(this));

        fusionarRepeticiones(factores);
        agruparPotencias(factores);

        return Expresion.concatenacion(factores);
    }

    // ------------------------------------------------------------------
    // Reglas sobre la lista de factores
    // ------------------------------------------------------------------

    /** Combina pares vecinos que repiten la misma base con estrella o clausura. */
    private void fusionarRepeticiones(List<ExpresionRegular> f) {
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            for (int i = 0; i + 1 < f.size(); i++) {
                ExpresionRegular a = f.get(i), b = f.get(i + 1);
                ExpresionRegular fusion = fusionar(a, b);
                if (fusion != null) {
                    anotar(a, b, fusion);
                    f.set(i, fusion);
                    f.remove(i + 1);
                    cambio = true;
                    break;
                }
            }
        }
    }

    private ExpresionRegular fusionar(ExpresionRegular a, ExpresionRegular b) {
        ExpresionRegular baseA = baseRepetida(a), baseB = baseRepetida(b);
        if (baseA == null || baseB == null || !baseA.equals(baseB)) return null;

        boolean estrellaA = a instanceof Estrella, estrellaB = b instanceof Estrella;

        // L* L* = L*
        if (estrellaA && estrellaB) return a;
        // L* L+ = L+ L* = L+
        if (estrellaA && b instanceof ClausuraPositiva) return b;
        if (estrellaB && a instanceof ClausuraPositiva) return a;
        // L L* = L* L = L+
        if (estrellaA && !esRepetidor(b)) return Expresion.clausuraPositiva(baseA);
        if (estrellaB && !esRepetidor(a)) return Expresion.clausuraPositiva(baseA);

        return null;
    }

    /** Base sobre la que un factor repite, o el propio factor si no es repetidor. */
    private ExpresionRegular baseRepetida(ExpresionRegular e) {
        if (e instanceof Estrella) return ((Estrella) e).getBase();
        if (e instanceof ClausuraPositiva) return ((ClausuraPositiva) e).getBase();
        if (e instanceof Potencia) return null;   // las potencias las trata agruparPotencias
        return e;
    }

    private boolean esRepetidor(ExpresionRegular e) {
        return e instanceof Estrella || e instanceof ClausuraPositiva;
    }

    /** Convierte tramos de factores identicos en una potencia. */
    private void agruparPotencias(List<ExpresionRegular> f) {
        int i = 0;
        while (i < f.size()) {
            int j = i;
            int veces = 0;
            ExpresionRegular base = baseDePotencia(f.get(i));
            if (base == null) { i++; continue; }
            while (j < f.size() && base.equals(baseDePotencia(f.get(j)))) {
                veces += exponenteDe(f.get(j));
                j++;
            }
            if (veces >= 2 && j - i >= 2) {
                ExpresionRegular pot = Expresion.potencia(base, veces);
                List<ExpresionRegular> tramo = new ArrayList<ExpresionRegular>(f.subList(i, j));
                anotarPotencia(tramo, pot);
                for (int k = j - 1; k >= i; k--) f.remove(k);
                f.add(i, pot);
            }
            i++;
        }
    }

    private ExpresionRegular baseDePotencia(ExpresionRegular e) {
        if (e instanceof Potencia) return ((Potencia) e).getBase();
        if (esRepetidor(e)) return null;
        return e;
    }

    private int exponenteDe(ExpresionRegular e) {
        return e instanceof Potencia ? ((Potencia) e).getExponente() : 1;
    }

    // ------------------------------------------------------------------

    private void anotar(ExpresionRegular a, ExpresionRegular b, ExpresionRegular resultado) {
        bitacora.add(a + " " + b + "  =  " + resultado);
    }

    private void anotarPotencia(List<ExpresionRegular> tramo, ExpresionRegular pot) {
        StringBuilder sb = new StringBuilder();
        for (ExpresionRegular e : tramo) sb.append(e);
        bitacora.add(sb + "  =  " + pot);
    }
}
