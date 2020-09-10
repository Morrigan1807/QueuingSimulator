package controller;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import shop.Shop;

@Log4j2
public class Controller {
    public TextField numberOfCashDecks;
    public TextField queueLimit;
    public TextField customerIntensity;
    public TextField averageCustomerServiceTime;
    public TextField simulationRunTime;

    public void startSimulationButton()
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
                        Double.parseDouble(customerIntensity.getText()), Integer.parseInt(averageCustomerServiceTime.getText()),
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
}
