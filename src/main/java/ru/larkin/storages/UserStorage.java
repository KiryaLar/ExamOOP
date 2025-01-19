package ru.larkin.storages;

import ru.larkin.entities.User;

import java.nio.file.Path;

public class UserStorage extends Storage.AbstractStorage<String, User> {

    public UserStorage() {
        super("users.json", User.class);
    }
}
