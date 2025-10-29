package it.unibs.visite.model;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Luogo implements Serializable {
    private final String id;
    private String nome;
    private String descrizione;

    public Luogo(String nome, String descrizione) {
        this.id = UUID.randomUUID().toString();
        this.nome = Objects.requireNonNull(nome);
        this.descrizione = descrizione == null ? "" : descrizione;
    }
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
}
