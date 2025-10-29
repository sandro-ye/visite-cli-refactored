package it.unibs.visite.core;

import java.util.Objects;

/**
 * Classe di utilità per verificare precondizioni, postcondizioni e invarianti.
 * Se una condizione non è rispettata, lancia una DomainException con messaggio descrittivo.
 *
 * Esempi d'uso:
 *   Preconditions.notNull(obj, "L'oggetto non può essere nullo");
 *   Preconditions.check(x > 0, "x deve essere positivo");
 *   Preconditions.notBlank(stringa, "Il nome non può essere vuoto");
 */
public final class Preconditions {

    private Preconditions() {
        // utility class: costruttore privato
    }

    /** Controlla una condizione generica. */
    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new DomainException(message);
        }
    }

    /** Controlla che un oggetto non sia null. */
    public static <T> T notNull(T value, String message) {
        if (value == null) {
            throw new DomainException(message);
        }
        return value;
    }

    /** Controlla che una stringa non sia vuota o composta solo da spazi. */
    public static String notBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(message);
        }
        return value;
    }
}
