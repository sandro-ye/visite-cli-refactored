package it.unibs.visite.service;

import it.unibs.visite.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

/**
* Genera il piano mensile di visite proponibili in base alle disponibilità.
* Include vincoli aggiuntivi sui periodi programmabili e sull'assenza di
* sovrapposizioni temporali tra visite dello stesso luogo
*/
public class PlannerService {

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
}
