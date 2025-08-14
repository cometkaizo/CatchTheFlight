package com.cometkaizo.system.app;

import java.lang.reflect.Field;

public class AppSettings {


    public String toString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(getClass().getSimpleName());
            builder.append('{');

            for (Field field : getClass().getFields()) {
                builder.append("\n\t")
                        .append(field.getName())
                        .append(": ")
                        .append(field.get(this));
            }

            if (getClass().getFields().length > 0)
                builder.append('\n');
            builder.append('}');
            return builder.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
