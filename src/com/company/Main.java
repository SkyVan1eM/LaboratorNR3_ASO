package com.company;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static Dep depozit = new Dep();
    static CyclicBarrier barrier = new CyclicBarrier(6);
    static ArrayList<Integer> numImpare = new ArrayList<>();
    static Producer producer = new Producer(depozit, barrier);
    static Consumer consumer = new Consumer(depozit, barrier);
    static Thread producator1 = new Thread(producer, "Producator - 1");
    static Thread producator2 = new Thread(producer, "Producator - 2");
    static Thread producator3 = new Thread(producer, "Producator - 3");
    static Thread consumator1 = new Thread(consumer, "Consumator - 1 ");
    static Thread consumator2 = new Thread(consumer, "Consumator - 2 ");
    static Thread consumator3 = new Thread(consumer, "Consumator - 3 ");

    static boolean depStatus = false;

    static public void addArrayOFnumber() {
        for (int i = 1; i < 81; i++) {
            if (i % 2 != 0) {
                numImpare.add(i);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        addArrayOFnumber();
        producator1.start();
        producator2.start();
        producator3.start();
        consumator1.start();
        consumator2.start();
        consumator3.start();

        while (true) {
            Thread.sleep(1000);
            if (CountProd.getCount() == 40 && CountProd.getCountConsum() == 40) {
                System.out.println("Well done!");
                System.out.println("Sau produs :" + CountProd.getCount() + " produse!");
                System.out.println("Sau conumat :" + CountProd.getCountConsum() + " produse!");
                break;
            }
        }

    }


    static class CountProd {
        static int count = 0;
        static int consum = 0;

        public static void count() {
            count++;
        }

        public static void countConsum() {
            consum++;
        }

        public static int getCount() {
            return count;
        }

        public static int getCountConsum() {
            return consum;
        }
    }

    static class Producer extends Thread {

        Dep dep;
        CyclicBarrier barrier;
        ReentrantLock locker;
        Condition condition;

        Producer(Dep dep, CyclicBarrier barrier) {
            this.dep = dep;
            this.barrier = barrier;
            locker = new ReentrantLock();
            condition = locker.newCondition();
        }

        public void run() {
            while (CountProd.getCount() < 41) {
                if (CountProd.getCount() == 40) {
                    System.out.println(Thread.currentThread().getName() + " pe azi gata!");
                    barrier.reset();
                    break;
                }
                locker.lock();

                try {
                    if (!depStatus) {
                        if (Dep.dep.size() == 2) {
                            depStatus = true;
                            System.out.println(Thread.currentThread().getName() + " Ups! Dep is FULL!");

                                barrier.await();

                            locker.unlock();

                        } else {

                            System.out.println(Thread.currentThread().getName() + " a produs : " + numImpare.get(0));
                            dep.put(numImpare.get(0));
                            CountProd.count();
                            System.out.println(CountProd.getCount() + " sau produs");
                            locker.unlock();

                                barrier.await();

                        }
                    } else {

                            locker.unlock();
                            System.out.println(Thread.currentThread().getName() + " asteapta consumarea dep!");
                            barrier.await();

                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    barrier.reset();
                }
            }
        }
    }

    static class Consumer extends Thread {

        Dep dep;
        CyclicBarrier barrier;
        ReentrantLock locker;
        Condition condition;

        Consumer(Dep dep, CyclicBarrier barrier) {
            this.dep = dep;
            this.barrier = barrier;
            locker = new ReentrantLock();
            condition = locker.newCondition();
        }

        @Override
        public void run() {
            while (CountProd.getCountConsum() < 41) {
                if (CountProd.getCountConsum() == 40) {
                    System.out.println(Thread.currentThread().getName() + " pe azi gata!");
                    barrier.reset();
                    break;
                }
                locker.lock();
                try {
                    if (depStatus) {
                        if (Dep.dep.size() == 0) {

                            depStatus = false;
                            System.out.println(Thread.currentThread().getName() + " Ups! Dep is empty!");
                            barrier.await();
                            locker.unlock();

                        } else {

                            System.out.println(Thread.currentThread().getName() + " a consumat : " + Dep.dep.get(0));
                            dep.get();
                            CountProd.countConsum();
                            System.out.println(CountProd.getCountConsum() + " sau consumat");
                            locker.unlock();
                            barrier.await();

                        }
                    } else {

                        locker.unlock();
                        System.out.println(Thread.currentThread().getName() + " asteapta umplirea dep!");
                        barrier.await();

                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    barrier.reset();
                }
            }
        }
    }

    static class Dep {
        static ArrayList<Integer> dep = new ArrayList<>();

        public void get() {
            dep.remove(0);
        }

        public void put(int i) {
            dep.add(i);
            numImpare.remove(0);
        }
    }
}