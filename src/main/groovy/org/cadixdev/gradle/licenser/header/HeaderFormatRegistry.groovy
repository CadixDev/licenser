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

package org.cadixdev.gradle.licenser.header

import org.cadixdev.gradle.licenser.util.CaseInsensitiveMap

class HeaderFormatRegistry extends CaseInsensitiveMap<HeaderFormat> {

    HeaderFormatRegistry() {
        for (HeaderStyle style : HeaderStyle.values()) {
            style.register(this)
        }
    }

    // Note: This is Groovy magic

    HeaderFormat put(String key, HeaderStyle value) {
        return put(key, value.format)
    }

    HeaderFormat put(String key, String value) {
        return put(key, value as HeaderStyle)
    }

    HeaderFormat putAt(String key, HeaderStyle value) {
        return put(key, value)
    }

    HeaderFormat putAt(String key, String value) {
        return put(key, value)
    }

    @Override
    Object getProperty(String key) {
        return get(key)
    }

    @Override
    void setProperty(String key, Object value) {
        put(key, value)
    }

}
