package co.edu.unbosque.view;

import co.edu.unbosque.controller.*;
import co.edu.unbosque.model.*;

public interface VistaPrincipal {

    void setAcciones(AccionesVista acciones);

    LienzoAutomata getLienzo();

    void mostrarDerivacion(Derivacion derivacion);

    void mostrarConversion(ConversionAFD conversion);

    void habilitarConversion(boolean habilitado);

    void limpiarDerivacion();

    void resaltarEstado(String nombreEstado);

    void mostrarEnBarra(String texto);

    void mostrarResultadoCadena(String cadena, boolean aceptada);

    void limpiarResultadoCadena();

    void mostrarError(String titulo, String mensaje);

    void mostrarInformacion(String titulo, String mensaje);

    void mostrarTextoLargo(String titulo, String cuerpo);

    String pedirTexto(String mensaje, String valorInicial);

    void aplicarColorPasoAPaso();

    void refrescar();
}
