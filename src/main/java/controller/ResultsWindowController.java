package controller;

import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;
import shop.Shop;

import java.io.File;
import java.io.FileWriter;

@Log4j2
public class ResultsWindowController {
    public Shop shop;
    public TextField systemDowntimeProbability;
    public TextField rejectionProbability;
    public TextField absoluteBandwidth;
    public TextField relativeBandwidth;
    public TextField averageNumberOfBusyChannels;
    public TextField averageNumberOfRequestsInTheQueue;
    public TextField averageNumberOfRequestsInTheSystem;
    public TextField averageTimeInQueue;
    public TextField averageTimeInSystem;

    public void saveResultsButton()
    {
        try
        {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text File", "*.txt");
            fileChooser.getExtensionFilters().add(extensionFilter);
            File file = fileChooser.showSaveDialog(this.averageNumberOfBusyChannels.getScene().getWindow());

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("Number of Service Channels: " + shop.cashWindows.size() + ";\n");
            fileWriter.write("Number of Service Channels: " + shop.customerIntensity + ";\n");
            fileWriter.write("Channel performance: " + shop.serviceIntensity + ";\n");
            fileWriter.write("Maximum Queue Length: " + shop.queueLimit + ";\n");
            fileWriter.write("Channel Downtime Probability: " + systemDowntimeProbability.getText() + ";\n");
            fileWriter.write("Rejection Probability: " + rejectionProbability.getText() + ";\n");
            fileWriter.write("Absolute Bandwidth: " + absoluteBandwidth.getText() + ";\n");
            fileWriter.write("Relative Bandwidth: " + relativeBandwidth.getText() + ";\n");
            fileWriter.write("Average Number Of Busy Channels: " + averageNumberOfBusyChannels.getText() + ";\n");
            fileWriter.write("Average Number Of Requests In The Queue: " + averageNumberOfRequestsInTheQueue.getText() + ";\n");
            fileWriter.write("Average Number Of Requests In The System: " + averageNumberOfRequestsInTheSystem.getText() + ";\n");
            fileWriter.write("Average Time In Queue: " + averageTimeInQueue.getText() + ";\n");
            fileWriter.write("Average Time In System: " + averageTimeInSystem.getText() + ".\n");

            fileWriter.close();
        }
        catch (Exception exp){
            log.error(exp);
        }
    }

    private double systemDowntimeProbability(int n, double channelLoad, int m)
    {
        double res = 0;
        double temp = 1;
        if(channelLoad == 1)
        {
            res++;
            for(int k = 1; k <= n; k++)
            {
                temp *= (double) n /k;
                res += temp;
            }
            res += temp * m;
        }
        else
        {
            res++;
            for(int k = 1; k <= n; k++)
            {
                temp *= ((double) n * channelLoad) / k;
                res += temp;
            }
            res += (temp * (channelLoad * (1- Math.pow(channelLoad, m)))) / (1 - channelLoad);
        }
        return 1 / res;
    }

    private double rejectionProbability(int n, double channelLoad, int m, double systemDowntimeProbabilityValue)
    {
        double res = systemDowntimeProbabilityValue * Math.pow(channelLoad, n+m);
        for(int i = 1; i <= n; i++)
        {
            res *= (double) n /i;
        }
        return res;
    }

    private double averageNumberOfRequestsInTheQueue(int n, int m, double channelLoad, double systemDowntimeProbabilityValue)
    {
        double res = systemDowntimeProbabilityValue;
        if(channelLoad == 1)
        {
            for(int i = 1; i <=n; i++)
            {
                res *= ((double) n /i);
            }
            res *= (double) (m * (m + 1)) / 2;
        }
        else
        {
            for(int i = 1; i <=n; i++)
            {
                res *= ((double) n /i) * channelLoad;
            }
            res *= channelLoad * (1 - (m+1) * Math.pow(channelLoad, m) + m * Math.pow(channelLoad, m+1)) /
                    Math.pow((1-channelLoad), 2);
        }
        return res;
    }

    public void calculateResults()
    {
        double qsLoad = shop.customerIntensity / shop.serviceIntensity;
        double channelLoad = qsLoad / shop.cashWindows.size();

        double systemDowntimeProbabilityValue = systemDowntimeProbability(shop.cashWindows.size(), channelLoad,
                shop.totalQueueCapacity);
        double rejectionProbabilityValue = rejectionProbability(shop.cashWindows.size(), channelLoad,
                shop.totalQueueCapacity, systemDowntimeProbabilityValue);
        double relativeBandwidthValue = 1 - rejectionProbabilityValue;
        double absoluteBandwidthValue = shop.customerIntensity * relativeBandwidthValue;
        double averageNumberOfBusyChannelsValue = absoluteBandwidthValue / shop.totalQueueCapacity;
        double averageNumberOfRequestsInTheQueueValue = averageNumberOfRequestsInTheQueue(shop.cashWindows.size(),
                shop.totalQueueCapacity, channelLoad, systemDowntimeProbabilityValue);
        double averageNumberOfRequestsInTheSystemValue = averageNumberOfBusyChannelsValue +
                averageNumberOfRequestsInTheQueueValue;
        double averageTimeInQueueValue = (1 / shop.customerIntensity) * averageNumberOfRequestsInTheQueueValue;
        double averageTimeInSystemValue = (1 / shop.customerIntensity) * averageNumberOfRequestsInTheSystemValue;

        systemDowntimeProbability.setText(String.valueOf(systemDowntimeProbabilityValue));
        rejectionProbability.setText(String.valueOf(rejectionProbabilityValue));
        absoluteBandwidth.setText(String.valueOf(absoluteBandwidthValue));
        relativeBandwidth.setText(String.valueOf(relativeBandwidthValue));
        averageNumberOfBusyChannels.setText(String.valueOf(averageNumberOfBusyChannelsValue));
        averageNumberOfRequestsInTheQueue.setText(String.valueOf(averageNumberOfRequestsInTheQueueValue));
        averageNumberOfRequestsInTheSystem.setText(String.valueOf(averageNumberOfRequestsInTheSystemValue));
        averageTimeInQueue.setText(String.valueOf(averageTimeInQueueValue));
        averageTimeInSystem.setText(String.valueOf(averageTimeInSystemValue));
    }
}
