package dev.bodewig.autoserializable;

class AnonymousClass {
    static final AnonymousClass impl = new AnonymousClass() {
    };

    AnonymousClass() {
        this(1);
    }

    AnonymousClass(int a) {
    }

    static final AnonymousClass second = new AnonymousClass(10) {
    };
}
