package net.minecrell.gradle.licenser.header;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;

public interface PreparedHeader {

    boolean check(File file, String charset) throws IOException;

    boolean update(File file, String charset, Closure<File> callback) throws IOException;

}
