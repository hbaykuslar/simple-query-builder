package io.simplequerybuilder.utils;

import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

public class TestUtils {


    public static String inlined(String expectedString) {
        return Pattern.compile("\n").splitAsStream(expectedString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(joining(" "));
    }
}
