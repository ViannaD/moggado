package com.mogger.mod.common.capability;

import net.minecraft.nbt.CompoundTag;

public class MoggerDataImpl implements IMoggerData {

    private int mogLevel = 1;
    private long mogXP = 0;
    private boolean inDuel = false;
    private int duelTimer = 0;

    @Override public int getMogLevel() { return mogLevel; }
    @Override public void setMogLevel(int level) { this.mogLevel = Math.max(1, level); }

    @Override public long getMogXP() { return mogXP; }
    @Override public void setMogXP(long xp) { this.mogXP = Math.max(0, xp); }

    @Override public boolean isInDuel() { return inDuel; }
    @Override public void setInDuel(boolean inDuel) { this.inDuel = inDuel; }

    @Override public int getDuelTimer() { return duelTimer; }
    @Override public void setDuelTimer(int timer) { this.duelTimer = timer; }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("MogLevel", mogLevel);
        tag.putLong("MogXP", mogXP);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        mogLevel = tag.getInt("MogLevel");
        if (mogLevel < 1) mogLevel = 1;
        mogXP = tag.getLong("MogXP");
    }
}
