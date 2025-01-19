package ru.larkin.entities;

public record User(String login, String password, Wallet wallet) implements Storable<String> {
    @Override
    public String getKey() {
        return login();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}


