package co.edu.unbosque.view;

import co.edu.unbosque.controller.*;
import co.edu.unbosque.model.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class VentanaPrincipal extends JFrame implements VistaPrincipal {

    private static final long serialVersionUID = 1L;

    private final LienzoAutomata lienzo;
    private final PanelPasos panelPasos = new PanelPasos();
    private final JLabel barraEstado = new JLabel(" ");
    private final JButton botonConvertir = new JButton("Convertir AFN a AFD");
    private final JTextField campoCadena = new JTextField();
    private final JLabel resultadoCadena = new JLabel(" ");

    private AccionesVista acciones;

    public VentanaPrincipal(Automata automata) {
        super("AFD/AFN \u2192 Expresi\u00F3n regular  \u00B7  generador con paso a paso");
        this.lienzo = new LienzoAutomata(automata);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(construirMenu());

        JScrollPane scroll = new JScrollPane(lienzo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Tema.PANEL);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(Tema.FONDO);
        pie.add(construirBarraCadena(), BorderLayout.NORTH);
        pie.add(construirBarraEstado(), BorderLayout.SOUTH);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setBackground(Tema.FONDO);
        izquierda.add(construirBarra(), BorderLayout.NORTH);
        izquierda.add(scroll, BorderLayout.CENTER);
        izquierda.add(pie, BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, panelPasos);
        division.setResizeWeight(0.50);
        division.setDividerSize(6);
        division.setBorder(BorderFactory.createEmptyBorder());

        setContentPane(division);
        setSize(1340, 810);
        setMinimumSize(new Dimension(1020, 650));
        setLocationRelativeTo(null);
        division.setDividerLocation(0.5);

        panelPasos.setAlSeleccionarPaso(p -> {
            if (acciones != null) acciones.pasoSeleccionado(p);
        });
        panelPasos.setAlDibujarResultado(() -> {
            if (acciones != null) acciones.dibujarAutomataConvertido();
        });
    }

    // ------------------------------------------------------------------
    // Contrato de vista
    // ------------------------------------------------------------------

    @Override public void setAcciones(AccionesVista a) { acciones = a; }

    @Override public LienzoAutomata getLienzo() { return lienzo; }

    @Override public void mostrarDerivacion(Derivacion d) { panelPasos.mostrar(d); }

    @Override public void mostrarConversion(ConversionAFD c) { panelPasos.mostrarConversion(c); }

    @Override public void habilitarConversion(boolean habilitado) { botonConvertir.setEnabled(habilitado); }

    @Override public void limpiarDerivacion() { panelPasos.limpiar(); }

    @Override public void resaltarEstado(String nombre) { lienzo.setEstadoResaltado(nombre); }

    @Override public void mostrarEnBarra(String texto) { barraEstado.setText("  " + texto); }

    @Override public void mostrarResultadoCadena(String cadena, boolean aceptada) {
        String mostrada = cadena.isEmpty() ? "ε" : cadena;
        if (aceptada) {
            resultadoCadena.setText("✓  \"" + mostrada + "\" es aceptada");
            resultadoCadena.setForeground(Tema.acentoOscuro());
        } else {
            resultadoCadena.setText("✗  \"" + mostrada + "\" no es aceptada");
            resultadoCadena.setForeground(Tema.TINTA_SUAVE);
        }
    }

    @Override public void limpiarResultadoCadena() { resultadoCadena.setText(" "); }

    @Override public void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    @Override public void mostrarInformacion(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    @Override public void mostrarTextoLargo(String titulo, String cuerpo) {
        JTextArea area = new JTextArea(cuerpo);
        area.setEditable(false);
        area.setFont(Tema.F_MONO);
        area.setForeground(Tema.TINTA);
        area.setBackground(Tema.PANEL);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(720, 470));
        JOptionPane.showMessageDialog(this, sp, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    @Override public String pedirTexto(String mensaje, String valorInicial) {
        return (String) JOptionPane.showInputDialog(this, mensaje, "",
                JOptionPane.QUESTION_MESSAGE, null, null, valorInicial);
    }

    @Override public void aplicarColorPasoAPaso() {
        Tema.instalar();
        SwingUtilities.updateComponentTreeUI(this);
        panelPasos.aplicarColor();
        lienzo.repaint();
        repaint();
    }

    @Override public void refrescar() {
        lienzo.repaint();
        repaint();
    }

    // ------------------------------------------------------------------
    // Barra de herramientas
    // ------------------------------------------------------------------

    private JToolBar construirBarra() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.setBackground(Tema.FONDO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        barra.add(construirFicha());
        barra.add(Box.createHorizontalStrut(14));

        ButtonGroup grupo = new ButtonGroup();
        for (Herramienta h : Herramienta.values())
            barra.add(botonHerramienta(h, grupo, h == Herramienta.MOVER));

        barra.add(Box.createHorizontalGlue());

        botonConvertir.setFont(Tema.F_UI_B);
        botonConvertir.setForeground(Tema.acentoOscuro());
        botonConvertir.setEnabled(false);
        botonConvertir.addActionListener(e -> { if (acciones != null) acciones.convertirAAFD(); });
        barra.add(botonConvertir);
        barra.add(Box.createHorizontalStrut(10));

        JButton generar = new JButton("Generar expresi\u00F3n regular");
        generar.setFont(Tema.F_UI_B);
        generar.setForeground(Tema.acentoOscuro());
        generar.addActionListener(e -> { if (acciones != null) acciones.generarExpresion(); });
        barra.add(generar);

        return barra;
    }

    /** Ficha que se arrastra hasta el lienzo para crear un estado. */
    private JComponent construirFicha() {
        final JLabel ficha = new JLabel("\u25CF  arrastra un estado al lienzo");
        ficha.setFont(Tema.F_UI_B);
        ficha.setForeground(Tema.acentoOscuro());
        ficha.setOpaque(true);
        ficha.setBackground(Tema.acentoTenue());
        ficha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.acentoMedio(), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        ficha.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ficha.setToolTipText("Mant\u00E9n pulsado y suelta sobre el lienzo "
                + "para crear un estado nuevo");
        ficha.setTransferHandler(LienzoAutomata.fuenteArrastre());
        ficha.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                TransferHandler th = ficha.getTransferHandler();
                if (th != null) th.exportAsDrag(ficha, e, TransferHandler.COPY);
            }
        });
        ficha.setMaximumSize(new Dimension(270, 34));
        return ficha;
    }

    private JToggleButton botonHerramienta(final Herramienta h, ButtonGroup g, boolean sel) {
        JToggleButton b = new JToggleButton(h.getEtiqueta());
        b.setFont(Tema.F_UI);
        b.setToolTipText(h.getAyuda());
        b.setSelected(sel);
        b.addActionListener(e -> {
            if (acciones != null) acciones.seleccionarHerramienta(h);
        });
        g.add(b);
        return b;
    }

    private JPanel construirBarraCadena() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        p.setBackground(Tema.PANEL);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));

        JLabel etiqueta = new JLabel("Probar cadena:");
        etiqueta.setFont(Tema.F_UI);
        etiqueta.setForeground(Tema.TINTA_SUAVE);
        p.add(etiqueta);

        campoCadena.setFont(Tema.F_MONO);
        campoCadena.setColumns(16);
        campoCadena.addActionListener(e -> verificarCadena());
        p.add(campoCadena);

        JButton verificar = new JButton("Verificar");
        verificar.setFont(Tema.F_UI);
        verificar.addActionListener(e -> verificarCadena());
        p.add(verificar);

        resultadoCadena.setFont(Tema.F_UI_B);
        p.add(resultadoCadena);

        return p;
    }

    private void verificarCadena() {
        if (acciones != null) acciones.verificarCadena(campoCadena.getText());
    }

    private JPanel construirBarraEstado() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        p.setBackground(Tema.FONDO);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));
        barraEstado.setFont(Tema.F_UI);
        barraEstado.setForeground(Tema.TINTA_SUAVE);
        p.add(barraEstado);
        return p;
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    private JMenuBar construirMenu() {
        JMenuBar mb = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        archivo.add(item("Nuevo", e -> acciones.nuevoAutomata()));
        JMenu ejemplos = new JMenu("Cargar ejemplo");
        ejemplos.add(item("Cadenas que terminan en \"ab\"", e -> acciones.cargarEjemplo(0)));
        ejemplos.add(item("Cadenas con exactamente un 1  (\u03A3 = {0,1})",
                e -> acciones.cargarEjemplo(1)));
        ejemplos.add(item("N\u00FAmero par de aes", e -> acciones.cargarEjemplo(2)));
        ejemplos.add(item("Cadenas que contienen \"aa\"", e -> acciones.cargarEjemplo(3)));
        ejemplos.addSeparator();
        ejemplos.add(item("AFN: \"a⁺ b*\" (no determinista)", e -> acciones.cargarEjemplo(4)));
        archivo.add(ejemplos);
        archivo.addSeparator();
        archivo.add(item("Salir", e -> dispose()));
        mb.add(archivo);

        JMenu lenguaje = new JMenu("Lenguaje");
        lenguaje.add(item("Reflexi\u00F3n del lenguaje  L\u1D3F", e -> acciones.mostrarReflexion()));
        lenguaje.add(item("Potencia del lenguaje  L\u207F", e -> acciones.mostrarPotencia()));
        lenguaje.addSeparator();
        lenguaje.add(item("Cadenas de ejemplo del lenguaje",
                e -> acciones.mostrarCadenasDeEjemplo()));
        mb.add(lenguaje);

        JMenu ver = new JMenu("Ver");
        ver.add(item("Color del paso a paso\u2026", e -> elegirColor()));
        ver.addSeparator();

        JCheckBoxMenuItem clausura = new JCheckBoxMenuItem(
                "Usar clausura positiva y potencias", true);
        clausura.addActionListener(e ->
                acciones.alternarClausuraYPotencias(clausura.isSelected()));
        ver.add(clausura);
        ver.addSeparator();

        JMenu notacion = new JMenu("Notaci\u00F3n");
        ButtonGroup gn = new ButtonGroup();
        notacion.add(radio("Del curso:   a U b   ab   a*   a\u207A   a\u00B2",
                Notacion.deCurso(), gn, true));
        notacion.add(radio("Conjuntista:   a \u222A b   a\u00B7b   a*",
                Notacion.conjuntista(), gn, false));
        notacion.add(radio("Algebraica:   a + b   ab   a*",
                Notacion.algebraica(), gn, false));
        notacion.add(radio("ASCII pura:   a U b   ab   a*   a+   a^2",
                Notacion.ascii(), gn, false));
        ver.add(notacion);
        mb.add(ver);

        JMenu ayuda = new JMenu("Ayuda");
        ayuda.add(item("C\u00F3mo se usa", e -> acciones.mostrarAyuda()));
        ayuda.add(item("Operadores y reglas", e -> acciones.mostrarReglas()));
        mb.add(ayuda);

        return mb;
    }

    private JMenuItem item(String texto, ActionListener a) {
        JMenuItem m = new JMenuItem(texto);
        m.addActionListener(a);
        return m;
    }

    private JRadioButtonMenuItem radio(String texto, final Notacion n,
                                       ButtonGroup g, boolean sel) {
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(texto, sel);
        r.addActionListener(e -> acciones.cambiarNotacion(n));
        g.add(r);
        return r;
    }

    private void elegirColor() {
        Color c = JColorChooser.showDialog(this,
                "Color con el que se resalta el paso a paso", Tema.getAcento());
        if (c != null && acciones != null) acciones.cambiarColorPasoAPaso(c);
    }
}
