package com.banana.banana;

/* TextParser.java */
/* Author: Leif Grosswiler */
/* Takes the OCR output and turn it into a List of String Lists, with format (Title, Price). */
/* Each title/price pair corresponds to one item on the receipt. */
/* Also performs a number of error-correction techniques to ensure usable output is produced. */

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.lang.*;



public class TextParser {

    // Split the input line-by-line and pass it to the parsing function
    public static List<List<String>> parse(String input) {
        List<String> lines = new ArrayList<>(Arrays.asList(input.split("\n")));
        return realParse(lines);
    }

    // Parsing function that takes the OCR string as input and returns a list of lists
    // of strings with format (Title, Price)
    private static List<List<String>> realParse(List<String> lines) {
        List<List<String>> checklistOutput = new ArrayList<>();
        List<Map<String, String>> wordTypes = new ArrayList<>();

        // Get the set of words in each line of the receipt
        for (String line : lines) {
            List<String> words = new ArrayList<>(Arrays.asList(line.split(" ")));

            // Determine and store the type of each of those words (price, title, or amount)
            Map<String, String> curTypes = new LinkedHashMap<>();
            for (String word : words) {
                String category = categorizeWord(word);

                // Replace all incorrect, guessable characters for prices
                if (category.equals("Price")) {
                    String newWord = correctErrors(word);
                    if (newWord.equals("."))
                        curTypes.put("0.00", category);
                    else
                        curTypes.put(newWord, category);
                } else {
                    curTypes.put(word, category);
                }

            }

            // For each line, store type counts in larger-scoped variable
            wordTypes.add(curTypes);
        }

        // Parse each line-worth of word types to find relevant lines
        for (Map<String, String> typeSet : wordTypes) {
            // Take the elements of a valid line and store them in the following order:
            // 1: Title  2: Quantity  3: Price
            List<String> outputLine = new ArrayList<>();
            outputLine.add(0, ""); // Default title to empty string
            outputLine.add(1, "1"); // Default quantity to 1
            outputLine.add(2, "0.00"); // Default price to 0.00
            for (String word : typeSet.keySet()) {
                if (typeSet.get(word).equals("Title")) {
                    outputLine.set(0, outputLine.get(0) + " " + word.trim()); // (trim removes leading and trailing whitespace)
                }

                // Quantity is currently added to the title
                // This is left to ease potential expansion of the checklist into (Title, Quantity, Price)
                else if (typeSet.get(word).equals("Quantity")) {
                    outputLine.set(0, outputLine.get(0) + " " + word.trim()); // (trim removes leading and trailing whitespace)
                }

                else if (typeSet.get(word).equals("Price")) {
                    outputLine.set(2, word);
                }
            }

            // If there is no title, output an error message in its place
            if (outputLine.get(0).trim().length() == 0) {
                outputLine.set(0, "<ITEM MISSING>");
            }
            checklistOutput.add(outputLine);
        }

        return checklistOutput;
    }

    // Categorize the word as a price if it contains a '$', '.', or other character that Tesseract
    // often misrecognizes a period for
    private static String categorizeWord(String word) {
        if (word.contains(String.valueOf('$')) || word.contains(String.valueOf('.')) || word.contains(String.valueOf(','))
         || word.contains(String.valueOf("'")) || word.contains(String.valueOf("-")) || word.contains(String.valueOf("~"))
         || word.contains(String.valueOf("_"))) {
            return "Price";
        } else if (isNumeric(word)) {
            return "Quantity";
        } else {
            return "Title";
        }
    }

    // Use regex to check if a string is numeric
    private static boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    // Correct common OCR errors for prices, resulting in a usable, properly formatted string
    // that can always be cast to a double value
    private static String correctErrors(String word) {
        String newWord = word;
        newWord = newWord.replaceAll(",", ".");
        newWord = newWord.replaceAll("'", ".");
        newWord = newWord.replaceAll("-", ".");
        newWord = newWord.replaceAll("~", ".");
        newWord = newWord.replaceAll("_", ".");
        newWord = newWord.replaceAll("$", "");
        newWord = newWord.replaceAll("o", "0");
        newWord = newWord.replaceAll("O", "0");
        newWord = newWord.replaceAll("s", "5");
        newWord = newWord.replaceAll("S", "5");
        newWord = newWord.replaceAll("i", "1");
        newWord = newWord.replaceAll("I", "1");
        newWord = newWord.replaceAll("l", "1");
        newWord = newWord.replaceAll("H", "A");
        newWord = newWord.replace("\\", "");
        newWord = newWord.replaceAll("[^\\d.]", "");

        // If there are still multiple periods, give up
        if (newWord.length() - newWord.replace(".", "").length() > 1) {
            newWord = "0.00";
        }

        // If the string is now empty, give it a price of 0.00
        if (newWord.trim().length() == 0) {
            newWord = "0.00";
        }

        // Make sure price is formatted correctly
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String formattedPrice;
        try {
            formattedPrice = formatter.format(Double.parseDouble(newWord));
            formattedPrice = formattedPrice.replaceAll(",", "");
        }
        catch (java.lang.NumberFormatException e) {
            formattedPrice = "$0.00";
        }
        formattedPrice = formattedPrice.substring(1);

        return formattedPrice;
    }
}