package it.unibs.visite.service;

import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import it.unibs.visite.model.Visita;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.model.AppPhase;

import it.unibs.visite.persistence.FileRepositoryPersistence;
import it.unibs.visite.repository.*;
import it.unibs.visite.security.AuthService;

/**
* Genera il piano mensile di visite proponibili in base alle disponibilità.
* Include vincoli aggiuntivi sui periodi programmabili e sull'assenza di
* sovrapposizioni temporali tra visite dello stesso luogo
*/
public class PlannerService {
    private final ParametriSistemaRepository parametriSistemaRepository;
    private final PreclusioneRepository preclusioneRepository;
    private final VolontarioRepository volontarioRepository;
    private final VisitaRepository visitaRepository;
    private final TipoVisitaRepository tipoVisitaRepository;
    private final LuogoRepository luogoRepository;
    private final DisponibilitaService disponibilitaService;

    public PlannerService(ParametriSistemaRepository parametriSistemaRepository, PreclusioneRepository preclusioneRepository, 
                VolontarioRepository volontarioRepository, VisitaRepository visitaRepository, 
                TipoVisitaRepository tipoVisitaRepository, LuogoRepository luogoRepository, DisponibilitaService disponibilitaService) {
        this.parametriSistemaRepository = parametriSistemaRepository;
        this.preclusioneRepository = preclusioneRepository;
        this.volontarioRepository = volontarioRepository;
        this.visitaRepository = visitaRepository;
        this.tipoVisitaRepository = tipoVisitaRepository;
        this.luogoRepository = luogoRepository;
        this.disponibilitaService = disponibilitaService;
    }

    public List<LocalDate> giorniNonPreclusiIn(YearMonth mese) {
        List<LocalDate> giorni = mese.atDay(1)
            .datesUntil(mese.atEndOfMonth().plusDays(1))
            .filter(d -> !preclusioneRepository.contains(d))
            .toList();
        return giorni;
    }

    public List<Volontario> volontariDisponibiliInDataPerTipo(LocalDate data, TipoVisita tipo) {
        List<Volontario> disponibili = tipo.getVolontariNicknames().stream()
            .filter(v -> disponibilitaService.verificaDisponibilita(v, data))
            .map(v -> volontarioRepository.findByNickname(v).orElse(null))
            .filter(Objects::nonNull)
            .toList();
        return disponibili;
    }

    public Map<LocalDate, List<TipoVisita>> visiteProgrammabiliPerData() {
        Map<LocalDate, List<TipoVisita>> mappa = new HashMap<>();
        for (LocalDate data : giorniNonPreclusiIn(YearMonth.now().plusMonths(1))) {
            List<TipoVisita> programmabili = tipiProgrammabiliInData(data);
            if (!programmabili.isEmpty()) {
                mappa.put(data, programmabili);
            }
        }
        return mappa;
    }
    
    private List<TipoVisita> tipiProgrammabiliInData(LocalDate data) {
        return tipoVisitaRepository.findAll().stream()
            .filter(t -> t.getDataInizioProgrammazione() != null && t.getDataFineProgrammazione() != null)
            .filter(t -> !data.isBefore(t.getDataInizioProgrammazione()) && !data.isAfter(t.getDataFineProgrammazione()))
            .toList();
    }

    private boolean siSovrapponeVisita(TipoVisita tipo, LocalDate data) {
        List<Visita> visiteStessoLuogo = visitaRepository.findAll().stream()
            .filter(v -> v.getData().equals(data)) // Considera solo visite nello stesso giorno
            .filter(v -> {
                Optional<TipoVisita> tOpt = tipoVisitaRepository.findById(v.getTipoVisitaId());
                return tOpt.isPresent() && tOpt.get().getLuogoId().equals(tipo.getLuogoId());
            })
            .toList();
        for (Visita v : visiteStessoLuogo) {
            Optional<TipoVisita> tOpt = tipoVisitaRepository.findById(v.getTipoVisitaId());
            if (tOpt.isPresent() && siSovrapponeOrario(tipo, tOpt.get())) {
                return true;
            }    
        }
        return false;
    }

