package net.minecrell.gradle.licenser.header;

public interface HeaderFormat {

    String getName();

    PreparedHeader prepare(String text, boolean newLine);

}
