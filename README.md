# Budget Tracker Application

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-19-lightgrey)
![License](https://img.shields.io/badge/License-MIT-green)

A comprehensive personal finance manager with income/expense tracking, subscription management, and visual analytics. Created for the IB Computer Science HL Internal Assessment.

## Features

📊 **Financial Dashboard**
- Real-time income/expense summaries
- Interactive pie charts and line graphs
- Budget limit alerts (90% and 100% thresholds)

💰 **Transaction Management**
- Track one-time incomes and expenses
- Recurring subscription tracking with payment reminders
- Custom notification days before renewal

📈 **Data Visualization**
- Expense category breakdown (Pie Chart)
- Financial trend analysis (Line Chart)
- Color-coded UI for quick status recognition

🔄 **Data Portability**
- JSON import/export functionality
- Persistent local storage
- Cross-session data preservation

## Technologies Used

- **Core**: Java 17+
- **UI**: JavaFX 19
- **Charts**: JavaFX Charts API
- **Data**: GSON for JSON serialization
- **Build**: Maven

## Installation

1. **Prerequisites**:
   - JDK 17 or later
   - JavaFX 19 SDK

2. **Run from source**:
   ```bash
   git clone https://github.com/cuzohh/budgetapp
   cd budgetapp
   mvn clean javafx:run
