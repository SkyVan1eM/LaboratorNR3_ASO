package com.company;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static ArrayList  <Thread> arrayThread = new ArrayList<>();
    static CyclicBarrier barrier = new CyclicBarrier(3);
    static CyclicBarrier barrier2 = new CyclicBarrier(3);
    static Semaphore semaphore = new Semaphore(1);
    static Dep depozit = new Dep();
    static Producer producer = new Producer(depozit, barrier, semaphore, barrier2);
    static Consumer consumer = new Consumer(depozit, barrier, semaphore);
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
        CyclicBarrier barrier;
        CyclicBarrier barrier2;
        Semaphore semaphore;
        Dep dep;

        Producer(Dep dep, CyclicBarrier barrier, Semaphore semaphore, CyclicBarrier barrier2) {
            this.dep = dep;
            this.barrier = barrier;
            this.barrier2 = barrier2;
            this.semaphore = semaphore;
        }

        public void run() {
            int i = 0;
            try {

                //System.out.println(Thread.currentThread().getName() + " asteapta randul!");
                while (CountProd.getCount() < 56) {
                    if (CountProd.getCount() == 55) {
                        break;
                    }
                    if (!(Dep.dep.size() == 0 )) {

                        System.out.println("In depozit sunt produse,asteptam!");
                        barrier.await();

                    } else {
                        barrier2.await();
                        if(!(Dep.dep.size() == 2)) {
                            if (!(i % 2 == 0)) {
                                dep.put(i);

                                System.out.println("Sau produs " + CountProd.getCount() + " elemente");
                            }
                     i++;
                            arrayThread.add(Thread.currentThread());
                            if (arrayThread.size() == 3) {
                                System.out.println(Dep.dep);
                                arrayThread.clear();
                            }
                            System.out.println(Thread.currentThread().getName() + " asteapta sa finalizeze restul producatorilor");
                        }
                    }

                }
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    static class Consumer extends Thread {
        CyclicBarrier barrier;
        Semaphore semaphore;
        Dep dep;

        Consumer(Dep dep, CyclicBarrier barrier, Semaphore semaphore) {
            this.dep = dep;
            this.barrier = barrier;
            this.semaphore = semaphore;
        }

        public void run() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("///PUT---> START"+ Thread.currentThread().getName());
            while (CountProd.getCountConsum() < 56) {
                if (CountProd.getCount() == 55) {
                    break;
                }
                try {
                    System.out.println(Dep.dep.size());
                    if (Dep.dep.size() == 0) {

                        System.out.println("Asteptam umplirea depozitului!");
                        semaphore.release();
                        barrier.await();
                    } else {
                        dep.get();
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
                System.out.println("///PUT---> STOP"+ Thread.currentThread().getName());
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

        public void get() throws InterruptedException {
            System.out.println("GET---> START"+ Thread.currentThread().getName());
            System.out.println(Thread.currentThread().getName() + " vrea un produs!");
            System.out.println("Produsele in depozit: " + dep);
            System.out.println(Thread.currentThread().getName() + " a luat un produsul " + dep.get(0));
            dep.remove(0);
            System.out.println("Produce in depozit: " + dep);
            CountProd.countConsum();
            System.out.println("Sau consumat " + CountProd.getCount() + " elemente");
            System.out.println("GET---> STOP" + Thread.currentThread().getName());
        }

        public void put(int i) {
            dep.add(i);
            CountProd.count();
            System.out.println(Thread.currentThread().getName() + " a produs produsul: " + i);

        }
    }
}