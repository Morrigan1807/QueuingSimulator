package server;

import lombok.extern.log4j.Log4j2;
import model.OptimizationData;
import model.OptimizationMethod;
import model.OptimizationResult;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

@Log4j2
public class ServerWorker extends Thread{
    private final Socket socket;
    private static final double DOUBLE_EPS = 0.00001;
    private double tFreq;
    private double tServAvg;
    private double checkAvg;
    private double salaryPerHour;
    private int queueCap;
    private int maxNumCashDesks;
    private OptimizationMethod optimizationMethod;

    public ServerWorker(Socket socket)
    {
        super();
        this.socket = socket;
        this.start();
    }

    private void serverData(OptimizationData optimizationData)
    {
        tFreq = optimizationData.getCustomerIntensityOptimization();
        tServAvg = optimizationData.getAverageCustomerServiceTimeOptimization();
        checkAvg = optimizationData.getAveragePurchaseCheck();
        salaryPerHour = optimizationData.getSellersHourlySalary();
        queueCap = optimizationData.getQueueLimitOptimization();
        maxNumCashDesks = optimizationData.getMaxNumberOfCashDesks();
        optimizationMethod = optimizationData.getOptimizationMethod();
    }

    private OptimizationResult monteCarlo(int iter)
    {
        System.out.println("\n\n****************************************\n\nMonte-Carlo\n");
        Random random = new Random();
        int res = random.nextInt(maxNumCashDesks) + 1, tempN;
        double tempFun, maxFun = function(res);

        for (int i = 0; i < iter; i++)
        {
            tempN = random.nextInt(maxNumCashDesks) + 1;
            tempFun = function(tempN);
            System.out.printf("%d. Function = %.2f; N = %d%n", i, tempFun, tempN);
            if (tempFun > maxFun)
            {
                maxFun = tempFun;
                res = tempN;
            }
        }

        return new OptimizationResult(res, maxFun);
    }

    private OptimizationResult bionic()
    {
        System.out.println("\n\n****************************************\n\nBionic genetic algorithm\n");

        Random random = new Random();
        int tempN, prevN, maxN = random.nextInt(maxNumCashDesks);
        double tempF, maxF = function(maxN);

        System.out.println("\n-----Generation 0-----");
        System.out.printf("1. Function = %.2f; N = %d%n", maxF, maxN);

        for (int i = 2; i < (maxNumCashDesks / 10) + 1; i++)
        {
            tempN = random.nextInt(maxNumCashDesks);
            tempF = function(tempN);
            System.out.printf("%d. Function = %.2f; N = %d%n", i, tempF, tempN);

            if (tempF > maxF)
            {
                maxF = tempF;
                maxN = tempN;
            }
        }

        prevN = maxN;

        for (int i = 1; i < 101; i++)
        {
            System.out.println("\n-----Generation " + i + "-----");

            for (int k = -1; k < 2; k++)
            {
                tempN = prevN + k;
                tempF = function(tempN);
                System.out.printf("%d. Function = %.2f; N = %d%n", k + 2, tempF, tempN);

                if (tempF > maxF)
                {
                    maxF = tempF;
                    maxN = tempN;
                }
            }

            if(maxN > maxNumCashDesks)
            {
                maxN = prevN;
                maxF = function(prevN);
            }

            if(maxN == prevN) {
                break;
            }

            prevN = maxN;
        }

        return new OptimizationResult(maxN, maxF);
    }

    private double function(int n)
    {
        final double psi = tFreq * tServAvg / n;
        double result = tFreq * 60 * checkAvg;
        double series_elem = 1;
        double series_sum = 1;

        if (Math.abs(psi - 1) > DOUBLE_EPS) {
            for (int k = 1; k <= n; k++) {
                series_elem*=psi/k;
                series_sum+=series_elem;
            }
            series_sum += series_elem * psi * (1 - Math.pow(psi, queueCap)) / (1 - psi);
            result *= (1 - series_elem * Math.pow(psi, queueCap) / series_sum);
        }
        else {
            for (int k = 1; k <= n; k++) {
                series_elem/=k;
                series_sum+=series_elem;
            }
            series_sum += series_elem * queueCap;
            result *= (1 - series_elem / series_sum);
        }
        result -= salaryPerHour * n;
        return result;
    }

    public void run()
    {
        try {
            OptimizationResult result = new OptimizationResult(0, 0);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            serverData((OptimizationData) objectInputStream.readObject());

            switch (optimizationMethod)
            {
                case MONTE_CARLO:
                    result = monteCarlo(maxNumCashDesks * 2);
                    break;
                case GENETIC:
                    result = bionic();
                    break;
            }

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(result);

            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
        }
        catch (Exception exp)
        {
            log.error(exp.getMessage());
        }
    }
}
