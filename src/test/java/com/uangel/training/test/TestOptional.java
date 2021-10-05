package com.uangel.training.test;

import org.junit.Test;

import java.util.Optional;

public class TestOptional {

    class ASuper{}
    class A extends ASuper{}

    class AImpl extends A{}



    @Test
    public void test() {
        // 비어 있는 container
        var empty = Optional.<String>empty();

        // string 이 들어 있는 container
        var box = Optional.of("10");


        // container 에   string -> integer 로 변환하는 함수를 넣어주면
        // container 의 타입이  integer 로 바뀜
        // box는 string 이 들어 있던 container 이기 때문에  ,
        // box2에는 함수가 실행되어  10 이라는 int 값이 들어 있음.
        // Function 타입의 lambda 시험 example
        var box2 = box.map(a -> {
            System.out.println("hello "+ a);
            return Integer.parseInt(a);
        });


        // empty 는 비어 있던 container 이기 때문에 , map 안의 함수가 실행되지 않음
        // box3 는 빈 box 가 됨
        var box3 = empty.map( a -> {
            System.out.println("empty "+ a);
            return Integer.parseInt(a);
        });

        System.out.println("box2 isEmpty = " + box2.isEmpty());
        System.out.println("box3 isEmpty = " + box3.isEmpty());


        // filter 는 조건을 만족하지 못할 경우  비어있는 container 를 리턴함
        // box4 는 빈 박스가 됨.
        // Predicate 타입의 lambda 시험 example
        var box4 = box2.filter(integer -> integer > 10);
        System.out.println("box4 isEmpty = " + box4.isEmpty());

        // or 는 ,  container 가 비어 있는 경우에 실행됨
        // box2 는 10 이라는 값이 들어 있는 container 이기 때문에 or가 실행되지 않음
        // Supplier 타입의 lambda 시험 example
        box2 = box2.or(() -> {
            System.out.println("box2 or");
            return Optional.of(11);
        });

        // box4 는 비어 있었기 때문에  or 가 실행됨
        // box5 는 11이 들어 있는 box가 됨
        var box5 = box4.or(() -> {
            System.out.println("box4 or");
            return Optional.of(11);
        });
        System.out.println("box5 isEmpty = " + box5.isEmpty());

        // ifPresent 는  container 안에 내용이 있을 때에만 실행됨
        // Consumer 타입 lambda 시험 example
        box3.ifPresent(integer -> {
            System.out.println("box3 value = " + integer);
        });

        box5.ifPresent(integer -> {
            System.out.println("box5 value = " + integer);
        });


        var abox = Optional.of(new A());

        // 공변성 시험
        var abox2 = abox.or(() ->
            {
               return Optional.of(new AImpl());
            }
        );

//        var abox3 = abox.or(() -> {
//                return Optional.of(new ASuper());
//            }
//        );

    }

}
