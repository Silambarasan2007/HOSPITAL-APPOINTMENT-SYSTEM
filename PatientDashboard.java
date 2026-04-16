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
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PatientDashboard extends BaseDashboard {

    private Runnable refreshOverviewVars;
    private Runnable refreshQueueData;
    private Runnable refreshAppointmentsData;
    private String currentAppointmentFilter = "All";

    public PatientDashboard(User user) {
        super(user, "Patient Dashboard");
    }

    @Override
    protected LinkedHashMap<String, String> getNavItems() {
        LinkedHashMap<String, String> nav = new LinkedHashMap<>();
        nav.put("overview",       "⬛  Overview");
        nav.put("book",           "📅  Book Appointment");
        nav.put("appointments",   "🗓  My Appointments");
        nav.put("queue",          "🔢  Queue / Token");
        nav.put("prescriptions",  "💊  Prescriptions");
        nav.put("doctors",        "👨‍⚕️  Find Doctors");
        return nav;
    }

    @Override
    protected void buildPanels() {
        addPage("overview",      buildOverview());
        addPage("book",          buildBookingPanel());
        addPage("appointments",  buildAppointmentsPanel());
        addPage("queue",         buildQueuePanel());
        addPage("prescriptions", buildPrescriptionsPanel());
        addPage("doctors",       buildDoctorsPanel());
    }

    public void refreshAllPanels() {
        if (refreshOverviewVars != null) refreshOverviewVars.run();
        if (refreshQueueData != null) refreshQueueData.run();
        if (refreshAppointmentsData != null) refreshAppointmentsData.run();
    }

    // ─── Overview ─────────────────────────────────────────────────
    private JPanel buildOverview() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        JLabel title = new JLabel("Good day, " + currentUser.name.split(" ")[0] + "  👋");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel dynamicCenter = new JPanel(new BorderLayout(0, 20));
        dynamicCenter.setOpaque(false);

        refreshOverviewVars = () -> {
            dynamicCenter.removeAll();

            List<Appointment> appts = HospitalService.getPatientAppointments(currentUser.id);
            long booked    = appts.stream().filter(a -> a.status.equals("Booked")).count();
            long completed = appts.stream().filter(a -> a.status.equals("Completed")).count();
            long cancelled = appts.stream().filter(a -> a.status.equals("Cancelled")).count();
            long total     = appts.size();

            JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
            statsRow.setOpaque(false);
            statsRow.add(UIComponents.statCard("Total Appointments", String.valueOf(total),     Theme.ACCENT_BLUE));
            statsRow.add(UIComponents.statCard("Upcoming",           String.valueOf(booked),    Theme.ACCENT_TEAL));
            statsRow.add(UIComponents.statCard("Completed",          String.valueOf(completed), Theme.ACCENT_GREEN));
            statsRow.add(UIComponents.statCard("Cancelled",          String.valueOf(cancelled), Theme.ACCENT_ROSE));

            JPanel recentSection = new JPanel(new BorderLayout(0, 12));
            recentSection.setOpaque(false);
            recentSection.add(UIComponents.sectionLabel("Recent Appointments"), BorderLayout.NORTH);

            String[] cols = {"Token", "Doctor", "Department", "Date", "Time", "Status"};
            JTable table = UIComponents.styledTable(cols);
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            List<Appointment> recentList = new ArrayList<>();
            for (Appointment a : appts) {
                if (!a.status.equals("Cancelled")) {
                    recentList.add(a);
                }
            }

            for (int i = recentList.size() - 1; i >= Math.max(0, recentList.size() - 5); i--) {
                Appointment a = recentList.get(i);
                Doctor d = HospitalService.getDoctorById(a.doctorId);
                model.addRow(new Object[]{
                    a.token,
                    d != null ? "Dr. " + d.name : a.doctorId,
                    d != null ? d.department : "-",
                    a.date, a.time, a.status
                });
            }

            styleStatusColumn(table, 5);
            recentSection.add(UIComponents.darkScroll(table), BorderLayout.CENTER);

            dynamicCenter.add(statsRow, BorderLayout.NORTH);
            dynamicCenter.add(recentSection, BorderLayout.CENTER);
            dynamicCenter.revalidate();
            dynamicCenter.repaint();
        };

        refreshOverviewVars.run();

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(dynamicCenter, BorderLayout.CENTER);

        // Quick action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        StyledButton bookNow = new StyledButton("+ Book Appointment", Theme.ACCENT_BLUE);
        StyledButton viewAll = new StyledButton("View All Appointments", Theme.ACCENT_TEAL);
        bookNow.addActionListener(e -> showPage("book"));
        viewAll.addActionListener(e -> showPage("appointments"));
        actions.add(bookNow); actions.add(viewAll);

        center.add(actions, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ─── Book Appointment ─────────────────────────────────────────
    private JPanel buildBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Book an Appointment"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 24, 0));
        body.setOpaque(false);

        // Left: search
        CardPanel searchCard = new CardPanel();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));

        JLabel searchTitle = UIComponents.fieldLabel("Search Doctor");
        searchTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledField searchField = new StyledField("Name or department...");
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton searchBtn = new StyledButton("Search", Theme.ACCENT_BLUE);
        searchBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] deptCols = {"Name", "Department", "Hours", "Rating"};
        JTable doctorTable = UIComponents.styledTable(deptCols);
        DefaultTableModel dtModel = (DefaultTableModel) doctorTable.getModel();
        refreshDoctorTable(dtModel, "");

        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane doctorScroll = UIComponents.darkScroll(doctorTable);

        searchBtn.addActionListener(e -> refreshDoctorTable(dtModel, searchField.getText()));
        searchField.addActionListener(e -> refreshDoctorTable(dtModel, searchField.getText()));

        searchCard.add(searchTitle);
        searchCard.add(Box.createVerticalStrut(8));
        searchCard.add(searchField);
        searchCard.add(Box.createVerticalStrut(8));
        searchCard.add(searchBtn);
        searchCard.add(Box.createVerticalStrut(16));
        searchCard.add(doctorScroll);

        // Right: booking form
        CardPanel formCard = new CardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel formTitle = UIComponents.sectionLabel("Appointment Details");
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel selDoctorLbl = UIComponents.fieldLabel("Selected Doctor");
        selDoctorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel selectedDoctor = new JLabel("— select from the list —");
        selectedDoctor.setFont(Theme.FONT_BODY);
        selectedDoctor.setForeground(Theme.ACCENT_TEAL);
        selectedDoctor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLbl = UIComponents.fieldLabel("Select Date");
        dateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        hospital.util.DatePicker datePicker = new hospital.util.DatePicker();
        datePicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        datePicker.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLbl = UIComponents.fieldLabel("Select Time Slot");
        timeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.util.List<String> timeSlots = new java.util.ArrayList<>();
        for (int h = 9; h <= 22; h++) timeSlots.add(String.format("%02d:00", h));
        StyledCombo timeCombo = new StyledCombo(timeSlots.toArray(new String[0]));
        timeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        timeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(Theme.FONT_SMALL);
        errLbl.setForeground(Theme.ACCENT_ROSE);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton bookBtn = new StyledButton("Confirm Booking", Theme.ACCENT_GREEN);
        bookBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        bookBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Token preview
        JPanel tokenPreview = buildTokenPreview("—");
        tokenPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        tokenPreview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        final String[] pickedDoctorId = {null};

        doctorTable.getSelectionModel().addListSelectionListener(e2 -> {
            int row = doctorTable.getSelectedRow();
            if (row >= 0) {
                String docName = (String) doctorTable.getValueAt(row, 0);
                String dept    = (String) doctorTable.getValueAt(row, 1);
                selectedDoctor.setText("Dr. " + docName + " — " + dept);
                for (Doctor d : HospitalService.getAllDoctors())
                    if (d.name.equals(docName)) { pickedDoctorId[0] = d.id; break; }
            }
        });

        bookBtn.addActionListener(e -> {
            if (pickedDoctorId[0] == null) { errLbl.setText("Please select a doctor."); return; }
            String date = datePicker.getSelectedDateString();
            String time = (String) timeCombo.getSelectedItem();
            Appointment a = HospitalService.bookAppointment(currentUser.id, pickedDoctorId[0], date, time);
            updateTokenPreview(tokenPreview, a.token);
            refreshAllPanels(); // Immediately update all other panels visually with the new appointment!
            JOptionPane.showMessageDialog(this,
                "Appointment booked!\nToken: " + a.token + "\nDate: " + date + "  Time: " + time,
                "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            errLbl.setText(" ");
            showPage("appointments");
        });

        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(selDoctorLbl);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(selectedDoctor);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(dateLbl);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(datePicker);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(timeLbl);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(timeCombo);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(tokenPreview);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(errLbl);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(bookBtn);

        body.add(searchCard);
        body.add(formCard);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTokenPreview(String tokenText) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Theme.BG_INPUT);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel caption = UIComponents.mutedLabel("YOUR TOKEN NUMBER");
        JLabel token   = new JLabel(tokenText, SwingConstants.CENTER);
        token.setFont(Theme.FONT_MONO);
        token.setForeground(Theme.ACCENT_TEAL);
        p.add(caption, BorderLayout.NORTH);
        p.add(token,   BorderLayout.CENTER);
        return p;
    }

    private void updateTokenPreview(JPanel preview, String tokenText) {
        for (Component c : preview.getComponents()) {
            if (c instanceof JLabel && ((JLabel)c).getFont().equals(Theme.FONT_MONO)) {
                ((JLabel)c).setText(tokenText);
            }
        }
        preview.revalidate(); preview.repaint();
    }

    private void refreshDoctorTable(DefaultTableModel model, String query) {
        model.setRowCount(0);
        List<Doctor> docs = query.isEmpty() ? HospitalService.getAllDoctors() : HospitalService.searchDoctors(query);
        for (Doctor d : docs)
            model.addRow(new Object[]{d.name, d.department, d.availableFrom+"-"+d.availableTo, d.getRatingStr()});
    }

    // ─── My Appointments ──────────────────────────────────────────
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIComponents.sectionLabel("My Appointments"), BorderLayout.WEST);
        StyledButton refreshBtn = new StyledButton("↻ Refresh", Theme.ACCENT_BLUE);
        topRow.add(refreshBtn, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        String[] cols = {"Token","Doctor","Department","Date","Time","Status","Action"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (currentAppointmentFilter == null) {
            currentAppointmentFilter = "All";
        }

        refreshAppointmentsData = () -> {
            model.setRowCount(0);
            for (Appointment a : HospitalService.getPatientAppointments(currentUser.id)) {
                
                // Filtering Logic
                if (currentAppointmentFilter.equals("All")) {
                    if (a.status.equals("Cancelled") || a.status.equals("No-show")) continue;
                } else {
                    if (!a.status.equals(currentAppointmentFilter)) continue;
                }

                Doctor d = HospitalService.getDoctorById(a.doctorId);
                model.addRow(new Object[]{
                    a.token,
                    d != null ? "Dr. "+d.name : a.doctorId,
                    d != null ? d.department : "-",
                    a.date, a.time, a.status,
                    a.status.equals("Booked") ? "Cancel" : "—"
                });
            }
        };
        refreshAppointmentsData.run();
        styleStatusColumn(table, 5);

        // Action column — cancel
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r >= 0 && c == 6) { // 6 is the Action column
                    String action = (String) model.getValueAt(r, 6);
                    if ("Cancel".equals(action)) {
                        String token = (String) model.getValueAt(r, 0);
                        int confirm = JOptionPane.showConfirmDialog(PatientDashboard.this,
                            "Are you sure you want to cancel token " + token + "?", "Confirm Cancel",
                            JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            boolean ok = HospitalService.cancelAppointment(token);
                            if (ok) {
                                refreshAllPanels();
                            } else {
                                JOptionPane.showMessageDialog(PatientDashboard.this, 
                                    "System Error: Could not update database for token " + token, 
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

        refreshBtn.addActionListener(e -> refreshAllPanels());

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        filterRow.add(UIComponents.mutedLabel("Filter:"));
        String[] statuses = {"All","Booked","Completed","Cancelled","No-show"};
        for (String s : statuses) {
            OutlineButton fb = new OutlineButton(s, Theme.ACCENT_BLUE);
            fb.addActionListener(e -> {
                currentAppointmentFilter = s;
                refreshAppointmentsData.run();
            });
            filterRow.add(fb);
        }

        panel.add(filterRow, BorderLayout.CENTER);
        panel.add(UIComponents.darkScroll(table), BorderLayout.SOUTH);
        ((BorderLayout)panel.getLayout()).setVgap(12);
        panel.remove(panel.getComponent(2));
        panel.add(UIComponents.darkScroll(table), BorderLayout.CENTER);
        panel.add(filterRow, BorderLayout.SOUTH);
        return panel;
    }

    // ─── Queue Panel ──────────────────────────────────────────────
    private JPanel buildQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIComponents.sectionLabel("Token / Queue Status"), BorderLayout.WEST);
        StyledButton refreshBtn = new StyledButton("↻ Refresh", Theme.ACCENT_BLUE);
        topRow.add(refreshBtn, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        panel.add(contentArea, BorderLayout.CENTER);

        refreshQueueData = () -> {
            contentArea.removeAll();
            JPanel queueGrid = new JPanel(new GridLayout(0, 3, 16, 16));
            queueGrid.setOpaque(false);

            List<Appointment> myAppts = HospitalService.getPatientAppointments(currentUser.id);
            for (Appointment a : myAppts) {
                if (!a.status.equals("Booked")) continue;
                Doctor d = HospitalService.getDoctorById(a.doctorId);

                JPanel card = new JPanel(new BorderLayout(0, 8));
                card.setBackground(Theme.BG_CARD);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, Theme.ACCENT_TEAL),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)
                    )
                ));

                JLabel tokenLbl = new JLabel(a.token, SwingConstants.CENTER);
                tokenLbl.setFont(new Font("Consolas", Font.BOLD, 36));
                tokenLbl.setForeground(Theme.ACCENT_TEAL);

                JLabel docLbl = new JLabel(d != null ? "Dr. " + d.name : a.doctorId, SwingConstants.CENTER);
                docLbl.setFont(Theme.FONT_BODY);
                docLbl.setForeground(Theme.TEXT_PRIMARY);

                JLabel detailLbl = new JLabel(a.date + " at " + a.time, SwingConstants.CENTER);
                detailLbl.setFont(Theme.FONT_SMALL);
                detailLbl.setForeground(Theme.TEXT_SECONDARY);

                JLabel dept = new JLabel(d != null ? d.department : "", SwingConstants.CENTER);
                dept.setFont(Theme.FONT_SMALL);
                dept.setForeground(Theme.ACCENT_BLUE);

                card.add(tokenLbl, BorderLayout.CENTER);

                JPanel info = new JPanel(new GridLayout(3, 1, 0, 4));
                info.setOpaque(false);
                info.add(docLbl); info.add(dept); info.add(detailLbl);
                card.add(info, BorderLayout.SOUTH);
                queueGrid.add(card);
            }

            if (queueGrid.getComponentCount() == 0) {
                JLabel noData = new JLabel("No upcoming appointments in queue.", SwingConstants.CENTER);
                noData.setFont(Theme.FONT_BODY);
                noData.setForeground(Theme.TEXT_MUTED);
                contentArea.add(noData, BorderLayout.CENTER);
            } else {
                contentArea.add(UIComponents.darkScroll(queueGrid), BorderLayout.CENTER);
            }
            contentArea.revalidate();
            contentArea.repaint();
        };

        refreshBtn.addActionListener(e -> refreshAllPanels());
        refreshQueueData.run();

        return panel;
    }

    // ─── Prescriptions ────────────────────────────────────────────
    private JPanel buildPrescriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("My Prescriptions"), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        List<DataStore.Prescription> prescriptions = HospitalService.getPatientPrescriptions(currentUser.id);
        if (prescriptions.isEmpty()) {
            JLabel empty = UIComponents.mutedLabel("No prescriptions found.");
            list.add(empty);
        } else {
            for (DataStore.Prescription p : prescriptions) {
                Appointment a = null;
                for (Appointment appt : HospitalService.getAllAppointments()) if (appt.id.equals(p.appointmentId)) a = appt;
                Doctor d = HospitalService.getDoctorById(p.doctorId);

                CardPanel card = new CardPanel();
                card.setLayout(new BorderLayout(0, 10));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel docName = new JLabel(d != null ? "Dr. "+d.name+" — "+d.department : "Unknown Doctor");
                docName.setFont(Theme.FONT_SUBTITLE);
                docName.setForeground(Theme.TEXT_PRIMARY);

                JLabel dateLabel = UIComponents.mutedLabel("Date: " + p.date +
                    (a != null ? "   Token: "+a.token : ""));

                JTextArea notes = new JTextArea(p.medicines + "\n\n" + p.notes);
                notes.setFont(Theme.FONT_BODY);
                notes.setForeground(Theme.TEXT_SECONDARY);
                notes.setBackground(Theme.BG_CARD);
                notes.setEditable(false);
                notes.setLineWrap(true);
                notes.setWrapStyleWord(true);

                card.add(docName,   BorderLayout.NORTH);
                card.add(dateLabel, BorderLayout.CENTER);
                card.add(notes,     BorderLayout.SOUTH);
                list.add(card);
                list.add(Box.createVerticalStrut(12));
            }
        }
        panel.add(UIComponents.darkScroll(list), BorderLayout.CENTER);
        return panel;
    }

    // ─── Find Doctors ─────────────────────────────────────────────
    private JPanel buildDoctorsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Find a Doctor"), BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        StyledField search = new StyledField("Search by name or department...");
        search.setPreferredSize(new Dimension(280, 38));
        StyledCombo deptCombo = new StyledCombo(buildDeptOptions());
        StyledButton searchBtn = new StyledButton("Search", Theme.ACCENT_BLUE);
        searchRow.add(search); searchRow.add(deptCombo); searchRow.add(searchBtn);

        JPanel doctorCards = new JPanel(new GridLayout(0, 3, 16, 16));
        doctorCards.setOpaque(false);

        Runnable showDoctors = () -> {
            doctorCards.removeAll();
            String q    = search.getText().trim().toLowerCase();
            String dept = (String) deptCombo.getSelectedItem();
            for (Doctor d : HospitalService.getAllDoctors()) {
                boolean matchQ = q.isEmpty() || d.name.toLowerCase().contains(q) || d.department.toLowerCase().contains(q);
                boolean matchD = dept.equals("All Departments") || d.department.equals(dept);
                if (!matchQ || !matchD) continue;
                doctorCards.add(buildDoctorCard(d));
            }
            doctorCards.revalidate(); doctorCards.repaint();
        };
        showDoctors.run();

        searchBtn.addActionListener(e -> showDoctors.run());
        search.addActionListener(e -> showDoctors.run());
        deptCombo.addActionListener(e -> showDoctors.run());

        panel.add(searchRow, BorderLayout.NORTH);
        panel.add(UIComponents.darkScroll(doctorCards), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDoctorCard(Doctor d) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel name = new JLabel("Dr. " + d.name);
        name.setFont(Theme.FONT_SUBTITLE);
        name.setForeground(Theme.TEXT_PRIMARY);

        JLabel dept = new JLabel(d.department);
        dept.setFont(Theme.FONT_SMALL);
        dept.setForeground(Theme.ACCENT_BLUE);

        JLabel qual = UIComponents.mutedLabel(d.qualification);

        JLabel rating = new JLabel(d.getRatingStr());
        rating.setFont(Theme.FONT_SMALL);
        rating.setForeground(Theme.ACCENT_AMBER);

        JLabel hours = UIComponents.mutedLabel("⏰ " + d.availableFrom + " – " + d.availableTo);

        StyledButton bookBtn = new StyledButton("Book Now", Theme.ACCENT_BLUE);
        bookBtn.addActionListener(e -> showPage("book"));

        JPanel info = new JPanel(new GridLayout(4, 1, 0, 4));
        info.setOpaque(false);
        info.add(dept); info.add(qual); info.add(rating); info.add(hours);

        card.add(name,    BorderLayout.NORTH);
        card.add(info,    BorderLayout.CENTER);
        card.add(bookBtn, BorderLayout.SOUTH);
        return card;
    }

    private String[] buildDeptOptions() {
        List<String> opts = new ArrayList<>();
        opts.add("All Departments");
        for (DataStore.Department d : HospitalService.getAllDepartments()) opts.add(d.name);
        return opts.toArray(new String[0]);
    }

    // ─── Helpers ──────────────────────────────────────────────────
    private void styleStatusColumn(JTable table, int col) {
        table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int column) {
                JLabel lbl = new JLabel(" " + value + " ");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setOpaque(true);
                Color c = Theme.statusColor(String.valueOf(value));
                lbl.setForeground(c);
                lbl.setBackground(sel ? Theme.BG_HOVER : Theme.BG_CARD);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });
    }

    // ─── Button Cell Renderer/Editor ──────────────────────────────
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
            btn.setBackground(Theme.ACCENT_ROSE);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            return btn;
        }
    }


}
