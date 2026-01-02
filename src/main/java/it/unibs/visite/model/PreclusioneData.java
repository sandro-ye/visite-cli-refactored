package it.unibs.visite.model;

import java.time.LocalDate;
import java.util.Objects;

public class PreclusioneData {
    private final LocalDate data;

    public PreclusioneData(LocalDate data) {
        this.data = Objects.requireNonNull(data);
    }

    public LocalDate getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreclusioneData that = (PreclusioneData) o;
        return data.equals(that.data);
    }
}
