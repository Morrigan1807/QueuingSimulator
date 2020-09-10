package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import shop.Shop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

@Log4j2
public class SimulationProcessWindowController{
    public HBox cashDecksSimulation;
    public TextArea logOutput;
    public final ObservableList<String> logList = FXCollections.observableArrayList();
    public Shop shop;
    public Button saveButton;
    public Button resultsButton;

    public void initialize()
    {
        logList.addListener((ListChangeListener<String>) change -> {
            change.next();
            logOutput.setText(change.getAddedSubList().get(0) + "\n" + logOutput.getText());
        });
    }

    public void drawCashDecks() throws URISyntaxException
    {
        for(int i = 0; i < shop.cashWindows.size(); i++) {
            TextField textField = new TextField(Integer.toString(i+1));
            textField.setFont(Font.font("System", FontWeight.BOLD, 16));
            textField.setAlignment(Pos.CENTER);
            textField.setEditable(false);
            VBox vBox = new VBox();
            vBox.setPrefWidth(100);
            vBox.setAlignment(Pos.CENTER);
            vBox.getChildren().add(textField);
            ProgressBar progressBar = new ProgressBar();
            shop.cashWindows.get(i).progressValue = progressBar.progressProperty();
            vBox.getChildren().add(progressBar);
            shop.cashWindows.get(i).customersInQueueNumber = new SimpleIntegerProperty(0);
            for(int n = 0; n < shop.queueLimit; n++)
            {
                ImageView image = new ImageView(getClass().getResource("/img/person.png").toURI().toString());
                image.setVisible(false);
                image.setFitWidth(50);
                image.setFitHeight(100);
                vBox.getChildren().add(image);
            }
            shop.cashWindows.get(i).customersInQueueNumber.addListener((value, prev, post)-> Platform.runLater(()->{
                if(prev.intValue() < post.intValue())
                {
                    vBox.getChildren().get(post.intValue() + 1).setVisible(true);
                }
                else {
                    vBox.getChildren().get(prev.intValue() + 1).setVisible(false);
                }
            }));
            cashDecksSimulation.getChildren().add(vBox);
        }
        shop.hasFinished.addListener((observableValue, aBoolean, t1) -> Platform.runLater(() -> {
            saveButton.setDisable(!t1);
            resultsButton.setDisable(!t1);
        }));
    }

    public void showResultsButton() throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resultsWindow.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Results");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        ResultsWindowController controller = loader.getController();
        controller.shop = this.shop;
        controller.calculateResults();
        stage.showAndWait();
    }

    public void saveLogButton()
    {
        try
        {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text File", "*.txt");
            fileChooser.getExtensionFilters().add(extensionFilter);
            File file = fileChooser.showSaveDialog(this.logOutput.getScene().getWindow());

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(logOutput.getText());
            fileWriter.close();
        }
        catch (Exception exp){
            log.error(exp);
        }
    }
}
