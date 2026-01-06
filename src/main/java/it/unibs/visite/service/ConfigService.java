package it.unibs.visite.service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Paths;

import it.unibs.visite.model.DataStore;
import it.unibs.visite.model.Luogo;
import it.unibs.visite.model.TipoVisita;
import it.unibs.visite.model.Volontario;
import it.unibs.visite.persistence.AvailabilityRepository;
import it.unibs.visite.persistence.FileAvailabilityRepository;
import it.unibs.visite.persistence.FilePersistence;
import it.unibs.visite.model.Visita;
import it.unibs.visite.model.AppPhase;
import it.unibs.visite.security.AuthService;

/**
 * Servizio per la configurazione del sistema.
 * - gestione luoghi (aggiunte/rimozione)
 * - gestione tipi di visita (aggiunte/rimozione/associazione volontari)
 * - gestione volontari (aggiunte/rimozione)
 * - controlla fase app
 * - gestione preclusioni (controlla in che fase si è e raccoglie le preclusioni da file temporaneo FileAvailabilityRepository)
 * e generazione del piano visite
 * - gestione parametri di sistema (ambito territoriale, max persone per iscrizione)
 * - setting della fase applicativa
 * - fa da tramite tra CLI e DataStore
 * 
 * 
 * classe viola interface segregation principle (da controllare)
 */


public class ConfigService {
    private final FilePersistence fp;
    private DataStore ds;
    private AuthService authService;

    public ConfigService(FilePersistence fp, AuthService authService) {
        this.fp = fp;
        fp.ensureDirs();
        this.ds = fp.loadDataOrNew();
        this.authService = authService;
    }

    public AuthService getAuthService() { return authService; }

    /*
     * Solo per test: non devo caricare il ds vero
     */
    public ConfigService(DataStore ds, FilePersistence fp, AuthService authService) {
        this.fp = fp;
        this.ds = ds;
        this.authService = authService;
    }

    //--------------------------------------------------------------------------------------------------------
    //Nuovi metodi

    //===========================================
    //GESTIONE LUOGHI
    //===========================================
    public void aggiungiLuogo(Luogo l, List<TipoVisita> tipiAssociati) {
        checkFase(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);

        //Aggiunge il luogo al datastore
        ds.addLuogo(l);

        //Verifica che ogni tipo visita sia coerente con il luogo
        for (TipoVisita tipo : tipiAssociati) {
            if (!tipo.getLuogoId().equals(l.getId())) {
                throw new IllegalArgumentException("Il tipo di visita '"+tipo.getTitolo()+"' non appartiene al luogo "+l.getId());
            }
            ds.addTipoVisita(tipo);
            l.addTipoVisita(tipo);
        }
        
        fp.saveData(ds);
    }

    public void rimuoviLuogo(String id) { 
        checkFase(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);

        ds.rimuoviLuogo(id); 
        fp.saveData(ds); 
    }

    //===========================================
    //GESTIONE TIPI DI VISITA
    //===========================================
    public void aggiungiTipoVisita(TipoVisita t) {
        checkFase(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);

        ds.addTipoVisita(t);

        Luogo l = ds.getLuogo(t.getLuogoId());
        if (l != null) {
            l.getTipiVisitaIds().add(t.getId());
        }
        fp.saveData(ds);
    }

    public void rimuoviTipo(String id) { 
        checkFase(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);
        
        ds.rimuoviTipoVisita(id); 
        fp.saveData(ds); 
    }

    public void associaVolontarioATipoVisita(String tipoVisitaId, String nickname) {
        TipoVisita tipo = ds.getTipoVisita(tipoVisitaId);
        Volontario volontario = ds.getVolontario(nickname);
        if (tipo == null || volontario == null) return;
        tipo.addVolontario(nickname, ds);
        fp.saveData(ds);
    }

    //===========================================
    //GESTIONE VOLONTARI
    //===========================================
    public void aggiungiVolontario(Volontario v) {
        checkFase(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);

        ds.addVolontario(v);
        authService.createVolunteer(v.getNickname(), authService.getPswrdDefaultVolontario().toCharArray());
        fp.saveData(ds);
    }

    public void rimuoviVolontario(String nick) {
        checkFase(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);

        ds.rimuoviVolontario(nick);
        //Per prevenire future autenticazioni dopo la rimozione
        authService.getCredentialsStore().rimuoviCredenziali(nick);
        fp.saveData(ds); 
    }

    //

    private void checkFase(AppPhase richiesta) {
        if (ds.getFaseCorrente() != richiesta)
            throw new IllegalStateException("Operazione non consentita nella fase corrente: " + ds.getFaseCorrente());
    }

