package test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public class BeanWithNullAnnotation {

    @Documented
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
    public @interface Null {
    }

    public @Null String myMethod() {
        return null;
    }

    public @interface TypeAnnot {
    }

    @TypeAnnot
    public static class AnnotedType {
    }
}
