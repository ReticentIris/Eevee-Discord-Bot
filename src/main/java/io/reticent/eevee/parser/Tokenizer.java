package io.reticent.eevee.parser;

import io.reticent.eevee.configuration.ParserConfiguration;

import java.util.Stack;

public class Tokenizer {
    private int currentIndex;
    private Stack<Integer> indexStack;
    private char[] str;

    private final char DOUBLE_QUOTE = '"';
    private final char SINGLE_QUOTE = '\'';
    private final char ESCAPE_CHAR = '\\';

    public Tokenizer(String str) {
        currentIndex = 0;
        indexStack = new Stack<>();
        this.str = str.toCharArray();
    }

    public String next() {
        if (currentIndex >= str.length) {
            return null;
        }

        boolean escape = false;
        boolean double_quote = false;
        boolean single_quote = false;

        StringBuilder token = new StringBuilder();

        for (; currentIndex < str.length; currentIndex++) {
            if (!escape && str[currentIndex] == DOUBLE_QUOTE && !single_quote) {
                double_quote = !double_quote;
            } else if (!escape && str[currentIndex] == SINGLE_QUOTE && !double_quote) {
                single_quote = !single_quote;
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
        for (char whitespace : ParserConfiguration.WHITESPACE_CHARACTERS) {
            if (whitespace == c) {
                return true;
            }
        }

        return false;
    }
}
