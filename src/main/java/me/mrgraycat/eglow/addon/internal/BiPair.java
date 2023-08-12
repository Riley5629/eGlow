package me.mrgraycat.eglow.addon.internal;

import java.util.Objects;

/**
 * A simple utility class to store key-value pairs, ignoring order when comparing equality.
 */
public class BiPair<K, V> {

	private final K key;
	private final V value;

	public BiPair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BiPair<?, ?> biPair = (BiPair<?, ?>) o;
		return (Objects.equals(key, biPair.key) && Objects.equals(value, biPair.value)) || (Objects.equals(key, biPair.value) && Objects.equals(value, biPair.key));
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public String toString() {
		return "BiPair{" + "key=" + key + ", value=" + value + '}';
	}
}