    private boolean siSovrapponeOrario(TipoVisita a, TipoVisita b) {
        LocalTime inizioA = a.getOraInizio();
        LocalTime fineA = inizioA.plusMinutes(a.getDurataMinuti());
        LocalTime inizioB = b.getOraInizio();
        LocalTime fineB = inizioB.plusMinutes(b.getDurataMinuti());
        return inizioA.isBefore(fineB) && inizioB.isBefore(fineA);
    }

    public void salvaAssegnazioneVolontario(String volontario, TipoVisita tipo, LocalDate data) {
        if(siSovrapponeVisita(tipo, data)) {
            throw new IllegalStateException("Non è possibile assegnare questo volontario: sovrapposizione con altra visita nello stesso luogo");
        }
        Visita visita = new Visita(
            tipo.getId(), data,
            tipo.getNumeroMinimoPartecipanti(),
            tipo.getNumeroMassimoPartecipanti()
        );
        visita.setVolontarioNickname(volontario);
        visitaRepository.save(visita);
        FileRepositoryPersistence.salvaOggetto(visitaRepository, Paths.get("data", "visite-repo.ser"));

        // Aggiorna le disponibilità del volontario per quel mese
        disponibilitaService.rimuoviDisponibilita(volontario, data);
    }

    public void associaVolontarioATipoVisita(String nickname, String titolo) {
        Optional<TipoVisita> tipoOpt = tipoVisitaRepository.findByTitolo(titolo);
        if (tipoOpt.isEmpty()) {
            throw new IllegalArgumentException("Tipo di visita con titolo " + titolo + " non trovato");
        }
        TipoVisita tipo = tipoOpt.get();
        if (!volontarioRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("Volontario con nickname " + nickname + " non trovato");
        }
        tipo.addVolontario(nickname);
        tipoVisitaRepository.save(tipo);
        FileRepositoryPersistence.salvaOggetto(tipoVisitaRepository, Paths.get("data", "tipi-visita-repo.ser"));
    }

    public List<Volontario> getTuttiVolontari() {
        return volontarioRepository.findAll().stream()
            .sorted(Comparator.comparing(Volontario::getNickname))
            .collect(Collectors.toList());
    }

    public void aggiungiVolontario(String nickname) {
        if(volontarioRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("Volontario con nickname " + nickname + " già esistente");
        }
        volontarioRepository.save(new Volontario(nickname));
        new AuthService().createVolunteer(nickname);
        FileRepositoryPersistence.salvaOggetto(volontarioRepository, Paths.get("data", "volontari.ser"));
    }

    public void rimuoviVolontario(String nickname) {
        if(!volontarioRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("Volontario con nickname " + nickname + " non trovato");
        }
        volontarioRepository.delete(nickname);
        tipoVisitaRepository.findAll().stream()
            .filter(t -> t.getVolontariNicknames().contains(nickname))
            .forEach(t -> {
                t.removeVolontario(nickname);
                tipoVisitaRepository.save(t);
            });
        new AuthService().rimuoviCredenziali(nickname);
        FileRepositoryPersistence.salvaOggetto(volontarioRepository, Paths.get("data", "volontari.ser"));
    }

    public void aggiungiPreclusione(LocalDate data) {
        LocalDate oggi = LocalDate.now();
        if (data.isBefore(oggi.plusMonths(3)) || data.isAfter(oggi.plusMonths(4))) {
            throw new IllegalArgumentException("Impossibile aggiungere preclusione: data fuori dalla finestra temporale consentita (tra 3 e 4 mesi da oggi)");
        }
        if(preclusioneRepository.contains(data)) {
            throw new IllegalArgumentException("Preclusione per la data " + data + " già esistente");
        }
        preclusioneRepository.add(data);
        FileRepositoryPersistence.salvaOggetto(preclusioneRepository, Paths.get("data", "preclusioni.ser"));
    }

