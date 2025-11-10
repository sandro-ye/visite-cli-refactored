package it.unibs.visite.model;
import java.io.Serializable;

import it.unibs.visite.core.Preconditions;

public class ParametriSistema implements Serializable {
    private String ambitoTerritoriale;   // impostabile una sola volta
    private int maxPersonePerIscrizione; // modificabile
    private boolean initialized;         // true dopo wizard una tantum

    public String getAmbitoTerritoriale() { return ambitoTerritoriale; }
    public int getMaxPersonePerIscrizione() { return maxPersonePerIscrizione; }
    public boolean isInitialized() { return initialized; }

    public void setAmbitoTerritorialeUnaTantum(String ambito) {
        if (this.ambitoTerritoriale != null) throw new IllegalStateException("Ambito già impostato");
        this.ambitoTerritoriale = ambito;
    }
   public void setMaxPersonePerIscrizione(int max){
        Preconditions.check(max > 0, "il numero massimo di persone deve essere > 0");
        this.maxPersonePerIscrizione = max;
        assert this.maxPersonePerIscrizione > 0;
    }
    public void markInitialized() { this.initialized = true; }
}
