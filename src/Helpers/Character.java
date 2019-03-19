package Helpers;

public class Character {
    long id, currentExp;
    String name;
    short level;
    CharacterClass characterClass;

    public Character(long currentExp, String name, short level, CharacterClass characterClass) {
        this.currentExp = currentExp;
        this.name = name;
        this.level = level;
        this.characterClass = characterClass;
    }

    public Character(long id, long currentExp, String name, short level, CharacterClass characterClass) {
        this.id = id;
        this.currentExp = currentExp;
        this.name = name;
        this.level = level;
        this.characterClass = characterClass;
    }
}

