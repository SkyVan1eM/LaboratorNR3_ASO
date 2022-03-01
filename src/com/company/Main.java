package com.company;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static Dep depozit = new Dep();
    static Producer producer = new Producer(depozit);
    static Consumer consumer = new Consumer(depozit);
    static Thread producator1 = new Thread(producer, "Producator - 1");
    static Thread producator2 = new Thread(producer, "Producator - 2");
    static Thread producator3 = new Thread(producer, "Producator - 3");
    static Thread consumator1 = new Thread(consumer, "Consumator - 1 ");
    static Thread consumator2 = new Thread(consumer, "Consumator - 2 ");
    static Thread consumator3 = new Thread(consumer, "Consumator - 3 ");

    public static void main(String[] args) {

        producator1.start();
        producator2.start();
        producator3.start();
        consumator1.start();
        consumator2.start();
        consumator3.start();

    }


    static class CountProd {
        static int count = 0;

        public static void count() {
            count++;
        }

        public static int getCount() {
            return count;
        }
    }

    static class Producer extends Thread {

        Dep dep;

        Producer(Dep dep) {
            this.dep = dep;
        }

        public void run() {
            int i = 0;
            while (CountProd.getCount() < 56) {

                if (CountProd.getCount() == 55) {
                    break;
                }
                if(!(i% 2 == 0)) {
                    dep.put(i);
                    System.out.println("Sau produs " + CountProd.getCount() + " elemente");
                }
                i++;

            }
        }
    }

    static class Consumer extends Thread {

        Dep dep;

        Consumer(Dep dep) {
            this.dep = dep;
        }

        public void run() {
            while (!(producator1.getState() == State.TERMINATED) && !(producator2.getState() == State.TERMINATED) && !(producator3.getState() == State.TERMINATED)) {
                dep.get();
                System.out.println("Sau consumat " + CountProd.getCount() + " elemente");

            }
        }
    }

    static class Dep {
        static ArrayList<Integer> dep = new ArrayList<>();
        ReentrantLock locker;
        Condition condition;

        Dep() {
            locker = new ReentrantLock();
            condition = locker.newCondition();
        }

        public void get() {

            locker.lock();
            try {

                if (dep.size() == 0)
                    System.out.println("Depozitul este gol" + dep);
                while (dep.size() < 1)
                    condition.await();

                System.out.println(Thread.currentThread().getName() + " a luat un produsul " + dep.get(0));
                dep.remove(0);
                System.out.println("Produce in stock: " + dep);


                condition.signalAll();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            } finally {

                locker.unlock();
            }
        }

        public void put(int i) {

            locker.lock();
            try {

                while (dep.size() >= 2)
                    condition.await();

                dep.add(i);
                CountProd.count();
                System.out.println(Thread.currentThread().getName() + " a produs produsul: " + i);
                System.out.println("Produsele in depozit: " + dep);


                condition.signalAll();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            } finally {
                locker.unlock();

            }
        }
    }
}