package com.cdac.constant;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // =========================
    // Item Configuration
    // =========================
    public static final int MAX_ITEM_IMAGES = 3;

    public static final int MIN_OWNERSHIP_QUESTIONS = 3;

    public static final int MAX_OWNERSHIP_QUESTIONS = 5;

    // =========================
    // Matching Configuration
    // =========================
    public static final double MATCH_THRESHOLD = 70.0;
    
    // =========================
    // Verification Configuration
    // =========================
    public static final double VERIFICATION_PASSING_SCORE = 60.0;

    // =========================
    // Pagination
    // =========================
    public static final int DEFAULT_PAGE_NUMBER = 0;

    public static final int DEFAULT_PAGE_SIZE = 10;
}