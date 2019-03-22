package Helpers;

public class Character {
    long id, currentExp, enjoyerId;
    String name;
    short level;
    CharacterClass characterClass;

    public Character(long currentExp, String name, short level, CharacterClass characterClass) {
        this.currentExp = currentExp;
        this.name = name;
        this.level = level;
        this.characterClass = characterClass;
    }

    public Character(long currentExp, String name, short level, CharacterClass characterClass, long enjoyerId) {
        this.currentExp = currentExp;
        this.name = name;
        this.level = level;
        this.characterClass = characterClass;
        this.enjoyerId = enjoyerId;
    }

    public Character(long id, long currentExp, String name, short level, CharacterClass characterClass) {
        this.id = id;
        this.currentExp = currentExp;
        this.name = name;
        this.level = level;
        this.characterClass = characterClass;
    }

    public Character(long id, long currentExp, String name, short level, CharacterClass characterClass, long enjoyerId) {
        this.id = id;
        this.currentExp = currentExp;
        this.name = name;
        this.level = level;
        this.characterClass = characterClass;
        this.enjoyerId = enjoyerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(long currentExp) {
        this.currentExp = currentExp;
    }

    public long getEnjoyerId() {
        return enjoyerId;
    }

    public void setEnjoyerId(long enjoyerId) {
        this.enjoyerId = enjoyerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public void setCharacterClass(CharacterClass characterClass) {
        this.characterClass = characterClass;
    }
}

