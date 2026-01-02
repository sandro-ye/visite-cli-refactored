package it.unibs.visite.service;

import it.unibs.visite.core.DomainException;
import it.unibs.visite.repository.LuogoRepository;
import it.unibs.visite.repository.TipoVisitaRepository;

public class TipoVisitaService {
    private final TipoVisitaRepository tipoVisitaRepository;
    private final LuogoRepository luogoRepository;

    public TipoVisitaService(TipoVisitaRepository tipoVisitaRepository, LuogoRepository luogoRepository) {
        this.tipoVisitaRepository = tipoVisitaRepository;
        this.luogoRepository = luogoRepository;
    }

    public void deleteTipoVisita(String tipoVisitaId) {
        if(tipoVisitaRepository.findById(tipoVisitaId).isEmpty()) {
            throw new DomainException("tipo visita inesistente");
        }

        luogoRepository.findAllLuoghi()
                .forEach(luogo -> luogo.removeTipoVisita(tipoVisitaId));

        tipoVisitaRepository.delete(tipoVisitaId);
    }
}
