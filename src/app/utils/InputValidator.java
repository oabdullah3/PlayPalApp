package app.utils;

import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InputValidator {

    // Scanner instance, shared statically to avoid resource leaks
    private static final Scanner scanner = new Scanner(System.in);
    

    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        // Reads the line and returns it, even if it's empty (trimmed)
        return scanner.nextLine().trim(); 
    }
    
    /**
     * Reads a non-empty String input from the console.
     */
    public static String readString(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        } while (input.isEmpty());
        return input;
    }

    /**
     * Reads an integer input, ensuring the user enters a valid number.
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                // Use nextLine() and parse to handle the InputMismatchException cleanly
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    /**
     * Reads a double input, ensuring the user enters a valid decimal number.
     */
    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numerical amount.");
            }
        }
    }

    /**
     * Reads a LocalDateTime object in the required format (YYYY-MM-DD HH:MM).
     */
    public static LocalDateTime readDateTime(String prompt) {
        String pattern = "yyyy-MM-dd HH:mm";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        
        while (true) {
            System.out.printf("%s (Format: YYYY-MM-DD HH:MM): ", prompt);
            String input = scanner.nextLine().trim();
            
            try {
                LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
                if (dateTime.isBefore(LocalDateTime.now())) {
                    System.out.println("Time must be in the future. Please try again.");
                    continue;
                }
                return dateTime;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date and time format. Please use the format YYYY-MM-DD HH:MM.");
            }
        }
    }
}