package com.banana.banana;

import android.util.Log;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.lang.*;
import static android.content.ContentValues.TAG;


/* Created by Leif on 4/19/17. */
public class TextParser {

    public static List<List<String>> parse(String input) {
        Log.v(TAG, "Here comes the input: ");
        Log.v(TAG, input);
        List<String> lines = new ArrayList<>(Arrays.asList(input.split("\n")));
        return realParse(lines);
    }

    // Temporary parsing function that takes the first three elements of lines with 3 or more words
    // This is used to create the checklist while the OCR output is unusable
    private static List<List<String>> fakeParse(List<String> lines) {
        List<List<String>> checklistOutput = new ArrayList<>();
        for (String line : lines) {
            List<String> words = new ArrayList<>(Arrays.asList(line.split(" ")));
            if (words.size() >= 3) {
                List<String> outputLine = new ArrayList<>();
                outputLine.add(0, words.get(0));
                outputLine.add(1, words.get(1));
                outputLine.add(2, words.get(2));
                checklistOutput.add(outputLine);
            }
        }
        return checklistOutput;
    }

    // Eventual parsing function to be used when OCR output isn't crap
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
            outputLine.add(0, "");
            outputLine.add(1, "1");
            outputLine.add(2, "0.00");
            for (String word : typeSet.keySet()) {
                if (typeSet.get(word).equals("Title")) {
                    outputLine.set(0, outputLine.get(0) + " " + word.trim()); // (trim removes leading and trailing whitespace)
                } else if (typeSet.get(word).equals("Quantity")) {
                    outputLine.set(0, outputLine.get(0) + " " + word.trim()); // (trim removes leading and trailing whitespace)
                } else if (typeSet.get(word).equals("Price")) {
                    outputLine.set(2, word);
                }
            }
            checklistOutput.add(outputLine);
        }

        return checklistOutput;
    }

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

    private static boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

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
        if (newWord.equals("")) {
            newWord = "0.00";
        }

        // Make sure price is formatted correctly
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String formattedPrice = formatter.format(Double.parseDouble(newWord));
        formattedPrice = formattedPrice.substring(1);

        Log.v(TAG, "Corrected " +  word + " to " + formattedPrice);
        return formattedPrice;
    }
}