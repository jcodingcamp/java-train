package com.uangel.training.test;

import org.junit.Test;

import java.util.function.Function;

public class Covar {

    int a = 0;

    class ASuper {}


    class A extends ASuper {}

    class AImpl extends A {}


    class BSuper {}
    class B extends BSuper {}

    class BImpl extends B {}

    B Map(A a , Function<? super A,? extends B> f) {
        return f.apply(a);
    }

    BSuper AtoBSuper(A a) {
        return new BSuper();
    }

    BImpl AtoBImpl(A a) {
        return new BImpl();
    }

    @Test
    public void test() {
        //Map(new A(), this::AtoBSuper);
        Function<A,BImpl> f = this::AtoBImpl;

        Map(new A(), f);

        Map(new A(), this::AtoBImpl);

        Map(new A(), new Function<A, B>() {
            @Override
            public B apply(A a) {
                return AtoBImpl(a);
            }
        });
    }
}
