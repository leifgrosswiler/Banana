package com.banana.banana;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/* Created by Leif on 4/19/17. */

public class TextParser {

    public static List<List<String>> parse(String input) {
        List<String> lines = new ArrayList<>(Arrays.asList(input.split("\n")));
        List<Map<String, String>> wordTypes = new ArrayList<>();
        List<List<String>> checklistOutput = new ArrayList<>();

        // Get the set of words in each line of the receipt
        for(String line: lines) {
            List<String> words = new ArrayList<>(Arrays.asList(line.split("  "))); // ASSUMPTION: ITEMS SEPARATED BY 2 OR MORE SPACES

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
                for (String word : typeSet.keySet()) {
                    if(typeSet.get(word).equals("Title")) {
                        outputLine.add(0, word);
                    }
                    else if (typeSet.get(word).equals("Quantity")) {
                        outputLine.add(1, word);
                    }
                    else if (typeSet.get(word).equals("Price")) {
                        outputLine.add(2, word);
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
