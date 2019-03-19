package Helpers;

public enum CharacterClass {
    WIZARD(0),
    RANGER(1),
    WARRIOR(50),
    ROGUE(51),
    BARD(100),
    PRIEST(101);

    private final int value;
    private CharacterClass(int val){
        this.value = val;
    }

    int getValue(){
        return value;
    }
}

