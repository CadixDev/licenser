/*
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

package net.minecrell.gradle.licenser.util

class CaseInsensitiveMap<V> extends HashMap<String, V> {

    private static String normalizeKey(Object key) {
        return key.toString().toLowerCase(Locale.ROOT)
    }

    @Override
    boolean containsKey(Object key) {
        return super.containsKey(normalizeKey(key))
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
