package co.edu.unbosque.model;

public interface VisitanteExpresion<T> {

    T verVacio(Vacio e);

    T verCadenaVacia(CadenaVacia e);

    T verSimbolo(Simbolo e);

    T verUnion(Union e);

    T verConcatenacion(Concatenacion e);

    T verEstrella(Estrella e);

    T verClausuraPositiva(ClausuraPositiva e);

    T verPotencia(Potencia e);
}
