package com.uangel.training.test;

import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;

public class Lambda {

    class MyRun implements Runnable {
        @Override
        public void run() {
            System.out.println("hello");
        }
    }

    interface DoFunction {
        String apply( int arg );
    }

    void Map(  Function<Integer, String> f ) {
        String str =  f.apply( 10);
        System.out.println(str);
    }

    static String itoa( int a ) {
        return String.format("%d", a);
        //return Integer.valueOf(a).toString();
    }

    static class B {

        public C toC() {
            return new C(this);
        }
    }

    static class C {
        public C(B b) {

        }
    }

    C Apply( B b , Function<B, C> f) {
        return f.apply(b);
    }

    @Test
    public void test3() {
        var stackb = new B();

        Apply(stackb, (b) -> {
            return b.toC();
        });

        Apply(stackb, B::toC);
        Apply(stackb, C::new);

    }

    @Test
    public void test2()  {

//        Map( func( a int ) string {
//            return "hello " + a
//        })


        Optional<String> o = Optional.of("string");

        DoFunction f  = (a) -> "hello " + a;

        // =>
        Map(a -> "hello " + a);
        var b = new B();
        Map(Lambda::itoa);
    }

    @Test
    public void test() throws InterruptedException {

        final String world = "world";
        Thread t = new Thread(() -> System.out.println("hello " + world));
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });


        t.run();

        Thread.sleep(1000);
    }
}
