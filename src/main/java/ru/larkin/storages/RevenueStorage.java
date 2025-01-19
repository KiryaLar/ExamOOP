package ru.larkin.storages;

import ru.larkin.entities.Revenue;

public class RevenueStorage extends Storage.AbstractStorage<String, Revenue> {
    public RevenueStorage() {
        super("revenues.json", Revenue.class);
    }

    @Override
    public void save(Revenue value) {
        String key = value.getKey();

        if (getEntities().containsKey(key)) {
            Revenue existingRevenue = getEntities().get(key);
            getEntities().put(key, new Revenue(value.category(), existingRevenue.amount() + value.amount(), value.wallet()));
        } else {
            getEntities().put(key, value);
        }
    }
}
