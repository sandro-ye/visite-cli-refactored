package it.unibs.visite.cli;

import it.unibs.visite.service.ConfigService;
import it.unibs.visite.service.RegimeService;
import it.unibs.visite.model.AppPhase;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Visita;

import java.time.YearMonth;
import java.util.Scanner;
import java.util.List;

/**
 * !- da sistemare siccome nella classe vi è logica di business, mentre classe dovrebbe occuparsi solo di CLI
 */


/**
 * CLI per la gestione delle operazioni del giorno 16.
 * Implementa il flusso obbligatorio:
 *  1 Chiusura raccolta disponibilità + generazione piano visite
 *  2 Gestione aggiunte/rimozioni
 *  3 Riapertura raccolta disponibilità
 */
public class GiornoSediciCLI {

    private final ConfigService configService;
    private final Scanner scanner = new Scanner(System.in);

    public GiornoSediciCLI(ConfigService configService) {
        this.configService = configService;
    }

    public void start() {
        if (!configService.isGiornoSedici()) {
        throw new IllegalStateException("Le operazioni di gestione sono consentite solo il giorno 16 del mese.");
        }

        System.out.println("=====================================");
        System.out.println("    OPERAZIONI DEL GIORNO 16");
        System.out.println("=====================================\n");

        // Calcola mese corrente e mese successivo
        YearMonth meseCorrente = YearMonth.now();
        YearMonth meseProssimo = meseCorrente.plusMonths(1);
        YearMonth meseDopoProssimo = meseCorrente.plusMonths(2);

        // 1 CHIUSURA RACCOLTA DISPONIBILITÀ + GENERAZIONE PIANO
        System.out.println("[1] Chiusura raccolta disponibilità per " + meseProssimo + "...");
        try {
            
            if (configService.getSnapshot().getFaseCorrente() != AppPhase.RACCOLTA_DISPONIBILITA) {
                System.out.println("[WARN] Fase non coerente con la generazione del piano. Ripristino RACCOLTA_DISPONIBILITA...");
                configService.setPhase(AppPhase.RACCOLTA_DISPONIBILITA);
            }
            List<Visita> piano = configService.chiudiDisponibilitaEGeneraPiano(meseProssimo);
            System.out.println("Piano delle visite per " + meseProssimo + " generato con successo.\n");
            stampaPiano(piano, meseProssimo);
        } catch (Exception e) {
            System.out.println("Errore durante la generazione del piano: " + e.getMessage());
            return; // interrompe il flusso: senza piano non si prosegue
        }

        // 2 GESTIONE AGGIUNTE / RIMOZIONI
        System.out.println("[2] Ora puoi gestire aggiunte o rimozioni di luoghi, tipi di visita o volontari.");
        System.out.println("Premi INVIO per accedere al menu di gestione...");
        scanner.nextLine();
        new GestioneGiorno16CLI(scanner, new RegimeService(configService)).run();
        System.out.println("Gestione modifiche completata.\n");

        // 3 RIAPERTURA RACCOLTA DISPONIBILITÀ
        System.out.println("[3] Riapertura della raccolta delle disponibilità per " + meseDopoProssimo + "...");
        try {
            configService.riapriRaccoltaDisponibilita(meseDopoProssimo);
            System.out.println("Raccolta disponibilità per " + meseDopoProssimo + " aperta con successo.\n");
        } catch (Exception e) {
            System.out.println("Errore durante la riapertura: " + e.getMessage());
        }

        System.out.println("=========================================================");
        System.out.println(" Tutte le operazioni del giorno 16 sono state completate.");
        System.out.println("=========================================================");
    }

    private void stampaPiano(List<Visita> piano, YearMonth meseProssimo) {
        // Recupera e mostra il piano appena generato
        if (piano.isEmpty()) {
            System.out.println(" Nessuna visita generata per " + meseProssimo + ".");
        } else {
            System.out.println(" Piano visite per " + meseProssimo + ":");
            for (Visita v : piano) {
                TipoVisita tipo = configService.getSnapshot().getTipoVisita(v.getTipoVisitaId());
                Luogo luogo = configService.getSnapshot().getLuogo(tipo.getLuogoId());
                System.out.printf(
                    "• %s – %s (%s) con %s%n",
                    v.getData(),
                    tipo.getTitolo(),
                    luogo.getNome(),
                    v.getVolontarioNickname()
                );
            }
        }
        System.out.println();
    }

}
