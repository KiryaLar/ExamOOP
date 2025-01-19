package ru.larkin.storages;

import ru.larkin.entities.Expense;
import ru.larkin.entities.Revenue;

public class ExpenseStorage extends Storage.AbstractStorage<String, Expense> {
    public ExpenseStorage() {
        super("expenses.json", Expense.class);
    }

    @Override
    public void save(Expense value) {
            String key = value.getKey();

            if (getEntities().containsKey(key)) {
                Expense existingExpense = getEntities().get(key);
                double newLimit = value.limit();
                double oldLimit = getEntities().get(key).limit();

                if (newLimit == 0) {
                    newLimit = oldLimit;
                }

                getEntities().put(key, new Expense(value.category(), existingExpense.amount() + value.amount(), newLimit, value.wallet()));
            } else {
                getEntities().put(key, value);
            }
    }
}
