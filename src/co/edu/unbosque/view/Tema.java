package co.edu.unbosque.view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

public final class Tema {

    private static Color acento = new Color(0xC2, 0x5E, 0x1B); 

    public static final Color FONDO       = new Color(0xF4, 0xF1, 0xEC);
    public static final Color PANEL       = new Color(0xFF, 0xFF, 0xFF);
    public static final Color REJILLA     = new Color(0xE9, 0xE4, 0xDB);
    public static final Color TINTA       = new Color(0x22, 0x20, 0x1E);
    public static final Color TINTA_SUAVE = new Color(0x6E, 0x69, 0x62);
    public static final Color BORDE       = new Color(0xD2, 0xCC, 0xC2);
    public static final Color TRAZO       = new Color(0x33, 0x30, 0x2C);
    public static final Color SELECCION   = new Color(0x8A, 0x84, 0x7B);

    public static final Font F_UI     = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font F_UI_B   = new Font("SansSerif", Font.BOLD, 13);
    public static final Font F_MONO   = new Font("Monospaced", Font.PLAIN, 13);
    public static final Font F_MONO_B = new Font("Monospaced", Font.BOLD, 14);
    public static final Font F_RESULT = new Font("Monospaced", Font.BOLD, 20);
    public static final Font F_ESTADO = new Font("SansSerif", Font.BOLD, 14);

    private Tema() { }

    public static Color getAcento() { return acento; }

    public static void setAcento(Color c) { acento = c; }

    public static Color acentoTenue()  { return mezcla(acento, Color.WHITE, 0.86f); }

    public static Color acentoMedio()  { return mezcla(acento, Color.WHITE, 0.55f); }

    public static Color acentoOscuro() { return mezcla(acento, Color.BLACK, 0.35f); }

    public static Color mezcla(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed()   * (1 - t) + b.getRed()   * t),
                Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
                Math.round(a.getBlue()  * (1 - t) + b.getBlue()  * t));
    }

    public static void instalar() {
        final ColorUIResource p1 = new ColorUIResource(acentoOscuro());
        final ColorUIResource p2 = new ColorUIResource(acentoMedio());
        final ColorUIResource p3 = new ColorUIResource(acentoTenue());
        final ColorUIResource s1 = new ColorUIResource(SELECCION);
        final ColorUIResource s2 = new ColorUIResource(BORDE);
        final ColorUIResource s3 = new ColorUIResource(FONDO);
        final FontUIResource fu = new FontUIResource(F_UI);
        final FontUIResource fb = new FontUIResource(F_UI_B);

        try {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme() {
                @Override public String getName() { return "Sin azul"; }
                @Override protected ColorUIResource getPrimary1()   { return p1; }
                @Override protected ColorUIResource getPrimary2()   { return p2; }
                @Override protected ColorUIResource getPrimary3()   { return p3; }
                @Override protected ColorUIResource getSecondary1() { return s1; }
                @Override protected ColorUIResource getSecondary2() { return s2; }
                @Override protected ColorUIResource getSecondary3() { return s3; }
                @Override public FontUIResource getControlTextFont() { return fu; }
                @Override public FontUIResource getSystemTextFont()  { return fu; }
                @Override public FontUIResource getUserTextFont()    { return fu; }
                @Override public FontUIResource getMenuTextFont()    { return fu; }
                @Override public FontUIResource getSubTextFont()     { return fu; }
                @Override public FontUIResource getWindowTitleFont() { return fb; }
            });
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignorada) {
            // si falla se queda el look and feel por defecto
        }

        ColorUIResource selFondo = new ColorUIResource(acentoTenue());
        ColorUIResource selTexto = new ColorUIResource(acentoOscuro());
        String[] componentes = {
                "List", "Table", "Tree", "TextField", "TextArea", "TextPane",
                "EditorPane", "FormattedTextField", "PasswordField", "ComboBox"
        };
        for (String c : componentes) {
            UIManager.put(c + ".selectionBackground", selFondo);
            UIManager.put(c + ".selectionForeground", selTexto);
        }
        UIManager.put("MenuItem.selectionBackground", selFondo);
        UIManager.put("MenuItem.selectionForeground", selTexto);
        UIManager.put("Menu.selectionBackground", selFondo);
        UIManager.put("Menu.selectionForeground", selTexto);
        UIManager.put("RadioButtonMenuItem.selectionBackground", selFondo);
        UIManager.put("CheckBoxMenuItem.selectionBackground", selFondo);
        UIManager.put("ToolTip.background", selFondo);
        UIManager.put("ToolTip.foreground", new ColorUIResource(TINTA));
        UIManager.put("Panel.background", new ColorUIResource(FONDO));
        UIManager.put("OptionPane.background", new ColorUIResource(FONDO));
        UIManager.put("ProgressBar.foreground", new ColorUIResource(acento));
        UIManager.put("SplitPane.background", new ColorUIResource(FONDO));
    }
}
