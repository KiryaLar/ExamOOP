package ru.larkin.services;

import ru.larkin.entities.User;
import ru.larkin.entities.Wallet;
import ru.larkin.storages.UserStorage;

import java.util.Optional;

public class UserService {

    private final UserStorage users;

    public UserService(UserStorage users) {
        this.users = users;
    }

    public boolean checkUser(String login) {
        return users.getEntities().containsKey(login);
    }

    public Optional<User> getUser(String login) {
        return users.get(login);
    }

    public boolean checkPassword(String login, String password) {
        User user = users.get(login).get();
        String realPassword = user.password();
        return realPassword.equals(password);
    }

    public void createUser(String login, String password, Wallet wallet) {
        User user = new User(login, password, wallet);
        users.save(user);
    }

    public void createUser(String login, String password, User user) {
        users.save(user);
    }

}
