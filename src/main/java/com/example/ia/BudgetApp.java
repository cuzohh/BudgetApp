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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


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
    private double budgetLimit = Double.MAX_VALUE;



    private void checkForUpcomingSubscriptions() {
        LocalDate today = LocalDate.now();
        for (Subscription subscription : subscriptions) {
            LocalDate nextPaymentDate = calculateNextPaymentDate(subscription);
            long daysUntilNextPayment = ChronoUnit.DAYS.between(today, nextPaymentDate);
            if (daysUntilNextPayment <= subscription.getNotificationDays()) {
                // Show a notification if the due date is within the notification period
                showNotification("Upcoming Subscription", "Your subscription to " + subscription.getName() + " is due in " + daysUntilNextPayment + " days.");
            }
        }
    }


    private LocalDate calculateNextPaymentDate(Subscription subscription) {
        LocalDate nextPaymentDate = subscription.getStartDate();
        while (nextPaymentDate.isBefore(LocalDate.now())) {
            // Loop until we find a date that is after the current date
            switch (subscription.getRecurrencePeriod().toLowerCase()) {
                case "weekly":
                    // Add one week to the date
                    nextPaymentDate = nextPaymentDate.plusWeeks(1);
                    break;
                case "monthly":
                    // Add one month to the date
                    nextPaymentDate = nextPaymentDate.plusMonths(1);
                    break;
                case "yearly":
                    // Add one year to the date
                    nextPaymentDate = nextPaymentDate.plusYears(1);
                    break;
                // Add more cases if needed prolly not necessary tho
            }
        }
        return nextPaymentDate;
    }


    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Show the dialog and wait for the user to close it
        alert.showAndWait();
    }
    @Override
    public void start(Stage primaryStage) {
        // Load the financial data from storage
        loadFinancialData();

        // Check for upcoming subscriptions
        checkForUpcomingSubscriptions();

        // Create a tab pane for displaying multiple panes
        TabPane tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);

        // Create a tab for income
        Tab incomeTab = new Tab("Income", createIncomePane());
        incomeTab.setClosable(false);

        // Create a tab for expenses
        Tab expenseTab = new Tab("Expenses", createExpensePane());
        expenseTab.setClosable(false);

        // Create a tab for subscriptions
        Tab subscriptionTab = new Tab("Subscriptions", createSubscriptionPane());
        subscriptionTab.setClosable(false);

        // Create a tab for the statement
        Tab statementTab = new Tab("Statement", createStatementPane());
        statementTab.setClosable(false);

        // Add all tabs to the tab pane
        tabPane.getTabs().addAll(incomeTab, expenseTab, subscriptionTab, statementTab);

        // Set the statement tab as the default selected tab
        tabPane.getSelectionModel().select(statementTab);

        // Create a scene for the tab pane
        Scene scene = new Scene(tabPane, 800, 600);

        // Load the CSS styles for the application
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        // Create a root pane for the application
        VBox root = new VBox(tabPane);

        // Set the tab pane to take up all the available vertical space in the root pane
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Create a scene for the root pane
        Scene mainScene = new Scene(root, 1000, 800); // Set initial size to 1000x800

        // Load the CSS styles for the application
        mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        // Set the scene for the primary stage
        primaryStage.setScene(mainScene);

        // Set the title of the primary stage
        primaryStage.setTitle("Budget Tracker");

        // Show the primary stage
        primaryStage.show();
    }


    private VBox createIncomePane() {
        VBox incomePane = new VBox(10);
        incomePane.setPadding(new Insets(10));
        incomePane.setAlignment(Pos.TOP_CENTER);

        // Create label for income
        Label incomeLabel = new Label("Income:");

        // Create table for displaying income data
        TableView<Income> incomeTable = new TableView<>();
        configureIncomeTable(incomeTable);

        // Create input for adding new income
        HBox incomeInput = createIncomeInput(incomeTable);

        // Create buttons for deleting and editing income
        HBox incomeButtons = createIncomeButtons(incomeTable);

        // Add all components to the pane
        incomePane.getChildren().addAll(incomeLabel, incomeTable, incomeInput, incomeButtons);
        return incomePane;
    }

    private VBox createExpensePane() {
        // Create a VBox for the expense pane
        VBox expensePane = new VBox(10);

        // Set the padding and alignment of the pane
        expensePane.setPadding(new Insets(10));
        expensePane.setAlignment(Pos.TOP_CENTER);

        // Create a label for the expenses
        Label expenseLabel = new Label("Expenses:");

        // Create a table for displaying the expenses
        TableView<Expense> expenseTable = new TableView<>();
        configureExpenseTable(expenseTable);

        // Create a HBox for adding new expenses
        HBox expenseInput = createExpenseInput(expenseTable);

        // Create a HBox for deleting and editing expenses
        HBox expenseButtons = createExpenseButtons(expenseTable);

        // Add all components to the pane
        expensePane.getChildren().addAll(expenseLabel, expenseTable, expenseInput, expenseButtons);
        return expensePane;
    }


    private VBox createSubscriptionPane() {
        // Create a VBox for the subscription pane
        VBox subscriptionPane = new VBox(10);

        // Set the padding and alignment of the pane
        subscriptionPane.setPadding(new Insets(10));
        subscriptionPane.setAlignment(Pos.TOP_CENTER);

        // Create a label for the subscriptions
        Label subscriptionLabel = new Label("Subscriptions:");

        // Create a table for displaying the subscriptions
        TableView<Subscription> subscriptionTable = new TableView<>();
        configureSubscriptionTable(subscriptionTable);

        // Create a HBox for adding new subscriptions
        HBox subscriptionInput = createSubscriptionInput(subscriptionTable);

        // Create a HBox for deleting and editing subscriptions
        HBox subscriptionButtons = createSubscriptionButtons(subscriptionTable);

        // Add all components to the pane
        subscriptionPane.getChildren().addAll(subscriptionLabel, subscriptionTable, subscriptionInput, subscriptionButtons);
        return subscriptionPane;
    }

    private void checkBudgetLimit() {
        double totalExpenses = calculateTotalExpenses() + calculateTotalSubscriptions();
        if (budgetLimit != Double.MAX_VALUE) {
            if (totalExpenses > budgetLimit) {
                showNotification("Budget Exceeded", "You have exceeded your budget limit for the month.");
            } else if (totalExpenses >= budgetLimit * 0.9) {
                showNotification("Budget Alert", "You are close to exceeding your budget limit for the month.");
            }
        }
    }

    private VBox createStatementPane() {
        // Create the statement pane
        statementPane = new VBox(10);
        statementPane.setPadding(new Insets(10));
        statementPane.setAlignment(Pos.TOP_CENTER);

        // Create a label for the statement
        Label statementLabel = new Label("Total Statement:");

        // Create the table for displaying the summary
        summaryTable = new TableView<>();
        configureSummaryTable();

        // Create the pie chart
        pieChart = new PieChart();
        updatePieChart();

        // Create the line chart
        lineChart = createLineChart();
        updateLineChart();

        // Create a field for entering the budget limit
        TextField budgetLimitField = new TextField();
        budgetLimitField.setPromptText("Set Budget Limit");

        // Create a button for setting the budget limit
        Button setBudgetLimitButton = new Button("Set Budget Limit");
        setBudgetLimitButton.setOnAction(e -> {
            try {
                // Set the budget limit
                budgetLimit = Double.parseDouble(budgetLimitField.getText());
                showNotification("Budget Limit Set", "Your budget limit has been set to " + budgetLimit);
                updateSummaryTable();
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid input", "Please enter a valid number for the budget limit.");
            }
        });

        // Create a button for clearing the budget limit
        Button clearBudgetLimitButton = new Button("Clear Budget Limit");
        clearBudgetLimitButton.setOnAction(e -> {
            // Clear the budget limit
            budgetLimit = Double.MAX_VALUE;
            showNotification("Budget Limit Cleared", "Your budget limit has been cleared.");
            updateSummaryTable();
        });

        // Create a HBox for the budget limit buttons
        HBox budgetLimitButtons = new HBox(10, setBudgetLimitButton, clearBudgetLimitButton);
        budgetLimitButtons.setAlignment(Pos.CENTER);

        // Create a button for clearing all data
        Button clearAllDataButton = new Button("Clear All Data");
        clearAllDataButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all data?")) {
                // Clear all data
                incomes.clear();
                expenses.clear();
                subscriptions.clear();
                updateAllTables();
                updateStatementPane();
                saveFinancialData();
            }
        });

        // Create a button for exporting data
        Button exportDataButton = new Button("Export Data");
        exportDataButton.setOnAction(e -> exportData());

        // Create a button for importing data
        Button importDataButton = new Button("Import Data");
        importDataButton.setOnAction(e -> importData());

        // Create a HBox for the data buttons
        HBox dataButtons = new HBox(10, exportDataButton, importDataButton);
        dataButtons.setAlignment(Pos.CENTER);

        // Add all components to the statement pane
        statementPane.getChildren().addAll(statementLabel, summaryTable, pieChart, lineChart, budgetLimitField, budgetLimitButtons, clearAllDataButton, dataButtons);
        checkForData(statementPane);
        return statementPane;
    }

    private void configureSummaryTable() {
        // Create a TableColumn for the Category field
        TableColumn<SummaryItem, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        categoryColumn.setPrefWidth(150); // Set preferred width

        // Create a TableColumn for the Amount field
        TableColumn<SummaryItem, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        amountColumn.setPrefWidth(150); // Adjusted preferred width

        // Add the columns to the table
        summaryTable.getColumns().addAll(categoryColumn, amountColumn);

        // Update the table with the current data
        updateSummaryTable();
    }


    // Clear the table and add new items with the current data
    private void updateSummaryTable() {
        summaryTable.getItems().clear();
        // Add the total income
        summaryTable.getItems().add(new SummaryItem("Total Income", String.valueOf(calculateTotalIncome())));
        // Add the total expenses
        summaryTable.getItems().add(new SummaryItem("Total Expenses", String.valueOf(calculateTotalExpenses())));
        // Add the total subscriptions
        summaryTable.getItems().add(new SummaryItem("Total Subscriptions", String.valueOf(calculateTotalSubscriptions())));
        // Add the free income
        summaryTable.getItems().add(new SummaryItem("Free Income", String.valueOf(calculateFreeIncome())));
        // Add the budget limit
        summaryTable.getItems().add(new SummaryItem("Budget Limit", budgetLimit == Double.MAX_VALUE ? "No Budget Limit" : String.valueOf(budgetLimit)));
    }

    private void updateStatementPane() {
        // Update the summary table
        updateSummaryTable();

        // Update the pie chart
        updatePieChart();

        // Update the line chart
        updateLineChart();

        // Update the prompt text
        updatePrompt();

        // Check the budget limit
        checkBudgetLimit();
    }
    private void updatePieChart() {
        // Clear existing data from the pie chart
        pieChart.getData().clear();

        // Create data slices for income, expenses, and subscriptions
        PieChart.Data incomeData = new PieChart.Data("Income", calculateTotalIncome());
        PieChart.Data expenseData = new PieChart.Data("Expenses", calculateTotalExpenses());
        PieChart.Data subscriptionData = new PieChart.Data("Subscriptions", calculateTotalSubscriptions());

        // Add the data slices to the pie chart
        pieChart.getData().addAll(incomeData, expenseData, subscriptionData);

        // Set custom colors for each data slice
        incomeData.getNode().setStyle("-fx-pie-color: #808080;");
        expenseData.getNode().setStyle("-fx-pie-color: #a9a9a9;");
        subscriptionData.getNode().setStyle("-fx-pie-color: #d3d3d3;");

        // Hide the legend on the pie chart
        pieChart.setLegendVisible(false);
    }

    private LineChart<String, Number> createLineChart() {
        // Create the x-axis, which will display the categories
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");

        // Create the y-axis, which will display the amounts
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        // Create a line chart for displaying the financial data
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        // Set the title to an empty string, which will hide the title
        lineChart.setTitle("");
        return lineChart;
    }
    private void updateLineChart() {
        // Clear any existing data from the line chart
        lineChart.getData().clear();

        // Create a series for the financial data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Set the name of the series
        series.setName("Financial Data");

        // Add data points for income, expenses, and subscriptions
        series.getData().add(new XYChart.Data<>("Income", calculateTotalIncome()));
        series.getData().add(new XYChart.Data<>("Expenses", calculateTotalExpenses()));
        series.getData().add(new XYChart.Data<>("Subscriptions", calculateTotalSubscriptions()));

        // Add the series to the line chart
        lineChart.getData().add(series);

        // Set the stroke and fill colors for each data point
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
        // Calculate the free income by subtracting total expenses and subscriptions from total income
        return calculateTotalIncome() - (calculateTotalExpenses() + calculateTotalSubscriptions());
    }
    private void updatePrompt() {
        if (promptLabel == null) {
            // The label that is displayed when there is no financial data
            promptLabel = new Label("No financial data available. Please add some data.");
        }
        if (addDataLink == null) {
            // The hyperlink that is displayed when there is no financial data
            // It navigates to the income tab when clicked
            addDataLink = new Hyperlink("Go to Income Tab");
            addDataLink.setOnAction(e -> {
                TabPane tabPane = (TabPane) statementPane.getScene().getRoot().lookup(".tab-pane");
                tabPane.getSelectionModel().select(0);
            });
        }

        // If there is no financial data, add the label and hyperlink to the statement pane
        // If there is financial data, remove the label and hyperlink from the statement pane
        if (incomes.isEmpty() && expenses.isEmpty() && subscriptions.isEmpty()) {
            if (!statementPane.getChildren().contains(promptLabel)) {
                statementPane.getChildren().addAll(promptLabel, addDataLink);
            }
        } else {
            statementPane.getChildren().removeAll(promptLabel, addDataLink);
        }
    }
    private HBox createIncomeInput(TableView<Income> table) {
        // Create text field for income source
        TextField sourceField = new TextField();
        sourceField.setPromptText("Source");

        // Create text field for income amount
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        // Create add button
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            // Validate input fields
            if (validateInput(sourceField, amountField)) {
                try {
                    // Parse input and create new Income object
                    String source = sourceField.getText();
                    double amount = Double.parseDouble(amountField.getText());
                    Income income = new Income(source, amount);

                    // Add income to list and table
                    incomes.add(income);
                    table.getItems().add(income);

                    // Clear input fields
                    sourceField.clear();
                    amountField.clear();

                    // Animate table addition and update statement pane
                    animateAddition(table);
                    updateStatementPane();

                    // Save financial data
                    saveFinancialData();
                } catch (NumberFormatException ex) {
                    // Show error dialog for invalid input
                    showErrorDialog("Invalid input", "Please enter a valid number for the amount.");
                }
            }
        });

        // Set horizontal grow priority for text fields
        HBox.setHgrow(sourceField, Priority.ALWAYS);
        HBox.setHgrow(amountField, Priority.ALWAYS);

        // Return HBox containing input fields and add button
        return new HBox(10, sourceField, amountField, addButton);
    }

    // Creates a HBox for inputting new expenses
    private HBox createExpenseInput(TableView<Expense> table) {
        // Create text field for expense category
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");

        // Create text field for expense amount
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        // Create add button
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            // Validate input fields
            if (validateInput(categoryField, amountField)) {
                try {
                    // Parse input and create new Expense object
                    String category = categoryField.getText();
                    double amount = Double.parseDouble(amountField.getText());
                    Expense expense = new Expense(category, amount);

                    // Add expense to list and table
                    expenses.add(expense);
                    table.getItems().add(expense);

                    // Clear input fields
                    categoryField.clear();
                    amountField.clear();

                    // Animate table addition and update statement pane
                    animateAddition(table);
                    updateStatementPane();

                    // Save financial data
                    saveFinancialData();
                } catch (NumberFormatException ex) {
                    // Show error dialog for invalid input
                    showErrorDialog("Invalid input", "Please enter a valid number for the amount.");
                }
            }
        });

        // Set horizontal grow priority for text fields
        HBox.setHgrow(categoryField, Priority.ALWAYS);
        HBox.setHgrow(amountField, Priority.ALWAYS);

        // Return HBox containing input fields and add button
        return new HBox(10, categoryField, amountField, addButton);
    }
    private HBox createSubscriptionInput(TableView<Subscription> table) {
        // Create text fields and input controls for subscription
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField costField = new TextField();
        costField.setPromptText("Cost");
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        ComboBox<String> recurrenceComboBox = new ComboBox<>();
        recurrenceComboBox.getItems().addAll("Weekly", "Monthly", "Yearly");
        recurrenceComboBox.setPromptText("Recurrence");
        TextField notificationDaysField = new TextField();
        notificationDaysField.setPromptText("Notify Before (days)");

        // Create add button with action handler
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            // Validate input fields
            if (validateInput(nameField, costField, recurrenceComboBox, notificationDaysField)) {
                try {
                    // Parse input and create new Subscription object
                    String name = nameField.getText();
                    double cost = Double.parseDouble(costField.getText());
                    LocalDate startDate = startDatePicker.getValue();
                    String recurrencePeriod = recurrenceComboBox.getValue();
                    int notificationDays = Integer.parseInt(notificationDaysField.getText());

                    // Add subscription to list and table
                    Subscription subscription = new Subscription(name, cost, startDate, recurrencePeriod, notificationDays);
                    subscriptions.add(subscription);
                    table.getItems().add(subscription);

                    // Clear input fields
                    nameField.clear();
                    costField.clear();
                    startDatePicker.setValue(null);
                    recurrenceComboBox.setValue(null);
                    notificationDaysField.clear();

                    // Animate table addition and update statement pane
                    animateAddition(table);
                    updateStatementPane();
                    saveFinancialData();
                } catch (NumberFormatException ex) {
                    // Show error dialog for invalid input
                    showErrorDialog("Invalid input", "Please enter valid numbers for cost and notification days.");
                }
            }
        });

        // Set horizontal grow priority for text fields
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(costField, Priority.ALWAYS);
        HBox.setHgrow(startDatePicker, Priority.ALWAYS);
        HBox.setHgrow(recurrenceComboBox, Priority.ALWAYS);
        HBox.setHgrow(notificationDaysField, Priority.ALWAYS);

        // Return HBox containing input fields and add button
        return new HBox(10, nameField, costField, startDatePicker, recurrenceComboBox, notificationDaysField, addButton);
    }

    // Checks if all fields are filled in and valid
    private boolean validateInput(TextField nameField, TextField costField, ComboBox<String> recurrenceComboBox, TextField notificationDaysField) {
        // Check if any of the fields are empty
        if (nameField.getText().trim().isEmpty() || costField.getText().trim().isEmpty() || recurrenceComboBox.getValue() == null || notificationDaysField.getText().trim().isEmpty()) {
            showErrorDialog("Invalid input", "All fields must be filled.");
            return false;
        }
        try {
            // Try to parse the cost and notification days
            Double.parseDouble(costField.getText());
            Integer.parseInt(notificationDaysField.getText());
        } catch (NumberFormatException e) {
            // Show error dialog if invalid input
            showErrorDialog("Invalid input", "Please enter valid numbers for cost and notification days.");
            return false;
        }
        // Return true if all valid
        return true;
    }
    private void showErrorDialog(String title, String message) {
        // Create an error alert dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);

        // Set the dialog title
        alert.setTitle(title);

        // Remove the header text for a cleaner look
        alert.setHeaderText(null);

        // Set the content of the dialog with the provided message
        alert.setContentText(message);

        // Show the dialog and wait for the user to close it
        alert.showAndWait();
    }

    private void animateAddition(TableView<?> table) {
        // Create a TranslateTransition to animate the addition of the new row
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), table);

        // Set the starting and ending positions of the transition
        transition.setFromY(-10);
        transition.setToY(0);

        // Set the interpolator for a smoother animation
        transition.setInterpolator(Interpolator.EASE_OUT);

        // Play the animation
        transition.play();
    }

    private HBox createIncomeButtons(TableView<Income> table) {
        // Button for deleting the selected income
        Button deleteButton = getButton(table);

        // Button for clearing all income entries
        Button clearButton = new Button("Clear All");
        Result result = new Result(deleteButton, clearButton);
        result.clearButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all income sources?")) {
                // Clear all incomes from the list and table
                incomes.clear();
                table.getItems().clear();
                // Update the statement pane and save the financial data
                updateStatementPane();
                saveFinancialData();
            }
        });

        // Return an HBox containing the buttons with spacing
        return new HBox(10, result.deleteButton, result.clearButton);
    }

    // Helper class to store the result of a method that returns two buttons
    private static class Result {
        public final Button deleteButton;
        public final Button clearButton;

        public Result(Button deleteButton, Button clearButton) {
            this.deleteButton = deleteButton;
            this.clearButton = clearButton;
        }
    }

    private Button getButton(TableView<Income> table) {
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Income selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Remove the selected income from the list and table
                incomes.remove(selected);
                table.getItems().remove(selected);
                // Update the statement pane and save the financial data
                updateStatementPane();
                saveFinancialData();
            }
        });
        return deleteButton;
    }
    private HBox createExpenseButtons(TableView<Expense> table) {
        // Create a button for deleting the selected expense
        Button deleteButton = getDeleteButton(table);

        // Create a button for clearing all expenses
        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all expenses?")) {
                // Clear all expenses from the list and table
                expenses.clear();
                table.getItems().clear();
                // Update the statement pane and save the financial data
                updateStatementPane();
                saveFinancialData();
            }
        });

        // Return an HBox containing the buttons
        return new HBox(10, deleteButton, clearButton);
    }

    private Button getDeleteButton(TableView<Expense> table) {
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Remove the selected expense from the list and table
                expenses.remove(selected);
                table.getItems().remove(selected);
                // Update the statement pane and save the financial data
                updateStatementPane();
                saveFinancialData();
            }
        });
        return deleteButton;
    }
    private HBox createSubscriptionButtons(TableView<Subscription> table) {
        // Button for deleting the selected subscription
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

        // Button for clearing all subscriptions
        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> {
            if (showConfirmationDialog("Are you sure you want to clear all subscriptions?")) {
                subscriptions.clear();
                table.getItems().clear();
                updateStatementPane();
                saveFinancialData();
            }
        });

        // Layout the buttons
        return new HBox(10, deleteButton, clearButton);
    }
    // Show a confirmation dialog with the specified message
    private boolean showConfirmationDialog(String message) {
        // Create a confirmation alert dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        // Set the dialog title
        alert.setTitle("Confirmation");

        // Remove the header text for a cleaner look
        alert.setHeaderText(null);

        // Set the content of the dialog with the provided message
        alert.setContentText(message);

        // Show the dialog and wait for the user to respond, return true if OK
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }


    private void configureIncomeTable(TableView<Income> table) {
        TableColumn<Income, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSource()));
        sourceColumn.setPrefWidth(150); // Set preferred width

        TableColumn<Income, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        amountColumn.setPrefWidth(100); // Set preferred width

        table.getColumns().addAll(sourceColumn, amountColumn);
        table.setItems(javafx.collections.FXCollections.observableArrayList(incomes));

        // Set custom placeholder text
        table.setPlaceholder(new Label("No income yet!"));
    }


    private void configureExpenseTable(TableView<Expense> table) {
        TableColumn<Expense, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        categoryColumn.setPrefWidth(150); // Set preferred width

        TableColumn<Expense, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        amountColumn.setPrefWidth(100); // Set preferred width

        table.getColumns().addAll(categoryColumn, amountColumn);
        table.setItems(javafx.collections.FXCollections.observableArrayList(expenses));

        // Set custom placeholder text
        table.setPlaceholder(new Label("No expenses yet!"));
    }

    private void configureSubscriptionTable(TableView<Subscription> table) {
        TableColumn<Subscription, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(150); // Set preferred width

        TableColumn<Subscription, Double> costColumn = new TableColumn<>("Cost");
        costColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCost()).asObject());
        costColumn.setPrefWidth(100); // Set preferred width

        TableColumn<Subscription, LocalDate> startDateColumn = new TableColumn<>("Start Date");
        startDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStartDate()));
        startDateColumn.setPrefWidth(120); // Set preferred width

        TableColumn<Subscription, String> recurrenceColumn = new TableColumn<>("Recurrence");
        recurrenceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRecurrencePeriod()));
        recurrenceColumn.setPrefWidth(120); // Set preferred width

        TableColumn<Subscription, Integer> notificationDaysColumn = new TableColumn<>("Notify Before (days)");
        notificationDaysColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getNotificationDays()).asObject());
        notificationDaysColumn.setPrefWidth(160); // Adjusted preferred width

        table.getColumns().addAll(nameColumn, costColumn, startDateColumn, recurrenceColumn, notificationDaysColumn);
        table.setItems(javafx.collections.FXCollections.observableArrayList(subscriptions));

        // Set custom placeholder text
        table.setPlaceholder(new Label("No subscriptions yet!"));
    }

    private void loadFinancialData() {
        File dataFile = new File("financial_data.dat");
        if (!dataFile.exists()) {
            try {
                // Create a new file if the file does not exist
                dataFile.createNewFile();
                // Create empty lists for income, expenses, and subscriptions
                List<Income> emptyIncomes = new ArrayList<>();
                List<Expense> emptyExpenses = new ArrayList<>();
                List<Subscription> emptySubscriptions = new ArrayList<>();
                // Save the empty lists to the file
                try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dataFile.toPath()))) {
                    oos.writeObject(emptyIncomes);
                    oos.writeObject(emptyExpenses);
                    oos.writeObject(emptySubscriptions);
                }
            } catch (IOException e) {
                // Handle any exceptions that occur while creating the file or saving the data
                e.printStackTrace();
            }
        }

        try {
            // Load the financial data from the file
            List<Object> data = DataStorage.loadData();
            // Cast the loaded data to the correct types
            incomes = (List<Income>) data.get(0);
            expenses = (List<Expense>) data.get(1);
            subscriptions = (List<Subscription>) data.get(2);
        } catch (IOException | ClassNotFoundException e) {
            // Handle any exceptions that occur while loading the data
            e.printStackTrace();
        }
    }

    private void saveFinancialData() {
        try {
            // Save the financial data to a file
            DataStorage.saveData(incomes, expenses, subscriptions);
        } catch (IOException e) {
            // Handle any exceptions that occur while saving the data
            e.printStackTrace();
        }
    }
    private void updateAllTables() {
        // Clear and update the summary table
        summaryTable.getItems().clear();
        updateSummaryTable();

        // Locate and clear the income table
        TableView<Income> incomeTable = (TableView<Income>) ((VBox) ((Tab) ((TabPane) summaryTable.getScene().getRoot().lookup(".tab-pane")).getTabs().get(0)).getContent()).getChildren().get(1);
        incomeTable.getItems().clear();

        // Locate and clear the expense table
        TableView<Expense> expenseTable = (TableView<Expense>) ((VBox) ((Tab) ((TabPane) summaryTable.getScene().getRoot().lookup(".tab-pane")).getTabs().get(1)).getContent()).getChildren().get(1);
        expenseTable.getItems().clear();

        // Locate and clear the subscription table
        TableView<Subscription> subscriptionTable = (TableView<Subscription>) ((VBox) ((Tab) ((TabPane) summaryTable.getScene().getRoot().lookup(".tab-pane")).getTabs().get(2)).getContent()).getChildren().get(1);
        subscriptionTable.getItems().clear();
    }

    private void checkForData(VBox statementPane) {
        // Check if there is no financial data available
        if (incomes.isEmpty() && expenses.isEmpty() && subscriptions.isEmpty()) {
            // Create a label to prompt the user to add data
            promptLabel = new Label("No financial data available. Please add some data.");

            // Create a hyperlink that navigates to the Income tab
            addDataLink = new Hyperlink("Go to Income Tab");
            addDataLink.setOnAction(e -> {
                // Locate the tab pane and select the Income tab
                TabPane tabPane = (TabPane) statementPane.getScene().getRoot().lookup(".tab-pane");
                tabPane.getSelectionModel().select(0);
            });

            // Add the prompt label and hyperlink to the statement pane
            statementPane.getChildren().addAll(promptLabel, addDataLink);
        }
    }
    private boolean validateInput(TextField... fields) {
        // Iterate over the fields
        for (TextField field : fields) {
            // If the field is empty, show an error dialog and return false
            if (field.getText().trim().isEmpty()) {
                showErrorDialog("Invalid input", "All fields must be filled.");
                return false;
            }
        }
        // If no errors were found, return true
        return true;
    }
    // Show a confirmation dialog with the given title and message
    private void showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Export financial data to a JSON file
    private void exportData() {
        if (incomes.isEmpty() && expenses.isEmpty() && subscriptions.isEmpty()) {
            showErrorDialog("No Data to Export", "There is no financial data to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Create a Gson object to convert the data to JSON
                Gson gson = GsonUtil.createGson();
                // Create a JsonObject to store the data
                JsonObject data = new JsonObject();
                // Add the incomes to the JsonObject
                data.add("incomes", gson.toJsonTree(incomes));
                // Add the expenses to the JsonObject
                data.add("expenses", gson.toJsonTree(expenses));
                // Add the subscriptions to the JsonObject
                data.add("subscriptions", gson.toJsonTree(subscriptions));
                // Write the data to the file
                gson.toJson(data, writer);
                // Show a confirmation dialog if the export was successful
                showConfirmationDialog("Export Successful", "Data exported successfully.");
            } catch (IOException e) {
                // Show an error dialog if the export failed
                showErrorDialog("Export Failed", "Failed to export data.");
            }
        }
    }
    // Import financial data from a JSON file
    private void importData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (FileReader reader = new FileReader(file)) {
                // Create a Gson object to convert the JSON to the data
                Gson gson = GsonUtil.createGson();
                // Create a JsonObject to store the data
                JsonObject data = gson.fromJson(reader, JsonObject.class);
                // Get the incomes from the JsonObject
                incomes = gson.fromJson(data.get("incomes"), new TypeToken<List<Income>>(){}.getType());
                // Get the expenses from the JsonObject
                expenses = gson.fromJson(data.get("expenses"), new TypeToken<List<Expense>>(){}.getType());
                // Get the subscriptions from the JsonObject
                subscriptions = gson.fromJson(data.get("subscriptions"), new TypeToken<List<Subscription>>(){}.getType());
                // Update all tables
                updateAllTables();
                // Update the statement pane
                updateStatementPane();
                // Show a confirmation dialog if the import was successful
                showConfirmationDialog("Import Successful", "Data imported successfully.");
            } catch (IOException e) {
                // Show an error dialog if the import failed
                showErrorDialog("Import Failed", "Failed to import data.");
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
// to do:
