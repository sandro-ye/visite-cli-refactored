package it.unibs.visite.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class DisponibilitaVolontario implements Serializable{
    private final LocalDate data;

    public DisponibilitaVolontario(LocalDate data) {
        this.data = Objects.requireNonNull(data);
    }

    public LocalDate getData() {
        return data;
    }
}
