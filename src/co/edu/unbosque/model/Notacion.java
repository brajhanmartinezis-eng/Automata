package co.edu.unbosque.model;


public final class Notacion {

    private static Notacion actual = deCurso();

    private final String union;
    private final String concatenacion;
    private final String estrella;
    private final String clausuraPositiva;
    private final String reflexion;
    private final String vacio;
    private final String cadenaVacia;
    private final boolean exponenteEnSuperindice;

    public Notacion(String union, String concatenacion, String estrella,
                    String clausuraPositiva, String reflexion, String vacio,
                    String cadenaVacia, boolean exponenteEnSuperindice) {
        this.union = union;
        this.concatenacion = concatenacion;
        this.estrella = estrella;
        this.clausuraPositiva = clausuraPositiva;
        this.reflexion = reflexion;
        this.vacio = vacio;
        this.cadenaVacia = cadenaVacia;
        this.exponenteEnSuperindice = exponenteEnSuperindice;
    }

    // ------------------------------------------------------------------
    // Notaciones predefinidas
    // ------------------------------------------------------------------

    /** La del curso: union con U, concatenacion por yuxtaposicion. */
    public static Notacion deCurso() {
        return new Notacion(" U ", "", "*", "\u207A", "\u1D3F",
                "\u2205", "\u03B5", true);
    }

    /** Union con el simbolo de conjuntos. */
    public static Notacion conjuntista() {
        return new Notacion(" \u222A ", "\u00B7", "*", "\u207A", "\u1D3F",
                "\u2205", "\u03BB", true);
    }

    /** Notacion ASCII pura, util para pegar en documentos sin problemas de fuente. */
    public static Notacion ascii() {
        return new Notacion(" U ", "", "*", "+", "^R", "0", "e", false);
    }

    /** Notacion algebraica, con + como union (la de muchos libros de automatas). */
    public static Notacion algebraica() {
        return new Notacion(" + ", "", "*", "\u207A", "\u1D3F",
                "\u2205", "\u03B5", true);
    }

    public static Notacion actual() { return actual; }

    public static void establecerActual(Notacion n) { actual = n; }

    // ------------------------------------------------------------------

    public String getUnion()            { return union; }
    public String getConcatenacion()    { return concatenacion; }
    public String getEstrella()         { return estrella; }
    public String getClausuraPositiva() { return clausuraPositiva; }
    public String getReflexion()        { return reflexion; }
    public String getVacio()            { return vacio; }
    public String getCadenaVacia()      { return cadenaVacia; }

    /** Formatea un exponente como superindice o como ^n, segun la notacion. */
    public String exponente(int n) {
        if (!exponenteEnSuperindice) return "^" + n;
        final String[] digitos = {
                "\u2070", "\u00B9", "\u00B2", "\u00B3", "\u2074",
                "\u2075", "\u2076", "\u2077", "\u2078", "\u2079"
        };
        StringBuilder sb = new StringBuilder();
        for (char c : String.valueOf(n).toCharArray()) sb.append(digitos[c - '0']);
        return sb.toString();
    }
}
