package io.reticent.eevee.parser;

import java.util.Stack;

public class Tokenizer {
    private static final char[] WHITESPACE_CHARACTERS = {' ', '\n', '\t'};
    private int currentIndex;
    private Stack<Integer> indexStack;
    private char[] str;

    public Tokenizer(String str) {
        currentIndex = 0;
        indexStack = new Stack<>();
        this.str = str.toCharArray();
    }

    public String next() {
        if (currentIndex >= str.length) {
            return null;
        }

        final char DOUBLE_QUOTE = '"';
        final char SINGLE_QUOTE = '\'';
        final char ESCAPE_CHAR = '\\';

        boolean escape = false;
        boolean double_quote = false;
        boolean single_quote = false;

        StringBuilder token = new StringBuilder();

        for (; currentIndex < str.length; currentIndex++) {
            if (!escape && str[currentIndex] == DOUBLE_QUOTE && !single_quote) {
                double_quote = !double_quote;

                if (token.length() == 0 || (currentIndex + 1 < str.length && !isWhitespace(str[currentIndex + 1]))) {
                    token.append(str[currentIndex]);
                }
            } else if (!escape && str[currentIndex] == SINGLE_QUOTE && !double_quote) {
                single_quote = !single_quote;

                if (token.length() == 0 || (currentIndex + 1 < str.length && !isWhitespace(str[currentIndex + 1]))) {
                    token.append(str[currentIndex]);
                }
            } else if (!escape && !double_quote && !single_quote && isWhitespace(str[currentIndex])) {
                if (token.length() != 0) {
                    break;
                }
            } else if (!escape && str[currentIndex] == ESCAPE_CHAR) {
                escape = true;
            } else {
                token.append(str[currentIndex]);
                escape = false;
            }
        }

        return token.toString();
    }

    public boolean hasNext() {
        return this.currentIndex < str.length;
    }

    public void stash() {
        indexStack.push(currentIndex);
    }

    public void pop() {
        currentIndex = indexStack.pop();
    }

    private boolean isWhitespace(char c) {
        for (char whitespace : Tokenizer.WHITESPACE_CHARACTERS) {
            if (whitespace == c) {
                return true;
            }
        }

        return false;
    }
}
