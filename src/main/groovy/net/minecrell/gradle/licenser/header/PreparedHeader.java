package net.minecrell.gradle.licenser.header;

import java.io.File;
import java.io.IOException;

public interface PreparedHeader {

    boolean check(File file, String charset) throws IOException;

    boolean update(File file, String charset) throws IOException;

}
