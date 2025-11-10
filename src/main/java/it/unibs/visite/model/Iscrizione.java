package it.unibs.visite.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Iscrizione implements Serializable {
    private final String codice = UUID.randomUUID().toString();
    private final String codiceVisitaAssociato;
    private final Fruitore fruitore;
    private final int numeroPersone;

    public Iscrizione(Fruitore fruitore, String codiceVisita, int numeroPersone){
        if(numeroPersone < 1) throw new IllegalArgumentException("numero persone deve essere >= 1");
        this.fruitore = Objects.requireNonNull(fruitore);
        this.codiceVisitaAssociato = Objects.requireNonNull(codiceVisita);
        this.numeroPersone = numeroPersone;
    }

    public String getCodiceVisitaAssociato(){ return codiceVisitaAssociato; }
    public String getCodice(){ return codice; }
    public Fruitore getFruitore(){ return fruitore; }
    public int getNumeroPersone(){ return numeroPersone; }

    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Iscrizione)) return false;
        return codice.equals(((Iscrizione)o).codice);
    }
    @Override public int hashCode(){ return codice.hashCode(); }
}
