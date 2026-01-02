package it.unibs.visite.repository;

import java.util.Collection;
import java.util.Optional;
import it.unibs.visite.model.Volontario;

public interface VolontarioRepository {
    void save(Volontario volontario);
    Optional<Volontario> findByNickname(String nickname);
    Collection<Volontario> findAll();
    void delete(String nickname);
}
