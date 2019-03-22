package Helpers;

import java.util.ArrayList;

public class Enjoyer {
    long id;
    String email, passwordHash;
    byte[] salt;
    float[] location;

    public Enjoyer(long id, String email, String passwordHash, byte[] salt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public Enjoyer(String email, String passwordHash, byte[] salt, float[] location) {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public float[] getLocation() {
        return location;
    }

    public void setLocation(float[] location) {
        this.location = location;
    }
}
