package dev.bodewig.autoserializable;

abstract class BeanWithNoArgsConstructor {

    static class Explicit {
        Explicit() {
        }
    }

    static class Implicit {
    }

    static class Inherited extends BeanWithNoArgsConstructor {
    }

    BeanWithNoArgsConstructor() {
    }
}
