package co.edu.unbosque.view;

import co.edu.unbosque.model.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class DibujanteAutomata {

    public static final int RADIO = 27;

    /** Zona rectangular de una etiqueta y las transiciones que representa. */
    public static class ZonaEtiqueta {
        public final Rectangle2D area;
        public final List<Transicion> transiciones;

        ZonaEtiqueta(Rectangle2D area, List<Transicion> transiciones) {
            this.area = area;
            this.transiciones = transiciones;
        }
    }

    /** Datos de presentacion que no pertenecen al modelo. */
    public static class Contexto {
        public String estadoResaltado;
        public Estado seleccionado;
        public Estado origenTransicion;
        public Point puntoRaton;
    }

    private final List<ZonaEtiqueta> zonas = new ArrayList<ZonaEtiqueta>();

    public List<ZonaEtiqueta> getZonas() { return zonas; }

    // ------------------------------------------------------------------

    public void dibujar(Graphics2D g, Automata automata, Contexto ctx, int ancho, int alto) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Tema.PANEL);
        g.fillRect(0, 0, ancho, alto);
        dibujarRejilla(g, ancho, alto);

        zonas.clear();
        dibujarAristas(g, automata, ctx);

        if (ctx.origenTransicion != null && ctx.puntoRaton != null) {
            g.setColor(Tema.getAcento());
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[] { 6f, 6f }, 0f));
            g.draw(new Line2D.Double(ctx.origenTransicion.getX(), ctx.origenTransicion.getY(),
                    ctx.puntoRaton.x, ctx.puntoRaton.y));
        }

        for (Estado e : automata.getEstados()) dibujarEstado(g, e, ctx);
    }

    // ------------------------------------------------------------------

    private void dibujarRejilla(Graphics2D g, int ancho, int alto) {
        g.setColor(Tema.REJILLA);
        for (int x = 20; x < ancho; x += 24)
            for (int y = 20; y < alto; y += 24)
                g.fillRect(x, y, 1, 1);
    }

    private void dibujarAristas(Graphics2D g, Automata automata, Contexto ctx) {
        Map<String, List<Transicion>> grupos = new LinkedHashMap<String, List<Transicion>>();
        for (Transicion t : automata.getTransiciones()) {
            String clave = t.getOrigen().getNombre() + "\u0001" + t.getDestino().getNombre();
            List<Transicion> l = grupos.get(clave);
            if (l == null) { l = new ArrayList<Transicion>(); grupos.put(clave, l); }
            l.add(t);
        }

        for (List<Transicion> grupo : grupos.values()) {
            Estado a = grupo.get(0).getOrigen();
            Estado b = grupo.get(0).getDestino();

            StringBuilder etiqueta = new StringBuilder();
            for (Transicion t : grupo) {
                if (etiqueta.length() > 0) etiqueta.append(", ");
                etiqueta.append(t.getSimbolo());
            }

            boolean hayVuelta = a != b
                    && grupos.containsKey(b.getNombre() + "\u0001" + a.getNombre());
            boolean resaltada = ctx.estadoResaltado != null
                    && (a.getNombre().equals(ctx.estadoResaltado)
                     || b.getNombre().equals(ctx.estadoResaltado));

            g.setColor(resaltada ? Tema.getAcento() : Tema.TRAZO);
            g.setStroke(new BasicStroke(resaltada ? 2.4f : 1.5f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (a == b) dibujarBucle(g, a, etiqueta.toString(), grupo, resaltada);
            else dibujarArista(g, a, b, etiqueta.toString(),
                    hayVuelta ? 36 : 0, grupo, resaltada);
        }
    }

    private void dibujarArista(Graphics2D g, Estado a, Estado b, String etiqueta,
                               double curvatura, List<Transicion> grupo, boolean resaltada) {
        double x1 = a.getX(), y1 = a.getY(), x2 = b.getX(), y2 = b.getY();
        double dx = x2 - x1, dy = y2 - y1;
        double largo = Math.hypot(dx, dy);
        if (largo < 1) return;

        double px = -dy / largo, py = dx / largo;
        double cx = (x1 + x2) / 2 + px * curvatura;
        double cy = (y1 + y2) / 2 + py * curvatura;

        double angA = Math.atan2(cy - y1, cx - x1);
        double sx = x1 + RADIO * Math.cos(angA), sy = y1 + RADIO * Math.sin(angA);
        double angB = Math.atan2(cy - y2, cx - x2);
        double ex = x2 + RADIO * Math.cos(angB), ey = y2 + RADIO * Math.sin(angB);

        g.draw(new QuadCurve2D.Double(sx, sy, cx, cy, ex, ey));
        dibujarPunta(g, ex, ey, Math.atan2(ey - cy, ex - cx));

        double lx = 0.25 * sx + 0.5 * cx + 0.25 * ex;
        double ly = 0.25 * sy + 0.5 * cy + 0.25 * ey;
        dibujarEtiqueta(g, etiqueta, lx, ly - 4, grupo, resaltada);
    }

    private void dibujarBucle(Graphics2D g, Estado e, String etiqueta,
                              List<Transicion> grupo, boolean resaltada) {
        int x = e.getX(), y = e.getY();
        Path2D p = new Path2D.Double();
        p.moveTo(x - 13, y - RADIO + 4);
        p.curveTo(x - 46, y - RADIO - 50, x + 46, y - RADIO - 50, x + 13, y - RADIO + 4);
        g.draw(p);
        dibujarPunta(g, x + 13, y - RADIO + 4,
                Math.atan2((y - RADIO + 4) - (y - RADIO - 50), (x + 13) - (x + 46)));
        dibujarEtiqueta(g, etiqueta, x, y - RADIO - 34, grupo, resaltada);
    }

    private void dibujarPunta(Graphics2D g, double x, double y, double angulo) {
        double s = 10;
        Path2D p = new Path2D.Double();
        p.moveTo(x, y);
        p.lineTo(x - s * Math.cos(angulo - 0.42), y - s * Math.sin(angulo - 0.42));
        p.lineTo(x - s * Math.cos(angulo + 0.42), y - s * Math.sin(angulo + 0.42));
        p.closePath();
        g.fill(p);
    }

    private void dibujarEtiqueta(Graphics2D g, String texto, double x, double y,
                                 List<Transicion> grupo, boolean resaltada) {
        g.setFont(Tema.F_MONO_B);
        int w = g.getFontMetrics().stringWidth(texto);
        int h = g.getFontMetrics().getHeight();
        Rectangle2D caja = new Rectangle2D.Double(x - w / 2.0 - 5, y - h / 2.0 - 2, w + 10, h + 2);

        Color colorPrevio = g.getColor();
        Stroke trazoPrevio = g.getStroke();

        g.setColor(Tema.PANEL);
        g.fill(caja);
        g.setStroke(new BasicStroke(1f));
        g.setColor(resaltada ? Tema.getAcento() : Tema.BORDE);
        g.draw(caja);
        g.setColor(resaltada ? Tema.getAcento() : Tema.TINTA);
        g.drawString(texto, (float) (x - w / 2.0), (float) (y + h / 2.0 - 4));

        g.setColor(colorPrevio);
        g.setStroke(trazoPrevio);
        zonas.add(new ZonaEtiqueta(caja, grupo));
    }

    private void dibujarEstado(Graphics2D g, Estado e, Contexto ctx) {
        boolean resaltado = e.getNombre().equals(ctx.estadoResaltado);
        int x = e.getX(), y = e.getY();
        Shape circulo = new Ellipse2D.Double(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);

        g.setColor(resaltado ? Tema.acentoTenue() : Tema.PANEL);
        g.fill(circulo);
        g.setStroke(new BasicStroke(resaltado ? 3.2f : 1.9f));
        g.setColor(resaltado ? Tema.getAcento() : Tema.TRAZO);
        g.draw(circulo);

        if (e.esAceptacion()) {
            g.setStroke(new BasicStroke(resaltado ? 2.4f : 1.5f));
            g.draw(new Ellipse2D.Double(x - RADIO + 5, y - RADIO + 5,
                    (RADIO - 5) * 2, (RADIO - 5) * 2));
        }

        if (e.esInicial()) {
            g.setStroke(new BasicStroke(resaltado ? 2.6f : 1.9f));
            g.draw(new Line2D.Double(x - RADIO - 30, y, x - RADIO - 4, y));
            dibujarPunta(g, x - RADIO - 3, y, 0);
        }

        if (e == ctx.seleccionado) {
            g.setColor(Tema.SELECCION);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[] { 4f, 4f }, 0f));
            g.draw(new Ellipse2D.Double(x - RADIO - 6, y - RADIO - 6,
                    (RADIO + 6) * 2, (RADIO + 6) * 2));
        }

        g.setFont(Tema.F_ESTADO);
        g.setColor(resaltado ? Tema.acentoOscuro() : Tema.TINTA);
        int w = g.getFontMetrics().stringWidth(e.getNombre());
        g.drawString(e.getNombre(), x - w / 2, y + 5);
    }
}
