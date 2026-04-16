package hospital.util;

import java.awt.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

public class Theme {

    // ─── Palette ───────────────────────────────────────────────
    public static final Color BG_DARK        = new Color(10, 14, 26);
    public static final Color BG_CARD        = new Color(18, 24, 42);
    public static final Color BG_SIDEBAR     = new Color(14, 19, 35);
    public static final Color BG_INPUT       = new Color(24, 32, 54);
    public static final Color BG_HOVER       = new Color(30, 42, 70);
    public static final Color BG_TABLE_ALT   = new Color(20, 28, 48);

    public static final Color ACCENT_BLUE    = new Color(64, 156, 255);
    public static final Color ACCENT_TEAL    = new Color(32, 210, 175);
    public static final Color ACCENT_PURPLE  = new Color(130, 90, 255);
    public static final Color ACCENT_ROSE    = new Color(255, 80, 120);
    public static final Color ACCENT_AMBER   = new Color(255, 180, 50);
    public static final Color ACCENT_GREEN   = new Color(50, 210, 120);

    public static final Color TEXT_PRIMARY   = new Color(230, 236, 255);
    public static final Color TEXT_SECONDARY = new Color(130, 145, 180);
    public static final Color TEXT_MUTED     = new Color(70, 85, 120);
    public static final Color TEXT_ON_ACCENT = Color.WHITE;

    public static final Color BORDER_SUBTLE  = new Color(40, 55, 90);
    public static final Color BORDER_ACTIVE  = new Color(64, 156, 255);

    public static final Color STATUS_BOOKED     = new Color(64, 156, 255);
    public static final Color STATUS_COMPLETED  = new Color(50, 210, 120);
    public static final Color STATUS_CANCELLED  = new Color(255, 80, 120);
    public static final Color STATUS_NOSHOW     = new Color(255, 180, 50);

    // ─── Fonts ─────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_MONO     = new Font("Consolas", Font.BOLD, 20);
    public static final Font FONT_NAV      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_NAV_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD, 13);

    // ─── Borders ───────────────────────────────────────────────
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        );
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    public static Border activeBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_ACTIVE, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    public static Color statusColor(String status) {
        switch (status.toLowerCase()) {
            case "completed": return STATUS_COMPLETED;
            case "cancelled": return STATUS_CANCELLED;
            case "no-show":   return STATUS_NOSHOW;
            default:          return STATUS_BOOKED;
        }
    }
}
