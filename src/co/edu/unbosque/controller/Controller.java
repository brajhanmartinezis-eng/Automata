package co.edu.unbosque.controller;

import co.edu.unbosque.model.*;
import co.edu.unbosque.view.*;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class Controller implements AccionesVista, OyenteAutomata {

    private final Automata automata;
    private final VistaPrincipal vista;
    private final ServicioDerivacion servicio = new ServicioDerivacion();
    private final RepositorioAutomata repositorio = new RepositorioTextoPlano();
    private final ControllerLienzo controladorLienzo;

    private MetodoDerivacion metodoActual;
    private Derivacion derivacionActual;

    public Controller(Automata automata, VistaPrincipal vista) {
        this.automata = automata;
        this.vista = vista;
        this.metodoActual = servicio.getMetodos().get(0);
        this.controladorLienzo = new ControllerLienzo(automata, vista, this::alModificarModelo);

        vista.setAcciones(this);
        vista.setMetodos(servicio.getMetodos());
        automata.agregarOyente(this);
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
        actualizarBarra();
    }

    private void actualizarBarra() {
        ResultadoValidacion r = servicio.validar(automata);
        String diagnostico = r.esValido()
                ? (r.tieneAvisos() ? "AFD v\u00E1lido con " + r.getAvisos().size() + " aviso(s)"
                                   : "AFD v\u00E1lido")
                : r.getErrores().size() + " punto(s) por corregir";
        vista.mostrarEnBarra(automata.getEstados().size() + " estados \u00B7 "
                + automata.getTransiciones().size() + " transiciones \u00B7 \u03A3 = "
                + automata.getAlfabeto() + " \u00B7 " + diagnostico);
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
            default: ejemplo = EjemplosAutomata.terminaEnAB();      break;
        }
        automata.reemplazarPor(ejemplo);
        alModificarModelo();
    }

    @Override public void abrirArchivo() {
        File f = vista.pedirArchivoParaAbrir(
                repositorio.getDescripcionFormato(), repositorio.getExtension());
        if (f == null) return;
        try {
            automata.reemplazarPor(repositorio.cargar(f));
            alModificarModelo();
        } catch (IOException ex) {
            vista.mostrarError("No se pudo abrir",
                    "El archivo no se pudo leer:\n" + ex.getMessage());
        }
    }

    @Override public void guardarArchivo() {
        File f = vista.pedirArchivoParaGuardar(
                repositorio.getDescripcionFormato(), repositorio.getExtension());
        if (f == null) return;
        try {
            repositorio.guardar(automata, f);
            vista.mostrarEnBarra("Guardado en " + f.getAbsolutePath());
        } catch (IOException ex) {
            vista.mostrarError("No se pudo guardar",
                    "El archivo no se pudo escribir:\n" + ex.getMessage());
        }
    }

    @Override public void seleccionarHerramienta(Herramienta h) {
        controladorLienzo.setHerramienta(h);
        vista.mostrarEnBarra(h.getAyuda());
    }

    @Override public void seleccionarMetodo(MetodoDerivacion m) {
        metodoActual = m;
        if (derivacionActual != null) generarExpresion();
    }

    @Override public void generarExpresion() {
        ResultadoValidacion r = servicio.validar(automata);
        if (!r.esValido()) {
            vista.mostrarError("Revisa el AFD",
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
            vista.mostrarError("Revisa el AFD", r.erroresComoTexto());
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
              + "6. Elige el m\u00E9todo y pulsa \"Generar expresi\u00F3n regular\".\n"
              + "   La expresi\u00F3n aparece arriba a la derecha y el desarrollo completo\n"
              + "   en la lista de pasos. Al seleccionar un paso se resalta en el\n"
              + "   diagrama el estado que se est\u00E1 despejando o eliminando.\n\n"
              + "7. \"Reproducir\" avanza los pasos solo, uno cada 1,6 segundos.\n\n"
              + "8. El men\u00FA Lenguaje calcula la reflexi\u00F3n y la potencia del\n"
              + "   lenguaje obtenido, y lista cadenas de ejemplo para contrastar\n"
              + "   la expresi\u00F3n a mano.\n\n"
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
}
