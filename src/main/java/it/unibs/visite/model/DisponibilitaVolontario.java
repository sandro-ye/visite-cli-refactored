package it.unibs.visite.model;

import java.time.LocalDate;
import java.util.Objects;

public class DisponibilitaVolontario {
    private final LocalDate data;

    public DisponibilitaVolontario(LocalDate data) {
        this.data = Objects.requireNonNull(data);
    }

    public LocalDate getData() {
        return data;
    }
}
