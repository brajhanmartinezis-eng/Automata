package co.edu.unbosque.view;

import co.edu.unbosque.model.*;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;

public class PanelPasos extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DefaultListModel<Paso> modelo = new DefaultListModel<Paso>();
    private final JList<Paso> lista = new JList<Paso>(modelo);
    private final JTextPane detalle = new JTextPane();

    private final JLabel rotulo = new JLabel("Expresi\u00F3n regular del lenguaje");
    private final JTextArea expresion = new JTextArea(" ");
    private final JTextArea alternativa = new JTextArea(" ");
    private final JLabel contador = new JLabel(" ");

    private final JButton anterior = new JButton("\u25C0 Anterior");
    private final JButton siguiente = new JButton("Siguiente \u25B6");
    private final JButton reproducir = new JButton("Reproducir");

    private Timer temporizador;

    /** Aviso al controlador de que cambio el paso seleccionado. */
    private Consumer<Paso> alSeleccionarPaso = p -> { };

    public PanelPasos() {
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        add(construirCabecera(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirBotonera(), BorderLayout.SOUTH);
        actualizarBotones();
    }

    public void setAlSeleccionarPaso(Consumer<Paso> c) { alSeleccionarPaso = c; }

    // ------------------------------------------------------------------

    private JPanel construirCabecera() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        rotulo.setFont(Tema.F_UI);
        rotulo.setForeground(Tema.TINTA_SUAVE);

        // Areas de texto en vez de etiquetas: ajustan las expresiones largas en
        // varias lineas y el estudiante puede seleccionarlas y copiarlas.
        prepararArea(expresion, Tema.F_RESULT, Tema.getAcento());
        expresion.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        prepararArea(alternativa, Tema.F_MONO, Tema.TINTA_SUAVE);

        p.add(rotulo, BorderLayout.NORTH);
        p.add(expresion, BorderLayout.CENTER);
        p.add(alternativa, BorderLayout.SOUTH);
        return p;
    }

    private JSplitPane construirCentro() {
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Tema.PANEL);
        lista.setFixedCellHeight(30);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> l, Object valor, int i, boolean sel, boolean foco) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, valor, i, sel, foco);
                Paso p = (Paso) valor;
                c.setText(p.getNumero() + ".  " + p.getTitulo());
                c.setFont(sel ? Tema.F_UI_B : Tema.F_UI);
                c.setForeground(sel ? Tema.acentoOscuro() : Tema.TINTA);
                c.setBackground(sel ? Tema.acentoTenue() : Tema.PANEL);
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0,
                                sel ? Tema.getAcento() : Tema.PANEL),
                        BorderFactory.createEmptyBorder(6, 10, 6, 8)));
                return c;
            }
        });
        lista.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Paso p = lista.getSelectedValue();
            if (p != null) {
                pintarDetalle(p);
                alSeleccionarPaso.accept(p);
            }
            actualizarBotones();
        });

        detalle.setEditable(false);
        detalle.setBackground(Tema.PANEL);
        detalle.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        JScrollPane arriba = new JScrollPane(lista);
        arriba.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE));
        arriba.getViewport().setBackground(Tema.PANEL);

        JScrollPane abajo = new JScrollPane(detalle);
        abajo.setBorder(BorderFactory.createEmptyBorder());
        abajo.getViewport().setBackground(Tema.PANEL);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        sp.setResizeWeight(0.34);
        sp.setDividerSize(6);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setPreferredSize(new Dimension(480, 540));
        return sp;
    }

    private JPanel construirBotonera() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        contador.setFont(Tema.F_UI);
        contador.setForeground(Tema.TINTA_SUAVE);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        derecha.setBackground(Tema.FONDO);
        anterior.addActionListener(e -> desplazar(-1));
        siguiente.addActionListener(e -> desplazar(1));
        reproducir.addActionListener(e -> alternarReproduccion());
        derecha.add(anterior);
        derecha.add(siguiente);
        derecha.add(reproducir);

        p.add(contador, BorderLayout.WEST);
        p.add(derecha, BorderLayout.EAST);
        return p;
    }

    // ------------------------------------------------------------------
    // API que usa la ventana
    // ------------------------------------------------------------------

    public void mostrar(Derivacion d) {
        detener();
        modelo.clear();
        for (Paso p : d.getPasos()) modelo.addElement(p);

        rotulo.setText("Expresi\u00F3n regular del lenguaje  \u00B7  " + d.getNombreMetodo());
        expresion.setText("L = " + d.getExpresionSimplificada());
        alternativa.setText(d.huboSimplificacion()
                ? "sin clausura positiva ni potencias:  L = " + d.getExpresion()
                : " ");

        if (!modelo.isEmpty()) lista.setSelectedIndex(0);
        actualizarBotones();
    }

    public void limpiar() {
        detener();
        modelo.clear();
        rotulo.setText("Expresi\u00F3n regular del lenguaje");
        expresion.setText(" ");
        alternativa.setText(" ");
        detalle.setDocument(new DefaultStyledDocument());
        actualizarBotones();
    }

    /** Reaplica el color de acento tras cambiarlo. */
    public void aplicarColor() {
        expresion.setForeground(Tema.getAcento());
        Paso p = lista.getSelectedValue();
        if (p != null) pintarDetalle(p);
        lista.repaint();
        repaint();
    }

    public boolean tienePasos() { return !modelo.isEmpty(); }

    // ------------------------------------------------------------------

    private void desplazar(int delta) {
        int i = lista.getSelectedIndex() + delta;
        if (i >= 0 && i < modelo.size()) {
            lista.setSelectedIndex(i);
            lista.ensureIndexIsVisible(i);
        }
    }

    private void alternarReproduccion() {
        if (temporizador != null && temporizador.isRunning()) { detener(); return; }
        if (modelo.isEmpty()) return;
        if (lista.getSelectedIndex() < 0) lista.setSelectedIndex(0);
        temporizador = new Timer(1600, e -> {
            int i = lista.getSelectedIndex();
            if (i + 1 >= modelo.size()) detener();
            else { lista.setSelectedIndex(i + 1); lista.ensureIndexIsVisible(i + 1); }
        });
        temporizador.start();
        reproducir.setText("Pausar");
    }

    private void detener() {
        if (temporizador != null) temporizador.stop();
        temporizador = null;
        reproducir.setText("Reproducir");
    }

    private void actualizarBotones() {
        int i = lista.getSelectedIndex(), n = modelo.size();
        anterior.setEnabled(i > 0);
        siguiente.setEnabled(i >= 0 && i < n - 1);
        reproducir.setEnabled(n > 1);
        contador.setText(n == 0 ? "Sin pasos todav\u00EDa" : "Paso " + (i + 1) + " de " + n);
    }

    // ------------------------------------------------------------------

    private void pintarDetalle(Paso p) {
        StyledDocument d = new DefaultStyledDocument();

        SimpleAttributeSet titulo    = estilo("SansSerif", 16, Font.BOLD, Tema.TINTA);
        SimpleAttributeSet cuerpo    = estilo("SansSerif", 13, Font.PLAIN, Tema.TINTA_SUAVE);
        SimpleAttributeSet seccion   = estilo("SansSerif", 11, Font.BOLD, Tema.acentoOscuro());
        SimpleAttributeSet regla     = estilo("Monospaced", 13, Font.BOLD, Tema.getAcento());
        SimpleAttributeSet formula   = estilo("Monospaced", 14, Font.BOLD, Tema.getAcento());
        SimpleAttributeSet contexto  = estilo("Monospaced", 12, Font.PLAIN, Tema.TINTA_SUAVE);

        try {
            insertar(d, "Paso " + p.getNumero() + " \u00B7 " + p.getTitulo() + "\n\n", titulo);

            if (!p.getExplicacion().isEmpty())
                insertar(d, p.getExplicacion() + "\n\n", cuerpo);

            if (!p.getRegla().isEmpty()) {
                insertar(d, "REGLA APLICADA\n", seccion);
                insertar(d, "   " + p.getRegla() + "\n\n", regla);
            }

            if (!p.getDesarrollo().isEmpty()) {
                insertar(d, "DESARROLLO\n", seccion);
                for (String linea : p.getDesarrollo()) insertar(d, "   " + linea + "\n", formula);
                insertar(d, "\n", cuerpo);
            }

            if (!p.getSituacion().isEmpty()) {
                insertar(d, "C\u00D3MO QUEDA\n", seccion);
                for (String linea : p.getSituacion()) insertar(d, linea + "\n", contexto);
            }
        } catch (BadLocationException ignorada) {
            // no puede ocurrir: siempre se inserta al final
        }

        detalle.setDocument(d);
        detalle.setCaretPosition(0);
    }

    /** Area de solo lectura que ajusta lineas y se confunde con el fondo. */
    private static void prepararArea(JTextArea a, java.awt.Font fuente, Color color) {
        a.setFont(fuente);
        a.setForeground(color);
        a.setBackground(Tema.PANEL);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(false);
        a.setBorder(BorderFactory.createEmptyBorder());
    }

    private static void insertar(StyledDocument d, String texto, SimpleAttributeSet a)
            throws BadLocationException {
        d.insertString(d.getLength(), texto, a);
    }

    private static SimpleAttributeSet estilo(String familia, int tamano, int estilo, Color color) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setFontFamily(a, familia);
        StyleConstants.setFontSize(a, tamano);
        StyleConstants.setBold(a, (estilo & Font.BOLD) != 0);
        StyleConstants.setForeground(a, color);
        return a;
    }
}
