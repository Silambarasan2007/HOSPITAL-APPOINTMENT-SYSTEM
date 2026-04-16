package hospital;

import hospital.ui.LoginScreen;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Apply dark system UI hints
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Use system look but override with custom colors
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Force dark scrollbars and combo popups
        UIManager.put("ScrollBar.background",        new java.awt.Color(18, 24, 42));
        UIManager.put("ScrollBar.thumb",             new java.awt.Color(40, 55, 90));
        UIManager.put("ScrollBar.thumbDarkShadow",   new java.awt.Color(40, 55, 90));
        UIManager.put("ScrollBar.thumbHighlight",    new java.awt.Color(40, 55, 90));
        UIManager.put("ScrollBar.thumbShadow",       new java.awt.Color(40, 55, 90));
        UIManager.put("ScrollBar.track",             new java.awt.Color(18, 24, 42));
        UIManager.put("ScrollBar.trackHighlight",    new java.awt.Color(18, 24, 42));
        UIManager.put("ComboBox.background",         new java.awt.Color(24, 32, 54));
        UIManager.put("ComboBox.foreground",         new java.awt.Color(230, 236, 255));
        UIManager.put("ComboBox.selectionBackground",new java.awt.Color(30, 42, 70));
        UIManager.put("ComboBox.selectionForeground",new java.awt.Color(230, 236, 255));
        UIManager.put("PopupMenu.background",        new java.awt.Color(18, 24, 42));
        UIManager.put("PopupMenu.border",            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(40,55,90)));
        UIManager.put("MenuItem.background",         new java.awt.Color(18, 24, 42));
        UIManager.put("MenuItem.foreground",         new java.awt.Color(230, 236, 255));
        UIManager.put("MenuItem.selectionBackground",new java.awt.Color(30, 42, 70));
        UIManager.put("List.background",             new java.awt.Color(18, 24, 42));
        UIManager.put("List.foreground",             new java.awt.Color(230, 236, 255));
        UIManager.put("List.selectionBackground",    new java.awt.Color(30, 42, 70));
        UIManager.put("OptionPane.background",       new java.awt.Color(18, 24, 42));
        UIManager.put("Panel.background",            new java.awt.Color(18, 24, 42));
        UIManager.put("OptionPane.messageForeground",new java.awt.Color(230, 236, 255));

        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
