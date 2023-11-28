package week7;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

public class TimeCheck {

    public static void main(String[] args){
        Vector<Double> myVector = new Vector<>();
        ArrayList<Double> myArrayList = new ArrayList<>();
        LinkedList<Double> myLinkedList = new LinkedList<>();

        long startTime = 0;
        Random myRandom = new Random();

        double vectorT1, vectorT2, vectorT3, vectorT4;
        double arrayListT1, arrayListT2, arrayListT3, arrayListT4;
        double linkedListT1, linkedListT2, linkedListT3, linkedListT4;

        /*
        T1: add last
        T2: access random
        T3: add first
        T4: random add/remove
         */

        // 1. add last
        startTime = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            myVector.add(Math.random());
        }
        vectorT1 = (System.nanoTime() - startTime) / 1e6;

        // 2. access random
        startTime = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            myVector.get((int)(Math.random()*myVector.size()));
        }
        vectorT2 = (System.nanoTime() - startTime) / 1e6;

        // 3. add first
        myVector = new Vector<>();
        startTime = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            myVector.add(0, Math.random());
        }
        vectorT3 = (System.nanoTime() - startTime) / 1e6;

        // 4. random add/remove
        myVector = new Vector<>();
        startTime = System.nanoTime();
        int randomNumber = 0;
        for (int i = 0; i < 100000; i++) {
            randomNumber = myRandom.nextInt(2);
            if(myVector.size()==0 || randomNumber==0){
                myVector.add(Math.random());
            }else{
                myVector.remove((int)(Math.random()*myVector.size()));
            }
        }
        vectorT4 = (System.nanoTime() - startTime) / 1e6;

        System.out.println("[Vector] T1: "+vectorT1+"ms T2: "+vectorT2+"ms T3: "+vectorT3+"ms T4: "+vectorT4+"ms");

        // TODO: ArrayList 를 위해 위의 코드를 작성

        // TODO: LinkedList 를 위해 위의 코드 작성

    }
}
//수정되나??kjkjk
// alt + shift 위아래 옮기기.
