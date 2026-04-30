import java.util.ArrayList;

public class BudgetTracker {
    private ArrayList<Double> expenses;
    private ArrayList<Double> incomes;
    private double balance;

    public BudgetTracker() {
        expenses = new ArrayList<Double>();
        incomes = new ArrayList<Double>();
        balance = 0;
    }

    public void addExpense(double expense) {
        expenses.add(expense);
        balance -= expense;
    }

    public void addIncome(double income) {
        incomes.add(income);
        balance += income;
    }

    public double getBalance() {
        return balance;
    }

    public void printExpenses() {
        System.out.println("Expenses: ");
        for (double expense : expenses) {
            System.out.println(expense);
        }
    }

    public void printIncomes() {
        System.out.println("Incomes: ");
        for (double income : incomes) {
            System.out.println(income);
        }
    }
}
