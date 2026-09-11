package co.edu.unbosque.controller;

import co.edu.unbosque.model.*;


public class Diagnostico {

    public static void main(String[] args) {
        ServicioDerivacion servicio = new ServicioDerivacion();

        Automata[] casos = {
                EjemplosAutomata.exactamenteUnUno(),
                EjemplosAutomata.terminaEnAB(),
                EjemplosAutomata.paresDeA(),
                EjemplosAutomata.contieneAA()
        };

        for (Automata a : casos) {
            System.out.println("############################################");
            System.out.println("# " + a.getNombre() + "   Sigma = " + a.getAlfabeto());
            System.out.println("############################################\n");

            for (MetodoDerivacion m : servicio.getMetodos()) {
                Derivacion d = servicio.derivar(a, m);
                System.out.println("--- " + m.getNombre() + " ---\n");
                for (Paso p : d.getPasos()) imprimir(p);
                ExpresionRegular l = d.getExpresionSimplificada();
                System.out.println(">>> L = " + l);
                System.out.println(">>> L reflejado = " + servicio.reflexion(l));
                System.out.println();
            }
            System.out.println("Cadenas aceptadas: "
                    + servicio.cadenasDeEjemplo(a, 5, 12) + "\n");
        }
    }

    private static void imprimir(Paso p) {
        System.out.println("Paso " + p.getNumero() + " - " + p.getTitulo());
        if (!p.getRegla().isEmpty()) System.out.println("   regla: " + p.getRegla());
        for (String s : p.getDesarrollo()) System.out.println("   " + s);
        for (String s : p.getSituacion()) System.out.println("   " + s);
        System.out.println();
    }
}