    public List<Luogo> getTuttiLuoghi() {
        return luogoRepository.findAllLuoghi().stream()
            .sorted(Comparator.comparing(Luogo::getNome))
            .collect(Collectors.toList());
    }

    public void aggiungiLuogo(String nome, String descrizione) {
        // Crea un nuovo luogo e salvalo nel repository
        if(luogoRepository.findByNome(nome).isPresent()) {
            throw new IllegalArgumentException("Luogo con nome " + nome + " già esistente");
        }
        Luogo nuovoLuogo = new Luogo(nome, descrizione);
        luogoRepository.save(nuovoLuogo);
        FileRepositoryPersistence.salvaOggetto(luogoRepository, Paths.get("data", "luoghi-repo.ser"));
    }

    public void rimuoviLuogo(String nome) {
        Optional<Luogo> luogoOpt = luogoRepository.findByNome(nome);
        if (luogoOpt.isEmpty()) {
            throw new IllegalArgumentException("Luogo con nome " + nome + " non trovato");
        }
        luogoRepository.deleteLuogo(luogoOpt.get().getId());
        FileRepositoryPersistence.salvaOggetto(luogoRepository, Paths.get("data", "luoghi-repo.ser"));
    }

    public List<TipoVisita> getTuttiTipiVisita() {
        return tipoVisitaRepository.findAll().stream()
            .sorted(Comparator.comparing(TipoVisita::getTitolo))
            .collect(Collectors.toList());
    }
    
    public void aggiungiTipoVisita(String luogoNome, String titolo, String descrizione) {
        Optional<Luogo> luogoOpt = luogoRepository.findByNome(luogoNome);
        if (luogoOpt.isEmpty()) {
            throw new IllegalArgumentException("Luogo con nome " + luogoNome + " non trovato");
        }
        TipoVisita tipo = new TipoVisita(luogoOpt.get().getId(), titolo, descrizione);
        tipoVisitaRepository.save(tipo);
        FileRepositoryPersistence.salvaOggetto(tipoVisitaRepository, Paths.get("data", "tipi-visita-repo.ser"));
    }

    public void rimuoviTipoVisita(String titolo) {
        // Rimuovi il tipo di visita dal repository
        Optional<TipoVisita> tipoOpt = tipoVisitaRepository.findByTitolo(titolo);
        if (tipoOpt.isEmpty()) {
            throw new IllegalArgumentException("Tipo di visita con titolo " + titolo + " non trovato");
        }
        tipoVisitaRepository.delete(tipoOpt.get().getId());
        FileRepositoryPersistence.salvaOggetto(tipoVisitaRepository, Paths.get("data", "tipi-visita-repo.ser"));
    }

