package it.unibs.visite.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Visita implements Serializable{
    private final String id;
    private final String tipoVisitaId;
    private final LocalDate data;
    private StatoVisita stato;
    private String volontarioAssegnato;

    public Visita(String tipoVisitaId, LocalDate data, StatoVisita stato) {
        this.id = UUID.randomUUID().toString();
        this.tipoVisitaId = Objects.requireNonNull(tipoVisitaId);
        this.data = Objects.requireNonNull(data);
        this.stato = Objects.requireNonNull(stato);
    }

    public String getId() { return id; }
    public String getTipoVisitaId() { return tipoVisitaId; }
    public LocalDate getData() { return data; }
    public StatoVisita getStato() { return stato; }
    public void setStato(StatoVisita stato) { this.stato = Objects.requireNonNull(stato); }

    public String getVolontarioAssegnato() { return volontarioAssegnato; }
    public void setVolontarioAssegnato(String volontarioAssegnato) { this.volontarioAssegnato = volontarioAssegnato; }
    
    @Override
    public String toString() {
        return "visita{" + 
        "id= " + id + 
        ", tipoVisitaId= " + tipoVisitaId + 
        ", data= " + data + 
        ", stato= " + stato + 
        ", volontario= " + volontarioAssegnato + '}';
    }
}
