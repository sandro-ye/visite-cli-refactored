package it.unibs.visite.controller;

import java.time.LocalDate;
import it.unibs.visite.service.VisitBatchService;

public class VisitBatchController {
    private final VisitBatchService visitBatchService;

    public VisitBatchController(VisitBatchService visitBatchService) {
        this.visitBatchService = visitBatchService;
    }

    public void eseguiBatch(LocalDate today) {
        visitBatchService.run(today);
    }
}
