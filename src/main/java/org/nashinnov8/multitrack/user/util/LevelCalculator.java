package org.nashinnov8.multitrack.user.util;

public class LevelCalculator {

    /**
     * Calculates the user's level based on their total EXP.
     * The formula for the minimum EXP required for Level N is:
     * Min EXP = N * (N - 1) * 50
     * 
     * Examples:
     * Level 1: 1 * 0 * 50 = 0 EXP
     * Level 2: 2 * 1 * 50 = 100 EXP
     * Level 3: 3 * 2 * 50 = 300 EXP
     * Level 4: 4 * 3 * 50 = 600 EXP
     * 
     * @param totalExp The user's total EXP
     * @return The calculated level (minimum level is 1)
     */
    public static int calculateLevel(int totalExp) {
        if (totalExp < 0) {
            return 1;
        }

        // We need to find the maximum N such that N * (N - 1) * 50 <= totalExp
        // N^2 - N - (totalExp / 50) <= 0
        // Using quadratic formula: a = 1, b = -1, c = -(totalExp / 50)
        // N = (1 + sqrt(1 - 4(1)(-(totalExp/50)))) / 2
        // N = (1 + sqrt(1 + 4 * totalExp / 50)) / 2
        
        double c = (double) totalExp / 50.0;
        double n = (1.0 + Math.sqrt(1.0 + 4.0 * c)) / 2.0;
        
        return (int) Math.floor(n);
    }
}
