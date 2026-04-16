package hospital.ui;
import hospital.model.DataStore;
import hospital.model.DataStore.*;

import hospital.service.HospitalService;
import hospital.util.Theme;
import hospital.util.UIComponents;
import hospital.util.UIComponents.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DoctorDashboard extends BaseDashboard {

    private Doctor myDoctor;
    private Runnable refreshOverviewVars;

    public DoctorDashboard(User user) {
        super(user, "Doctor Dashboard");
        this.myDoctor = HospitalService.getDoctorById(user.id);
    }

    @Override
    protected LinkedHashMap<String, String> getNavItems() {
        LinkedHashMap<String, String> nav = new LinkedHashMap<>();
        nav.put("overview",     "⬛  Overview");
        nav.put("schedule",     "📅  My Schedule");
        nav.put("prescribe",    "💊  Write Prescription");
        nav.put("availability", "⚙️  Availability");
        return nav;
    }

    @Override
    protected void buildPanels() {
        addPage("overview",     buildOverview());
        addPage("schedule",     buildSchedulePanel());
        addPage("prescribe",    buildPrescribePanel());
        addPage("availability", buildAvailabilityPanel());
    }

    // ─── Overview ─────────────────────────────────────────────────
    private JPanel buildOverview() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        String docName = myDoctor != null ? "Dr. " + myDoctor.name : currentUser.name;
        JLabel title = new JLabel("Welcome, " + docName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel dynamicCenter = new JPanel(new BorderLayout(0, 20));
        dynamicCenter.setOpaque(false);

        refreshOverviewVars = () -> {
            dynamicCenter.removeAll();

            List<Appointment> appts = HospitalService.getDoctorAppointments(currentUser.id);
            long booked    = appts.stream().filter(a -> a.status.equals("Booked")).count();
            String today   = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            long completed = appts.stream().filter(a -> a.status.equals("Completed") && a.date.equals(today)).count();
            long noshow    = appts.stream().filter(a -> a.status.equals("No-show")).count();

            JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
            statsRow.setOpaque(false);
            statsRow.add(UIComponents.statCard("Total Patients",  String.valueOf(appts.size()), Theme.ACCENT_BLUE));
            statsRow.add(UIComponents.statCard("Upcoming",        String.valueOf(booked),       Theme.ACCENT_TEAL));
            statsRow.add(UIComponents.statCard("Completed Today", String.valueOf(completed),    Theme.ACCENT_GREEN));
            statsRow.add(UIComponents.statCard("No-shows",        String.valueOf(noshow),       Theme.ACCENT_ROSE));

            // Doctor info card
            JPanel infoRow = new JPanel(new GridLayout(1, 2, 16, 0));
            infoRow.setOpaque(false);

            if (myDoctor != null) {
                CardPanel docCard = new CardPanel();
                docCard.setLayout(new GridLayout(0, 1, 0, 8));
                docCard.add(makeInfoRow("Department",   myDoctor.department));
                docCard.add(makeInfoRow("Qualification", myDoctor.qualification));
                docCard.add(makeInfoRow("Available",    myDoctor.availableFrom + " – " + myDoctor.availableTo));
                docCard.add(makeInfoRow("Rating",       myDoctor.getRatingStr()));
                infoRow.add(docCard);
            }

            // Today's queue
            CardPanel queueCard = new CardPanel();
            queueCard.setLayout(new BoxLayout(queueCard, BoxLayout.Y_AXIS));
            JLabel qTitle = UIComponents.sectionLabel("Today's Queue");
            qTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            queueCard.add(qTitle);
            queueCard.add(Box.createVerticalStrut(12));

            int qNum = 1;
            for (Appointment a : appts) {
                if (!a.status.equals("Booked") || !a.date.equals(today)) continue;
                User patient = HospitalService.getUserById(a.patientId);
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

                JLabel tLabel = new JLabel(qNum++ + ".  " + (patient != null ? patient.name : a.patientId));
                tLabel.setFont(Theme.FONT_BODY);
                tLabel.setForeground(Theme.TEXT_PRIMARY);
                JLabel timeLabel = new JLabel(a.time + "  " + a.token);
                timeLabel.setFont(Theme.FONT_SMALL);
                timeLabel.setForeground(Theme.ACCENT_TEAL);

                row.add(tLabel, BorderLayout.WEST);
                row.add(timeLabel, BorderLayout.EAST);
                queueCard.add(row);
                queueCard.add(Box.createVerticalStrut(6));
            }
            if (qNum == 1) {
                JLabel emptyQ = new JLabel("No pending queue for today.");
                emptyQ.setFont(Theme.FONT_BODY);
                emptyQ.setForeground(Theme.TEXT_MUTED);
                queueCard.add(emptyQ);
            }

            infoRow.add(queueCard);

            dynamicCenter.add(statsRow, BorderLayout.NORTH);
            dynamicCenter.add(infoRow,  BorderLayout.CENTER);
            dynamicCenter.revalidate();
            dynamicCenter.repaint();
        };

        refreshOverviewVars.run();
        panel.add(dynamicCenter, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeInfoRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel k = UIComponents.fieldLabel(key + ":");
        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_BODY);
        v.setForeground(Theme.TEXT_PRIMARY);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    // ─── Schedule Panel ───────────────────────────────────────────
    private JPanel buildSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIComponents.sectionLabel("My Appointments"), BorderLayout.WEST);

        hospital.util.DatePicker datePicker = new hospital.util.DatePicker();
        StyledButton allDatesBtn = new StyledButton("List All", Theme.ACCENT_TEAL);
        
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRow.setOpaque(false);
        filterRow.add(datePicker);
        filterRow.add(allDatesBtn);
        topRow.add(filterRow, BorderLayout.EAST);
        
        panel.add(topRow, BorderLayout.NORTH);

        String[] cols = {"Token","Patient","Date","Time","Status","Mark As"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        table.setRowHeight(48);

        final String[] currentFilter = { "All Dates" };

        Runnable loadData = () -> {
            model.setRowCount(0);
            for (Appointment a : HospitalService.getDoctorAppointments(currentUser.id)) {
                if (!currentFilter[0].equals("All Dates") && !a.date.equals(currentFilter[0])) continue;
                User patient = HospitalService.getUserById(a.patientId);
                model.addRow(new Object[]{
                    a.token,
                    patient != null ? patient.name : a.patientId,
                    a.date, a.time, a.status,
                    a.status.equals("Booked") ? "Update" : "—"
                });
            }
        };

        allDatesBtn.addActionListener(e -> {
            currentFilter[0] = "All Dates";
            loadData.run();
        });

        datePicker.onDateChange(d -> {
            currentFilter[0] = datePicker.getSelectedDateString();
            loadData.run();
        });

        loadData.run();

        styleStatusColumn(table, 4);

        // Action column — Update
        table.getColumn("Mark As").setCellRenderer(new ButtonRenderer());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 5) { // 5 is the Mark As column
                    String action = (String) model.getValueAt(r, 5);
                    if ("Update".equals(action)) {
                        String token = (String) model.getValueAt(r, 0);
                        String[] options = {"Completed", "Cancelled", "No-show"};
                        int choice = JOptionPane.showOptionDialog(panel,
                            "Update status for token " + token, "Update Appointment",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                            
                        if (choice >= 0) {
                            String newStatus = options[choice];
                            boolean ok = HospitalService.updateAppointmentStatus(token, newStatus);
                            if (ok) {
                                loadData.run();
                                if (refreshOverviewVars != null) refreshOverviewVars.run();
                                // Offer to write prescription
                                if (newStatus.equals("Completed")) {
                                    int pChoice = JOptionPane.showConfirmDialog(panel,
                                        "Write prescription for this patient?", "Prescription",
                                        JOptionPane.YES_NO_OPTION);
                                    if (pChoice == JOptionPane.YES_OPTION) showPage("prescribe");
                                }
                            } else {
                                JOptionPane.showMessageDialog(panel, "System Error updating status for " + token);
                            }
                        }
                    }
                }
            }
        });

        panel.add(UIComponents.darkScroll(table), BorderLayout.CENTER);
        return panel;
    }

    // ─── Write Prescription ───────────────────────────────────────
    private JPanel buildPrescribePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Write Prescription"), BorderLayout.NORTH);

        CardPanel form = new CardPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

        JLabel patLbl = UIComponents.fieldLabel("Patient");
        patLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Build patient options from completed appointments
        List<String> patOptions = new ArrayList<>();
        patOptions.add("Select patient...");
        for (Appointment a : HospitalService.getDoctorAppointments(currentUser.id)) {
            User u = HospitalService.getUserById(a.patientId);
            if (u != null) patOptions.add(u.name + " (" + a.token + ")");
        }
        StyledCombo patCombo = new StyledCombo(patOptions.toArray(new String[0]));
        patCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        patCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel medLbl = UIComponents.fieldLabel("Medicines & Dosage");
        medLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea medArea = buildTextArea("e.g. Paracetamol 500mg — 1 tablet twice daily after meals");
        medArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        medArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel notesLbl = UIComponents.fieldLabel("Doctor Notes / Instructions");
        notesLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea notesArea = buildTextArea("e.g. Rest for 3 days. Avoid cold water. Follow-up in 1 week.");
        notesArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel successLbl = new JLabel(" ");
        successLbl.setFont(Theme.FONT_SMALL);
        successLbl.setForeground(Theme.ACCENT_GREEN);
        successLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton saveBtn = new StyledButton("Save Prescription", Theme.ACCENT_TEAL);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveBtn.addActionListener(e -> {
            String sel = (String) patCombo.getSelectedItem();
            if (sel == null || sel.startsWith("Select")) {
                successLbl.setForeground(Theme.ACCENT_ROSE);
                successLbl.setText("Please select a patient.");
                return;
            }
            // Extract token
            String token = sel.replaceAll(".*\\((.*)\\)", "$1");
            for (Appointment a : HospitalService.getAllAppointments()) {
                if (a.token.equals(token) && a.doctorId.equals(currentUser.id)) {
                    Prescription p = HospitalService.savePrescription(
                        a.id, a.patientId, currentUser.id, medArea.getText(), notesArea.getText()
                    );
                    if (p != null) {
                        successLbl.setForeground(Theme.ACCENT_GREEN);
                        successLbl.setText("✓ Prescription saved for " + sel);
                        medArea.setText(""); notesArea.setText("");
                    } else {
                        successLbl.setForeground(Theme.ACCENT_ROSE);
                        successLbl.setText("Database Error: Could not save prescription to MySQL.");
                    }
                    break;
                }
            }
        });

        form.add(patLbl);    form.add(Box.createVerticalStrut(6));
        form.add(patCombo);  form.add(Box.createVerticalStrut(16));
        form.add(medLbl);    form.add(Box.createVerticalStrut(6));
        form.add(new JScrollPane(medArea) {{ setBorder(Theme.inputBorder()); setPreferredSize(new Dimension(0, 100)); setAlignmentX(Component.LEFT_ALIGNMENT); setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); }});
        form.add(Box.createVerticalStrut(16));
        form.add(notesLbl);  form.add(Box.createVerticalStrut(6));
        form.add(new JScrollPane(notesArea) {{ setBorder(Theme.inputBorder()); setPreferredSize(new Dimension(0, 80)); setAlignmentX(Component.LEFT_ALIGNMENT); setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); }});
        form.add(Box.createVerticalStrut(16));
        form.add(saveBtn);   form.add(Box.createVerticalStrut(8));
        form.add(successLbl);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JTextArea buildTextArea(String placeholder) {
        JTextArea ta = new JTextArea(placeholder);
        ta.setFont(Theme.FONT_BODY);
        ta.setForeground(Theme.TEXT_SECONDARY);
        ta.setBackground(Theme.BG_INPUT);
        ta.setCaretColor(Theme.ACCENT_BLUE);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(Theme.inputBorder());
        ta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (ta.getText().equals(placeholder)) { ta.setText(""); ta.setForeground(Theme.TEXT_PRIMARY); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (ta.getText().isEmpty()) { ta.setText(placeholder); ta.setForeground(Theme.TEXT_SECONDARY); }
            }
        });
        return ta;
    }

    // ─── Availability ─────────────────────────────────────────────
    private JPanel buildAvailabilityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Set Availability"), BorderLayout.NORTH);

        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String from = myDoctor != null ? myDoctor.availableFrom : "09:00";
        String to   = myDoctor != null ? myDoctor.availableTo   : "17:00";

        StyledCombo fromCombo = new StyledCombo(new String[]{
            "07:00","08:00","09:00","10:00","11:00","12:00"
        });
        StyledCombo toCombo = new StyledCombo(new String[]{
            "14:00","15:00","16:00","17:00","18:00","19:00","20:00"
        });
        fromCombo.setSelectedItem(from);
        toCombo.setSelectedItem(to);
        fromCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        fromCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        toCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        toCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel successLbl = new JLabel(" ");
        successLbl.setFont(Theme.FONT_SMALL);
        successLbl.setForeground(Theme.ACCENT_GREEN);
        successLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton saveBtn = new StyledButton("Save Availability", Theme.ACCENT_TEAL);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveBtn.addActionListener(e -> {
            if (myDoctor != null) {
                myDoctor.availableFrom = (String) fromCombo.getSelectedItem();
                myDoctor.availableTo   = (String) toCombo.getSelectedItem();
                successLbl.setText("✓ Availability updated: " + myDoctor.availableFrom + " – " + myDoctor.availableTo);
            }
        });

        JLabel fromLbl = UIComponents.fieldLabel("Available From");
        fromLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel toLbl = UIComponents.fieldLabel("Available Until");
        toLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(fromLbl);    card.add(Box.createVerticalStrut(6));
        card.add(fromCombo);  card.add(Box.createVerticalStrut(14));
        card.add(toLbl);      card.add(Box.createVerticalStrut(6));
        card.add(toCombo);    card.add(Box.createVerticalStrut(20));
        card.add(saveBtn);    card.add(Box.createVerticalStrut(8));
        card.add(successLbl);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void styleStatusColumn(JTable table, int col) {
        table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int column) {
                JLabel lbl = new JLabel(" " + value + " ");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                Color c = Theme.statusColor(String.valueOf(value));
                lbl.setForeground(c);
                lbl.setBackground(sel ? Theme.BG_HOVER : Theme.BG_CARD);
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });
    }

    // ─── Button Cell Renderer ─────────────────────────────────────
    static class ButtonRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            if ("—".equals(val)) {
                JLabel dash = new JLabel("—", SwingConstants.CENTER);
                dash.setForeground(Theme.TEXT_MUTED);
                dash.setOpaque(true);
                dash.setBackground(sel ? Theme.BG_HOVER : Theme.BG_CARD);
                return dash;
            }
            JButton btn = new JButton(String.valueOf(val));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(Color.WHITE);
            btn.setBackground(Theme.ACCENT_TEAL);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            return btn;
        }
    }

}
