package shop;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

@Log4j2
public class CashWindow extends Thread{
    public final Queue<Customer> queue;
    private static int iter = 1;
    public int number;
    public final double serviceTime;
    public final int queueLimit;
    public ObservableList<String> logList;
    public volatile boolean isWorking = true;
    public DoubleProperty progressValue;
    public IntegerProperty customersInQueueNumber;

    public CashWindow(int queueLimit, double serviceTime, ObservableList<String> logList)
    {
        this.logList = logList;
        this.serviceTime = serviceTime;
        this.queueLimit = queueLimit;
        queue = new LinkedList<>();
        number = iter;
        iter++;
    }

    private double poisson(double serviceTime)
    {
        double res;
        Random random = new Random();

        res = Math.log(random.nextDouble()) * (- serviceTime);

        return res;
    }

    @Override
    public void run()
    {
        while (isWorking) {
            while (queueSize() != 0) {
                try {
                    Platform.runLater(()->progressValue.set(0));
                    Customer currentCustomer = queue.element();
                    Platform.runLater(() -> logList.add("Cash desk " + this.number + " is serving customer "
                            + currentCustomer.number + "."));
                    double tempPoisson = poisson(serviceTime);
                    for(int i = 0; i < 10; i++)
                    {
                        sleep(Math.round(tempPoisson * 100));
                        int finalI = i + 1;
                        Platform.runLater(()->
                                progressValue.set((double)finalI / 10));
                    }
                    synchronized (queue) {
                        queue.remove();
                        customersInQueueNumber.setValue(queue.size());
                    }
                    Platform.runLater(() -> logList.add("Customer " + currentCustomer.number
                            + " has been served by cash desk " + this.number + "."));

                } catch (Exception exp) {
                    log.error(exp);
                }
            }
        }
    }


    public int queueSize()
    {
        synchronized (queue){
            return this.queue.size();
        }
    }

    public boolean tryAssign(Customer customer)
    {
        if(queueSize() == queueLimit)
            return false;
        synchronized (queue){
            queue.add(customer);
            customersInQueueNumber.setValue(queue.size());
        }
        return true;
    }
}

