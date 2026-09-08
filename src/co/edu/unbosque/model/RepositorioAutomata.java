package co.edu.unbosque.model;

import java.io.File;
import java.io.IOException;


public interface RepositorioAutomata {

    String getExtension();

    String getDescripcionFormato();

    void guardar(Automata automata, File destino) throws IOException;

    Automata cargar(File origen) throws IOException;
}
