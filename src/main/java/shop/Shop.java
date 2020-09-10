package shop;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@Log4j2
public class Shop
{
    public ArrayList<CashWindow> cashWindows = new ArrayList<>();
    public final int queueLimit;
    public final int totalQueueCapacity;
    public final double customerIntensity;
    public final double serviceIntensity;
    public final double simulationRunTime;
    public final ObservableList<String> logList;
    public BooleanProperty hasFinished = new SimpleBooleanProperty(false);

    public Shop(int n, int queueLimit, double customerIntensity, int serviceTime,
                double simulationRunTime, ObservableList<String> logList)
    {
        serviceIntensity = 1.0 / serviceTime;
        this.logList = logList;
        this.queueLimit = queueLimit;
        this.customerIntensity = customerIntensity;
        totalQueueCapacity = queueLimit * n - n;
        this.simulationRunTime = simulationRunTime;

        for(int i = 0; i < n; i++) {
            cashWindows.add(new CashWindow(queueLimit, serviceTime, logList));
        }
    }

    private Customer generateCustomer()
    {
        /*
        TODO
        Poisson :)
         */
        try {
            Thread.sleep(Math.round(1000 / customerIntensity));
        }
        catch (Exception exp)
        {
            log.error(exp);
        }
        return new Customer();
    }

    private void assignCustomerToQueue(Customer customer)
    {
        int lowerQueue = 0;

        for (int i = 1; i < cashWindows.size(); i++) {
            if (cashWindows.get(lowerQueue).queueSize() > cashWindows.get(i).queueSize()) {
                lowerQueue = i;
            }
        }

        if(!cashWindows.get(lowerQueue).tryAssign(customer))
            Platform.runLater(() -> logList.add("Покупатель " + customer.number + " ушел."));
    }

    public void process()
    {
        final Boolean[] isWorking = {true};
        Timer timer = new Timer(false);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                isWorking[0] = false;
            }
        };
        for (CashWindow cashWindow : cashWindows) {
                cashWindow.start();
        }
        timer.schedule(timerTask, Math.round(simulationRunTime * 60000));
        while (isWorking[0]) {
            assignCustomerToQueue(generateCustomer());
        }
        timer.cancel();
        for (CashWindow cashWindow : cashWindows) {
                cashWindow.isWorking = false;
        }
        for (CashWindow cashWindow : cashWindows) {
            try
            {
                cashWindow.join();
            }
            catch (Exception exp){
                log.error(exp);
            }
        }
        hasFinished.setValue(true);
    }
}

