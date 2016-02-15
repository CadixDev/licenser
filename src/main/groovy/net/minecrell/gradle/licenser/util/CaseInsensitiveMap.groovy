package net.minecrell.gradle.licenser.util

class CaseInsensitiveMap<V> extends HashMap<String, V> {

    @Override
    boolean containsKey(Object key) {
        return super.containsKey(key.toString().toLowerCase(Locale.ROOT))
    }

    private static String normalizeKey(Object key) {
        return key.toString().toLowerCase(Locale.ROOT)
    }

    @Override
    V get(Object key) {
        return super.get(normalizeKey(key))
    }

    @Override
    V put(String key, V value) {
        return super.put(normalizeKey(key), value)
    }

    @Override
    V remove(Object key) {
        return super.remove(normalizeKey(key))
    }

    @Override
    void putAll(Map<? extends String, ? extends V> m) {
        m.each this.&put
    }

}
