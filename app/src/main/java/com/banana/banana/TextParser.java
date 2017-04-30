package com.banana.banana;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


/* Created by Leif on 4/19/17. */

public class TextParser {

    public static List<List<String>> parse(String input) {
        Log.v(TAG, "Here comes the input: ");
        Log.v(TAG, input);
        //String testInput = "Item 3.99 \n poop 3.45 \n peepee 4.00";
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
            List<String> words = new ArrayList<>(Arrays.asList(line.split(" "))); // SHIT ASSUMPTION: ITEMS SEPARATED BY 1 SPACE
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
            List<String> words = new ArrayList<>(Arrays.asList(line.split(" "))); // ASSUMPTION: ITEMS SEPARATED BY 2 OR MORE SPACES

            // Determine and store the type of each of those words (price, title, or amount)
            Map<String, String> curTypes = new HashMap<>();
            for(String word : words) {
                String category = categorizeWord(word);
                curTypes.put(word, category);
            }

            // For each line, store type counts in larger-scoped variable
            wordTypes.add(curTypes);
        }

        // Parse each line-worth of word types to find relevant lines
        for(Map<String, String> typeSet : wordTypes){

            // Only use lines that have a price in them
            if (typeSet.values().contains("Price")) {

                // Take the elements of a valid line and store them in the following order:
                // 1: Title  2: Quantity  3: Price
                List<String> outputLine = new ArrayList<>();
                outputLine.add(0, "");
                outputLine.add(1, "1");
                outputLine.add(2, "NULL");
                for (String word : typeSet.keySet()) {
                    if(typeSet.get(word).equals("Title")) {
                        outputLine.set(0, outputLine.get(0) + " " + word);
                    }
                    else if (typeSet.get(word).equals("Quantity")) {
                        //outputLine.set(1, word);
                        outputLine.set(0, outputLine.get(0) + " " + word);
                    }
                    else if (typeSet.get(word).equals("Price")) {
                        outputLine.set(2, word);
                    }
                }
                checklistOutput.add(outputLine);
            }
        }

        return checklistOutput;
    }

    private static String categorizeWord(String word) {
        if(word.contains(String.valueOf('$')) || word.contains(String.valueOf('.'))) {
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
}

