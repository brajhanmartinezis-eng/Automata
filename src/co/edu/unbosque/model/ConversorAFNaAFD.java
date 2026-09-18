package co.edu.unbosque.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Convierte un AFN a un AFD equivalente por construccion de subconjuntos, dejando
 * una traza de los 5 pasos: tabla de transicion, nuevos estados, cambio de variables,
 * el automata resultante (con vista previa dibujada) y la poda de estados a los que
 * no se llega.
 */
public class ConversorAFNaAFD {

    public ConversionAFD convertir(Automata afn) {
        ConversionAFD c = new ConversionAFD();
        List<String> sigma = afn.getAlfabeto().comoLista();

        pasoTablaAFN(c, afn, sigma);

        List<Set<Estado>> subconjuntos = new ArrayList<Set<Estado>>();
        List<Map<String, Integer>> filas = new ArrayList<Map<String, Integer>>();

        Set<Estado> primero = new LinkedHashSet<Estado>();
        primero.add(afn.getInicial());
        subconjuntos.add(primero);

        int i = 0;
        while (i < subconjuntos.size()) {
            Set<Estado> actual = subconjuntos.get(i);
            Map<String, Integer> fila = new LinkedHashMap<String, Integer>();
            for (String simbolo : sigma) {
                Set<Estado> union = new LinkedHashSet<Estado>();
                for (Estado e : actual)
                    for (Transicion t : afn.transicionesDe(e, simbolo)) union.add(t.getDestino());
                if (union.isEmpty()) continue;
                int idx = subconjuntos.indexOf(union);
                if (idx == -1) { subconjuntos.add(union); idx = subconjuntos.size() - 1; }
                fila.put(simbolo, idx);
            }
            filas.add(fila);
            i++;
        }

        pasoNuevosEstados(c, sigma, subconjuntos, filas);
        pasoCambioDeVariables(c, sigma, subconjuntos, filas);

        Automata resultado = construirAutomata(afn, sigma, subconjuntos, filas);
        Paso p4 = pasoPintar(resultado);
        c.agregar(p4);

        pasoEliminarNoAlcanzables(c, resultado);

        c.setResultado(resultado);
        return c;
    }

    // ------------------------------------------------------------------
    // Paso 1
    // ------------------------------------------------------------------

    private void pasoTablaAFN(ConversionAFD c, Automata afn, List<String> sigma) {
        Paso p1 = new Paso("Hallar la tabla de transición",
                "Se lista, para cada estado del AFN y cada símbolo del alfabeto, a qué "
              + "estados se puede llegar. A diferencia de un AFD, una celda puede tener "
              + "varios destinos (∅ si no hay ninguno).");

        List<String> filas = new ArrayList<String>();
        String[][] celdas = new String[afn.getEstados().size()][sigma.size()];
        for (int i = 0; i < afn.getEstados().size(); i++) {
            Estado e = afn.getEstados().get(i);
            filas.add(e.getNombre());
            for (int j = 0; j < sigma.size(); j++) {
                List<Transicion> ts = afn.transicionesDe(e, sigma.get(j));
                celdas[i][j] = ts.isEmpty() ? "∅" : formatoDestinos(ts);
            }
        }
        for (String linea : tabla(sigma, filas, celdas)) p1.desarrollo(linea);
        c.agregar(p1);
    }

    // ------------------------------------------------------------------
    // Paso 2
    // ------------------------------------------------------------------

    private void pasoNuevosEstados(ConversionAFD c, List<String> sigma,
                                   List<Set<Estado>> subconjuntos, List<Map<String, Integer>> filas) {
        Paso p2 = new Paso("Identificar los nuevos estados",
                "Cada nuevo estado es un subconjunto de estados del AFN. Se parte del "
              + "subconjunto con solo el estado inicial y, para cada símbolo, se une el "
              + "destino de todas las transiciones posibles desde ese subconjunto; cada unión "
              + "nueva es otro subconjunto (estado) del futuro AFD. En total se descubrieron "
              + subconjuntos.size() + " subconjunto(s).");
        p2.conRegla("nuevo(S, σ) = ⋃ { δ(q, σ) : q ∈ S }");

        List<String> etiquetasFilas = new ArrayList<String>();
        String[][] celdas = new String[subconjuntos.size()][sigma.size()];
        for (int i = 0; i < subconjuntos.size(); i++) {
            etiquetasFilas.add(formato(subconjuntos.get(i)));
            for (int j = 0; j < sigma.size(); j++) {
                Integer destino = filas.get(i).get(sigma.get(j));
                celdas[i][j] = destino == null ? "∅" : formato(subconjuntos.get(destino));
            }
        }
        for (String linea : tabla(sigma, etiquetasFilas, celdas)) p2.desarrollo(linea);
        c.agregar(p2);
    }

