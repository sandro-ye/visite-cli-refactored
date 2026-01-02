package it.unibs.visite.model;

import java.time.LocalDate;

public class DisponibilitaVolontario {
    private final LocalDate data;

    public DisponibilitaVolontario(LocalDate data) {
        this.data = data;
    }

    public LocalDate getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisponibilitaVolontario that = (DisponibilitaVolontario) o;
        return data.equals(that.data);
    }
}
