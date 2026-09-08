package co.edu.unbosque.model;


public class Estado {

    private String nombre;
    private int x, y;
    private boolean aceptacion;
    private boolean inicial;

    public Estado(String nombre, int x, int y) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getX() { return x; }

    public int getY() { return y; }

    public void mover(int x, int y) { this.x = x; this.y = y; }

    public boolean esAceptacion() { return aceptacion; }

    public void setAceptacion(boolean v) { aceptacion = v; }

    public boolean esInicial() { return inicial; }

    void setInicial(boolean v) { inicial = v; }

    public double distanciaA(int px, int py) {
        return Math.hypot(px - x, py - y);
    }

    @Override public String toString() { return nombre; }
}
