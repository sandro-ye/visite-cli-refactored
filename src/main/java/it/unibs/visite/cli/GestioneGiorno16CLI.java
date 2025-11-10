package it.unibs.visite.cli;

import it.unibs.visite.model.*;
import it.unibs.visite.service.RegimeService;
import it.unibs.visite.service.ConfigService;

import java.util.*;

/**
 * CLI dedicata alle operazioni di aggiunta e rimozione
 * consentite esclusivamente il giorno 16 di ogni mese.
 * Richiamata da GiornoSediciCLI durante la fase 2.
 */
public class GestioneGiorno16CLI {

    private final Scanner in;
    private final ConfigService config;

    public GestioneGiorno16CLI(Scanner in, RegimeService regime) {
        this.in = in;
        this.config = regime.getConfigService();

        //Vincolo temporale: operazioni ammesse solo il giorno 16
        int giorno = java.time.LocalDate.now().getDayOfMonth();
        if (giorno != 16) {
            throw new IllegalStateException(
                    "Le operazioni di gestione sono consentite solo il giorno 16 del mese.");
        }
    }

    /**
     * Menu principale per la gestione di luoghi, tipi e volontari.
     */
    public void run() {
        while (true) {
            System.out.println("\n=== MENU GESTIONE (GIORNO 16) ===");
            System.out.println("1) Aggiungi luogo");
            System.out.println("2) Rimuovi luogo");
            System.out.println("3) Aggiungi tipo di visita");
            System.out.println("4) Rimuovi tipo di visita");
            System.out.println("5) Aggiungi volontario");
            System.out.println("6) Rimuovi volontario");
            System.out.println("7) Associa volontario a tipo di visita");
            System.out.println("0) Esci gestione giorno 16");
            System.out.print("Scelta: ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> aggiungiLuogo();
                    case "2" -> rimuoviLuogo();
                    case "3" -> aggiungiTipoVisita();
                    case "4" -> rimuoviTipoVisita();
                    case "5" -> aggiungiVolontario();
                    case "6" -> rimuoviVolontario();
                    case "7" -> associaVolontarioATipoVisita();
                    case "0" -> { return; }
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    // ---  Metodi di gestione -----------------------------------------------------

    private void aggiungiLuogo() {
        System.out.println("== Nuovo luogo ==");
        System.out.print("Nome luogo: ");
        String nome = in.nextLine().trim();
        System.out.print("Inserisci descrizione: ");
        String descrizione = in.nextLine().trim();

        Luogo luogo = new Luogo(nome, descrizione);
        List<TipoVisita> tipiDaAssociare = new ArrayList<>();

        System.out.println("Devi creare almeno un tipo di visita per questo nuovo luogo:");
        do {
            // creaTipoVisita costruisce il TipoVisita con luogoId impostato
            TipoVisita nuovo = creaTipoVisita(luogo);
            // Prima di poter essere  usato il TipoVisita deve aver associato almeno un volontario
            associaVolontarioANuovoTipoVisita(nuovo);
            
            tipiDaAssociare.add(nuovo);

            System.out.print("Vuoi aggiungere un altro tipo? (s/n): ");
        } while (in.nextLine().trim().equalsIgnoreCase("s"));

        config.aggiungiLuogo(luogo, tipiDaAssociare);
    }

    private void rimuoviLuogo() {
        System.out.print("ID del luogo da rimuovere: ");
        String id = in.nextLine().trim();
        config.rimuoviLuogo(id);
        System.out.println("✔ Luogo rimosso.");
    }

    private void aggiungiTipoVisita() {
        var luoghi = new ArrayList<>(config.getSnapshot().getLuoghi());
        Map<Integer, Luogo> idx2Luogo = new LinkedHashMap<>();
        int i = 1;
        for (Luogo l : luoghi) { idx2Luogo.put(i, l); System.out.printf("%d) %s%n", i++, l.getNome()); }
        while (true) {
            System.out.print("Aggiungere un tipo di visita? (s/n): ");
            if (!in.nextLine().trim().equalsIgnoreCase("s")) break;

            int scelta;
            while (true) {
                try {
                    System.out.print("Scegli luogo (numero): ");
                    scelta = Integer.parseInt(in.nextLine());
                    if (!idx2Luogo.containsKey(scelta)) throw new RuntimeException();
                    break;
                } catch (Exception e) { System.out.println("Scelta non valida."); }
            }
            Luogo l = idx2Luogo.get(scelta);
            System.out.print("Titolo tipo visita: "); String titolo = in.nextLine();
            System.out.print("Descrizione: "); String desc = in.nextLine();

            TipoVisita tipo = new TipoVisita(l.getId(), titolo, desc);
            config.aggiungiTipoVisita(tipo);
            System.out.println("✔ Tipo di visita aggiunto.");

            var volontari = new ArrayList<>(config.getSnapshot().getVolontari());
            if (volontari.isEmpty()) {
                System.out.println("Attenzione: non ci sono volontari, creane almeno uno prima.");
            } else {
                System.out.println("Associa volontari (almeno uno). Disponibili:");
                for (Volontario v : volontari) System.out.println(" - " + v.getNickname());
                boolean added = false;
                while (true) {
                    System.out.print("Nickname volontario da associare (invio per terminare): ");
                    String nick = in.nextLine().trim();
                    if (nick.isEmpty()) {
                        if (added) break;
                        System.out.println("Devi associare almeno un volontario.");
                        continue;
                    }
                    try {
                        this.assocVolontarioATipo(tipo.getId(), nick);
                        added = true;
                        System.out.println("Associato.");
                    } catch (Exception e) { System.out.println("Errore: " + e.getMessage()); }
                }
            }
        }
    }
    
    private void rimuoviTipoVisita() {
        System.out.println("== Rimozione tipo di visita ==");

        // Recupera tutti i tipi di visita correnti
        List<TipoVisita> tipi = new ArrayList<>(config.getSnapshot().getTipiVisita());
        if (tipi.isEmpty()) {
            System.out.println("Nessun tipo di visita presente.");
            return;
        }

        // Mostra elenco con ID e luogo
        for (int i = 0; i < tipi.size(); i++) {
            TipoVisita t = tipi.get(i);
            String luogoNome = Optional.ofNullable(config.getSnapshot().getLuogo(t.getLuogoId()))
                                  .map(Luogo::getNome)
                                  .orElse("luogo sconosciuto");
            System.out.printf("%d) %s (%s) – Luogo: %s%n",
                    i + 1, t.getTitolo(), t.getId(), luogoNome);
        }

        // Scelta dell’utente
        System.out.print("Scegli il numero del tipo di visita da rimuovere: ");
        String input = in.nextLine().trim();
        int scelta;
        try {
            scelta = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Scelta non valida.");
            return;
        }

        if (scelta < 0 || scelta >= tipi.size()) {
            System.out.println("Indice fuori intervallo.");
            return;
        }

        TipoVisita daRimuovere = tipi.get(scelta);

        // Chiede conferma
        System.out.printf("Confermi la rimozione del tipo di visita '%s'? (s/n): ", daRimuovere.getTitolo());
        if (!in.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println("Operazione annullata.");
            return;
        }

        config.rimuoviTipo(daRimuovere.getId());
        System.out.println("Tipo di visita rimosso.");
    }

    private void aggiungiVolontario() {
        System.out.println("== Nuovo volontario ==");
        System.out.print("Nickname volontario: ");
        String nick = in.nextLine().trim();
        Volontario v = new Volontario(nick);
        config.aggiungiVolontario(v);
        System.out.println("✔ Volontario aggiunto.");
    }

    private void rimuoviVolontario() {
        System.out.print("Nickname del volontario da rimuovere: ");
        String nick = in.nextLine().trim();
        config.rimuoviVolontario(nick);
        System.out.println("Volontario rimosso.");
    }

    private void associaVolontarioATipoVisita() {
        System.out.println("== Associazione volontario ↔ tipo di visita ==");

        // Mostra elenco dei tipi di visita
        List<TipoVisita> tipi = new ArrayList<>(config.getSnapshot().getTipiVisita());
        if (tipi.isEmpty()) {
            System.out.println("Nessun tipo di visita presente. Creane uno prima di procedere.");
            return;
        }

        System.out.println("\nTipi di visita disponibili:");
        for (int i = 0; i < tipi.size(); i++) {
            TipoVisita t = tipi.get(i);
            String luogoNome = Optional.ofNullable(config.getSnapshot().getLuogo(t.getLuogoId()))
                                   .map(Luogo::getNome)
                                   .orElse("luogo sconosciuto");
            System.out.printf("%d) %s (%s) – Luogo: %s%n",
                    i + 1, t.getTitolo(), t.getDescrizione(), luogoNome);
        }

        System.out.print("Seleziona il numero del tipo di visita: ");
        int sceltaTipo;
        try {
            sceltaTipo = Integer.parseInt(in.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Scelta non valida.");
            return;
        }
        if (sceltaTipo < 0 || sceltaTipo >= tipi.size()) {
            System.out.println("Numero non valido.");
            return;
        }
        TipoVisita tipoScelto = tipi.get(sceltaTipo);

        // Mostra elenco dei volontari disponibili
        List<Volontario> volontari = new ArrayList<>(config.getSnapshot().getVolontari());
        if (volontari.isEmpty()) {
            System.out.println("Nessun volontario presente.");
            return;
        }

        System.out.println("\nVolontari disponibili:");
        for (int i = 0; i < volontari.size(); i++) {
            Volontario v = volontari.get(i);
            System.out.printf("%d) %s (%s)%n", i + 1, v.getNickname(), v.getNickname());
        }

        System.out.print("Seleziona il numero del volontario da associare: ");
        int sceltaVol;
        try {
            sceltaVol = Integer.parseInt(in.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Scelta non valida.");
            return;
        }
        if (sceltaVol < 0 || sceltaVol >= volontari.size()) {
            System.out.println("Numero non valido.");
            return;
        }
        Volontario volScelto = volontari.get(sceltaVol);

        // Esegui l’associazione vera e propria
        config.associaVolontarioATipoVisita(tipoScelto.getId(), volScelto.getNickname());
        System.out.printf("Volontario '%s' associato al tipo di visita '%s'%n",volScelto.getNickname(), tipoScelto.getTitolo());
    }

    //Logica interna per garantire il vincolo di almeno un volontario per un tipo di visita
    public void assocVolontarioATipo(String tipoVisitaId, String nickname) {
        TipoVisita t = config.getSnapshot().getTipoVisita(tipoVisitaId);
        if (t == null) throw new IllegalArgumentException("TipoVisita inesistente");
        if (config.getSnapshot().getVolontario(nickname) == null)
            throw new IllegalArgumentException("Volontario inesistente");
        t.addVolontario(nickname, config.getSnapshot());
        config.save();
    }


    
    // --- Metodi Helper --------------------------------------------------------
    /**
    * Interazione CLI per costruire un nuovo TipoVisita associato al Luogo l.
    * Restituisce un oggetto TipoVisita pronto per essere passato a ConfigService.
    */
    private TipoVisita creaTipoVisita(Luogo l) {
        System.out.println("== Creazione nuovo tipo di visita per il luogo: " + l.getNome() + " ==");

        System.out.print("Titolo: ");
        String titolo = in.nextLine().trim();
        System.out.print("Descrizione: ");
        String descrizione = in.nextLine().trim();

        TipoVisita tipo = new TipoVisita(l.getId(), titolo, descrizione);

        // NOTA: non assegniamo volontari qui: la CLI precedente nel progetto
        // gestisce l'associazione dei volontari dopo la creazione del tipo.
        return tipo;
    }

    /**
    * Associa almeno un volontario al tipo di visita appena creato.
    * Permette di selezionare volontari esistenti o crearne di nuovi.
    */
    private void associaVolontarioANuovoTipoVisita(TipoVisita tipo) {
        System.out.println("== Associazione volontario/i al tipo di visita: " + tipo.getTitolo() + " ==");

        // Recupera tutti i volontari esistenti dal DataStore
        List<Volontario> volontariEsistenti = new ArrayList<>(config.getSnapshot().getVolontari());

        if (volontariEsistenti.isEmpty()) {
            System.out.println("Nessun volontario esistente trovato. Creane uno nuovo.");
            System.out.print("Nickname volontario: ");
            String nick = in.nextLine().trim();
            Volontario nuovo = new Volontario(nick);
            config.aggiungiVolontario(nuovo);
            tipo.addVolontario(nuovo.getNickname(), config.getSnapshot());
            System.out.println("Volontario " + nuovo.getNickname() + " associato al tipo di visita.");
            return;
        }

        System.out.println("Volontari disponibili:");
        for (int i = 0; i < volontariEsistenti.size(); i++) {
            Volontario v = volontariEsistenti.get(i);
            System.out.printf("%d) %s%n", i + 1, v.getNickname());
        }

        do {
            System.out.print("Seleziona un volontario (numero) o premi invio per crearne uno nuovo: ");
            String scelta = in.nextLine().trim();
            if (scelta.isEmpty()) {
                 System.out.print("Nickname volontario: ");
                String nick = in.nextLine().trim();
                Volontario nuovo = new Volontario(nick);
                config.aggiungiVolontario(nuovo);
                tipo.addVolontario(nuovo.getNickname(), config.getSnapshot());
                System.out.println("Volontario " + nuovo.getNickname() + " associato al tipo di visita.");
            } else {
                try {
                    int idx = Integer.parseInt(scelta) - 1;
                    if (idx < 0 || idx >= volontariEsistenti.size()) {
                        System.out.println("Indice non valido.");
                        continue;
                    }
                    Volontario scelto = volontariEsistenti.get(idx);
                    tipo.addVolontario(scelto.getNickname(), config.getSnapshot());
                    System.out.println("Volontario " + scelto.getNickname() + " associato al tipo di visita.");
                } catch (NumberFormatException e) {
                    System.out.println("Inserisci un numero valido.");
                }
            }

            System.out.print("Vuoi aggiungere un altro volontario a questo tipo? (s/n): ");
        } while (in.nextLine().trim().equalsIgnoreCase("s"));
    }


}
