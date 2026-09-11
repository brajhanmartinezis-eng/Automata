package co.edu.unbosque.view;

import co.edu.unbosque.controller.*;
import co.edu.unbosque.model.*;
import java.io.File;
import java.util.List;

public interface VistaPrincipal {

    void setAcciones(AccionesVista acciones);

    void setMetodos(List<MetodoDerivacion> metodos);

    LienzoAutomata getLienzo();

    void mostrarDerivacion(Derivacion derivacion);

    void limpiarDerivacion();

    void resaltarEstado(String nombreEstado);

    void mostrarEnBarra(String texto);

    void mostrarError(String titulo, String mensaje);

    void mostrarInformacion(String titulo, String mensaje);

    void mostrarTextoLargo(String titulo, String cuerpo);

    String pedirTexto(String mensaje, String valorInicial);

    File pedirArchivoParaAbrir(String descripcion, String extension);

    File pedirArchivoParaGuardar(String descripcion, String extension);

    void aplicarColorPasoAPaso();

    void refrescar();
}
