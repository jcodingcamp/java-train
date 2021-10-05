package com.uangel.training.test;

import org.junit.Test;

public class Deco {

    interface Coffee {
        public double Cost() ;
        public String Ingredients();
        public String CupSize();
        public String Description();

        default void print() {
            System.out.printf("Cost = %f , CupSize = %s , Ingredients = %s , Description = %s\n", this.Cost(), this.CupSize(), this.Ingredients(), this.Description());
        }
    }

    abstract class CoffeeAdaptor implements  Coffee {
        Coffee c;
        public CoffeeAdaptor(Coffee c) {
            this.c = c;
        }

        @Override
        public String CupSize() {
            return c.CupSize();
        }

        @Override
        public double Cost() {
            return c.Cost();
        }

        @Override
        public String Ingredients() {
            return c.Ingredients();
        }

        @Override
        public String Description() {
            return c.Description();
        }
    }

    class Espresso implements Coffee {
        public double Cost() {
            return 1500.0;
        }

        public String Ingredients() {
            return "Coffee";
        }

        public String CupSize() {
            return "venti";
        }

        public String Description() {
            return "Just Coffee";
        }
    }

    class PlusMilk extends CoffeeAdaptor {

        public PlusMilk( Coffee c) {
            super(c);
        }

         public double Cost() {
             return super.Cost() + 500;
         }

        public String Ingredients() {
            return super.Ingredients() + ", Milk";
        }

        public String Description() {
           return super.Description() + " with milk";
        }
    }

    class PlusSyrup extends CoffeeAdaptor {
        public PlusSyrup( Coffee c) {
            super(c);
        }

        public double Cost() {
            return super.Cost() + 300;
        }

        public String Ingredients() {
            return super.Ingredients() + ", Syrup";
        }


        public String Description() {
            return super.Description() + " with Syrup";
        }
    }

    @Test
    public void test() {
        Coffee latte = new PlusMilk(new Espresso());
        latte.print();

        Coffee vanylaLatte = new PlusSyrup(new PlusMilk(new Espresso()));
        vanylaLatte.print();

        Coffee superSweetLatte = new PlusSyrup(new PlusSyrup(new PlusMilk(new Espresso())));
        superSweetLatte.print();
    }
}
