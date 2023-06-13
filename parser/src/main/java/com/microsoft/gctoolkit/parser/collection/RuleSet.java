// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.collection;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class RuleSet<K, V> implements Map<K, V>, Iterable<K> {

    private static class Node<K, V> extends AbstractMap.SimpleImmutableEntry<K,V> {

        private Node<K, V> next;
        private Node<K, V> prev;

        public Node(K key, V value) {
            super(key, value);
        }
    }

    // Instead of a java.util.LinkedList, we use a doubly-linked list of nodes.
    // This allows us to move the most-recently selected node from to the head of
    // the list in O(1) time, instead of O(n) time.
    private Node<K,V> head;

    private final HashMap<K, Node<K,V>> entries;

    public RuleSet() {
        entries = new HashMap<>();
    }

    public V get(Object key) {
        if (key != null) {
            Node<K,V> node = entries.get(key);
            return node.getValue();
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
        Node<K,V> node = new Node<>(key, value);
        if (head == null) {
            head = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        entries.put(key, node);
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
        Collection<V> values = new LinkedList<>();
        for (Node<K,V> node = head; node != null; node = node.next) {
            values.add(node.getValue());
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K,V>> entrySet = new HashSet<>();
        for (Node<K,V> node = head; node != null; node = node.next) {
            entrySet.add(node);
        }
        return entrySet;
    }

    public Stream<Entry<K,V>> stream() {
        return Stream.iterate(head, Objects::nonNull, node -> ((Node<K,V>)node).next);
    }

    private class KeyIterator implements Iterator<K> {
        private Node<K, V> node;

        public KeyIterator() {
            node = head;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public K next() {
            if (node == null) {
                // per the Iterator contract
                throw new NoSuchElementException();
            }
            Node<K, V> current = node;
            node = node.next;
            return current.getKey();
        }

    }

    @Override
    public Iterator<K> iterator() {
        return new KeyIterator();
    }

    public V select(K key) {
        // When a key is selected, move it to the head of the list.
        // The list will be ordered from most-recently selected to least-recently selected.
        // Side note: attempting to sort by most often selected resulted in worse performance.
        final Node<K,V> selected = entries.get(key);
        if (selected != head) {
            if (selected.next != null) {
                Node<K, V> next = selected.next;
                next.prev = selected.prev;
            }
            if (selected.prev != null) {
                Node<K, V> prev = selected.prev;
                prev.next = selected.next;
            }
            head.prev = selected;
            selected.next = head;
            head = selected;
        }
        return selected.getValue();
    }
}
