package net.minecrell.gradle.licenser.header

class PreparedCommentHeader implements PreparedHeader {

    final CommentHeaderFormat format
    final List<String> lines

    PreparedCommentHeader(CommentHeaderFormat format, List<String> lines) {
        this.format = format
        this.lines = lines
    }

    @Override
    boolean check(File file, String charset) throws IOException {
        boolean result = false
        file.withReader(charset) {
            result = verify(it) != null
        }
        return result
    }

    private boolean verify(Reader reader) {
        def itr = this.lines.iterator()
        String line
        while (itr.hasNext() && (line = reader.readLine()) != null) {
            def expected = itr.next()
            if (line != expected) {
                return false
            }
        }

        return !itr.hasNext()
    }

    @Override
    void update(File file, String charset) throws IOException {

    }

}
