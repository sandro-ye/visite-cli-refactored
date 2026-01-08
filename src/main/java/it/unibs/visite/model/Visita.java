package it.unibs.visite.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/*
    - rimuovi attributi non necessari (max e min partecipanti duplicati da TipoVisita)
*/

public class Visita implements Serializable {
    private final String id;
    private final String tipoVisitaId;
    private final LocalDate data;
    private String volontarioNickname;
    private StatoVisita stato = StatoVisita.PROPOSTA;
    private final int numeroMinimoPartecipanti;
    private final int numeroMassimoPartecipanti;
    private final java.util.List<Iscrizione> iscrizioni = new java.util.ArrayList<>();

    public Visita(String id, String tipoVisitaId, LocalDate data, int min, int max){
        this.id = Objects.requireNonNull(id);
        this.tipoVisitaId = Objects.requireNonNull(tipoVisitaId);
        this.data = Objects.requireNonNull(data);
        this.numeroMinimoPartecipanti = min;
        this.numeroMassimoPartecipanti = max;
    }

    public String getId(){ return id; }
    public String getTipoVisitaId(){ return tipoVisitaId; }
    public LocalDate getData(){ return data; }
    public String getVolontarioNickname(){ return volontarioNickname; }
    public void setVolontarioNickname(String v){ this.volontarioNickname = v; }
    public StatoVisita getStato(){ return stato; }
    public void setStato(StatoVisita s){ this.stato = s; }
    public int getNumeroMinimoPartecipanti(){ return numeroMinimoPartecipanti; }
    public int getNumeroMassimoPartecipanti(){ return numeroMassimoPartecipanti; }

    //  conteggio su persone e non su #prenotazioni
    public int getTotalePersone(){
        return iscrizioni.stream().mapToInt(Iscrizione::getNumeroPersone).sum();
    }

    public boolean isCompletaByPersone(){
        return getTotalePersone() >= numeroMassimoPartecipanti;
    }

    //  iscrizione con transizione di stato
    public Iscrizione aggiungiIscrizione(Fruitore fruitore, int numPersone){
        if(stato != StatoVisita.PROPOSTA && stato != StatoVisita.COMPLETA)
            throw new IllegalStateException("Iscrizioni non permesse in stato: "+stato);
        if(getTotalePersone() + numPersone > numeroMassimoPartecipanti)
            throw new IllegalStateException("superamento numero partecipanti");
        Iscrizione is = new Iscrizione(fruitore, id, numPersone);
        iscrizioni.add(is);
        if(isCompletaByPersone()) this.stato = StatoVisita.COMPLETA;
        return is;
    }

    //  disdetta con eventuale ritorno a PROPOSTA
    public void removeIscrizione(String codice){
        boolean removed = iscrizioni.removeIf(i -> i.getCodice().equals(codice));
        if(removed && stato==StatoVisita.COMPLETA && getTotalePersone() < numeroMassimoPartecipanti){
            this.stato = StatoVisita.PROPOSTA;
        }
    }

    public java.util.List<Iscrizione> getIscrizioni(){ return java.util.Collections.unmodifiableList(iscrizioni); }

    @Override public String toString(){
        return String.format("Visita{id=%s, tipo=%s, data=%s, guida=%s, stato=%s, persone=%d}",
            id, tipoVisitaId, data, volontarioNickname, stato, getTotalePersone());
    }
}
