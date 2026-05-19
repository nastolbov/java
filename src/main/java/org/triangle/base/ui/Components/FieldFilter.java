package org.triangle.base.ui.Components;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

// Универсальный фильтр: T — тип сущности (Customer, Genre...), V — тип поля для фильтрации
public class FieldFilter<T, V> {
    private List<T> items;
    private Set<V> availableValues;
    private Function<T, V> fieldExtractor; // лямбда, достающая нужное поле из T

    public FieldFilter(List<T> items, Function<T, V> fieldExtractor) {
        this.items = items;
        this.fieldExtractor = fieldExtractor;
        // собираем уникальные значения поля для выпадашки ComboBox
        this.availableValues = items.stream()
                .map(fieldExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    // возвращает отсортированный список значений для ComboBox
    public List<V> getAvailableValues() {
        List<V> list = new ArrayList<>(availableValues);
        Collections.sort((List) list);
        return list;
    }

    public List<T> filterByValue(V value) {
        return items.stream()
                .filter(item -> {
                    V fieldValue = fieldExtractor.apply(item);
                    return Objects.equals(fieldValue, value);
                })
                .collect(Collectors.toList());
    }
}
