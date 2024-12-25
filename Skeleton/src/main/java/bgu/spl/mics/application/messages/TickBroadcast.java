package bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private final int currentTick;

    public TickBroadcast(int currentTick) {
        this.currentTick = currentTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }
}
