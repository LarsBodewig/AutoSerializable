package dev.bodewig.autoserializable.test;

import java.io.PrintStream;
import java.util.Objects;

public class FieldBuilderBean {
    int a = 3;
    Integer b = 5;
    Object c = true;
    Object d = "new";
    short e = 5;
    Double f = 0.0;
    String g = "hi";
    Void h;
    PrintStream io = System.out;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FieldBuilderBean that))
            return false;
        //noinspection ConstantValue
        return a == that.a && e == that.e && Objects.equals(b, that.b) && Objects.equals(c, that.c) &&
                Objects.equals(d, that.d) && Objects.equals(f, that.f) && Objects.equals(g, that.g) &&
                Objects.equals(h, that.h) && Objects.equals(io, that.io);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f, g, h, io);
    }

    @Override
    public String toString() {
        return "FieldBuilderBean{" + "a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", e=" + e + ", f=" + f +
                ", g='" + g + '\'' + ", h=" + h + ", io=" + io + '}';
    }
}
