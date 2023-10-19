/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.cadixdev.gradle.licenser.util

class CaseInsensitiveMap<V> {
    @Delegate(includes = ["size", "isEmpty", "containsValue", "clear", "keySet", "values", "entrySet", "equals", "hashCode"])
    private final Map<String, V> map = new HashMap<>();

    private static String normalizeKey(Object key) {
        return key.toString().toLowerCase(Locale.ROOT)
    }

    boolean containsKey(Object key) {
        return map.containsKey(normalizeKey(key))
    }

    V get(Object key) {
        return map.get(normalizeKey(key))
    }

    V put(String key, V value) {
        return map.put(normalizeKey(key), value)
    }

    V remove(Object key) {
        return map.remove(normalizeKey(key))
    }

    void putAll(Map<? extends String, ? extends V> m) {
        m.each this.&put
    }
}
