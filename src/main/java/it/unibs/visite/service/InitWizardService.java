package it.unibs.visite.service;

import it.unibs.visite.core.Preconditions;
import it.unibs.visite.model.*;

public class InitWizardService {
    private final ConfigService config;

    public InitWizardService(ConfigService config) { this.config = config; }

    public Luogo addLuogo(String nome, String descrizione) {
        Preconditions.notBlank(nome, "Nome luogo obbligatorio");
        Luogo l = new Luogo(nome, descrizione);
        config.getSnapshot().addLuogo(l);
        config.save();
        return l;
    }

    public Volontario addVolontario(String nickname) {
        Preconditions.notBlank(nickname, "Nickname volontario obbligatorio");
        Volontario v = new Volontario(nickname);
        config.getSnapshot().addVolontario(v);
        config.save();
        return v;
    }

    public TipoVisita addTipoVisita(String luogoId, String titolo, String descrizione) {
        Preconditions.notBlank(luogoId, "Luogo obbligatorio");
        Preconditions.notBlank(titolo, "Titolo tipo visita obbligatorio");
        if (config.getSnapshot().getLuogo(luogoId) == null)
            throw new IllegalArgumentException("Luogo inesistente");
        TipoVisita t = new TipoVisita(luogoId, titolo, descrizione);
        config.getSnapshot().addTipoVisita(t);
        config.save();
        return t;
    }

    public void assocVolontarioATipo(String tipoVisitaId, String nickname) {
        TipoVisita t = config.getSnapshot().getTipoVisita(tipoVisitaId);
        if (t == null) throw new IllegalArgumentException("TipoVisita inesistente");
        if (config.getSnapshot().getVolontario(nickname) == null)
            throw new IllegalArgumentException("Volontario inesistente");
        t.addVolontario(nickname, config.getSnapshot());
        config.save();
    }

    public void validateInvariants() {
        // ogni TipoVisita ha >=1 volontario
        for (TipoVisita t : config.getSnapshot().getTipiVisita()) t.ensureInvariants();
    }
}
