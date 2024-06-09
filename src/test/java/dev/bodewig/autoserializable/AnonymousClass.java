package dev.bodewig.autoserializable;

public class AnonymousClass {
    public static final AnonymousClass impl = new AnonymousClass() {
    };

    public AnonymousClass() {
        this(1);
    }

    public AnonymousClass(int a) {
    }

    public static final AnonymousClass second = new AnonymousClass(10) {
    };
}
