package com.cometkaizo.io;

import com.cometkaizo.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.cometkaizo.io.KeyValues.*;

public class KeyValueReader extends BufferedReader {
    private final Reader reader;
    private Map<String, String> entries = null;

    public KeyValueReader(Reader reader) {
        super(reader);
        this.reader = reader;
    }

    public String read(String key) throws IOException {
        if (entries == null) readEntries();
        String result = entries.get(key);
        if (result == null) throw new NoSuchElementException("Unknown key '" + key + "'; available entries are: \n" + entries);
        return result;
    }

    private void readEntries() throws IOException {
        entries = new HashMap<>(0);
        BufferedReader reader = new BufferedReader(this.reader);
        String key = null;
        StringBuilder section = new StringBuilder();
        int prevChar = -1;
        int currentChar;

        while ((currentChar = reader.read()) != -1) {
            if (currentChar == '\r') continue;
            boolean escaped = prevChar == '\\';

            if (!escaped && currentChar == SEPARATOR) {
                if (section.isEmpty()) throw new InvalidObjectException("Illegal separator '" + SEPARATOR + "' with no key");
                if (key != null) throw new InvalidObjectException("Cannot have multiple keys; existing key: '" + key + "', illegal second key: '" + section + "'");

                key = section.toString();

                section = new StringBuilder();
            } else if (!escaped && currentChar == ENTRY_SEPARATOR) {
                tryAddEntry(key, section.toString());

                key = null;
                section = new StringBuilder();
            } else {
                section.append(Character.toString(currentChar));
            }
            prevChar = currentChar;
        }
        if (key != null) tryAddEntry(key, section.toString());

        Main.log("entries: \n" + entries);
    }

    private void tryAddEntry(String key, String value) throws InvalidObjectException {
        String unescapedValue = unescape(value);
        if (key == null) throw new InvalidObjectException("Illegal end of entry with no key; value: " + unescapedValue);
        String unescapedKey = unescape(key);

        String previousValue = entries.putIfAbsent(unescapedKey, unescapedValue);
        if (previousValue != null) throw new InvalidObjectException("Duplicate key '" + unescapedKey + "' for values '" + previousValue + "' and '" + unescapedValue + "'");
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return reader.read();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
