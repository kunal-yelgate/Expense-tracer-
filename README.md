# 💸 Expense Tracker

A lightweight desktop application built in Java (Swing) to help you log, categorize, and monitor your day-to-day expenses — all from a clean, dark-themed GUI.

---

## Features

- **Add expenses** with a category, amount (in ₹), and date
- **Six built-in categories** — Food 🍔, Transport 🚗, Bills 💡, Shopping 🛍, Health 💊, Others 📦
- **Filter by category** to focus on specific spending areas
- **Delete** a selected transaction or **clear all** entries at once
- **Live summary footer** showing total spend and transaction count
- **Dark UI** with alternating row colors and smooth rounded buttons

---

## Project Structure

```
Expense-tracer-/
├── Expense.java          # Main application (Swing GUI + logic)
├── Buget-tracker         # Supporting budget tracker file
├── whatapp-automation.py # WhatsApp automation script (Python)
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 17 or later (uses `record` types and `java.time`)
- Any standard JDK — no external libraries required

### Run

```bash
# Compile
javac Expense.java

# Run
java ExpenseTracker
```

---

## Usage

1. **Select a category** from the dropdown on the left sidebar.
2. **Enter an amount** in the amount field (positive numbers only).
3. **Set the date** in `YYYY-MM-DD` format (defaults to today).
4. Click **＋ Add Expense** to log the entry.
5. Use the **Filter by Category** dropdown to view spending by type.
6. Select a row and click **✕ Delete Selected** to remove it, or **↺ Clear All** to reset.

---

## Languages

| Language | Share  |
|----------|--------|
| Java     | 97.6%  |
| Python   | 2.4%   |

---

## Author

**Kunal Yelgate** — [github.com/kunal-yelgate](https://github.com/kunal-yelgate)
