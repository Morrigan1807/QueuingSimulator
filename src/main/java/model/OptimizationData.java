package model;

import lombok.Data;

import java.io.Serializable;

@Data
public class OptimizationData implements Serializable {
    private int queueLimitOptimization;
    private double customerIntensityOptimization;
    private double averageCustomerServiceTimeOptimization;
    private double averagePurchaseCheck;
    private double sellersHourlySalary;
    private int maxNumberOfCashDesks;
    private OptimizationMethod optimizationMethod;
}
