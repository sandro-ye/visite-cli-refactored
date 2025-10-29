package it.unibs.visite.model;
import java.io.Serializable;
import java.util.*;
import it.unibs.visite.core.Preconditions;

public class TipoVisita implements Serializable {
    private final String id;
    private final String luogoId;
    private String titolo;
    private String descrizione;
    // vincolo: almeno 1 volontario
    private final Set<String> volontariNicknames = new HashSet<>();

    public TipoVisita(String luogoId, String titolo, String descrizione) {
        this.id = UUID.randomUUID().toString();
        this.luogoId = Objects.requireNonNull(luogoId);
        this.titolo = Objects.requireNonNull(titolo);
        this.descrizione = descrizione == null ? "" : descrizione;
    }
    public String getId() { return id; }
    public String getLuogoId() { return luogoId; }
    public String getTitolo() { return titolo; }
    public String getDescrizione() { return descrizione; }
    public Set<String> getVolontariNicknames() { return Collections.unmodifiableSet(volontariNicknames); }

    public void addVolontario(String nickname, DataStore ds) {
        Preconditions.notBlank(nickname, "nickname volontario obbligatorio");
        Preconditions.notNull(ds, "DataStore non puo essere null");
        Preconditions.check(ds.volontarioEsiste(nickname), "Volontario specificato non esiste");

        volontariNicknames.add(nickname);

        assert volontariNicknames.size() > 0 : "Invariante violato: ogni TipoVisita deve avere almeno un volontario";
    }

    public void removeVolontario(String nickname) {
        Preconditions.notBlank(nickname, "nickname volontario obbligatorio");
        volontariNicknames.remove(nickname);

        assert volontariNicknames.size() > 0 : "Invariante violato: ogni TipoVisita deve avere almeno un volontario";
    } 

    public void ensureInvariants() {
        Preconditions.notBlank(luogoId, "luogoId obbligatorio");
        Preconditions.notBlank(titolo, "titolo obbligatorio");
        Preconditions.notBlank(descrizione, "descrizione obbligatoria");
        Preconditions.check(!volontariNicknames.isEmpty(), "invariante violato: ogni TipoVisita deve avere almeno un volontario");
    }
}
