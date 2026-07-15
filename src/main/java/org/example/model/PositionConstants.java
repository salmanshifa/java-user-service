package org.example.model;

public final class PositionConstants {
    public static final String STYLIST = "STYLIST";
    public static final String BARBER = "BARBER";
    public static final String NAIL_TECH = "NAIL_TECH";
    public static final String MASSAGE_THERAPIST = "MASSAGE_THERAPIST";
    public static final String ESTHETICIAN = "ESTHETICIAN";
    public static final String RECEPTIONIST = "RECEPTIONIST";
    public static final String MANAGER = "MANAGER";

    private PositionConstants() {
    }

    /**
     * Maps a staff position to its default service category.
     * Non-service positions (RECEPTIONIST, MANAGER) return null.
     */
    public static String toCategory(String position) {
        if (position == null) return null;
        return switch (position.toUpperCase()) {
            case STYLIST, BARBER -> CategoryConstants.HAIR;
            case NAIL_TECH -> CategoryConstants.NAILS;
            case MASSAGE_THERAPIST -> CategoryConstants.MASSAGE;
            case ESTHETICIAN -> CategoryConstants.FACIAL;
            default -> null;
        };
    }
}