    // ------------------------------------------------------------------
    // Paso 3
    // ------------------------------------------------------------------

    private void pasoCambioDeVariables(ConversionAFD c, List<String> sigma,
                                       List<Set<Estado>> subconjuntos, List<Map<String, Integer>> filas) {
        Paso p3 = new Paso("Cambio de variables (δ, k0, k1, k2, ...)",
                "Se nombra cada subconjunto en el orden en que se descubrió, empezando por "
              + "k0 para el subconjunto inicial, y se reescribe la tabla anterior con esos "
              + "nombres.");
        for (int j = 0; j < subconjuntos.size(); j++)
            p3.desarrollo("k" + j + " = " + formato(subconjuntos.get(j)));
        p3.desarrollo("");

        List<String> etiquetasFilas = new ArrayList<String>();
        String[][] celdas = new String[subconjuntos.size()][sigma.size()];
        for (int i = 0; i < subconjuntos.size(); i++) {
            etiquetasFilas.add("k" + i);
            for (int j = 0; j < sigma.size(); j++) {
                Integer destino = filas.get(i).get(sigma.get(j));
                celdas[i][j] = destino == null ? "∅" : "k" + destino;
            }
        }
        for (String linea : tabla(sigma, etiquetasFilas, celdas)) p3.desarrollo(linea);
        c.agregar(p3);
    }

    // ------------------------------------------------------------------
    // Paso 4
    // ------------------------------------------------------------------

    private Automata construirAutomata(Automata afn, List<String> sigma,
                                       List<Set<Estado>> subconjuntos, List<Map<String, Integer>> filas) {
        Automata resultado = new Automata();
        resultado.setNombre("AFD equivalente a " + afn.getNombre());

        int n = subconjuntos.size();
        List<Estado> nuevos = new ArrayList<Estado>();
        for (int j = 0; j < n; j++) {
            int[] xy = puntoEnCirculo(j, n);
            boolean aceptacion = false;
            for (Estado e : subconjuntos.get(j))
                if (e.esAceptacion()) { aceptacion = true; break; }
            nuevos.add(resultado.agregarEstadoSilencioso("k" + j, xy[0], xy[1], j == 0, aceptacion));
        }
        for (int j = 0; j < n; j++)
            for (String simbolo : sigma) {
                Integer destino = filas.get(j).get(simbolo);
                if (destino != null)
                    resultado.agregarTransicionSilenciosa(nuevos.get(j), simbolo, nuevos.get(destino));
            }
        return resultado;
    }

    /**
     * Coordenadas en circulo para evitar que, con varios estados en fila, una
     * transicion entre dos de ellos pase por encima de un tercero.
     */
    private int[] puntoEnCirculo(int indice, int total) {
        int cx = 420, cy = 300;
        if (total <= 1) return new int[] { cx, cy };
        int radio = Math.max(150, 70 * total / 2);
        double angulo = -Math.PI / 2 + 2 * Math.PI * indice / total;
        int x = cx + (int) Math.round(radio * Math.cos(angulo));
        int y = cy + (int) Math.round(radio * Math.sin(angulo));
        return new int[] { x, y };
    }

    private Paso pasoPintar(Automata resultado) {
        Paso p4 = new Paso("Pintar el nuevo autómata",
                "Con los nombres k0..k" + (resultado.getEstados().size() - 1) + " y la tabla del "
              + "paso anterior ya se puede dibujar el AFD: k0 es el estado inicial, y un estado "
              + "ki es de aceptación si su subconjunto contenía algún estado de aceptación "
              + "del AFN. Los estados se acomodan en círculo para que ninguna transición "
              + "quede tapada por otro estado.");
        p4.desarrollo("Estados: " + nombresDe(resultado.getEstados()));
        p4.desarrollo("Inicial: k0");
        p4.desarrollo("Aceptación: " + nombresAceptacion(resultado.getEstados()));
        p4.conAutomata(resultado);
        return p4;
    }

