package com.banana.banana;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        //return realParse(lines);
        Log.v(TAG, "LINES INCOMING");
        Log.v(TAG, lines.toString());
        return realParse(lines);
    }

    // Temporary parsing function that takes the first three elements of lines with 3 or more words
    // This is used to create the checklist while the OCR output is unusable
    private static List<List<String>> fakeParse(List<String> lines) {
        List<List<String>> checklistOutput = new ArrayList<>();
        for(String line: lines) {
            List<String> words = new ArrayList<>(Arrays.asList(line.split(" "))); // ASSUMPTION: ITEMS SEPARATED BY 1 SPACE
            if(words.size() >= 3) {
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
        for(String line: lines) {
            List<String> words = new ArrayList<>(Arrays.asList(line.split(" "))); // ASSUMPTION: ITEMS SEPARATED BY 1 SPACE

            // Determine and store the type of each of those words (price, title, or amount)
            Map<String, String> curTypes = new LinkedHashMap<>();
            for(String word : words) {
                String category = categorizeWord(word);

                // Replace all incorrect, guessable characters for prices
                if(category.equals("Price")) {
                    String newWord = correctErrors(word);
                    curTypes.put(newWord, category);
                } else {
                    curTypes.put(word, category);
                }
            }

            // For each line, store type counts in larger-scoped variable
            wordTypes.add(curTypes);
        }

        // Parse each line-worth of word types to find relevant lines
        for(Map<String, String> typeSet : wordTypes){

            // Only use lines that have a price in them
            //if (typeSet.values().contains("Price")) {

            // Take the elements of a valid line and store them in the following order:
            // 1: Title  2: Quantity  3: Price
            List<String> outputLine = new ArrayList<>();
            outputLine.add(0, "");
            outputLine.add(1, "1");
            outputLine.add(2, "0.00");
            for (String word : typeSet.keySet()) {
                if(typeSet.get(word).equals("Title")) {
                    outputLine.set(0, outputLine.get(0) + " " + word);
                }
                else if (typeSet.get(word).equals("Quantity")) {
                    //outputLine.set(1, word);
                    outputLine.set(0, outputLine.get(0) + " " + word);
                }
                else if (typeSet.get(word).equals("Price")) {
                    // o/O -> 0, i/I -> 1, s/S-> 5
                    outputLine.set(2, word);
                }
            }
            checklistOutput.add(outputLine);
            //}
        }

        return checklistOutput;
    }

    private static String categorizeWord(String word) {
        if(word.contains(String.valueOf('$')) || word.contains(String.valueOf('.')) || word.contains(String.valueOf(','))
                || word.contains(String.valueOf("'")) || word.contains(String.valueOf("-"))) {
            return "Price";
        }
        else if (isNumeric(word)) {
            return "Quantity";
        }
        else {
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
        newWord = newWord.replaceAll("$", "");
        newWord = newWord.replaceAll("o", "0");
        newWord = newWord.replaceAll("O", "0");
        newWord = newWord.replaceAll("s", "5");
        newWord = newWord.replaceAll("S", "5");
        newWord = newWord.replaceAll("i", "1");
        newWord = newWord.replaceAll("I", "1");
        newWord = newWord.replaceAll("l", "1");

//        for (int i = 0; i < newWord.length(); i++){
//            char c = newWord.charAt(i);
//            if(!Character.isDigit(c) && c != '.') {
//                newWord = newWord.replaceAll(String.valueOf(c), "");
//            }
//        }
        return newWord;
    }
}


