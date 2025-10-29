package it.unibs.visite.model;
import java.io.Serializable;
import java.util.Objects;

public class Volontario implements Serializable {
    private final String nickname; // univoco

    public Volontario(String nickname) { this.nickname = Objects.requireNonNull(nickname); }
    public String getNickname() { return nickname; }
}
