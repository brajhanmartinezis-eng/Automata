package co.edu.unbosque.model;


public final class EjemplosAutomata {

    private EjemplosAutomata() { }

    /** Sobre {a,b}: cadenas que terminan en "ab". */
    public static Automata terminaEnAB() {
        Automata a = new Automata();
        a.setNombre("Cadenas que terminan en \"ab\"");
        Estado q0 = a.agregarEstadoSilencioso("q0", 170, 230, true, false);
        Estado q1 = a.agregarEstadoSilencioso("q1", 380, 140, false, false);
        Estado q2 = a.agregarEstadoSilencioso("q2", 590, 230, false, true);
        a.agregarTransicionSilenciosa(q0, "a", q1);
        a.agregarTransicionSilenciosa(q0, "b", q0);
        a.agregarTransicionSilenciosa(q1, "a", q1);
        a.agregarTransicionSilenciosa(q1, "b", q2);
        a.agregarTransicionSilenciosa(q2, "a", q1);
        a.agregarTransicionSilenciosa(q2, "b", q0);
        return a;
    }

    /** Sobre {0,1}: cadenas con exactamente un 1. La expresion esperada es 0* 1 0*. */
    public static Automata exactamenteUnUno() {
        Automata a = new Automata();
        a.setNombre("Cadenas con exactamente un 1");
        Estado q0 = a.agregarEstadoSilencioso("q0", 190, 220, true, false);
        Estado q1 = a.agregarEstadoSilencioso("q1", 430, 220, false, true);
        Estado q2 = a.agregarEstadoSilencioso("q2", 670, 220, false, false);
        a.agregarTransicionSilenciosa(q0, "0", q0);
        a.agregarTransicionSilenciosa(q0, "1", q1);
        a.agregarTransicionSilenciosa(q1, "0", q1);
        a.agregarTransicionSilenciosa(q1, "1", q2);
        a.agregarTransicionSilenciosa(q2, "0", q2);
        a.agregarTransicionSilenciosa(q2, "1", q2);
        return a;
    }

    /** Sobre {a,b}: cadenas con un numero par de aes. */
    public static Automata paresDeA() {
        Automata a = new Automata();
        a.setNombre("N\u00FAmero par de aes");
        Estado par = a.agregarEstadoSilencioso("par", 230, 220, true, true);
        Estado impar = a.agregarEstadoSilencioso("impar", 520, 220, false, false);
        a.agregarTransicionSilenciosa(par, "a", impar);
        a.agregarTransicionSilenciosa(impar, "a", par);
        a.agregarTransicionSilenciosa(par, "b", par);
        a.agregarTransicionSilenciosa(impar, "b", impar);
        return a;
    }

    /** Sobre {a,b}: cadenas con al menos dos aes seguidas. */
    public static Automata contieneAA() {
        Automata a = new Automata();
        a.setNombre("Cadenas que contienen \"aa\"");
        Estado q0 = a.agregarEstadoSilencioso("q0", 180, 230, true, false);
        Estado q1 = a.agregarEstadoSilencioso("q1", 400, 150, false, false);
        Estado q2 = a.agregarEstadoSilencioso("q2", 620, 230, false, true);
        a.agregarTransicionSilenciosa(q0, "a", q1);
        a.agregarTransicionSilenciosa(q0, "b", q0);
        a.agregarTransicionSilenciosa(q1, "a", q2);
        a.agregarTransicionSilenciosa(q1, "b", q0);
        a.agregarTransicionSilenciosa(q2, "a", q2);
        a.agregarTransicionSilenciosa(q2, "b", q2);
        return a;
    }

    /**
     * AFN sobre {a,b}: una o mas aes seguidas de cualquier cantidad de bes (a+ b*).
     * q0 es no determinista en "a" (se queda en q0 o pasa a q1), pensado para probar
     * la conversion a AFD por construccion de subconjuntos.
     */
    public static Automata afnAMasBEstrella() {
        Automata a = new Automata();
        a.setNombre("AFN: una o más \"a\" seguidas de cualquier cantidad de \"b\"");
        Estado q0 = a.agregarEstadoSilencioso("q0", 220, 220, true, false);
        Estado q1 = a.agregarEstadoSilencioso("q1", 480, 220, false, true);
        a.agregarTransicionSilenciosa(q0, "a", q0);
        a.agregarTransicionSilenciosa(q0, "a", q1);
        a.agregarTransicionSilenciosa(q0, "b", q0);
        a.agregarTransicionSilenciosa(q1, "b", q1);
        return a;
    }
}
