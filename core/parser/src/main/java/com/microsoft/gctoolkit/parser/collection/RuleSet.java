// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuleSet<K, V> implements Map<K, V>, Iterable<K> {

    private HashMap<K, V> entries;
    private LinkedList<K> keys;

    public RuleSet() {
        entries = new HashMap<>();
        keys = new LinkedList<>();
    }

    public V get(Object key) {
        if (key != null) {
            return entries.get(key);
        }
        return null;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V put(K key, V value) {
        entries.put(key, value);
        keys.offer(key);
        return value;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return entries.keySet();
    }

    @Override
    public Collection<V> values() {
        return entries.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entries.entrySet();
    }

    public List<K> keys() {
        return keys;
    }

    @Override
    public Iterator<K> iterator() {
        return keys.iterator();
    }
}
