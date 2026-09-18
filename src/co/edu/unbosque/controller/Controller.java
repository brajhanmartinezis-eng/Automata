package co.edu.unbosque.controller;

import co.edu.unbosque.model.*;
import co.edu.unbosque.view.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


public class Controller implements AccionesVista, OyenteAutomata,
        MouseListener, MouseMotionListener, LienzoAutomata.OyenteSoltar {

    private static final int MARGEN = 32;

    private final Automata automata;
    private final VistaPrincipal vista;
    private final LienzoAutomata lienzo;
    private final ServicioDerivacion servicio = new ServicioDerivacion();
    private final ConversorAFNaAFD conversor = new ConversorAFNaAFD();

    private MetodoDerivacion metodoActual;
    private Derivacion derivacionActual;
    private Automata conversionPendiente;

    private Herramienta herramienta = Herramienta.MOVER;
    private Estado arrastrado;
    private int agarreX, agarreY;
    private boolean seMovio;

    public Controller(Automata automata, VistaPrincipal vista) {
        this.automata = automata;
        this.vista = vista;
        this.lienzo = vista.getLienzo();
        this.metodoActual = servicio.getMetodos().get(0);

        vista.setAcciones(this);
        automata.agregarOyente(this);

        lienzo.addMouseListener(this);
        lienzo.addMouseMotionListener(this);
        lienzo.setOyenteSoltar(this);
    }

    public void iniciar() {
        actualizarBarra();
    }

    // ------------------------------------------------------------------
    // Reaccion a cambios del modelo
    // ------------------------------------------------------------------

    @Override
    public void automataCambio(Automata a) {
        actualizarBarra();
    }

    /** Tras editar el diagrama la derivacion anterior deja de ser valida. */
    private void alModificarModelo() {
        derivacionActual = null;
        vista.limpiarDerivacion();
        vista.resaltarEstado(null);
        vista.limpiarResultadoCadena();
        actualizarBarra();
    }

    private void actualizarBarra() {
        ResultadoValidacion r = servicio.validar(automata);
        String tipo = automata.esDeterminista() ? "AFD" : "AFN";
        String diagnostico = r.esValido()
                ? (r.tieneAvisos() ? "v\u00E1lido con " + r.getAvisos().size() + " aviso(s)" : "v\u00E1lido")
                : r.getErrores().size() + " punto(s) por corregir";
        vista.mostrarEnBarra(tipo + " \u00B7 " + automata.getEstados().size() + " estados \u00B7 "
                + automata.getTransiciones().size() + " transiciones \u00B7 \u03A3 = "
                + automata.getAlfabeto() + " \u00B7 " + diagnostico);
        vista.habilitarConversion(!automata.esDeterminista());
    }

    // ------------------------------------------------------------------
    // Acciones de la vista
    // ------------------------------------------------------------------

    @Override public void nuevoAutomata() {
        automata.limpiar();
        alModificarModelo();
    }

    @Override public void cargarEjemplo(int indice) {
        Automata ejemplo;
        switch (indice) {
            case 1:  ejemplo = EjemplosAutomata.exactamenteUnUno(); break;
            case 2:  ejemplo = EjemplosAutomata.paresDeA();         break;
            case 3:  ejemplo = EjemplosAutomata.contieneAA();       break;
            case 4:  ejemplo = EjemplosAutomata.afnAMasBEstrella(); break;
            default: ejemplo = EjemplosAutomata.terminaEnAB();      break;
        }
        automata.reemplazarPor(ejemplo);
        alModificarModelo();
    }

    @Override public void convertirAAFD() {
        ResultadoValidacion r = servicio.validar(automata);
        if (!r.esValido()) {
            vista.mostrarError("Revisa el autómata",
                    "Antes de convertir hay que corregir:\n\n" + r.erroresComoTexto());
            return;
        }
        if (automata.esDeterminista()) {
            vista.mostrarInformacion("Ya es un AFD",
                    "El autómata actual ya es determinista; no hay nada que convertir.");
            return;
        }
        ConversionAFD conversion = conversor.convertir(automata);
        conversionPendiente = conversion.getResultado();
        vista.mostrarConversion(conversion);
    }

    @Override public void dibujarAutomataConvertido() {
        if (conversionPendiente == null) return;
        automata.reemplazarPor(conversionPendiente);
        conversionPendiente = null;
        alModificarModelo();
    }

    @Override public void verificarCadena(String cadena) {
        String limpia = cadena.replaceAll("\\s+", "");
        List<String> simbolos = new ArrayList<String>();
        for (char ch : limpia.toCharArray()) simbolos.add(String.valueOf(ch));
        vista.mostrarResultadoCadena(limpia, automata.acepta(simbolos));
    }

    @Override public void seleccionarHerramienta(Herramienta h) {
        herramienta = h;
        lienzo.setOrigenTransicion(null);
        lienzo.repaint();
        vista.mostrarEnBarra(h.getAyuda());
    }

    @Override public void generarExpresion() {
        ResultadoValidacion r = servicio.validar(automata);
        if (!r.esValido()) {
            vista.mostrarError("Revisa el aut\u00F3mata",
                    "Antes de generar la expresi\u00F3n hay que corregir:\n\n"
                            + r.erroresComoTexto());
            return;
        }

        derivacionActual = servicio.derivar(automata, metodoActual);
        vista.mostrarDerivacion(derivacionActual);

        if (r.tieneAvisos())
            vista.mostrarEnBarra("Generado. Avisos: " + r.avisosComoTexto()
                    .replace("\n", "   ").replace("\u2022 ", ""));
        else
            actualizarBarra();
    }

    @Override public void cambiarNotacion(Notacion n) {
        Notacion.establecerActual(n);
        if (derivacionActual != null) generarExpresion();
        vista.refrescar();
    }

    @Override public void cambiarColorPasoAPaso(Color c) {
        Tema.setAcento(c);
        vista.aplicarColorPasoAPaso();
    }

    @Override public void alternarClausuraYPotencias(boolean activo) {
        servicio.setUsarClausuraYPotencias(activo);
        if (derivacionActual != null) generarExpresion();
    }

    // ------------------------------------------------------------------
    // Operaciones sobre el lenguaje obtenido
    // ------------------------------------------------------------------

    @Override public void mostrarReflexion() {
        if (!hayExpresion()) return;
        ExpresionRegular l = derivacionActual.getExpresionSimplificada();
        vista.mostrarTextoLargo("Reflexi\u00F3n del lenguaje",
                "La reflexi\u00F3n L\u1D3F contiene las mismas cadenas de L escritas al rev\u00E9s.\n"
              + "Se calcula recorriendo la expresi\u00F3n y aplicando:\n\n"
              + "   \u03C3\u1D3F = \u03C3                      (un s\u00EDmbolo es su propio reflejo)\n"
              + "   (L U M)\u1D3F = L\u1D3F U M\u1D3F           (la uni\u00F3n no cambia de orden)\n"
              + "   (L M)\u1D3F   = M\u1D3F L\u1D3F             (la concatenaci\u00F3n s\u00ED lo invierte)\n"
              + "   (L*)\u1D3F    = (L\u1D3F)*\n"
              + "   (L\u207A)\u1D3F    = (L\u1D3F)\u207A\n"
              + "   (L\u207F)\u1D3F    = (L\u1D3F)\u207F\n\n"
              + "----------------------------------------------------------\n\n"
              + "   L   = " + l + "\n\n"
              + "   L\u1D3F  = " + servicio.reflexion(l));
    }

    @Override public void mostrarPotencia() {
        if (!hayExpresion()) return;
        String entrada = vista.pedirTexto(
                "Exponente n para calcular L\u207F\n"
                        + "(L\u2070 = \u03B5, L\u00B9 = L, L\u00B2 = L L, \u2026)", "2");
        if (entrada == null) return;
        int n;
        try {
            n = Integer.parseInt(entrada.trim());
        } catch (NumberFormatException ex) {
            vista.mostrarError("Exponente inv\u00E1lido",
                    "Escribe un n\u00FAmero entero mayor o igual que cero.");
            return;
        }
        if (n < 0 || n > 20) {
            vista.mostrarError("Exponente fuera de rango",
                    "Usa un exponente entre 0 y 20.");
            return;
        }
        ExpresionRegular l = derivacionActual.getExpresionSimplificada();
        vista.mostrarTextoLargo("Potencia del lenguaje",
                "La potencia L\u207F es L concatenado consigo mismo n veces.\n\n"
              + "   L\u2070 = \u03B5        L\u00B9 = L        L\u207F = L L\u207F\u207B\u00B9\n\n"
              + "----------------------------------------------------------\n\n"
              + "   L    = " + l + "\n\n"
              + "   L^" + n + "  = " + servicio.potencia(l, n) + "\n\n"
              + "   desarrollada:\n"
              + "   " + Expresion.desarrollarPotencia(l, n));
    }

    @Override public void mostrarCadenasDeEjemplo() {
        ResultadoValidacion r = servicio.validar(automata);
        if (!r.esValido()) {
            vista.mostrarError("Revisa el autómata", r.erroresComoTexto());
            return;
        }
        List<String> cadenas = servicio.cadenasDeEjemplo(automata, 8, 40);
        StringBuilder sb = new StringBuilder();
        sb.append("Primeras cadenas que el aut\u00F3mata acepta, en orden de longitud.\n")
          .append("Sirven para contrastar a mano contra la expresi\u00F3n regular.\n\n")
          .append("\u03A3 = ").append(automata.getAlfabeto()).append("\n\n");
        if (cadenas.isEmpty()) sb.append("   (el lenguaje est\u00E1 vac\u00EDo)");
        else for (String c : cadenas) sb.append("   ").append(c).append("\n");
        vista.mostrarTextoLargo("Cadenas de ejemplo", sb.toString());
    }

    private boolean hayExpresion() {
        if (derivacionActual == null) {
            vista.mostrarError("Todav\u00EDa no hay expresi\u00F3n",
                    "Primero genera la expresi\u00F3n regular del aut\u00F3mata.");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Paso a paso y ayuda
    // ------------------------------------------------------------------

    @Override public void pasoSeleccionado(Paso p) {
        vista.resaltarEstado(p == null ? null : p.getEstadoResaltado());
    }

    @Override public void mostrarAyuda() {
        vista.mostrarTextoLargo("C\u00F3mo se usa",
                "CONSTRUIR EL DIAGRAMA\n\n"
              + "1. Arrastra la ficha \"arrastra un estado al lienzo\" desde la barra\n"
              + "   superior hasta el lienzo. Tambi\u00E9n sirve activar \"+ Estado\" y\n"
              + "   hacer clic donde quieras el estado.\n\n"
              + "2. Con \"Mover\" arrastra los estados para acomodar el diagrama.\n\n"
              + "3. Con \"+ Transici\u00F3n\" haz clic en el estado origen y luego en el\n"
              + "   destino. Se pide el s\u00EDmbolo; puedes escribir varios separados\n"
              + "   por coma para crear varias aristas de una vez.\n\n"
              + "4. Doble clic sobre un estado lo marca o desmarca como de aceptaci\u00F3n.\n"
              + "   Clic derecho abre el men\u00FA: inicial, aceptaci\u00F3n, renombrar,\n"
              + "   agregar transici\u00F3n, eliminar.\n\n"
              + "5. Con \"Borrar\" haz clic sobre un estado, o sobre la etiqueta de una\n"
              + "   transici\u00F3n para eliminar esa arista.\n\n"
              + "GENERAR LA EXPRESI\u00D3N\n\n"
              + "6. Pulsa \"Generar expresi\u00F3n regular\".\n"
              + "   La expresi\u00F3n aparece arriba a la derecha y el desarrollo completo\n"
              + "   en la lista de pasos. Al seleccionar un paso se resalta en el\n"
              + "   diagrama el estado que se est\u00E1 despejando o eliminando.\n\n"
              + "7. \"Reproducir\" avanza los pasos solo, uno cada 1,6 segundos.\n\n"
              + "8. El men\u00FA Lenguaje calcula la reflexi\u00F3n y la potencia del\n"
              + "   lenguaje obtenido, y lista cadenas de ejemplo para contrastar\n"
              + "   la expresi\u00F3n a mano.\n\n"
              + "AFN Y CONVERSI\u00D3N A AFD\n\n"
              + "9. Puedes dibujar un AFN con normalidad: nada impide que un estado\n"
              + "   tenga varias transiciones con el mismo s\u00EDmbolo. La barra de\n"
              + "   estado indica en todo momento si lo dibujado es AFD o AFN.\n\n"
              + "10. Cuando es AFN se activa el bot\u00F3n \"Convertir AFN a AFD\" en la\n"
              + "    barra superior. Muestra los 5 pasos de la construcci\u00F3n de\n"
              + "    subconjuntos y, al final, un bot\u00F3n para dibujar el AFD\n"
              + "    resultante en el lienzo.\n\n"
              + "EL COLOR\n\n"
              + "La interfaz usa solo neutros m\u00E1s un \u00FAnico color de acento, que es\n"
              + "el del paso a paso. No hay azul en ninguna parte. Se cambia desde\n"
              + "Ver > Color del paso a paso, o en el c\u00F3digo, en una sola l\u00EDnea:\n"
              + "el campo 'acento' de la clase Tema.");
    }

    @Override public void mostrarReglas() {
        String u = Notacion.actual().getUnion().trim();
        vista.mostrarTextoLargo("Operadores y reglas",
                "OPERADORES DEL LENGUAJE REGULAR\n\n"
              + "   \u03A3          alfabeto: el conjunto de s\u00EDmbolos del aut\u00F3mata\n"
              + "   \u03C3          un s\u00EDmbolo del alfabeto\n"
              + "   \u03B5          la cadena vac\u00EDa\n"
              + "   \u2205          el lenguaje vac\u00EDo\n"
              + "   L M        concatenaci\u00F3n\n"
              + "   L " + u + " M      uni\u00F3n\n"
              + "   L*         estrella de Kleene: cero o m\u00E1s repeticiones\n"
              + "   L\u207A         clausura positiva: una o m\u00E1s repeticiones\n"
              + "   L\u207F         potencia: L concatenado consigo mismo n veces\n"
              + "   L\u1D3F         reflexi\u00F3n: las cadenas de L escritas al rev\u00E9s\n\n"
              + "IDENTIDADES QUE APLICA EL PROGRAMA\n\n"
              + "   L " + u + " \u2205 = L            L " + u + " L = L\n"
              + "   L \u2205 = \u2205              L \u03B5 = L\n"
              + "   \u2205* = \u03B5               \u03B5* = \u03B5             (L*)* = L*\n"
              + "   \u2205\u207A = \u2205               (L\u207A)\u207A = L\u207A        (L*)\u207A = L*\n"
              + "   L\u2070 = \u03B5               L\u00B9 = L\n"
              + "   L L* = L* L = L\u207A     L L \u2026 L (n veces) = L\u207F\n\n"
              + "M\u00C9TODO 1 \u00B7 ECUACIONES CARACTER\u00CDSTICAS + LEMA DE ARDEN\n\n"
              + "   Una ecuaci\u00F3n por estado, armada con uni\u00F3n y concatenaci\u00F3n:\n\n"
              + "      X(q) = uni\u00F3n de  \u03C3 X(\u03B4(q,\u03C3))  para cada \u03C3 \u2208 \u03A3\n"
              + "                                    ( " + u + " \u03B5 si q \u2208 F )\n\n"
              + "   Regla del bucle (lema de Arden):\n\n"
              + "      X = R X " + u + " S   \u27F9   X = R* S\n\n"
              + "   Se despeja una inc\u00F3gnita a la vez y se sustituye en las dem\u00E1s.\n"
              + "   La respuesta es X(q\u2080).\n\n"
              + "M\u00C9TODO 2 \u00B7 ELIMINACI\u00D3N DE ESTADOS\n\n"
              + "      R(i,j) := R(i,j) " + u + " R(i,q) R(q,q)* R(q,j)\n\n"
              + "   Se borra un estado intermedio a la vez hasta que solo quedan el\n"
              + "   nodo de entrada y el de salida; la etiqueta que los une es la\n"
              + "   respuesta.\n\n"
              + "UNA ADVERTENCIA SOBRE LA NOTACI\u00D3N\n\n"
              + "   Cuidado con no confundir uni\u00F3n y concatenaci\u00F3n. Para \u03A3 = {0,1}\n"
              + "   y el lenguaje de las cadenas con exactamente un 1, la expresi\u00F3n\n"
              + "   correcta es\n\n"
              + "      L = 0* 1 0*        (concatenaci\u00F3n)\n\n"
              + "   y no\n\n"
              + "      L = 0* " + u + " 1 " + u + " 0*      (uni\u00F3n)\n\n"
              + "   La segunda describe otro lenguaje: cualquier cadena de ceros, o\n"
              + "   bien la cadena \"1\" sola. No contiene, por ejemplo, la cadena 010.\n"
              + "   Carga el ejemplo \"Cadenas con exactamente un 1\" desde el men\u00FA\n"
              + "   Archivo y compru\u00E9balo con Lenguaje > Cadenas de ejemplo.");
    }

    // ------------------------------------------------------------------
    // Interaccion con el lienzo: arrastre desde la barra de herramientas
    // ------------------------------------------------------------------

    @Override
    public void seSolto(int x, int y) {
        crearEstado(x, y);
    }

    private void crearEstado(int x, int y) {
        Estado e = automata.agregarEstado(automata.proponerNombre(),
                Math.max(MARGEN, x), Math.max(MARGEN, y));
        lienzo.setSeleccionado(e);
        alModificarModelo();
    }

    // ------------------------------------------------------------------
    // Interaccion con el lienzo: raton
    // ------------------------------------------------------------------

    @Override
    public void mousePressed(MouseEvent e) {
        lienzo.requestFocusInWindow();
        lienzo.setPuntoRaton(e.getPoint());
        Estado bajoElCursor = lienzo.estadoEn(e.getX(), e.getY());

        if (e.isPopupTrigger()) { abrirMenu(e, bajoElCursor); return; }

        seMovio = false;
        switch (herramienta) {
            case MOVER:
                lienzo.setSeleccionado(bajoElCursor);
                if (bajoElCursor != null) iniciarArrastre(bajoElCursor, e);
                break;

            case ESTADO:
                if (bajoElCursor == null) crearEstado(e.getX(), e.getY());
                else iniciarArrastre(bajoElCursor, e);
                break;

            case TRANSICION:
                manejarTransicion(bajoElCursor);
                break;

            case BORRAR:
                if (bajoElCursor != null) borrarEstado(bajoElCursor);
                else borrarTransicionesEn(e.getX(), e.getY());
                break;
        }
        lienzo.repaint();
    }

    private void iniciarArrastre(Estado e, MouseEvent ev) {
        arrastrado = e;
        agarreX = ev.getX() - e.getX();
        agarreY = ev.getY() - e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        lienzo.setPuntoRaton(e.getPoint());
        if (arrastrado != null) {
            automata.moverEstado(arrastrado,
                    Math.max(MARGEN, e.getX() - agarreX),
                    Math.max(MARGEN, e.getY() - agarreY));
            seMovio = true;
        }
        lienzo.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lienzo.setPuntoRaton(e.getPoint());
        if (herramienta == Herramienta.TRANSICION && lienzo.getOrigenTransicion() != null)
            lienzo.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            abrirMenu(e, lienzo.estadoEn(e.getX(), e.getY()));
            return;
        }
        if (arrastrado != null && seMovio) alModificarModelo();
        arrastrado = null;
        lienzo.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
            Estado bajo = lienzo.estadoEn(e.getX(), e.getY());
            if (bajo != null) {
                automata.alternarAceptacion(bajo);
                alModificarModelo();
            }
        }
    }

    @Override public void mouseEntered(MouseEvent e) { }

    @Override public void mouseExited(MouseEvent e) { }

    // ------------------------------------------------------------------
    // Interaccion con el lienzo: operaciones
    // ------------------------------------------------------------------

    private void manejarTransicion(Estado bajoElCursor) {
        if (bajoElCursor == null) {
            lienzo.setOrigenTransicion(null);
            return;
        }
        Estado origen = lienzo.getOrigenTransicion();
        if (origen == null) {
            lienzo.setOrigenTransicion(bajoElCursor);
            vista.mostrarEnBarra("Ahora haz clic en el estado destino");
        } else {
            pedirSimbolos(origen, bajoElCursor);
            lienzo.setOrigenTransicion(null);
        }
    }

    private void pedirSimbolos(Estado origen, Estado destino) {
        String entrada = vista.pedirTexto(
                "S\u00EDmbolo(s) de " + origen.getNombre() + " \u2192 " + destino.getNombre()
                        + "\n(separa con coma para agregar varios)", "");
        if (entrada == null) return;

        List<String> rechazados = new ArrayList<String>();
        for (String bruto : entrada.split(",")) {
            String simbolo = bruto.trim();
            if (simbolo.isEmpty()) continue;
            if (!automata.agregarTransicion(origen, simbolo, destino))
                rechazados.add(simbolo);
        }
        if (!rechazados.isEmpty())
            vista.mostrarError("Transici\u00F3n duplicada",
                    origen.getNombre() + " \u2192 " + destino.getNombre()
                            + " ya tiene exactamente esa transici\u00F3n con: "
                            + String.join(", ", rechazados));
        alModificarModelo();
    }

    private void borrarEstado(Estado e) {
        automata.eliminarEstado(e);
        if (lienzo.getSeleccionado() == e) lienzo.setSeleccionado(null);
        if (lienzo.getOrigenTransicion() == e) lienzo.setOrigenTransicion(null);
        alModificarModelo();
    }

    private void borrarTransicionesEn(int x, int y) {
        List<Transicion> ts = lienzo.transicionesEn(x, y);
        if (ts.isEmpty()) return;
        automata.eliminarTransiciones(new ArrayList<Transicion>(ts));
        alModificarModelo();
    }

    // ------------------------------------------------------------------
    // Interaccion con el lienzo: menu contextual
    // ------------------------------------------------------------------

    private void abrirMenu(MouseEvent ev, final Estado e) {
        if (e == null) return;
        lienzo.setSeleccionado(e);

        JPopupMenu menu = new JPopupMenu();

        JMenuItem inicial = new JMenuItem("Marcar como estado inicial");
        inicial.addActionListener(a -> { automata.marcarInicial(e); alModificarModelo(); });
        menu.add(inicial);

        JMenuItem aceptacion = new JMenuItem(e.esAceptacion()
                ? "Quitar aceptaci\u00F3n" : "Marcar como aceptaci\u00F3n");
        aceptacion.addActionListener(a -> { automata.alternarAceptacion(e); alModificarModelo(); });
        menu.add(aceptacion);

        JMenuItem renombrar = new JMenuItem("Renombrar\u2026");
        renombrar.addActionListener(a -> renombrar(e));
        menu.add(renombrar);

        JMenuItem transicion = new JMenuItem(
                "Agregar transici\u00F3n desde " + e.getNombre() + "\u2026");
        transicion.addActionListener(a -> {
            seleccionarHerramienta(Herramienta.TRANSICION);
            lienzo.setOrigenTransicion(e);
            vista.mostrarEnBarra("Ahora haz clic en el estado destino");
        });
        menu.add(transicion);

        menu.addSeparator();
        JMenuItem eliminar = new JMenuItem("Eliminar estado " + e.getNombre());
        eliminar.addActionListener(a -> borrarEstado(e));
        menu.add(eliminar);

        menu.show(lienzo, ev.getX(), ev.getY());
    }

    private void renombrar(Estado e) {
        String nuevo = vista.pedirTexto("Nuevo nombre:", e.getNombre());
        if (nuevo == null || nuevo.trim().isEmpty()) return;
        nuevo = nuevo.trim();
        Estado otro = automata.buscarEstado(nuevo);
        if (otro != null && otro != e) {
            vista.mostrarError("Nombre repetido",
                    "Ya existe un estado llamado " + nuevo + ".");
            return;
        }
        automata.renombrarEstado(e, nuevo);
        alModificarModelo();
    }

    /** Punto actual del raton, por si se necesita desde afuera. */
    public Point getUltimoPunto() { return null; }
}
