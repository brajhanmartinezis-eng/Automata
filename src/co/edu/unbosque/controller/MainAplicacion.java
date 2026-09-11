package co.edu.unbosque.controller;

import co.edu.unbosque.model.*;
import co.edu.unbosque.view.*;

import javax.swing.SwingUtilities;


public class MainAplicacion {

    public static void main(String[] args) {
        Tema.instalar();
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                Automata modelo = new Automata();
                modelo.reemplazarPor(EjemplosAutomata.exactamenteUnUno());

                VentanaPrincipal vista = new VentanaPrincipal(modelo);
                Controller controlador = new Controller(modelo, vista);
                controlador.iniciar();

                vista.setVisible(true);
            }
        });
    }
}
