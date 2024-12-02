package com.example.ia;

import com.example.ia.model.Expense;
import com.example.ia.model.Income;
import com.example.ia.model.Subscription;
import com.example.ia.storage.DataStorage;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonObject;

public class BudgetApp extends Application {
    private List<Income> incomes = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
    private List<Subscription> subscriptions = new ArrayList<>();
    private TableView<SummaryItem> summaryTable;
    private PieChart pieChart;
    private LineChart<String, Number> lineChart;
    private Label promptLabel;
    private Hyperlink addDataLink;
    private VBox statementPane;

    @Override
    public void start(Stage primaryStage) {
        loadFinancialData();

        TabPane tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);

        Tab incomeTab = new Tab("Income", createIncomePane());
        incomeTab.setClosable(false);
        Tab expenseTab = new Tab("Expenses", createExpensePane());
        expenseTab.setClosable(false);
        Tab subscriptionTab = new Tab("Subscriptions", createSubscriptionPane());
        subscriptionTab.setClosable(false);
        Tab statementTab = new Tab("Statement", createStatementPane());
        statementTab.setClosable(false);

        tabPane.getTabs().addAll(incomeTab, expenseTab, subscriptionTab, statementTab);
        tabPane.getSelectionModel().select(statementTab);

        Scene scene = new Scene(tabPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        VBox root = new VBox(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        Scene mainScene = new Scene(root, 800, 600);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Budget Tracker");
        primaryStage.show();
    }

    private VBox createIncomePane() {
        VBox incomePane = new VBox(10);
        incomePane.setPadding(new Insets(10));
        incomePane.setAlignment(Pos.TOP_CENTER);

        Label incomeLabel = new Label("Income:");
        TableView<Income> incomeTable = new TableView<>();
        configureIncomeTable(incomeTable);
        HBox incomeInput = createIncomeInput(incomeTable);
        HBox incomeButtons = createIncomeButtons(incomeTable);

        incomePane.getChildren().addAll(incomeLabel, incomeTable, incomeInput, incomeButtons);
        return incomePane;
    }

    private VBox createExpensePane() {
        VBox expensePane = new VBox(10);
        expensePane.setPadding(new Insets(10));
        expensePane.setAlignment(Pos.TOP_CENTER);

        Label expenseLabel = new Label("Expenses:");
        TableView<Expense> expenseTable = new TableView<>();
        configureExpenseTable(expenseTable);
        HBox expenseInput = createExpenseInput(expenseTable);
        HBox expenseButtons = createExpenseButtons(expenseTable);

        expensePane.getChildren().addAll(expenseLabel, expenseTable, expenseInput, expenseButtons);
        return expensePane;
    }

    private VBox createSubscriptionPane() {
        VBox subscriptionPane = new VBox(10);
        subscriptionPane.setPadding(new Insets(10));
        subscriptionPane.setAlignment(Pos.TOP_CENTER);

        Label subscriptionLabel = new Label("Subscriptions:");
        TableView<Subscription> subscriptionTable = new TableView<>();
        configureSubscriptionTable(subscriptionTable);
        HBox subscriptionInput = createSubscriptionInput(subscriptionTable);
        HBox subscriptionButtons = createSubscriptionButtons(subscriptionTable);

        subscriptionPane.getChildren().addAll(subscriptionLabel, subscriptionTable, subscriptionInput, subscriptionButtons);
        return subscriptionPane;
    }

    private VBox createStatementPane() {
        statementPane = new VBox(10);
        statementPane.setPadding(new Insets(10));
        statementPane.setAlignment(Pos.TOP_CENTER);

        Label statementLabel = new Label("Total Statement:");

        summaryTable = new TableView<>();
        configureSummaryTable();

        pieChart = new PieChart();
        updatePieChart();

        lineChart = createLineChart();
        updateLineChart();

        Button clearAllDataButton = new Button("Clear All Data");
        clearAllDataButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all data?")) {
                incomes.clear();
                expenses.clear();
                subscriptions.clear();
                updateAllTables();
                updateStatementPane();
                saveFinancialData();
            }
        });

        Button exportDataButton = new Button("Export Data");
        exportDataButton.setOnAction(e -> exportData());

        statementPane.getChildren().addAll(statementLabel, summaryTable, pieChart, lineChart, clearAllDataButton, exportDataButton);
        checkForData(statementPane);
        return statementPane;
    }
    private void configureSummaryTable() {
        TableColumn<SummaryItem, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());

        TableColumn<SummaryItem, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        summaryTable.getColumns().addAll(categoryColumn, amountColumn);
        updateSummaryTable();
    }

    private void updateSummaryTable() {
        summaryTable.getItems().clear();
        summaryTable.getItems().add(new SummaryItem("Total Income", calculateTotalIncome()));
        summaryTable.getItems().add(new SummaryItem("Total Expenses", calculateTotalExpenses()));
        summaryTable.getItems().add(new SummaryItem("Total Subscriptions", calculateTotalSubscriptions()));
        summaryTable.getItems().add(new SummaryItem("Free Income", calculateFreeIncome()));
    }

    private void updateStatementPane() {
        updateSummaryTable();
        updatePieChart();
        updateLineChart();
        updatePrompt();
    }

    private void updatePieChart() {
        pieChart.getData().clear();
        PieChart.Data incomeData = new PieChart.Data("Income", calculateTotalIncome());
        PieChart.Data expenseData = new PieChart.Data("Expenses", calculateTotalExpenses());
        PieChart.Data subscriptionData = new PieChart.Data("Subscriptions", calculateTotalSubscriptions());

        pieChart.getData().addAll(incomeData, expenseData, subscriptionData);

        incomeData.getNode().setStyle("-fx-pie-color: #808080;");
        expenseData.getNode().setStyle("-fx-pie-color: #a9a9a9;");
        subscriptionData.getNode().setStyle("-fx-pie-color: #d3d3d3;");

        pieChart.setLegendVisible(false);
    }

    private LineChart<String, Number> createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        return lineChart;
    }

    private void updateLineChart() {
        lineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Financial Data");

        series.getData().add(new XYChart.Data<>("Income", calculateTotalIncome()));
        series.getData().add(new XYChart.Data<>("Expenses", calculateTotalExpenses()));
        series.getData().add(new XYChart.Data<>("Subscriptions", calculateTotalSubscriptions()));

        lineChart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-stroke: #808080; -fx-background-color: #808080, white;");
        }
    }
    private double calculateTotalIncome() {
        return incomes.stream().mapToDouble(Income::getAmount).sum();
    }

    private double calculateTotalExpenses() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    private double calculateTotalSubscriptions() {
        return subscriptions.stream().mapToDouble(Subscription::getCost).sum();
    }

    private double calculateFreeIncome() {
        return calculateTotalIncome() - (calculateTotalExpenses() + calculateTotalSubscriptions());
    }
    private void updatePrompt() {
        if (promptLabel == null) {
            promptLabel = new Label("No financial data available. Please add some data.");
        }
        if (addDataLink == null) {
            addDataLink = new Hyperlink("Go to Income Tab");
            addDataLink.setOnAction(e -> {
                TabPane tabPane = (TabPane) statementPane.getScene().getRoot().lookup(".tab-pane");
                tabPane.getSelectionModel().select(0);
            });
        }

        if (incomes.isEmpty() && expenses.isEmpty() && subscriptions.isEmpty()) {
            if (!statementPane.getChildren().contains(promptLabel)) {
                statementPane.getChildren().addAll(promptLabel, addDataLink);
            }
        } else {
            statementPane.getChildren().removeAll(promptLabel, addDataLink);
        }
    }

    private HBox createIncomeInput(TableView<Income> table) {
        TextField sourceField = new TextField();
        sourceField.setPromptText("Source");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            try {
                String source = sourceField.getText();
                double amount = Double.parseDouble(amountField.getText());
                Income income = new Income(source, amount);
                incomes.add(income);
                table.getItems().add(income);
                sourceField.clear();
                amountField.clear();
                animateAddition(table);
                updateStatementPane();
                saveFinancialData();
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid input", "Please enter a valid number for the amount.");
            }
        });
        HBox.setHgrow(sourceField, Priority.ALWAYS);
        HBox.setHgrow(amountField, Priority.ALWAYS);
        return new HBox(10, sourceField, amountField, addButton);
    }

    private HBox createExpenseInput(TableView<Expense> table) {
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            try {
                String category = categoryField.getText();
                double amount = Double.parseDouble(amountField.getText());
                Expense expense = new Expense(category, amount);
                expenses.add(expense);
                table.getItems().add(expense);
                categoryField.clear();
                amountField.clear();
                animateAddition(table);
                updateStatementPane();
                saveFinancialData();
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid input", "Please enter a valid number for the amount.");
            }
        });
        HBox.setHgrow(categoryField, Priority.ALWAYS);
        HBox.setHgrow(amountField, Priority.ALWAYS);
        return new HBox(10, categoryField, amountField, addButton);
    }

    private HBox createSubscriptionInput(TableView<Subscription> table) {
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField costField = new TextField();
        costField.setPromptText("Cost");
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double cost = Double.parseDouble(costField.getText());
                Subscription subscription = new Subscription(name, cost);
                subscriptions.add(subscription);
                table.getItems().add(subscription);
                nameField.clear();
                costField.clear();
                animateAddition(table);
                updateStatementPane();
                saveFinancialData();
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid input", "Please enter a valid number for the cost.");
            }
        });
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(costField, Priority.ALWAYS);
        return new HBox(10, nameField, costField, addButton);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void animateAddition(TableView<?> table) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), table);
        transition.setFromY(-10);
        transition.setToY(0);
        transition.setInterpolator(Interpolator.EASE_OUT);
        transition.play();
    }

    private HBox createIncomeButtons(TableView<Income> table) {
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Income selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                incomes.remove(selected);
                table.getItems().remove(selected);
                updateStatementPane();
                saveFinancialData();
            }
        });

        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all income sources?")) {
                incomes.clear();
                table.getItems().clear();
                updateStatementPane();
                saveFinancialData();
            }
        });

        return new HBox(10, deleteButton, clearButton);
    }

    private HBox createExpenseButtons(TableView<Expense> table) {
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                expenses.remove(selected);
                table.getItems().remove(selected);
                updateStatementPane();
                saveFinancialData();
            }
        });

        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all expenses?")) {
                expenses.clear();
                table.getItems().clear();
                updateStatementPane();
                saveFinancialData();
            }
        });

        return new HBox(10, deleteButton, clearButton);
    }

    private HBox createSubscriptionButtons(TableView<Subscription> table) {
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Subscription selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                subscriptions.remove(selected);
                table.getItems().remove(selected);
                updateStatementPane();
                saveFinancialData();
            }
        });

        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all subscriptions?")) {
                subscriptions.clear();
                table.getItems().clear();
                updateStatementPane();
                saveFinancialData();
            }
        });

        return new HBox(10, deleteButton, clearButton);
    }

    private boolean showConfirmationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void configureIncomeTable(TableView<Income> table) {
        TableColumn<Income, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSource()));

        TableColumn<Income, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());

        table.getColumns().addAll(sourceColumn, amountColumn);
        table.setItems(javafx.collections.FXCollections.observableArrayList(incomes));
    }

    private void configureExpenseTable(TableView<Expense> table) {
        TableColumn<Expense, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));

        TableColumn<Expense, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());

        table.getColumns().addAll(categoryColumn, amountColumn);
        table.setItems(javafx.collections.FXCollections.observableArrayList(expenses));
    }

    private void configureSubscriptionTable(TableView<Subscription> table) {
        TableColumn<Subscription, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<Subscription, Double> costColumn = new TableColumn<>("Cost");
        costColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCost()).asObject());

        table.getColumns().addAll(nameColumn, costColumn);
        table.setItems(javafx.collections.FXCollections.observableArrayList(subscriptions));
    }

    private void loadFinancialData() {
        File dataFile = new File("financial_data.dat");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                List<Income> emptyIncomes = new ArrayList<>();
                List<Expense> emptyExpenses = new ArrayList<>();
                List<Subscription> emptySubscriptions = new ArrayList<>();
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                    oos.writeObject(emptyIncomes);
                    oos.writeObject(emptyExpenses);
                    oos.writeObject(emptySubscriptions);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            List<Object> data = DataStorage.loadData();
            incomes = (List<Income>) data.get(0);
            expenses = (List<Expense>) data.get(1);
            subscriptions = (List<Subscription>) data.get(2);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveFinancialData() {
        try {
            DataStorage.saveData(incomes, expenses, subscriptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAllTables() {
        summaryTable.getItems().clear();
        updateSummaryTable();

        TableView<Income> incomeTable = (TableView<Income>) ((VBox) ((Tab) ((TabPane) summaryTable.getScene().getRoot().lookup(".tab-pane")).getTabs().get(0)).getContent()).getChildren().get(1);
        incomeTable.getItems().clear();

        TableView<Expense> expenseTable = (TableView<Expense>) ((VBox) ((Tab) ((TabPane) summaryTable.getScene().getRoot().lookup(".tab-pane")).getTabs().get(1)).getContent()).getChildren().get(1);
        expenseTable.getItems().clear();

        TableView<Subscription> subscriptionTable = (TableView<Subscription>) ((VBox) ((Tab) ((TabPane) summaryTable.getScene().getRoot().lookup(".tab-pane")).getTabs().get(2)).getContent()).getChildren().get(1);
        subscriptionTable.getItems().clear();
    }

    private void checkForData(VBox statementPane) {
        if (incomes.isEmpty() && expenses.isEmpty() && subscriptions.isEmpty()) {
            promptLabel = new Label("No financial data available. Please add some data.");
            addDataLink = new Hyperlink("Go to Income Tab");
            addDataLink.setOnAction(e -> {
                TabPane tabPane = (TabPane) statementPane.getScene().getRoot().lookup(".tab-pane");
                tabPane.getSelectionModel().select(0);
            });
            statementPane.getChildren().addAll(promptLabel, addDataLink);
        }
    }
    private boolean validateInput(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                showErrorDialog("Invalid input", "All fields must be filled.");
                return false;
            }
        }
        return true;
    }
    private void showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new Gson();
                JsonObject data = new JsonObject();
                data.add("incomes", gson.toJsonTree(incomes));
                data.add("expenses", gson.toJsonTree(expenses));
                data.add("subscriptions", gson.toJsonTree(subscriptions));
                gson.toJson(data, writer);
                showConfirmationDialog("Export Successful", "Data exported successfully.");
            } catch (IOException e) {
                showErrorDialog("Export Failed", "Failed to export data.");
            }
        }
    }

    private void importData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                JsonObject data = gson.fromJson(reader, JsonObject.class);
                incomes = gson.fromJson(data.get("incomes"), new TypeToken<List<Income>>(){}.getType());
                expenses = gson.fromJson(data.get("expenses"), new TypeToken<List<Expense>>(){}.getType());
                subscriptions = gson.fromJson(data.get("subscriptions"), new TypeToken<List<Subscription>>(){}.getType());
                updateAllTables();
                updateStatementPane();
                showConfirmationDialog("Import Successful", "Data imported successfully.");
            } catch (IOException e) {
                showErrorDialog("Import Failed", "Failed to import data.");
            }
        }
    }

    private void generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Financial Report\n\n");
        report.append("Total Income: ").append(calculateTotalIncome()).append("\n");
        report.append("Total Expenses: ").append(calculateTotalExpenses()).append("\n");
        report.append("Total Subscriptions: ").append(calculateTotalSubscriptions()).append("\n");
        report.append("Free Income: ").append(calculateFreeIncome()).append("\n");

        showReportDialog("Financial Report", report.toString());
    }

    private void showReportDialog(String title, String report) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(report);
        alert.showAndWait();
    }
    private void saveUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(BudgetApp.class);
        prefs.put("theme", "light");
    }

    private void loadUserPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(BudgetApp.class);
        String theme = prefs.get("theme", "light");
    }


    public static void main(String[] args) {
        launch(args);
    }
}

// to do:
// add commments
// add more comments
// idk
// im done