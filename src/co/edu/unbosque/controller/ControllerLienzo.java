package co.edu.unbosque.controller;

import co.edu.unbosque.model.*;
import co.edu.unbosque.view.*;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class ControllerLienzo extends MouseAdapter
        implements LienzoAutomata.OyenteSoltar {

    private static final int MARGEN = 32;

    private final Automata automata;
    private final LienzoAutomata lienzo;
    private final VistaPrincipal vista;
    private final Runnable alModificar;

    private Herramienta herramienta = Herramienta.MOVER;
    private Estado arrastrado;
    private int agarreX, agarreY;
    private boolean seMovio;

    public ControllerLienzo(Automata automata, VistaPrincipal vista, Runnable alModificar) {
        this.automata = automata;
        this.vista = vista;
        this.lienzo = vista.getLienzo();
        this.alModificar = alModificar;

        lienzo.addMouseListener(this);
        lienzo.addMouseMotionListener(this);
        lienzo.setOyenteSoltar(this);
    }

    public void setHerramienta(Herramienta h) {
        herramienta = h;
        lienzo.setOrigenTransicion(null);
        lienzo.repaint();
    }

    // ------------------------------------------------------------------
    // Arrastre desde la barra de herramientas
    // ------------------------------------------------------------------

    @Override
    public void seSolto(int x, int y) {
        crearEstado(x, y);
    }

    private void crearEstado(int x, int y) {
        Estado e = automata.agregarEstado(automata.proponerNombre(),
                Math.max(MARGEN, x), Math.max(MARGEN, y));
        lienzo.setSeleccionado(e);
        alModificar.run();
    }

    // ------------------------------------------------------------------
    // Raton
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
        if (arrastrado != null && seMovio) alModificar.run();
        arrastrado = null;
        lienzo.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
            Estado bajo = lienzo.estadoEn(e.getX(), e.getY());
            if (bajo != null) {
                automata.alternarAceptacion(bajo);
                alModificar.run();
            }
        }
    }

    // ------------------------------------------------------------------
    // Operaciones
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
            vista.mostrarError("Determinismo",
                    "En un AFD cada estado admite un solo destino por s\u00EDmbolo.\n"
                            + origen.getNombre() + " ya define: "
                            + String.join(", ", rechazados));
        alModificar.run();
    }

    private void borrarEstado(Estado e) {
        automata.eliminarEstado(e);
        if (lienzo.getSeleccionado() == e) lienzo.setSeleccionado(null);
        if (lienzo.getOrigenTransicion() == e) lienzo.setOrigenTransicion(null);
        alModificar.run();
    }

    private void borrarTransicionesEn(int x, int y) {
        List<Transicion> ts = lienzo.transicionesEn(x, y);
        if (ts.isEmpty()) return;
        automata.eliminarTransiciones(new ArrayList<Transicion>(ts));
        alModificar.run();
    }

    // ------------------------------------------------------------------
    // Menu contextual
    // ------------------------------------------------------------------

    private void abrirMenu(MouseEvent ev, final Estado e) {
        if (e == null) return;
        lienzo.setSeleccionado(e);

        JPopupMenu menu = new JPopupMenu();

        JMenuItem inicial = new JMenuItem("Marcar como estado inicial");
        inicial.addActionListener(a -> { automata.marcarInicial(e); alModificar.run(); });
        menu.add(inicial);

        JMenuItem aceptacion = new JMenuItem(e.esAceptacion()
                ? "Quitar aceptaci\u00F3n" : "Marcar como aceptaci\u00F3n");
        aceptacion.addActionListener(a -> { automata.alternarAceptacion(e); alModificar.run(); });
        menu.add(aceptacion);

        JMenuItem renombrar = new JMenuItem("Renombrar\u2026");
        renombrar.addActionListener(a -> renombrar(e));
        menu.add(renombrar);

        JMenuItem transicion = new JMenuItem(
                "Agregar transici\u00F3n desde " + e.getNombre() + "\u2026");
        transicion.addActionListener(a -> {
            setHerramienta(Herramienta.TRANSICION);
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
        alModificar.run();
    }

    /** Punto actual del raton, por si el controlador principal lo necesita. */
    public Point getUltimoPunto() { return null; }
}
