package dev.bodewig.autoserializable;

public final class OneWayToggle {
    private final Object lock = new Object();
    private boolean value;
    private boolean finalized;

    public OneWayToggle(boolean initial) {
        value = initial;
        finalized = false;
    }

    public OneWayToggle() {
        this(false);
    }

    public boolean get() {
        synchronized (lock) {
            return value;
        }
    }

    public void toggle() {
        synchronized (lock) {
            if (finalized) {
                throw new IllegalStateException("Value was already toggled once");
            }
            finalized = true;
            value = !value;
        }
    }
}
