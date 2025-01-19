package ru.larkin.storages;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ru.larkin.entities.Storable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Storage<K, V> {

    void save(V value);

    boolean delete(K key);

    Optional<V> get(K key);

    void reload();

    List<V> values();

    abstract class AbstractStorage<K, V extends Storable<K>> implements Storage<K, V> {
        private final Gson gson = new Gson();
        private final Map<K, V> entities = new HashMap<>();
        private final Path path;
        private final Class<V> valueType;

        protected AbstractStorage(String fileName, Class<V> valueType) {
            this.path = Paths.get(fileName);
            this.valueType = valueType;
            reload();
        }

        @Override
        public void reload() {
            if (!Files.exists(path)) {
                return;
            }
            try {
                String json = Files.readString(path);
                if (json.isEmpty()) {
                    return;
                }

                Type type = TypeToken.getParameterized(Map.class, Object.class, valueType).getType();
                Map<K, V> loaded = gson.fromJson(json, type);
                if (loaded != null) {
                    entities.clear();
                    entities.putAll(loaded);
                }
            } catch (IOException e) {
                System.out.println("Error reading file " + path + ": " + e.getMessage());
            }
        }

        public void persist() {
            try {
                Type type = TypeToken.getParameterized(Map.class, Object.class, valueType).getType();
                String json = gson.toJson(entities, type);
                Files.writeString(path, json);
            } catch (IOException e) {
                System.out.println("Error writing file " + path + ": " + e.getMessage());
            }
        }

        @Override
        public void save(V value) {
            K key = value.getKey();
            if (entities.containsKey(key)) {
                    System.out.println(key + " already exists");
            } else {
                entities.put(key, value);
            }
        }

        @Override
        public boolean delete(K key) {
            if (!entities.containsKey(key)) {
                System.out.println(key + " doesn't exist");
                return false;
            }
            entities.remove(key);
            return true;
        }

        @Override
        public Optional<V> get(K key) {
            return Optional.ofNullable(entities.get(key));
        }

        public Map<K, V> getEntities() {
            return entities;
        }

        @Override
        public List<V> values() {
            return entities.values().stream().toList();
        }
    }
}
