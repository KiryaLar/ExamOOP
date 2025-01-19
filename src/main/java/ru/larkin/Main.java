package ru.larkin;

import ru.larkin.entities.*;
import ru.larkin.services.OperationService;
import ru.larkin.services.UserService;
import ru.larkin.services.WalletService;
import ru.larkin.storages.ExpenseStorage;
import ru.larkin.storages.RevenueStorage;
import ru.larkin.storages.UserStorage;
import ru.larkin.storages.WalletStorage;

import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final UserStorage userStorage = new UserStorage();
    private static final UserService userService = new UserService(userStorage);
    private static final RevenueStorage revenueStorage = new RevenueStorage();
    private static final ExpenseStorage expenseStorage = new ExpenseStorage();
    private static final WalletStorage walletStorage = new WalletStorage();
    private static final OperationService operationService = new OperationService(revenueStorage, expenseStorage);
    private static final WalletService walletService = new WalletService(walletStorage);

    private static boolean isAppRunning = true;
    private static boolean isUserLogin = true;
    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser;

    public static void main(String[] args) {

        System.out.println("Personal Finance Management App");

        while (isAppRunning) {
            printStartOptions();

            if (!scanner.hasNextInt()) {
                System.out.println("Incorrect input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1 -> createUser();
                case 2 -> loginUser();
                case 3 -> exitApp();
                default -> System.out.println("No such option");
            }
        }
    }

    private static void printStartOptions() {
        System.out.println("\nAvailable options:");
        System.out.println("1. Create a new user");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.println("Select an option");
    }

    private static void createUser() {
        String login = "";
        String password = "";
        boolean isLoginUnique = false;
        boolean isPasswordSatisfy = false;

        while (!isLoginUnique) {

            System.out.println("Enter login:");
            login = scanner.nextLine();

            if (login.isEmpty()) {
                System.out.println("Login can't be empty.");
            } else if (userService.checkUser(login)) {
                System.out.println("This username already exists.");
            } else isLoginUnique = true;
        }

        while (!isPasswordSatisfy) {
            System.out.println("Enter password (at least 6 characters long):");
            password = scanner.nextLine();

            if (password.length() < 6) {
                System.out.println("Password must be longer.");
            } else isPasswordSatisfy = true;
        }

        Wallet wallet = walletService.createWallet(0);
        userService.createUser(login, password, wallet);

        System.out.println("The user has been successfully created!");
    }

    private static void loginUser() {
        String login = "";
        String password;
        boolean isLoginExist = false;
        boolean isPasswordCorrect = false;

        while (!isLoginExist) {

            System.out.println("Enter login or write \"exit\" to go to the main menu:");
            login = scanner.nextLine();

            if (login.equalsIgnoreCase("exit")) {
                return;
            } else if (login.isEmpty()) {
                System.out.println("Login can't be empty.");
            } else if (!userService.checkUser(login)) {
                System.out.println("There is no such user.");
            } else isLoginExist = true;
        }

        while (!isPasswordCorrect) {

            System.out.println("Enter password:");
            password = scanner.nextLine();

            if (password.length() < 6) {
                System.out.println("Password must be longer.");
            } else if (!userService.checkPassword(login, password)) {
                System.out.println("Incorrect password!");
            } else isPasswordCorrect = true;
        }

        currentUser = userService.getUser(login).get();
        System.out.println("You are logged in as a user: " + currentUser.login());

        userOptions();
    }

    private static void userOptions() {

        while (isUserLogin) {
            System.out.println("\nAvailable options");
            System.out.println("1. Add revenue");
            System.out.println("2. Add expense");
            System.out.println("3. Delete revenue");
            System.out.println("4. Delete expense");
            System.out.println("5. Set/Update expense limit");
            System.out.println("6. Show all statistics");
            System.out.println("7. Show detail by revenue category");
            System.out.println("8. Show detail by expense category");
            System.out.println("9. Log out");
            System.out.println("Select an option");

            if (!scanner.hasNextInt()) {
                System.out.println("Incorrect input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1 -> addOperation(OperationType.REVENUE);
                case 2 -> addOperation(OperationType.EXPENSE);
                case 3 -> deleteOperation(OperationType.REVENUE, currentUser);
                case 4 -> deleteOperation(OperationType.EXPENSE, currentUser);
                case 5 -> setExpenseLimit();
                case 6 -> showAllStatistics(currentUser);
                case 7 -> showDetailByCategory(currentUser, OperationType.REVENUE);
                case 8 -> showDetailByCategory(currentUser, OperationType.EXPENSE);
                case 9 -> logoutUser();
                default -> System.out.println("No such option");
            }
        }
    }

    private static void addOperation(OperationType type) {

        operationService.addOperation(type, currentUser);
    }

    private static void deleteOperation(OperationType type, User user) {
        operationService.deleteOperation(type, user);
    }

    private static void setExpenseLimit() {
        System.out.println("Enter category to update limit: ");
        String category = scanner.nextLine();
        operationService.setLimit(category, currentUser);
    }

    private static void showAllStatistics(User user) {
        double totalRev = operationService.getTotalRevenues(currentUser);
        double totalExp = operationService.getTotalExpenses(currentUser);
        System.out.println("--- Overall Statistics ---");
        System.out.println("Total Revenues: " + totalRev);
        System.out.println("Total Expenses: " + totalExp);
        if (totalExp > totalRev) {
            System.out.println("Warning: Expenses exceed revenues!");
        }

        System.out.println("\n--- Category Details (Expenses) ---");
        for (Expense e : operationService.getAllExpenses(user).values()) {
            System.out.println("Category: " + e.category() + ", Amount: "
                    + e.amount() + ", Limit: " + e.limit());
        }

        System.out.println("\n--- Category Details (Revenues) ---");
        for (Revenue r : operationService.getAllRevenues(user).values()) {
            System.out.println("Category: " + r.category() + ", Amount: " + r.amount());
        }
    }

    private static void showDetailByCategory(User user, OperationType type) {
        System.out.println("Enter category to show details:");
        String category = scanner.nextLine();

        switch (type) {
            case EXPENSE -> {
                Optional<Expense> expense = Optional.empty();
                try {
                    expense = operationService.getExpense(category, user);
                } catch (Exception e) {
                    System.out.println("You haven't expense category " + category);
                }
                Expense e = expense.get();
                System.out.println("=== Expense info ===");
                System.out.println("Category: " + e.category());
                System.out.println("Amount: " + e.amount());
                System.out.println("Limit: " + e.limit());
                double remaining = operationService.getRemainingBudget(e);
                if (Double.isFinite(remaining)) {
                    System.out.println("Remaining limit: " + remaining);
                    if (remaining < 0) {
                        System.out.println("WARNING: Limit exceeded for category: " + e.category());
                    }
                } else {
                    System.out.println("No limit set for this category.");
                }

            }
            case REVENUE -> {
                Optional<Revenue> revenue = Optional.empty();
                try {
                    revenue = operationService.getRevenue(category, user);
                } catch (Exception e) {
                    System.out.println("You haven't revenue category " + category);
                }
                    Revenue r = revenue.get();
                    System.out.println("=== Revenue info ===");
                    System.out.println("Category: " + r.category());
                    System.out.println("Amount: " + r.amount());
            }
        }
    }

    private static void logoutUser() {
        currentUser = null;
        isUserLogin = false;
    }

    private static void exitApp() {
        userStorage.persist();
        walletStorage.persist();
        revenueStorage.persist();
        expenseStorage.persist();
        System.out.println("Exiting the app...");
        isAppRunning = false;
    }
}

