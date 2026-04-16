package hospital.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePicker extends JButton {

    private Date selectedDate;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private JPopupMenu popup;
    private java.util.List<java.util.function.Consumer<Date>> dateListeners = new java.util.ArrayList<>();

    public DatePicker() {
        super();
        this.selectedDate = new Date(); // default today
        setText(format.format(selectedDate));
        
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT_PRIMARY);
        setBackground(Theme.BG_INPUT);
        setBorder(Theme.inputBorder());
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.LEFT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { setBackground(Theme.BG_HOVER); }
            public void mouseExited(MouseEvent e)  { setBackground(Theme.BG_INPUT); }
        });

        addActionListener(e -> showPopup());
    }

    public String getSelectedDateString() {
        return format.format(selectedDate);
    }

    public void onDateChange(java.util.function.Consumer<Date> listener) {
        dateListeners.add(listener);
    }

    private void showPopup() {
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
            return;
        }
        
        popup = new JPopupMenu();
        popup.setBackground(Theme.BG_CARD);
        popup.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        
        JPanel calendarPanel = buildCalendarPanel(selectedDate);
        popup.add(calendarPanel);
        popup.show(this, 0, getHeight());
    }

    private JPanel buildCalendarPanel(Date targetMonth) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setPreferredSize(new Dimension(280, 260));

        Calendar cal = Calendar.getInstance();
        cal.setTime(targetMonth);

        // Top Navigation
        JPanel navRow = new JPanel(new BorderLayout());
        navRow.setBackground(Theme.BG_CARD);
        navRow.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        styleNavButton(prevBtn);
        styleNavButton(nextBtn);

        JLabel monthYearLbl = new JLabel(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()), SwingConstants.CENTER);
        monthYearLbl.setFont(Theme.FONT_SUBTITLE);
        monthYearLbl.setForeground(Theme.TEXT_PRIMARY);

        navRow.add(prevBtn, BorderLayout.WEST);
        navRow.add(monthYearLbl, BorderLayout.CENTER);
        navRow.add(nextBtn, BorderLayout.EAST);

        // Days Grid
        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        grid.setBackground(Theme.BG_CARD);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String d : days) {
            JLabel dl = new JLabel(d, SwingConstants.CENTER);
            dl.setFont(Theme.FONT_LABEL);
            dl.setForeground(Theme.TEXT_SECONDARY);
            grid.add(dl);
        }

        cal.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = cal.get(Calendar.DAY_OF_WEEK);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar todayCal = Calendar.getInstance(); // for past date checking

        for (int i = 1; i < startDay; i++) {
            grid.add(new JLabel("")); // empty cell
        }

        for (int i = 1; i <= maxDays; i++) {
            final int day = i;
            JButton dayBtn = new JButton(String.valueOf(i));
            dayBtn.setFont(Theme.FONT_BODY);
            dayBtn.setMargin(new Insets(0, 0, 0, 0));
            dayBtn.setFocusPainted(false);
            dayBtn.setBorderPainted(false);
            dayBtn.setContentAreaFilled(false);
            dayBtn.setOpaque(true);

            Calendar checkCal = Calendar.getInstance();
            checkCal.setTime(cal.getTime());
            checkCal.set(Calendar.DAY_OF_MONTH, i);
            checkCal.set(Calendar.HOUR_OF_DAY, 0);
            checkCal.set(Calendar.MINUTE, 0);
            checkCal.set(Calendar.SECOND, 0);
            checkCal.set(Calendar.MILLISECOND, 0);

            Calendar todayMidnight = Calendar.getInstance();
            todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
            todayMidnight.set(Calendar.MINUTE, 0);
            todayMidnight.set(Calendar.SECOND, 0);
            todayMidnight.set(Calendar.MILLISECOND, 0);

            if (checkCal.before(todayMidnight)) {
                // Past date
                dayBtn.setForeground(Theme.TEXT_MUTED);
                dayBtn.setBackground(Theme.BG_CARD);
                dayBtn.setEnabled(false);
            } else {
                dayBtn.setForeground(Theme.TEXT_PRIMARY);
                dayBtn.setBackground(Theme.BG_CARD);
                dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                dayBtn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { dayBtn.setBackground(Theme.BG_HOVER); }
                    public void mouseExited(MouseEvent e)  { dayBtn.setBackground(Theme.BG_CARD); }
                });

                dayBtn.addActionListener(e -> {
                    selectedDate = checkCal.getTime();
                    setText(format.format(selectedDate));
                    popup.setVisible(false);
                    for(java.util.function.Consumer<Date> l : dateListeners) l.accept(selectedDate);
                });
            }

            // Highlight if it's the currently selected date
            Calendar selCal = Calendar.getInstance();
            selCal.setTime(selectedDate);
            if (selCal.get(Calendar.YEAR) == checkCal.get(Calendar.YEAR) &&
                selCal.get(Calendar.DAY_OF_YEAR) == checkCal.get(Calendar.DAY_OF_YEAR)) {
                dayBtn.setBackground(Theme.ACCENT_BLUE);
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setOpaque(true);
            }

            grid.add(dayBtn);
        }

        prevBtn.addActionListener(e -> {
            cal.add(Calendar.MONTH, -1);
            popup.setVisible(false);
            selectedDate = cal.getTime();
            showPopup();
        });

        nextBtn.addActionListener(e -> {
            cal.add(Calendar.MONTH, 1);
            popup.setVisible(false);
            selectedDate = cal.getTime();
            showPopup();
        });

        panel.add(navRow, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void styleNavButton(JButton btn) {
        btn.setFont(new Font("Consolas", Font.BOLD, 14));
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBackground(Theme.BG_CARD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(Theme.BG_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(Theme.BG_CARD); }
        });
    }
}
