package controller;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import model.OptimizationData;
import model.OptimizationMethod;
import model.OptimizationResult;
import shop.Shop;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.UnaryOperator;

@Log4j2
public class Controller {
    public RadioButton simulationModelingRadioButton;
    public GridPane simulationDataGridPane;
    public TextField numberOfCashDecks;
    public TextField queueLimit;
    public TextField customerIntensity;
    public TextField averageCustomerServiceTime;
    public TextField simulationRunTime;
    public Label attentionMessage;
    public Button startSimulationButton;

    public RadioButton modelOptimizationRadioButton;
    public GridPane optimizationDataGridPane;
    public TextField queueLimitOptimization;
    public TextField customerIntensityOptimization;
    public TextField averageCustomerServiceTimeOptimization;
    public TextField averagePurchaseCheck;
    public TextField sellersHourlySalary;
    public TextField maxNumberOfCashDesks;
    public RadioButton monteCarloRadioButton;
    public RadioButton bionicAlgorithmRadioButton;
    public Button calculateButton;

    private OptimizationMethod chosenOptimizationMethod = OptimizationMethod.MONTE_CARLO;

    public void initialize() {
        simulationModelingRadioButton.setOnAction(e -> {
            modelOptimizationRadioButton.setSelected(false);
            optimizationDataGridPane.setDisable(true);
            monteCarloRadioButton.setDisable(true);
            bionicAlgorithmRadioButton.setDisable(true);
            calculateButton.setDisable(true);

            simulationDataGridPane.setDisable(false);
            attentionMessage.setVisible(true);
            startSimulationButton.setDisable(false);
        });

        modelOptimizationRadioButton.setOnAction(e ->
        {
            simulationModelingRadioButton.setSelected(false);
            simulationDataGridPane.setDisable(true);
            attentionMessage.setVisible(false);
            startSimulationButton.setDisable(true);

            optimizationDataGridPane.setDisable(false);
            monteCarloRadioButton.setDisable(false);
            bionicAlgorithmRadioButton.setDisable(false);
            calculateButton.setDisable(false);
        });

        monteCarloRadioButton.setOnAction(e -> {
            bionicAlgorithmRadioButton.setSelected(false);
            chosenOptimizationMethod = OptimizationMethod.MONTE_CARLO;
        });
        bionicAlgorithmRadioButton.setOnAction(e -> {
            monteCarloRadioButton.setSelected(false);
            chosenOptimizationMethod = OptimizationMethod.GENETIC;
        });

        UnaryOperator<TextFormatter.Change> intFilter = (change -> {
            String text = change.getText();

            if (text.matches("[0-9]*")) {
                return change;
            }

            return null;
        });

        UnaryOperator<TextFormatter.Change> doubleFilter = (change -> {
            String text = change.getText();

            if (text.matches("([0-9]*[.]?[0-9]*)")) {
                return change;
            }

            return null;
        });

        for (TextField textField : Arrays.asList(numberOfCashDecks, queueLimit, maxNumberOfCashDesks, queueLimitOptimization)) {
            textField.setTextFormatter(new TextFormatter<>(intFilter));
        }
        for (TextField textField : Arrays.asList(customerIntensity, averageCustomerServiceTime, simulationRunTime,
                customerIntensityOptimization, averageCustomerServiceTimeOptimization, averagePurchaseCheck, sellersHourlySalary)) {
            textField.setTextFormatter(new TextFormatter<>(doubleFilter));
        }

    }

    public void startSimulationOnAction()
    {
        if(!numberOfCashDecks.getText().equals("") && !queueLimit.getText().equals("") && !customerIntensity.getText().equals("")
                && !averageCustomerServiceTime.getText().equals("") && !simulationRunTime.getText().equals(""))
        {
            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/simulationProcessWindow.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Simulation process");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                SimulationProcessWindowController controller = loader.getController();

                Shop shop = new Shop(Integer.parseInt(numberOfCashDecks.getText()), Integer.parseInt(queueLimit.getText()),
                        Double.parseDouble(customerIntensity.getText()), Double.parseDouble(averageCustomerServiceTime.getText()),
                        Double.parseDouble(simulationRunTime.getText()), controller.logList);
                controller.shop = shop;
                controller.drawCashDecks();
                stage.show();
                new Thread(shop::process).start();
                ((Stage)numberOfCashDecks.getScene().getWindow()).close();
            }
            catch (Exception exp)
            {
                log.error(exp);
            }
        }
    }

    public void calculateOnAction()
    {
        OptimizationData optimizationData = new OptimizationData();
        optimizationData.setQueueLimitOptimization(Integer.parseInt(queueLimitOptimization.getText()));
        optimizationData.setCustomerIntensityOptimization(Double.parseDouble(customerIntensityOptimization.getText()));
        optimizationData.setAverageCustomerServiceTimeOptimization(Double.parseDouble(averageCustomerServiceTimeOptimization.getText()));
        optimizationData.setAveragePurchaseCheck(Double.parseDouble(averagePurchaseCheck.getText()));
        optimizationData.setSellersHourlySalary(Double.parseDouble(sellersHourlySalary.getText()));
        optimizationData.setMaxNumberOfCashDesks(Integer.parseInt(maxNumberOfCashDesks.getText()));
        optimizationData.setOptimizationMethod(chosenOptimizationMethod);

        try {
            Socket socket = new Socket("localhost", 1234);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(optimizationData);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            OptimizationResult optimizationResult = (OptimizationResult) objectInputStream.readObject();

            objectInputStream.close();
            objectOutputStream.close();
            socket.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(String.format(Locale.US, "Optimal number of cash desks = %d;\nMax profit = %.2f",
                    optimizationResult.getNumberOfCashDesks(), optimizationResult.getMaxProfit()));
            alert.setTitle("Results");
            alert.show();
        }
        catch (Exception exp)
        {
            log.error(exp.getMessage());
        }

    }
}
