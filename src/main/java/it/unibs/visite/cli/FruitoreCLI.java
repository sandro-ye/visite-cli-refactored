package it.unibs.visite.cli;

import it.unibs.visite.model.*;
import it.unibs.visite.service.FruitoreService;
import it.unibs.visite.service.ConfigService;

import java.util.*;

/**
 * CLI del fruitore.
 * [MODIFICA V4-CLI]
 * - Evita di chiedere l'id se non ci sono visite disponibili.
 * - Mostra elenco numerato; l'utente può scegliere per numero o id completo.
 * - Valida numero persone su range e capienza residua.
 * - Gestisce eccezioni senza far crashare l'app.
 */
public class FruitoreCLI {

    private final String username;
    private final ConfigService config;
    private final FruitoreService service;
    private final Scanner in = new Scanner(System.in);

    public FruitoreCLI(String username, ConfigService config) {
        this.username = username;
        this.config = config;
        // Recupera/crea il profilo Fruitore dal DataStore
        Fruitore f = config.getSnapshot().getFruitore(username);
        if (f == null) {
            f = new Fruitore(username);
            config.getSnapshot().addFruitore(f);
            config.save();
        }
        this.service = new FruitoreService(config, f);
    }

    public void run() {
        while (true) {
            System.out.println("\n=== MENU FRUITORE ===");
            System.out.println("1) Visualizza visite disponibili");
            System.out.println("2) Effettua iscrizione");
            System.out.println("3) Le mie iscrizioni");
            System.out.println("4) Disdici una iscrizione");
            System.out.println("0) Logout");
            System.out.print("> ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1" -> mostraDisponibili();
                case "2" -> iscriviti();
                case "3" -> mieIscrizioni();
                case "4" -> disdici();
                case "0" -> {
                    System.out.println("Logout. Arrivederci!");
                    return;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void mostraDisponibili() {
        List<Visita> proposte = service.visiteDisponibili();
        stampaElencoVisite("-- visite disponibili --", proposte);
    }

    private void stampaElencoVisite(String titolo, List<Visita> visite) {
        System.out.println(titolo);
        if(visite == null || visite.isEmpty()) {
            System.out.println("nessuna visita è disponibile");
            return;
        }
        int i = 1;
        for (Visita v : visite) {
            TipoVisita tv = config.getSnapshot().getTipoVisita(v.getTipoVisitaId());
            String biglietto = tv.getBigliettoRichiesto() ? " | biglietto richiesto" : "";
            System.out.printf("%d) id=%s | %s — %s %s | punto: %s | min %d, max %d%s%n",
                    i++,
                    v.getId(),
                    tv.getTitolo(),
                    v.getData(),
                    tv.getOraInizio(),
                    safe(tv.getPuntoIncontro()),
                    tv.getNumeroMinimoPartecipanti(),
                    tv.getNumeroMassimoPartecipanti(),
                    biglietto
            );
        }
    }

    private String safe(String s) { return (s == null ? "-" : s); }

    private void iscriviti() {
        List<Visita> proposte = service.visiteDisponibili();
        // Mostra elenco numerato per facilitare l'utente
        stampaElencoVisite("-- visite disponibili --", proposte);

        // Scelta id o indice
        System.out.print("\nInserisci **numero** della visita nell'elenco oppure incolla l'**id** completo: ");
        String input = in.nextLine().trim();

        Visita scelta = null;
        // Proviamo come numero
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= proposte.size()) {
                scelta = proposte.get(idx - 1);
            }
        } catch (NumberFormatException ignore) {
            // Non è un numero: trattiamo come id
        }
        if (scelta == null) {
            // trattiamo input come id "letterale"
            scelta = config.getSnapshot().getVisita(input);
        }

        if (scelta == null) {
            System.out.println("Visita non trovata. Operazione annullata.");
            return;
        }
        if (scelta.getStato() != StatoVisita.PROPOSTA && scelta.getStato() != StatoVisita.COMPLETA) {
            System.out.println("Iscrizione non consentita: la visita non è in stato PROPOSTA/COMPLETA.");
            return;
        }

        // Numero persone
        int maxSingola = config.getSnapshot().getParametri().getMaxPersonePerIscrizione();
        int capResidua = scelta.getNumeroMassimoPartecipanti() - scelta.getTotalePersone();
        if (capResidua <= 0) {
            System.out.println("La visita è al completo. Non è possibile iscriversi.");
            return;
        }

        int numeroIscritti = checkNumeroPartecipanti(maxSingola, capResidua);
        if (numeroIscritti == -1) {
            // errore di input già segnalato
            return;
        }

        try {
            Iscrizione is = service.iscriviFruitoreAllaVisita(scelta.getId(), numeroIscritti);
            System.out.println("Iscrizione effettuata con successo.");
            System.out.println("Codice prenotazione: " + is.getCodice());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // Messaggi di dominio (capienza, stato, ecc.)
            System.out.println("Iscrizione rifiutata: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore imprevisto durante l'iscrizione: " + ex.getMessage());
        }
    }

    private void mieIscrizioni() {
        List<Iscrizione> mie = service.mieIscrizioni();
        if (mie == null || mie.isEmpty()) {
            System.out.println("-- le mie iscrizioni --");
            System.out.println("(nessuna)");
            return;
        }
        System.out.println("-- le mie iscrizioni --");
        int i = 1;
        for (Iscrizione is : mie) {
            Visita v = config.getSnapshot().getVisita(is.getCodiceVisitaAssociato());
            if (v == null) continue; // in caso sia finita in archivio o rimossa
            TipoVisita tv = config.getSnapshot().getTipoVisita(v.getTipoVisitaId());
            System.out.printf("%d) codice=%s | %s — %s %s | persone=%d | stato=%s%n",
                    i++,
                    is.getCodice(),
                    tv.getTitolo(),
                    v.getData(),
                    tv.getOraInizio(),
                    is.getNumeroPersone(),
                    v.getStato()
            );
        }
    }

    private void disdici() {
        System.out.println("-- disdetta iscrizione --");
        System.out.print("Inserisci il codice prenotazione: ");
        String codice = in.nextLine().trim();
        try {
            service.disdiciIscrizione(codice);
            System.out.println("Disdetta effettuata.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Disdetta rifiutata: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore imprevisto durante la disdetta: " + ex.getMessage());
        }
    }

    private int checkNumeroPartecipanti(int maxPartecipanti, int capienzaResidua) {
        System.out.printf("Inserisci il numero di persone (1..%d, capienza residua: %d): ", Math.min(maxPartecipanti, capienzaResidua), capienzaResidua);
        String nstr = in.nextLine().trim();
        int n;
        try {
            n = Integer.parseInt(nstr);
        } catch (NumberFormatException e) {
            System.out.println("Valore non numerico. Operazione annullata.");
            return -1;
        }
        if (n < 1 || n > maxPartecipanti) {
            System.out.println("Numero persone fuori range per singola iscrizione. Operazione annullata.");
            return -1;
        }
        if (n > capienzaResidua) {
            System.out.println("Numero persone eccede la capienza residua. Operazione annullata.");
            return -1;
        }
        return n;
    }
}
