package com.company;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;


public class Main {
    static ArrayList<Integer> numImpare = new ArrayList<>();
    static CyclicBarrier barrier = new CyclicBarrier(2);
    static CyclicBarrier barrier2 = new CyclicBarrier(2);
    static Semaphore semaphore = new Semaphore(2);
    static Dep depozit = new Dep();
    static Producer producer = new Producer(depozit, barrier, semaphore, barrier2);
    static Consumer consumer = new Consumer(depozit, barrier, semaphore);
    static Thread producator1 = new Thread(producer, "Producator - 1");
    static Thread producator2 = new Thread(producer, "Producator - 2");
    static Thread producator3 = new Thread(producer, "Producator - 3");
    static Thread consumator1 = new Thread(consumer, "Consumator - 1 ");
    static Thread consumator2 = new Thread(consumer, "Consumator - 2 ");
    static Thread consumator3 = new Thread(consumer, "Consumator - 3 ");
    static int product ;

    static public void addArrayOFnumber(){
        for (int i = 1; i < 81; i++ ){
            if( i % 2 != 0){
                numImpare.add(i);
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        addArrayOFnumber();

        Thread.sleep(100);
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

            while(CountProd.getCount() < 42) {

                if(CountProd.getCount() == 41) {
                    System.out.println(CountProd.getCount() + "sau produs");
                    break;
                }
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (Dep.dep.size() == 0) {
                    System.out.println("Dep is empty " + Dep.dep );
                }

                try {
                    System.out.println(numImpare);

                            dep.put(numImpare.get(0));
                            numImpare.remove(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(Thread.currentThread().getName() + " put the product : " + Dep.dep.get(0));

                    semaphore.release();
                    try {
                        System.out.println(Thread.currentThread().getName() + " wait for the products to be consumed");
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
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
            while (CountProd.getCountConsum() < 41){

                if(CountProd.getCountConsum() == 40){
                    System.out.println("sau consumat " + CountProd.getCountConsum() + " " + CountProd.getCount());
                    break;
                }
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(Dep.dep.size() >= 1){

                    try {
                        dep.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + "get the product: " + product);
                    CountProd.countConsum();
                    semaphore.release();
                }else{
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class Dep extends Thread {
        static ArrayList<Integer> dep = new ArrayList<>();

        public void get() throws InterruptedException {
            sleep(400);
            dep.remove(0);
            CountProd.countConsum();

        }

        public void put(int i) throws InterruptedException {
            dep.add(i);
            product = i;
            CountProd.count();
        }
    }
}