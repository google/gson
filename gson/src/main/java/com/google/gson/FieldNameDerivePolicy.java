package com.google.gson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rqg on 28/04/2018.
 */
public enum FieldNameDerivePolicy implements FieldNameDeriveStrategy {
    /**
     * Using this naming policy with Gson will ensure that the field name is
     * unchanged.
     */
    IDENTITY() {
        @Override
        public List<String> deriveNames(Field f, List<String> fieldNames) {
            return fieldNames;
        }
    },

    /**
     * 驼峰和下划线形式兼容。
     * 策略如下：
     * 1。 如果包含 '_' 则认为是下划线形式，不关心首字母大小写
     * 2。 如果不包含 '_' 有大写字母则认为是 Camel case
     */
    CAMEL_AND_UNDERSCORES() {
        @Override
        public List<String> deriveNames(Field f, List<String> fieldNames) {
            List<String> deriveNames = new ArrayList<String>();
            for (String fname : fieldNames) {
                if (fname.contains("_")) {
                    //underscore case
                    deriveNames.add(toCamelName(fname));
                } else if (!fname.equals(fname.toLowerCase())) {
                    // camel case
                    deriveNames.add(toUnderscoreName(fname));
                }
            }

            return deriveNames;
        }
    };

    static String toCamelName(String name) {
        StringBuilder fieldNameBuilder = new StringBuilder();
        int index = 0;
        char c;
        int length = name.length();

        boolean needUpper = false;

        while (index < length) {
            c = name.charAt(index++);

            if (Character.isLetter(c)) {
                if (needUpper) {
                    needUpper = false;
                    c = Character.toUpperCase(c);
                }
            } else {
                needUpper = true;
                continue;
            }

            fieldNameBuilder.append(c);
        }
        return fieldNameBuilder.toString();
    }

    /**
     * add '_' before upper case character, and convert character to lower case
     *
     * @param name
     * @return
     */
    static String toUnderscoreName(String name) {
        StringBuilder fieldNameBuilder = new StringBuilder();
        int index = 0;
        char c;
        int length = name.length();

        while (index < length) {
            c = name.charAt(index++);

            if (Character.isUpperCase(c)) {
                fieldNameBuilder.append('_');
                c = Character.toLowerCase(c);
            }

            fieldNameBuilder.append(c);
        }
        return fieldNameBuilder.toString();
    }
}
