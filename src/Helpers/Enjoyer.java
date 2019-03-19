package Helpers;

import java.util.ArrayList;

public class Enjoyer {
    long id;
    String email, passwordHash;
    byte[] salt;
    float[] location;

    public Enjoyer(String email, String passwordHash, byte[] salt, float[] location) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.location = location;
    }

    public Enjoyer(long id, String email, String passwordHash, byte[] salt, float[] location) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.location = location;
    }
}
