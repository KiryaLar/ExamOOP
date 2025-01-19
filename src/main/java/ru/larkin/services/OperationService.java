package ru.larkin.services;

import ru.larkin.entities.*;
import ru.larkin.storages.ExpenseStorage;
import ru.larkin.storages.RevenueStorage;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class OperationService {

    private final Scanner scanner = new Scanner(System.in);
    private final RevenueStorage revenues;
    private final ExpenseStorage expenses;

    public OperationService(RevenueStorage revenues, ExpenseStorage expenses) {
        this.revenues = revenues;
        this.expenses = expenses;
    }

    public void addOperation(OperationType type, User user) {

        String category = "";
        double amount = 0;
        double limit = 0;

        boolean isCategoryCorrect = false;
        boolean isAmountCorrect = false;
        boolean isExpenseLimitCorrect = false;

        switch (type) {
            case REVENUE -> {

                while (!isCategoryCorrect) {

                    System.out.println("Enter the revenue category:");
                    category = scanner.nextLine();

                    if (category.isEmpty()) {
                        System.out.println("Category can't be empty.");
                    } else isCategoryCorrect = true;
                }

                while (!isAmountCorrect) {

                    System.out.println("Enter the amount of revenue by category " + category + ":");

                    if (!scanner.hasNextDouble()) {
                        System.out.println("Incorrect input. Please enter a number.");
                        scanner.nextLine();
                        continue;
                    }

                    amount = scanner.nextDouble();
                    scanner.nextLine();

                    if (amount < 0) {
                        System.out.println("The amount of revenue must not be less than 0.");
                    } else isAmountCorrect = true;
                }

                Revenue revenue = new Revenue(category, amount, user.wallet());
                revenues.save(revenue);
                System.out.println("The " + category + "  category has been added to revenues");
                System.out.println("The sum of these revenues: " + amount);

                checkIfExpensesExceedRevenues(user);
            }
            case EXPENSE -> {

                while (!isCategoryCorrect) {

                    System.out.println("Enter the expense category:");
                    category = scanner.nextLine();

                    if (category.isEmpty()) {
                        System.out.println("Category can't be empty.");
                    } else isCategoryCorrect = true;
                }

                while (!isAmountCorrect) {

                    System.out.println("Enter the amount of expense by category " + category + ":");

                    if (!scanner.hasNextDouble()) {
                        System.out.println("Incorrect input. Please enter a number.");
                        scanner.nextLine();
                        continue;
                    }

                    amount = scanner.nextDouble();
                    scanner.nextLine();

                    if (amount < 0) {
                        System.out.println("The amount of expense must not be less than 0.");
                    } else isAmountCorrect = true;
                }

                while (!isExpenseLimitCorrect) {
                    System.out.println("Do you need limit of expense by category " + category + "?");
                    System.out.println("1. Yes");
                    System.out.println("2. No");

                    if (!scanner.hasNextInt()) {
                        System.out.println("Incorrect input. Please enter a number.");
                        scanner.nextLine();
                        continue;
                    }

                    int answer = scanner.nextInt();
                    scanner.nextLine();

                    if (answer != 2 && answer != 1) {
                        System.out.println("Enter correct option.");
                        continue;
                    } else if (answer == 1) {

                        System.out.println("Enter the limit of expense by category " + category + ":");

                        if (!scanner.hasNextDouble()) {
                            System.out.println("Incorrect input. Please enter a number.");
                            scanner.nextLine();
                            continue;
                        }

                        limit = scanner.nextDouble();
                        scanner.nextLine();

                        if (limit < 0) {
                            System.out.println("The amount of limit must not be less than 0.");
                            continue;
                        }
                    }

                    isExpenseLimitCorrect = true;
                }

                Expense expense = new Expense(category, amount, limit, user.wallet());
                expenses.save(expense);

                System.out.println("The " + category + "  category has been added to expenses");
                System.out.println("The sum of these expenses: " + amount);

                if (limit == 0) {
                    System.out.println("Limit wasn't set");
                } else {
                    System.out.println("Limit: " + limit);

                    double totalCategoryExpense = getTotalExpensesByCategory(category);
                    if (totalCategoryExpense > limit) {
                        System.out.println("WARNING: You exceeded the limit for category " + category);
                    }
                }

                checkIfExpensesExceedRevenues(user);
            }
            default -> System.out.println("There isn't such operation.");
        }
    }

    private boolean checkOperationForUser(OperationType type, String category, User user) {
        boolean isExistForCurrentUser = false;

        if (Objects.requireNonNull(type) == OperationType.REVENUE) {
            Revenue revenue = revenues.getEntities().get(category);
            if (revenue != null && revenue.wallet().equals(user.wallet())) {
                isExistForCurrentUser = true;
            }
        } else if (type == OperationType.EXPENSE) {
            Expense expense = expenses.getEntities().get(category);
            if (expense != null && expense.wallet().equals(user.wallet())) {
                isExistForCurrentUser = true;
            }
        }
        return isExistForCurrentUser;
    }

    public void deleteOperation(OperationType type, User user) {

        System.out.println("Enter the category you want to delete.");
        String category = scanner.nextLine();

        if (!checkOperationForUser(type, category, user)) {
            System.out.println("You don't have this revenue/expense category.");
            return;
        }

        switch (type) {
            case REVENUE -> {
                if (revenues.delete(category)) {
                    System.out.println("Category " + category + " in revenues was deleted.");
                } else {
                    System.out.println("There isn't such category.");
                }
            }
            case EXPENSE -> {
                if (expenses.delete(category)) {
                    System.out.println("Category " + category + " in expenses was deleted.");
                }
            }
            default -> System.out.println("There isn't such operation.");
        }
    }

    public void setLimit(String category, User user) {

        if (!checkOperationForUser(OperationType.EXPENSE, category, user)) {
            System.out.println("You don't have this expense category.");
            return;
        }

        Optional<Expense> existingCategory = expenses.get(category);
        if (existingCategory.isEmpty()) {
            System.out.println("There isn't such category.");
            return;
        }

        System.out.println("Enter new limit for category " + category + ": ");
        if (!scanner.hasNextDouble()) {
            System.out.println("Incorrect input. Please enter a number.");
            scanner.nextLine();
            return;
        }
        double newLimit = scanner.nextDouble();
        scanner.nextLine();

        if (newLimit < 0) {
            System.out.println("Limit can't be negative.");
            return;
        }

        Expense oldExpense = existingCategory.get();
        Expense updatedExpense = new Expense(
                oldExpense.category(),
                oldExpense.amount(),
                newLimit,
                oldExpense.wallet()
        );
        expenses.save(updatedExpense);

        System.out.println("Limit for category '" + category + "' updated to " + newLimit);
    }

    public Map<String, Expense> getAllExpenses(User user) {
        Map<String, Expense> mapForCurrentUser = expenses.getEntities()
                .entrySet().stream()
                .filter(entry -> entry.getValue().wallet().equals(user.wallet()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        return mapForCurrentUser;
    }

    public Map<String, Revenue> getAllRevenues(User user) {
        Map<String, Revenue> mapForCurrentUser = revenues.getEntities()
                .entrySet().stream()
                .filter(entry -> entry.getValue().wallet().equals(user.wallet()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        return mapForCurrentUser;
    }

    public Optional<Expense> getExpense(String category, User user) {
        Optional<Expense> expense = expenses.get(category);
        if (expense.isPresent() && expense.get().wallet().equals(user.wallet())) {
            return expense;
        } else {
            throw new RuntimeException();
        }
    }

    public Optional<Revenue> getRevenue(String category, User user) {
        Optional<Revenue> revenue = revenues.get(category);
        if (revenue.isPresent() && revenue.get().wallet().equals(user.wallet())) {
            return revenue;
        } else {
            throw new RuntimeException();
        }
    }

    public double getRemainingBudget(Expense expense) {
        if (expense.limit() == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double totalCategoryExpenses = getTotalExpensesByCategory(expense.category());
        return expense.limit() - totalCategoryExpenses;
    }

    public double getTotalExpenses(User user) {
        return expenses.values().stream()
                .filter(entry -> entry.wallet().equals(user.wallet()))
                .mapToDouble(Expense::amount)
                .sum();
    }

    public double getTotalRevenues(User user) {
        return revenues.values().stream()
                .filter(entry -> entry.wallet().equals(user.wallet()))
                .mapToDouble(Revenue::amount)
                .sum();
    }

    public double getTotalExpensesByCategory(String category) {
        return expenses.values().stream()
                .filter(e -> e.category().equals(category))
                .mapToDouble(Expense::amount)
                .sum();
    }

    private void checkIfExpensesExceedRevenues(User user) {
        double totalExp = getTotalExpenses(user);
        double totalRev = getTotalRevenues(user);
        if (totalExp > totalRev) {
            System.out.println("WARNING: Your total expenses (" + totalExp + ") exceed total revenues (" + totalRev + ")!");
        }
    }
}
