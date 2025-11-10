package it.unibs.visite.model;

import java.io.Serializable;
import java.util.*;

public class Fruitore implements Serializable {
    private final String username;
    private final Set<Iscrizione> mieIscrizioni = new java.util.HashSet<>();

    public Fruitore(String username){ this.username = java.util.Objects.requireNonNull(username); }
    public String getUsername(){ return username; }
    public Set<Iscrizione> getMieIscrizioni(){ return java.util.Collections.unmodifiableSet(mieIscrizioni); }

    public Iscrizione getIscrizione(String codice){
        for(Iscrizione i : mieIscrizioni){
            if(i.getCodice().equals(codice)) return i;
        }
        return null;
    }
    public void removeIscrizione(String codice){
        mieIscrizioni.removeIf(i -> i.getCodice().equals(codice));
    }
    public void addIscrizione(Iscrizione i){ mieIscrizioni.add(i); }
}
