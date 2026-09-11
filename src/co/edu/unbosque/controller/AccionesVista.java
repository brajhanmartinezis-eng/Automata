package co.edu.unbosque.controller;

import co.edu.unbosque.model.*;
import java.awt.Color;


public interface AccionesVista {

    void nuevoAutomata();

    void cargarEjemplo(int indice);

    void abrirArchivo();

    void guardarArchivo();

    void seleccionarHerramienta(Herramienta herramienta);

    void seleccionarMetodo(MetodoDerivacion metodo);

    void generarExpresion();

    void cambiarNotacion(Notacion notacion);

    void cambiarColorPasoAPaso(Color color);

    void alternarClausuraYPotencias(boolean activo);

    void mostrarReflexion();

    void mostrarPotencia();

    void mostrarCadenasDeEjemplo();

    void mostrarAyuda();

    void mostrarReglas();

    void pasoSeleccionado(Paso paso);
}
