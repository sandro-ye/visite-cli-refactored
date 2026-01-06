package it.unibs.visite.service;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import it.unibs.visite.core.Preconditions;
import it.unibs.visite.model.*;
import it.unibs.visite.persistence.FileRepositoryPersistence;
import it.unibs.visite.repository.ParametriSistemaRepository;
import it.unibs.visite.repository.memory.InMemoryParametriSistemaRepository;

public class InitWizardService{
    private final ConfigService config;
    private final ParametriSistemaRepository parametriSistemaRepository;

    public InitWizardService(ConfigService config) { 
        this.config = config;
        this.parametriSistemaRepository = FileRepositoryPersistence.caricaOggetto(
            Paths.get("data", "parametri-sistema.ser"),
            InMemoryParametriSistemaRepository::new
        );
    }

    public void esegui(String ambito, int maxPersonePerIscrizione) {
        ParametriSistema p = parametriSistemaRepository.load();

        if(p.isInitialized()) {
            throw new IllegalStateException("Parametri di sistema già inizializzati");
        }
        if(maxPersonePerIscrizione <= 0) {
            throw new IllegalArgumentException("Il numero massimo di persone per iscrizione deve essere > 0");
        }

        p.setAmbitoTerritorialeUnaTantum(ambito);
        p.setMaxPersonePerIscrizione(maxPersonePerIscrizione);
        p.markInitialized();

        parametriSistemaRepository.save(p);

        FileRepositoryPersistence.salvaOggetto(parametriSistemaRepository, 
                Paths.get("data", "parametri-sistema.ser"));
    }












    public Luogo addLuogo(String nome, String descrizione) {
        Preconditions.notBlank(nome, "Nome luogo obbligatorio");
        Luogo l = new Luogo(nome, descrizione);
        config.getSnapshot().addLuogo(l);
        config.save();
        return l;
    }

    public DataStore getSnapshot() {
        return config.getSnapshot();
    }

    public ParametriSistema getParametriSistema() {
        return config.getSnapshot().getParametri();
    }

    public void addLuogo(Luogo l) {
        Preconditions.notNull(l, "Luogo nullo");
        config.getSnapshot().addLuogo(l);
        config.save();
    }

    public boolean isInitialized() {
        return config.getSnapshot().getParametri().isInitialized();
    }

    public void markInitialized() {
        config.getSnapshot().getParametri().markInitialized();
        config.save();
    }

    public void setMaxPersone(int max) {
        config.getSnapshot().getParametri().setMaxPersonePerIscrizione(max);
        config.save();
    }

    public void setAmbitoUnaTantum(String ambito) {
        Preconditions.notBlank(ambito, "Ambito non può essere vuoto");
        ParametriSistema p = config.getSnapshot().getParametri();
        p.setAmbitoTerritorialeUnaTantum(ambito);
        config.save();
    }

    public Volontario addNewVolontario(String nickname) {
        Preconditions.notBlank(nickname, "Nickname volontario obbligatorio");
        Volontario v = new Volontario(nickname);
        config.getSnapshot().addVolontario(v);
        config.getAuthService().createVolunteer(nickname, config.getAuthService().getPswrdDefaultVolontario().toCharArray());
        config.save();
        return v;
    }

    public Volontario getVolontario(String nickname) {
        if(!config.getSnapshot().volontarioEsiste(nickname)) return addNewVolontario(nickname);
        else return config.getSnapshot().getVolontario(nickname);
    }

    public List<String> getAllVolontariNicknames() {
        return List.copyOf(config.getSnapshot().getVolontari().stream()
                .map(Volontario::getNickname)
                .collect(Collectors.toList()));
    }

    public TipoVisita addTipoVisitaInDS(String luogoId, String titolo, String descrizione) {
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
