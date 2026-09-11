package co.edu.unbosque.view;

import co.edu.unbosque.model.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;


public class LienzoAutomata extends JPanel implements OyenteAutomata {

    private static final long serialVersionUID = 1L;

    /** Marca que viaja en el arrastre desde la ficha de la barra. */
    public static final String CARGA_NUEVO_ESTADO = "AFD:NUEVO_ESTADO";

    /** Aviso de que se solto la ficha en un punto del lienzo. */
    public interface OyenteSoltar {
        void seSolto(int x, int y);
    }

    private final Automata automata;
    private final DibujanteAutomata dibujante = new DibujanteAutomata();
    private final DibujanteAutomata.Contexto contexto = new DibujanteAutomata.Contexto();

    private OyenteSoltar oyenteSoltar;

    public LienzoAutomata(Automata automata) {
        this.automata = automata;
        setBackground(Tema.PANEL);
        setPreferredSize(new Dimension(820, 560));
        setFocusable(true);
        automata.agregarOyente(this);
        instalarDestinoArrastre();
    }

    public Automata getAutomata() { return automata; }

    // ------------------------------------------------------------------
    // Estado de presentacion
    // ------------------------------------------------------------------

    public void setEstadoResaltado(String nombre) {
        contexto.estadoResaltado = nombre;
        repaint();
    }

    public void setSeleccionado(Estado e) {
        contexto.seleccionado = e;
        repaint();
    }

    public Estado getSeleccionado() { return contexto.seleccionado; }

    public void setOrigenTransicion(Estado e) {
        contexto.origenTransicion = e;
        repaint();
    }

    public Estado getOrigenTransicion() { return contexto.origenTransicion; }

    public void setPuntoRaton(Point p) { contexto.puntoRaton = p; }

    public void setOyenteSoltar(OyenteSoltar o) { oyenteSoltar = o; }

    // ------------------------------------------------------------------
    // Consultas geometricas: las responde la vista porque conoce el dibujo
    // ------------------------------------------------------------------

    public Estado estadoEn(int x, int y) {
        List<Estado> estados = automata.getEstados();
        for (int i = estados.size() - 1; i >= 0; i--) {
            Estado e = estados.get(i);
            if (e.distanciaA(x, y) <= DibujanteAutomata.RADIO) return e;
        }
        return null;
    }

    public List<Transicion> transicionesEn(int x, int y) {
        for (DibujanteAutomata.ZonaEtiqueta z : dibujante.getZonas())
            if (z.area.contains(x, y)) return z.transiciones;
        return Collections.emptyList();
    }

    /** Ajusta el tamano preferido para que quepan todos los estados. */
    public void ajustarTamano() {
        int ancho = 400, alto = 300;
        for (Estado e : automata.getEstados()) {
            ancho = Math.max(ancho, e.getX() + 140);
            alto = Math.max(alto, e.getY() + 140);
        }
        Dimension actual = getPreferredSize();
        if (actual.width != ancho || actual.height != alto) {
            setPreferredSize(new Dimension(ancho, alto));
            revalidate();
        }
    }

    // ------------------------------------------------------------------
    // Arrastrar y soltar
    // ------------------------------------------------------------------

    /** TransferHandler para la ficha arrastrable de la barra de herramientas. */
    public static TransferHandler fuenteArrastre() {
        return new TransferHandler() {
            @Override protected Transferable createTransferable(JComponent c) {
                return new StringSelection(CARGA_NUEVO_ESTADO);
            }
            @Override public int getSourceActions(JComponent c) { return COPY; }
        };
    }

    private void instalarDestinoArrastre() {
        setTransferHandler(new TransferHandler() {
            @Override public boolean canImport(TransferSupport s) {
                return s.isDrop() && s.isDataFlavorSupported(DataFlavor.stringFlavor);
            }
            @Override public boolean importData(TransferSupport s) {
                if (!canImport(s) || oyenteSoltar == null) return false;
                try {
                    Object dato = s.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    if (!CARGA_NUEVO_ESTADO.equals(dato)) return false;
                    Point p = s.getDropLocation().getDropPoint();
                    oyenteSoltar.seSolto(p.x, p.y);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        });
    }

    // ------------------------------------------------------------------

    @Override
    public void automataCambio(Automata a) {
        ajustarTamano();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        dibujante.dibujar(g, automata, contexto, getWidth(), getHeight());
        g.dispose();
    }
}
