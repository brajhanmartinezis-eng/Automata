package co.edu.unbosque.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;


public class RepositorioTextoPlano implements RepositorioAutomata {

    @Override public String getExtension() { return "afd"; }

    @Override public String getDescripcionFormato() { return "Aut\u00F3mata (*.afd)"; }

    @Override
    public void guardar(Automata a, File destino) throws IOException {
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(destino), StandardCharsets.UTF_8));
        try {
            w.write("#AFD1");
            w.newLine();
            w.write("N;" + a.getNombre());
            w.newLine();
            for (Estado e : a.getEstados()) {
                w.write("E;" + e.getNombre() + ";" + e.getX() + ";" + e.getY() + ";"
                        + e.esInicial() + ";" + e.esAceptacion());
                w.newLine();
            }
            for (Transicion t : a.getTransiciones()) {
                w.write("T;" + t.getOrigen().getNombre() + ";" + t.getSimbolo() + ";"
                        + t.getDestino().getNombre());
                w.newLine();
            }
        } finally {
            w.close();
        }
    }

    @Override
    public Automata cargar(File origen) throws IOException {
        Automata a = new Automata();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(origen), StandardCharsets.UTF_8));
        try {
            String linea;
            while ((linea = r.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                String[] c = linea.split(";", -1);
                if (c[0].equals("N") && c.length >= 2) {
                    a.setNombre(c[1]);
                } else if (c[0].equals("E") && c.length >= 6) {
                    a.agregarEstadoSilencioso(c[1],
                            Integer.parseInt(c[2]), Integer.parseInt(c[3]),
                            Boolean.parseBoolean(c[4]), Boolean.parseBoolean(c[5]));
                } else if (c[0].equals("T") && c.length >= 4) {
                    Estado o = a.buscarEstado(c[1]);
                    Estado d = a.buscarEstado(c[3]);
                    if (o != null && d != null) a.agregarTransicionSilenciosa(o, c[2], d);
                }
            }
        } catch (NumberFormatException ex) {
            throw new IOException("El archivo tiene coordenadas mal formadas.", ex);
        } finally {
            r.close();
        }
        return a;
    }
}
