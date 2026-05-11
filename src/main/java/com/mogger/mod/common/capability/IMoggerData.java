package com.mogger.mod.common.capability;

public interface IMoggerData {

    int getMogLevel();
    void setMogLevel(int level);

    long getMogXP();
    void setMogXP(long xp);

    boolean isInDuel();
    void setInDuel(boolean inDuel);

    int getDuelTimer();
    void setDuelTimer(int timer);

    // XP required to reach next level
    static long xpRequiredForLevel(int level) {
        return (long)(50 * Math.pow(level, 1.5));
    }

    default void addMogXP(long amount) {
        long newXP = getMogXP() + amount;
        setMogXP(newXP);
        checkLevelUp();
    }

    default void checkLevelUp() {
        while (getMogXP() >= xpRequiredForLevel(getMogLevel())) {
            setMogXP(getMogXP() - xpRequiredForLevel(getMogLevel()));
            setMogLevel(getMogLevel() + 1);
        }
    }

    default String getRankTitle() {
        int level = getMogLevel();
        if (level < 5)  return "§7Weak Aura";
        if (level < 15) return "§fNormal";
        if (level < 30) return "§aSigma";
        if (level < 50) return "§eAlpha";
        if (level < 75) return "§6Mogger";
        return "§cSupreme Mogger";
    }

    // Entity mog power based on max health
    static int calculateEntityMogPower(float maxHealth) {
        return Math.max(1, (int)(maxHealth * 0.25f));
    }

    // XP reward based on entity power
    static long calculateXPReward(int entityMogPower) {
        return Math.max(1, (long)(entityMogPower * 2.5));
    }
}
