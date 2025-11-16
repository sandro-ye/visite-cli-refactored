package it.unibs.visite.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Istanza concreta di un TipoVisita collocata in una specifica data,
 * con assegnazione di un volontario e stato della visita.
 */
public class Visita implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id; // es. "T1_2025-04-05"
    private final String tipoVisitaId;
    private final LocalDate data;
    private String volontarioNickname;
    private StatoVisita stato;
    private final int numeroMinimoPartecipanti;
    private final int numeroMassimoPartecipanti;
    private final Set<String> codiciPrenotazione = new HashSet<>();

    public Visita(String id, String tipoVisitaId, LocalDate data,
                  int numeroMinimoPartecipanti, int numeroMassimoPartecipanti) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tipoVisitaId);
        Objects.requireNonNull(data);
        this.id = id;
        this.tipoVisitaId = tipoVisitaId;
        this.data = data;
        this.numeroMinimoPartecipanti = numeroMinimoPartecipanti;
        this.numeroMassimoPartecipanti = numeroMassimoPartecipanti;
        this.stato = StatoVisita.PROPOSTA;
    }

    public String getId() { return id; }
    public String getTipoVisitaId() { return tipoVisitaId; }
    public LocalDate getData() { return data; }
    public String getVolontarioNickname() { return volontarioNickname; }
    public void setVolontarioNickname(String volontarioNickname) { this.volontarioNickname = volontarioNickname; }
    public StatoVisita getStato() { return stato; }
    public void setStato(StatoVisita stato) { this.stato = stato; }

    public int getNumeroIscritti() { return codiciPrenotazione.size(); }
    public int getNumeroMinimoPartecipanti() { return numeroMinimoPartecipanti; }
    public int getNumeroMassimoPartecipanti() { return numeroMassimoPartecipanti; }

    public boolean isCompleta() {
        return codiciPrenotazione.size() >= numeroMassimoPartecipanti;
    }

    public boolean addPrenotazione(String codice) {
        if (isCompleta()) return false;
        return codiciPrenotazione.add(codice);
    }

    public boolean removePrenotazione(String codice) {
        return codiciPrenotazione.remove(codice);
    }

    @Override
    public String toString() {
        return String.format("Visita{id=%s, tipo=%s, data=%s, guida=%s, stato=%s, iscritti=%d}",
                id, tipoVisitaId, data, volontarioNickname, stato, codiciPrenotazione.size());
    }
}
