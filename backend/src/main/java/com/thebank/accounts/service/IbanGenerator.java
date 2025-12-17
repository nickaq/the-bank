package com.thebank.accounts.service;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Utility for generating IBAN numbers.
 * Generates German-style IBAN: DE + check digits + bank code + account number
 */
@Component
public class IbanGenerator {

    private static final String COUNTRY_CODE = "DE";
    private static final String BANK_CODE = "10010010"; // Fictitious bank code
    private static final Random RANDOM = new Random();

    /**
     * Generate a new unique IBAN.
     */
    public String generateIban() {
        // Generate 10-digit account number
        String accountNumber = String.format("%010d", RANDOM.nextLong(10_000_000_000L));
        
        // Basic IBAN structure: DExx + 8 digit bank code + 10 digit account number
        String basicIban = BANK_CODE + accountNumber;
        
        // Calculate check digits (simplified - not full ISO 7064)
        int checkDigits = calculateCheckDigits(COUNTRY_CODE, basicIban);
        
        return COUNTRY_CODE + String.format("%02d", checkDigits) + basicIban;
    }

    private int calculateCheckDigits(String countryCode, String bban) {
        // Move country code to end and add 00 for check digit calculation
        String rearranged = bban + countryCodeToDigits(countryCode) + "00";
        
        // Calculate mod 97
        int remainder = mod97(rearranged);
        
        return 98 - remainder;
    }

    private String countryCodeToDigits(String countryCode) {
        StringBuilder result = new StringBuilder();
        for (char c : countryCode.toCharArray()) {
            result.append(c - 'A' + 10);
        }
        return result.toString();
    }

    private int mod97(String number) {
        int remainder = 0;
        for (int i = 0; i < number.length(); i++) {
            int digit = Character.getNumericValue(number.charAt(i));
            remainder = (remainder * 10 + digit) % 97;
        }
        return remainder;
    }
}
