package it.unibs.visite.core;


/**
 * Eccezione di dominio applicativo.
 * Viene lanciata quando una regola di business o una precondizione
 * non viene rispettata (es. data non valida, ruolo errato, ecc.).
 *
 * È una RuntimeException, quindi non obbliga al catch esplicito,
 * ma può essere intercettata a livello di CLI per mostrare messaggi utente.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
