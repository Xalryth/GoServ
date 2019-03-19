package Helpers;

public enum StatType {
    Strength(0),
    Intelligence(1),
    Endurance(2);

    private final int value;
    private StatType(int val){
        this.value = val;
    }

    int getValue(){
        return value;
    }
}
