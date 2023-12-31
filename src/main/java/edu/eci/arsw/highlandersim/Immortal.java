package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean stop = false;

    private boolean running = true;


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {

        while (running) {
            Immortal im;

            if(!stop) {
                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());
    
                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                Immortal lockA;
                Immortal lockB;
                
                im = immortalsPopulation.get(nextFighterIndex);
                if (myIndex < nextFighterIndex) {
                    lockA = this;
                    lockB = im;
                } else {
                    lockA = im;
                    lockB = this;
                }

                synchronized(lockA) {
                    synchronized(lockB){
                        this.fight(im);
                    }
                }

                

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                synchronized(immortalsPopulation) {
                    try {
                        immortalsPopulation.wait();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    
                }
            }
       
        }

    }

    public void fight(Immortal i2) {
        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }


    }

    public void changeHealth(int v) {
        health = v;
    }


    public int getHealth() {
        return health;
    }

    public void terminate() {
        running = false;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
    
    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