    public void riapriRaccoltaDisponibilita() {
        parametriSistemaRepository.load().setAppPhase(AppPhase.RACCOLTA_DISPONIBILITA);
        FileRepositoryPersistence.salvaOggetto(parametriSistemaRepository, Paths.get("data", "parametri-sistema.ser"));
    }
}

    /*
    private final DataStore store;

    public PlannerService(DataStore store) { this.store = store; }

    public List<Visita> generaPiano(YearMonth mese) {
        List<LocalDate> giorni = mese.atDay(1)
            .datesUntil(mese.atEndOfMonth().plusDays(1))
            .filter(d -> !store.getPreclusioni(mese).contains(d))
            .filter(d -> store.getTipiVisita().stream().anyMatch(tipo ->
                                tipo.getVolontariNicknames().stream()
                                    .anyMatch(v -> store.volontarioDisponibile(v, d))
            ))
            .toList();

        Map<LocalDate, Set<String>> occupatiVolontari = new HashMap<>();
        Map<LocalDate, Set<String>> istanzeTipo = new HashMap<>();
        List<Visita> piano = new ArrayList<>();

        for (LocalDate data : giorni) {
            occupatiVolontari.put(data, new HashSet<>());
            istanzeTipo.put(data, new HashSet<>());

            // Traccia le visite già pianificate per ogni luogo in quella data
            // Contiene tutti i luoghi con le loro visite pianificabili per quel giorno
            Map<String, List<TipoVisita>> visitePerLuogo = new HashMap<>();

            //Recupera solo i tipi di visita programmabili per quella data
            for (TipoVisita tipo : store.tipiProgrammabili(data)) {

                // === [1] Vincolo: periodo programmabile ===
                if (data.isBefore(tipo.getDataInizioProgrammazione()) ||
                    data.isAfter(tipo.getDataFineProgrammazione())) {
                        continue;
                }

                // === [2] Vincolo: nessuna sovrapposizione oraria nello stesso luogo ===
                List<TipoVisita> esistentiLuogo = visitePerLuogo.getOrDefault(tipo.getLuogoId(), new ArrayList<>()); //Contiene tutti i tipo visita di un solo luogo per quel giorno
                boolean conflitto = false;
                for (TipoVisita altra : esistentiLuogo) {
                    if (siSovrappone(tipo, altra)) {
                        conflitto = true;
                        break;
                    }
                }
                if (conflitto) continue;

                // Filtra solo i volontari associati e disponibili per quella data
                List<String> candidati = tipo.getVolontariNicknames().stream()
                    .filter(v -> store.volontarioDisponibile(v, data))
                    .filter(v -> !occupatiVolontari.get(data).contains(v))
                    .toList();
                // Se nessuno è disponibile si passa al prossimo tipo di visita
                if (candidati.isEmpty()) {
                    System.out.println("Nessun volontario disponibile per la visita " + tipo.getTitolo() + " del " + data);
                    continue;
                }

                // Mostra solo i candidati realmente disponibili
                System.out.println("\n== Assegnazione volontario per la visita ==");
                System.out.println("Data: " + data);
                System.out.println("Tipo di visita: " + tipo.getTitolo());
                System.out.println("Luogo: " + store.getLuogo(tipo.getLuogoId()).getNome());
                System.out.println("Orario: " + tipo.getOraInizio() + "(durata " + tipo.getDurataMinuti() + " min)");

                for (int i = 0; i < candidati.size(); i++) {
                    System.out.printf("%d) %s%n", i + 1, candidati.get(i));
                }

                System.out.print("Scegli il numero del volontario da assegnare (invio per saltare): ");
                @SuppressWarnings("resource")
                String line = new Scanner(System.in).nextLine().trim();
                if (line.isEmpty()) {
                    System.out.println("Salto questa visita");
                    continue;
                }

                int scelta;
                try {
                    scelta = Integer.parseInt(line) - 1;
                    if  (scelta < 0 || scelta >= candidati.size()) {
                        System.out.println("Indice fuori intervallo, salto questa visita.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Scelta non valida, salto questa visita.");
                    continue;
                }

                String guida = candidati.get(scelta);

                // Crea la visita con il volontario scelto
                Visita visita = new Visita(
                    tipo.getId() + " " + data,
                    tipo.getId(), data,
                    tipo.getNumeroMinimoPartecipanti(),
                    tipo.getNumeroMassimoPartecipanti()
                );
                visita.setVolontarioNickname(guida);

                // Aggiungi al piano
                piano.add(visita);
                occupatiVolontari.get(data).add(guida);
                istanzeTipo.get(data).add(tipo.getId());

                //Aggiorna le visite già pianificate per quel luogo
                esistentiLuogo.add(tipo);
                visitePerLuogo.put(tipo.getLuogoId(), esistentiLuogo);
            }
        }

        store.setVisiteProposte(mese, piano);
        store.clearDisponibilita(mese);
        return piano;
    }

    private boolean siSovrappone(TipoVisita a, TipoVisita b) {
        LocalTime inizioA = a.getOraInizio();
        LocalTime fineA = inizioA.plusMinutes(a.getDurataMinuti());
        LocalTime inizioB = b.getOraInizio();
        LocalTime fineB = inizioB.plusMinutes(b.getDurataMinuti());
        return inizioA.isBefore(fineB) && inizioB.isBefore(fineA);
    }
        */
