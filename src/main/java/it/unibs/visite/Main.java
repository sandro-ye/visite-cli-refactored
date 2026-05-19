package it.unibs.visite;

import java.nio.file.*;
import java.util.Scanner;

import it.unibs.visite.bootstrap.AuthBootstrap;
import it.unibs.visite.cli.*;
import it.unibs.visite.controller.*;
import it.unibs.visite.service.*;
import it.unibs.visite.repository.*;
import it.unibs.visite.persistence.PersistenceManager;
import it.unibs.visite.security.*;
import it.unibs.visite.repository.CredentialsRepository;
import it.unibs.visite.repository.file.FileCredentialsRepository;


public class Main {
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        //persistencemanager
        PersistenceManager persistenceManager = new PersistenceManager();

        //repositories
        VisitaRepository visitaRepository = persistenceManager.getVisitaRepository();
        VisitaRepository archivioVisiteRepository = persistenceManager.getArchivioVisiteRepository();
        TipoVisitaRepository tipoVisitaRepository = persistenceManager.getTipoVisitaRepository();
        LuogoRepository luogoRepository = persistenceManager.getLuogoRepository();
        VolontarioRepository volontarioRepository = persistenceManager.getVolontarioRepository();
        ParametriSistemaRepository parametriSistemaRepository = persistenceManager.getParametriSistemaRepository();
        PreclusioneRepository preclusioneRepository = persistenceManager.getPreclusioneRepository();
        FruitoreRepository fruitoreRepository = persistenceManager.getFruitoreRepository();

        //credentials
        Path credsPath = Paths.get("data", "credentials.ser");
        CredentialsRepository credentialsRepository = new FileCredentialsRepository(credsPath);
        CredentialsStore credentialsStore = AuthBootstrap.initialize(credentialsRepository);
        AuthService authService = new AuthService(credentialsRepository, credentialsStore);

        //services
        DisponibilitaService disponibilitaService = new DisponibilitaService(preclusioneRepository, volontarioRepository);
        FruitoreService fruitoreService = new FruitoreService(parametriSistemaRepository, visitaRepository, tipoVisitaRepository, fruitoreRepository);
        InitWizardService initWizardService = new InitWizardService(parametriSistemaRepository, luogoRepository, volontarioRepository, 
                    tipoVisitaRepository, authService);
        PlannerService plannerService = new PlannerService(parametriSistemaRepository, preclusioneRepository, volontarioRepository, 
                    visitaRepository, tipoVisitaRepository, luogoRepository, disponibilitaService, authService);
        RegimeService regimeService = new RegimeService(preclusioneRepository, visitaRepository, archivioVisiteRepository, parametriSistemaRepository, 
                    volontarioRepository, luogoRepository, tipoVisitaRepository);
        TipoVisitaService tipoVisitaService = new TipoVisitaService(tipoVisitaRepository);
        VisitBatchService visitBatchService = new VisitBatchService(archivioVisiteRepository, visitaRepository);

        //controllers
        FruitoreController fruitoreController = new FruitoreController(fruitoreService);
        GiornoSediciController giornoSediciController = new GiornoSediciController(plannerService);
        InitWizardController initWizardController = new InitWizardController(initWizardService);
        LoginController loginController = new LoginController(authService);
        RegimeController regimeController = new RegimeController(regimeService);
        TipoVisitaController tipoVisitaController = new TipoVisitaController(tipoVisitaService);
        VisitBatchController visitBatchController = new VisitBatchController(visitBatchService);
        VolunteerController volunteerController = new VolunteerController(disponibilitaService);

        //cli
        FruitoreCLI fruitoreCLI = new FruitoreCLI(in, fruitoreController);
        TipoVisitaCLI tipoVisitaCLI = new TipoVisitaCLI(in, tipoVisitaController);
        GiornoSediciCLI giornoSediciCLI = new GiornoSediciCLI(giornoSediciController, tipoVisitaCLI, in);
        InitWizardCLI initWizardCLI = new InitWizardCLI(in, initWizardController, tipoVisitaCLI);
        LoginCLI loginCLI = new LoginCLI(loginController, in);
        RegimeCLI regimeCLI = new RegimeCLI(in, regimeController, giornoSediciCLI);
        VolunteerCLI volunteerCLI = new VolunteerCLI(in, volunteerController);

        //mainCLI
        MainCLI mainCLI = new MainCLI(authService, visitBatchController, loginCLI, fruitoreCLI, volunteerCLI, regimeCLI, initWizardCLI);

        //avvio applicazione 
        mainCLI.run();
    }
}
