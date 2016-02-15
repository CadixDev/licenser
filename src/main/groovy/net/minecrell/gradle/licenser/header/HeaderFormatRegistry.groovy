package net.minecrell.gradle.licenser.header

import net.minecrell.gradle.licenser.util.CaseInsensitiveMap

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
    void setProperty(String key, Object value) {
        put(key, value)
    }

}
