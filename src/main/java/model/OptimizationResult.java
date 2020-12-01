package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class OptimizationResult implements Serializable {
    private int numberOfCashDesks;
    private double maxProfit;
}
