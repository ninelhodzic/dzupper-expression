package org.datazup.expression;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import java.util.*;

/*import java.util.regex.Matcher;
import java.util.regex.Pattern;*/

/**
 * Created by admin@datazup on 3/14/16.
 */
public class Tokenizer {
    private Pattern pattern;
    private String tokenDelimiters;
    private boolean trimTokens;

    public Tokenizer(List<String> delimiters) {
        if (this.onlyOneChar(delimiters)) {
            StringBuilder builder = new StringBuilder();
            Iterator i$ = delimiters.iterator();

            while (i$.hasNext()) {
                String delimiter = (String) i$.next();
                builder.append(delimiter);
            }

            this.tokenDelimiters = builder.toString();
        } else {
            this.pattern = delimitersToRegexp(delimiters);
        }

        this.trimTokens = true;
    }

    public boolean isTrimTokens() {
        return this.trimTokens;
    }

    public void setTrimTokens(boolean trimTokens) {
        this.trimTokens = trimTokens;
    }

    private boolean onlyOneChar(List<String> delimiters) {
        Iterator i$ = delimiters.iterator();

        String delimiter;
        do {
            if (!i$.hasNext()) {
                return true;
            }

            delimiter = (String) i$.next();
        } while (delimiter.length() == 1);

        return false;
    }

    private static Pattern delimitersToRegexp(List<String> delimiters) {
        Collections.sort(delimiters, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return -o1.compareTo(o2);
            }
        });

        StringBuilder result = new StringBuilder();
        result.append('(');
        //result.append("'#.*?#'"); //special case
        result.append("('#(?:[^'##']+|(?1))*+#')"); // added library: florianingerl - suppor for "recursion" in Java regex (?1)

        result.append('|');
        result.append("('(?!#).*?(?!#)')");
        String delim;
        for (Iterator i$ = delimiters.iterator(); i$.hasNext(); result.append("\\Q").append(delim).append("\\E")) {
            delim = (String) i$.next();
            if (result.length() != 1) {
                // add to not match expressions inside the '' e.g. 'yyyy-MM-dd' (this should be literal)
                // result.append("(?![^']*')");
                result.append('|');
            }
        }

        result.append(')');

        return Pattern.compile(result.toString(), Pattern.DOTALL);
    }

    private void addToTokens(List<String> tokens, String token) {
        if (this.trimTokens) {
            token = token.trim();
        }

        if (!token.isEmpty()) {
            tokens.add(token);
        }
    }

    private Iterator<String> internalTokenizer(String string) {
        ArrayList res = new ArrayList();
        Matcher m = this.pattern.matcher(string);

        int pos;
        for (pos = 0; m.find(); pos = m.end()) {
            if (pos != m.start()) {
                this.addToTokens(res, string.substring(pos, m.start()));
            }

            this.addToTokens(res, m.group());
        }

        if (pos != string.length()) {
            this.addToTokens(res, string.substring(pos));
        }

        return res.iterator();
    }

    private Iterator<String> specialCaseTokenizer(String string) {
        Iterator<String> matched = internalTokenizer(string);
        return matched;
    }

    public Iterator<String> tokenize(String string) {
        if (this.pattern != null) {
            return specialCaseTokenizer(string);
        } else {
            return new Tokenizer.StringTokenizerIterator(new StringTokenizer(string, this.tokenDelimiters, true));
        }
    }

    private class StringTokenizerIterator implements Iterator<String> {
        private StringTokenizer tokens;
        private String nextToken = null;

        public StringTokenizerIterator(StringTokenizer tokens) {
            this.tokens = tokens;
        }

        public boolean hasNext() {
            return this.buildNextToken();
        }

        public String next() {
            if (!this.buildNextToken()) {
                throw new NoSuchElementException();
            } else {
                String token = this.nextToken;
                this.nextToken = null;
                return token;
            }
        }

        private boolean buildNextToken() {
            while (this.nextToken == null && this.tokens.hasMoreTokens()) {
                this.nextToken = this.tokens.nextToken();
                if (Tokenizer.this.trimTokens) {
                    this.nextToken = this.nextToken.trim();
                }

                if (this.nextToken.isEmpty()) {
                    this.nextToken = null;
                }
            }

            return this.nextToken != null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
