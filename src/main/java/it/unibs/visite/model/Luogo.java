package it.unibs.visite.model;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class Luogo implements Serializable {
    private final String id;
    private String nome;
    private String descrizione;
    private Set<String> tipiVisitaIds = new HashSet<>();

    public Luogo(String nome, String descrizione) {
        this.id = UUID.randomUUID().toString();
        this.nome = Objects.requireNonNull(nome);
        this.descrizione = descrizione == null ? "" : descrizione;
    }
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }

    public Set<String> getTipiVisitaIds() { return tipiVisitaIds; }

    //Aggiungi un tipo di visita alla lista tipiVisita
    public void addTipoVisita(TipoVisita tipoVisita) {
        tipiVisitaIds.add(tipoVisita.getId());
    }

    @Override
    public String toString() {
        return "Luogo{" + id + ", " + nome + "}";
    }
}
