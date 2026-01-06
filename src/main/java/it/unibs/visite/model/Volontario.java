package it.unibs.visite.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class Volontario implements Serializable {
    private final String nickname; // univoco
    private final Set<DisponibilitaVolontario> disponibilita = new HashSet<>();

    public Volontario(String nickname) { this.nickname = Objects.requireNonNull(nickname); }
    public String getNickname() { return nickname; }

    public Set<DisponibilitaVolontario> getDisponibilita() { return disponibilita; }

    public void addDisponibilita(LocalDate data) {
        disponibilita.add(new DisponibilitaVolontario(data));
    }

    public void removeDisponibilita(LocalDate data) {
        disponibilita.removeIf(d -> d.getData().equals(data));
    }

    public boolean isDisponibile(LocalDate data) {
        return disponibilita.stream().anyMatch(d -> d.getData().equals(data));
    }
}