    // ------------------------------------------------------------------
    // Paso 5
    // ------------------------------------------------------------------

    private void pasoEliminarNoAlcanzables(ConversionAFD c, Automata resultado) {
        Set<Estado> alcanzables = resultado.estadosAlcanzables();
        List<Estado> podados = new ArrayList<Estado>();
        for (Estado e : new ArrayList<Estado>(resultado.getEstados()))
            if (!alcanzables.contains(e)) podados.add(e);
        for (Estado e : podados) resultado.eliminarEstado(e);

        Paso p5 = new Paso("Eliminar salidas a las que no se llega",
                "Se revisa qué estados del AFD construido son alcanzables desde k0; como el "
              + "paso 2 solo descubre subconjuntos a los que efectivamente se llega, esta "
              + "verificación normalmente no elimina nada, pero se deja como comprobación "
              + "explícita.");
        if (podados.isEmpty())
            p5.situacion("Los " + resultado.getEstados().size()
                    + " estados construidos son alcanzables desde k0; no hay nada que eliminar.");
        else
            for (Estado e : podados) p5.situacion("Se elimina " + e.getNombre() + ": no se llega a él.");
        c.agregar(p5);
    }

    // ------------------------------------------------------------------
    // Formato de tabla (monoespaciado: delta arriba, estados a la izquierda)
    // ------------------------------------------------------------------

    private List<String> tabla(List<String> columnas, List<String> filas, String[][] celdas) {
        int anchoEtiqueta = "δ".length();
        for (String f : filas) anchoEtiqueta = Math.max(anchoEtiqueta, f.length());

        int[] anchoCol = new int[columnas.size()];
        for (int j = 0; j < columnas.size(); j++) {
            anchoCol[j] = columnas.get(j).length();
            for (int i = 0; i < filas.size(); i++)
                anchoCol[j] = Math.max(anchoCol[j], celdas[i][j].length());
        }

        List<String> lineas = new ArrayList<String>();
        StringBuilder cabecera = new StringBuilder(pad("δ", anchoEtiqueta));
        for (int j = 0; j < columnas.size(); j++)
            cabecera.append("   ").append(pad(columnas.get(j), anchoCol[j]));
        lineas.add(cabecera.toString());

        for (int i = 0; i < filas.size(); i++) {
            StringBuilder fila = new StringBuilder(pad(filas.get(i), anchoEtiqueta));
            for (int j = 0; j < columnas.size(); j++)
                fila.append("   ").append(pad(celdas[i][j], anchoCol[j]));
            lineas.add(fila.toString());
        }
        return lineas;
    }

    private String pad(String s, int ancho) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < ancho) sb.append(' ');
        return sb.toString();
    }

    // ------------------------------------------------------------------

    private String formatoDestinos(List<Transicion> ts) {
        List<String> nombres = new ArrayList<String>();
        for (Transicion t : ts) nombres.add(t.getDestino().getNombre());
        Collections.sort(nombres);
        return "{" + String.join(", ", nombres) + "}";
    }

    private String formato(Set<Estado> subconjunto) {
        List<String> nombres = new ArrayList<String>();
        for (Estado e : subconjunto) nombres.add(e.getNombre());
        Collections.sort(nombres);
        return "{" + String.join(", ", nombres) + "}";
    }

    private String nombresDe(List<Estado> estados) {
        List<String> nombres = new ArrayList<String>();
        for (Estado e : estados) nombres.add(e.getNombre());
        return String.join(", ", nombres);
    }

    private String nombresAceptacion(List<Estado> estados) {
        List<String> nombres = new ArrayList<String>();
        for (Estado e : estados) if (e.esAceptacion()) nombres.add(e.getNombre());
        return nombres.isEmpty() ? "(ninguno)" : String.join(", ", nombres);
    }
}