    public List<Visita> chiudiDisponibilitaEGeneraPiano(YearMonth mese) {
        checkFase(AppPhase.RACCOLTA_DISPONIBILITA);
        
        // Legge disponibilità da file temporaneo e la salva nel data store
        for (Volontario v : ds.getVolontari()) {
            Path file = Paths.get("data", v.getNickname() + "_availabilities.ser");
            AvailabilityRepository repo = new FileAvailabilityRepository(file);
            try {
                Set<LocalDate> dates = repo.getDates(v.getNickname(), mese);
                if (!dates.isEmpty()) {
                    ds.setDisponibilita(mese, v.getNickname(), dates);
                }
            } catch (IOException e) {
                System.err.println("Errore nel caricamento disponibilità per " + v.getNickname());
            }
        }
        
        fp.saveData(ds);

        PlannerService planner = new PlannerService(ds);
        List<Visita> result = planner.generaPiano(mese);
        
        ds.setFaseCorrente(AppPhase.GESTIONE_AGGIUNTE_RIMOZIONI);
        
        fp.saveData(ds);

        // Elimina le disponibilità dei volontari
        FileAvailabilityRepository.clearAll(Paths.get("data"));

        return result;
    }

    public void impostaPreclusioni(YearMonth mese, Set<LocalDate> precluse) {
        ds.setPreclusioni(mese, precluse);
        fp.saveData(ds);
    }

    //Prepara la nuova struttura dati per la raccolta delle disponibilità
    public void riapriRaccoltaDisponibilita(YearMonth mese) {
        ds.getDisponibilitaPerMese().putIfAbsent(mese, new HashMap<>());
        ds.setFaseCorrente(AppPhase.RACCOLTA_DISPONIBILITA);
        fp.saveData(ds);
    }

    public DataStore getStore() { return ds; }

    /**
    * Restituisce true se oggi è il giorno 16 del mese.
    * Può essere usato per abilitare le operazioni speciali di gestione.
    */
    public boolean isGiornoSedici() {
        return java.time.LocalDate.now().getDayOfMonth() == 16;
    }

    // restituisce tutti i tipi di visita attualmente memorizzati (collezione copiata)
    public List<TipoVisita> getAllTipiVisita() {
        // DataStore espone getTipiVisita() che ritorna Collection<TipoVisita>
        return new ArrayList<>(ds.getTipiVisita());
    }

    public void setPhase(AppPhase nuovaFase) {
        ds.setFaseCorrente(nuovaFase);

        // Salva lo stato aggiornato nel DataStore
        try {
            fp.saveData(ds);
            System.out.println("[INFO] Fase applicativa impostata a: " + nuovaFase);
        } catch (Exception e) {
            System.err.println("[ERRORE] Impossibile aggiornare la fase applicativa: " + e.getMessage());
        }
    }

    //-----------------------------------------------------------------------------------------------------------

    public DataStore getSnapshot() {
        return ds;
    }

    public void save() {
        fp.saveData(ds);
    }

    // Restituisce le date precluse per un certo mese
    public Set<LocalDate> getPrecludedDates(YearMonth ym) {
        return ds.getPreclusioniFor(ym);
    }

    // Programmabilità (semplificata): giorno settimana + periodo
    public boolean existsVisitTypeProgrammableOn(String nickname, LocalDate date){
        for(TipoVisita tv : ds.getTipiVisita()){
            if(tv.getVolontariNicknames().contains(nickname) &&
               tv.getGiorniSettimana().contains(date.getDayOfWeek()) &&
               !date.isBefore(tv.getDataInizioProgrammazione()) &&
               !date.isAfter(tv.getDataFineProgrammazione())) return true;
        }
        return false;
    }


    //  pubblica piano nel DataStore
    public void pubblicaPiano(List<Visita> piano){
        for(Visita v: piano) ds.addVisita(v);
        save();
    }

    public Set<String> visitTypeDescriptionsForVolunteer(String nickname) {
        Volontario vol = ds.getVolontario(nickname);
        if (vol == null) return Set.of();

        Set<String> result = new LinkedHashSet<>();

        for (TipoVisita tv : ds.getTipiVisita()) {
            if (tv.getVolontariNicknames().contains(nickname)) {
                Luogo luogo = ds.getLuogo(tv.getLuogoId());
                String luogoNome = (luogo != null ? luogo.getNome() : "(luogo sconosciuto)");
                result.add(tv.getTitolo() + " @ " + luogoNome);
            }
        }
        return result;
    }


    public Set<DayOfWeek> getProgrammableDaysForVolunteer(String nickname, YearMonth ym) {
        Volontario vol = ds.getVolontario(nickname);
        if (vol == null) return Set.of();

    // Al momento il dominio NON modella:
    // - giorni settimanali ammessi per ciascun tipo di visita
    // - validità nel mese
    // quindi non possiamo calcolare delle informazioni reali qui.

        return Set.of();
    }


}
