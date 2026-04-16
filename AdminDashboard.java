package hospital.ui;
import hospital.model.DataStore;
import hospital.model.DataStore.*;

import hospital.service.HospitalService;
import hospital.service.HospitalService;
import hospital.util.Theme;
import hospital.util.UIComponents;
import hospital.util.UIComponents.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AdminDashboard extends BaseDashboard {

    public AdminDashboard(User user) {
        super(user, "Admin Control Panel");
    }

    @Override
    protected LinkedHashMap<String, String> getNavItems() {
        LinkedHashMap<String, String> nav = new LinkedHashMap<>();
        nav.put("overview",     "⬛  Overview");
        nav.put("appointments", "📅  All Appointments");
        nav.put("doctors",      "👨‍⚕️  Manage Doctors");
        nav.put("departments",  "🏥  Departments");
        nav.put("users",        "👥  Manage Users");
        nav.put("reports",      "📊  Reports");
        nav.put("ratings",      "⭐  Doctor Ratings");
        return nav;
    }

    @Override
    protected void buildPanels() {
        addPage("overview",     buildOverview());
        addPage("appointments", buildAllAppointments());
        addPage("doctors",      buildDoctorsPanel());
        addPage("departments",  buildDepartmentsPanel());
        addPage("users",        buildUsersPanel());
        addPage("reports",      buildReportsPanel());
        addPage("ratings",      buildRatingsPanel());
    }

    // ─── Overview ─────────────────────────────────────────────────
    private JPanel buildOverview() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        JLabel title = new JLabel("Admin Control Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        long total     = HospitalService.getAllAppointments().size();
        long booked    = HospitalService.getAllAppointments().stream().filter(a->a.status.equals("Booked")).count();
        long completed = HospitalService.getAllAppointments().stream().filter(a->a.status.equals("Completed")).count();
        long patients  = HospitalService.getAllUsers().stream().filter(u->u.role.equals("patient")).count();
        long doctors   = HospitalService.getAllDoctors().size();
        long depts     = HospitalService.getAllDepartments().size();

        JPanel statsRow1 = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow1.setOpaque(false);
        statsRow1.add(UIComponents.statCard("Total Appointments", String.valueOf(total),     Theme.ACCENT_BLUE));
        statsRow1.add(UIComponents.statCard("Active Bookings",    String.valueOf(booked),    Theme.ACCENT_TEAL));
        statsRow1.add(UIComponents.statCard("Completed",          String.valueOf(completed), Theme.ACCENT_GREEN));

        JPanel statsRow2 = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow2.setOpaque(false);
        statsRow2.add(UIComponents.statCard("Registered Patients", String.valueOf(patients), Theme.ACCENT_PURPLE));
        statsRow2.add(UIComponents.statCard("Doctors",             String.valueOf(doctors),  Theme.ACCENT_AMBER));
        statsRow2.add(UIComponents.statCard("Departments",         String.valueOf(depts),    Theme.ACCENT_ROSE));

        // Recent activity
        CardPanel recentCard = new CardPanel();
        recentCard.setLayout(new BorderLayout(0, 12));
        recentCard.add(UIComponents.sectionLabel("Recent Appointments"), BorderLayout.NORTH);

        String[] cols = {"Token","Patient","Doctor","Dept","Date","Status"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        List<Appointment> recents = HospitalService.getAllAppointments();
        for (int i = recents.size()-1; i >= Math.max(0, recents.size()-6); i--) {
            Appointment a = recents.get(i);
            User patient  = HospitalService.getUserById(a.patientId);
            Doctor doctor = HospitalService.getDoctorById(a.doctorId);
            model.addRow(new Object[]{
                a.token,
                patient != null ? patient.name : a.patientId,
                doctor  != null ? "Dr. "+doctor.name : a.doctorId,
                doctor  != null ? doctor.department : "-",
                a.date, a.status
            });
        }
        styleStatusColumn(table, 5);
        recentCard.add(UIComponents.darkScroll(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);

        JPanel statsWrapper = new JPanel(new GridLayout(2, 1, 0, 16));
        statsWrapper.setOpaque(false);
        statsWrapper.add(statsRow1); statsWrapper.add(statsRow2);

        center.add(statsWrapper, BorderLayout.NORTH);
        center.add(recentCard,   BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ─── All Appointments ─────────────────────────────────────────
    private JPanel buildAllAppointments() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIComponents.sectionLabel("All Appointments"), BorderLayout.WEST);

        StyledCombo statusFilter = new StyledCombo(new String[]{"All","Booked","Completed","Cancelled","No-show"});
        topRow.add(statusFilter, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        String[] cols = {"Token","Patient","Doctor","Department","Date","Time","Status"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable load = () -> {
            model.setRowCount(0);
            String filter = (String) statusFilter.getSelectedItem();
            for (Appointment a : HospitalService.getAllAppointments()) {
                if (!filter.equals("All") && !a.status.equals(filter)) continue;
                User patient  = HospitalService.getUserById(a.patientId);
                Doctor doctor = HospitalService.getDoctorById(a.doctorId);
                model.addRow(new Object[]{
                    a.token,
                    patient != null ? patient.name : a.patientId,
                    doctor  != null ? "Dr. "+doctor.name : a.doctorId,
                    doctor  != null ? doctor.department : "-",
                    a.date, a.time, a.status
                });
            }
        };
        load.run();
        statusFilter.addActionListener(e -> load.run());
        styleStatusColumn(table, 6);
        panel.add(UIComponents.darkScroll(table), BorderLayout.CENTER);
        return panel;
    }

    // ─── Manage Doctors ───────────────────────────────────────────
    private JPanel buildDoctorsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Manage Doctors"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);

        // Doctor table
        String[] cols = {"ID","Name","Department","Qualification","Hours","Rating"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable reload = () -> {
            model.setRowCount(0);
            for (Doctor d : HospitalService.getAllDoctors())
                model.addRow(new Object[]{d.id, d.name, d.department, d.qualification,
                    d.availableFrom+"-"+d.availableTo, d.getRatingStr()});
        };
        reload.run();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(UIComponents.darkScroll(table), BorderLayout.CENTER);

        // Add doctor form
        CardPanel addForm = new CardPanel();
        addForm.setLayout(new BoxLayout(addForm, BoxLayout.Y_AXIS));

        JLabel formTitle = UIComponents.sectionLabel("Add New Doctor");
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledField nameF  = new StyledField("Doctor name");
        StyledField qualF  = new StyledField("e.g. MBBS, MD");
        StyledCombo deptC  = new StyledCombo(buildDeptArray());
        StyledCombo fromC  = new StyledCombo(new String[]{"08:00","09:00","10:00","11:00"});
        StyledCombo toC    = new StyledCombo(new String[]{"16:00","17:00","18:00","19:00","20:00"});
        StyledField emailF = new StyledField("doctor@hospital.com");
        StyledPasswordField passF = new StyledPasswordField();

        for (JComponent c : new JComponent[]{nameF,qualF,deptC,fromC,toC,emailF,passF}) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(Theme.FONT_SMALL);
        errLbl.setForeground(Theme.ACCENT_ROSE);
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton addBtn = new StyledButton("Add Doctor", Theme.ACCENT_TEAL);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        addBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            String email = emailF.getText().trim();
            if (name.isEmpty() || email.isEmpty()) { errLbl.setText("Name and email required."); return; }
            if (HospitalService.emailExists(email)) { errLbl.setText("Email already in use."); return; }
            String id = HospitalService.generateDoctorId();
            Doctor nd = new Doctor(id, name, (String)deptC.getSelectedItem(),
                qualF.getText(), (String)fromC.getSelectedItem(), (String)toC.getSelectedItem());
            String pass = new String(passF.getPassword());
            if (pass.isEmpty()) pass = "doctor123";
            boolean success = HospitalService.addDoctor(nd, email, pass, "");
            if (!success) { errLbl.setText("Database error."); return; }
            reload.run();
            nameF.setText(""); qualF.setText(""); emailF.setText(""); passF.setText("");
            errLbl.setForeground(Theme.ACCENT_GREEN);
            errLbl.setText("✓ Doctor added successfully.");
        });

        String[][] rows = {{"Doctor Name", null}, {"Qualification", null}, {"Department", null},
            {"From", null}, {"Until", null}, {"Email", null}, {"Password", null}};
        JComponent[] fields = {nameF, qualF, deptC, fromC, toC, emailF, passF};
        String[] labels = {"Doctor Name","Qualification","Department","From","Until","Email","Password"};

        addForm.add(formTitle); addForm.add(Box.createVerticalStrut(16));
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = UIComponents.fieldLabel(labels[i]);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            addForm.add(lbl); addForm.add(Box.createVerticalStrut(4));
            addForm.add(fields[i]); addForm.add(Box.createVerticalStrut(10));
        }
        addForm.add(errLbl); addForm.add(Box.createVerticalStrut(4));
        addForm.add(addBtn);

        // Remove doctor button
        StyledButton removeBtn = new StyledButton("Remove Selected Doctor", Theme.ACCENT_ROSE);
        removeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        removeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(panel,"Select a doctor first."); return; }
            String id = (String) model.getValueAt(row, 0);
            HospitalService.removeDoctor(id);
            reload.run();
        });
        addForm.add(Box.createVerticalStrut(10)); addForm.add(removeBtn);

        body.add(tablePanel); body.add(addForm);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    // ─── Departments ──────────────────────────────────────────────
    private JPanel buildDepartmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Manage Departments"), BorderLayout.NORTH);

        String[] cols = {"ID","Department Name","Description"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable reload = () -> {
            model.setRowCount(0);
            for (Department d : HospitalService.getAllDepartments())
                model.addRow(new Object[]{d.id, d.name, d.description});
        };
        reload.run();

        // Add form below
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        addRow.setOpaque(false);
        StyledField nameF = new StyledField("Department name");
        nameF.setPreferredSize(new Dimension(200, 38));
        StyledField descF = new StyledField("Short description");
        descF.setPreferredSize(new Dimension(260, 38));
        StyledButton addBtn = new StyledButton("Add Department", Theme.ACCENT_TEAL);

        addBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            if (name.isEmpty()) return;
            HospitalService.addDepartment(name, descF.getText().trim());
            reload.run();
            nameF.setText(""); descF.setText("");
        });

        addRow.add(nameF); addRow.add(descF); addRow.add(addBtn);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(UIComponents.darkScroll(table), BorderLayout.CENTER);
        body.add(addRow, BorderLayout.SOUTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    // ─── Manage Users ─────────────────────────────────────────────
    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Registered Users"), BorderLayout.NORTH);

        String[] cols = {"ID","Name","Email","Role","Phone"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable reload = () -> {
            model.setRowCount(0);
            for (User u : HospitalService.getAllUsers())
                model.addRow(new Object[]{u.id, u.name, u.email, u.role.toUpperCase(), u.phone});
        };
        reload.run();

        // Role filter
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(UIComponents.mutedLabel("Filter by role:"));
        for (String role : new String[]{"All","patient","doctor","admin"}) {
            OutlineButton rb = new OutlineButton(role.substring(0,1).toUpperCase()+role.substring(1), Theme.ACCENT_BLUE);
            rb.addActionListener(e -> {
                model.setRowCount(0);
                for (User u : HospitalService.getAllUsers()) {
                    if (!role.equals("All") && !u.role.equals(role)) continue;
                    model.addRow(new Object[]{u.id, u.name, u.email, u.role.toUpperCase(), u.phone});
                }
            });
            topRow.add(rb);
        }

        // Remove user
        StyledButton removeBtn = new StyledButton("Remove Selected User", Theme.ACCENT_ROSE);
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String id = (String) model.getValueAt(row, 0);
            String role = ((String)model.getValueAt(row, 3)).toLowerCase();
            if (role.equals("admin")) { JOptionPane.showMessageDialog(panel, "Cannot remove admin."); return; }
            if (role.equals("doctor")) { HospitalService.removeDoctor(id); }
            else { HospitalService.deleteUser(id); }
            reload.run();
        });
        topRow.add(removeBtn);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(topRow, BorderLayout.NORTH);
        body.add(UIComponents.darkScroll(table), BorderLayout.CENTER);
        
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    // ─── Reports ──────────────────────────────────────────────────
    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Reports & Analytics"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);

        // Appointments per doctor
        CardPanel docReport = new CardPanel();
        docReport.setLayout(new BoxLayout(docReport, BoxLayout.Y_AXIS));
        JLabel r1Title = UIComponents.sectionLabel("Appointments per Doctor");
        r1Title.setAlignmentX(Component.LEFT_ALIGNMENT);
        docReport.add(r1Title);
        docReport.add(Box.createVerticalStrut(16));

        for (Doctor d : HospitalService.getAllDoctors()) {
            long count = HospitalService.getAllAppointments().stream().filter(a->a.doctorId.equals(d.id)).count();
            long done  = HospitalService.getAllAppointments().stream().filter(a->a.doctorId.equals(d.id)&&a.status.equals("Completed")).count();

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel name = new JLabel("Dr. " + d.name);
            name.setFont(Theme.FONT_BODY);
            name.setForeground(Theme.TEXT_PRIMARY);
            name.setPreferredSize(new Dimension(160, 24));

            // Progress bar
            JPanel bar = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.BG_INPUT);
                    g2.fillRoundRect(0, 8, getWidth(), 10, 5, 5);
                    if (count > 0) {
                        int w = (int)(getWidth() * (double)done/count);
                        g2.setColor(Theme.ACCENT_TEAL);
                        g2.fillRoundRect(0, 8, w, 10, 5, 5);
                    }
                    g2.dispose();
                }
            };
            bar.setOpaque(false);
            bar.setPreferredSize(new Dimension(0, 26));

            JLabel countLabel = new JLabel(done + "/" + count);
            countLabel.setFont(Theme.FONT_SMALL);
            countLabel.setForeground(Theme.TEXT_SECONDARY);
            countLabel.setPreferredSize(new Dimension(50, 24));
            countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            row.add(name,       BorderLayout.WEST);
            row.add(bar,        BorderLayout.CENTER);
            row.add(countLabel, BorderLayout.EAST);

            docReport.add(row);
            docReport.add(Box.createVerticalStrut(8));
        }

        // Department summary
        CardPanel deptReport = new CardPanel();
        deptReport.setLayout(new BoxLayout(deptReport, BoxLayout.Y_AXIS));
        JLabel r2Title = UIComponents.sectionLabel("Appointments by Department");
        r2Title.setAlignmentX(Component.LEFT_ALIGNMENT);
        deptReport.add(r2Title);
        deptReport.add(Box.createVerticalStrut(16));

        for (Department dept : HospitalService.getAllDepartments()) {
            long count = HospitalService.getAllAppointments().stream().filter(a -> {
                Doctor d = HospitalService.getDoctorById(a.doctorId);
                return d != null && d.department.equals(dept.name);
            }).count();
            if (count == 0) continue;

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel name = new JLabel(dept.name);
            name.setFont(Theme.FONT_BODY);
            name.setForeground(Theme.TEXT_PRIMARY);
            name.setPreferredSize(new Dimension(120, 24));

            JLabel cnt = new JLabel(count + " appointments");
            cnt.setFont(Theme.FONT_SMALL);
            cnt.setForeground(Theme.ACCENT_BLUE);

            row.add(name, BorderLayout.WEST);
            row.add(cnt,  BorderLayout.EAST);
            deptReport.add(row);
            deptReport.add(Box.createVerticalStrut(8));
        }

        // Summary card at bottom
        CardPanel summary = new CardPanel();
        summary.setLayout(new GridLayout(2, 2, 16, 12));
        long total     = HospitalService.getAllAppointments().size();
        long booked    = HospitalService.getAllAppointments().stream().filter(a->a.status.equals("Booked")).count();
        long completed = HospitalService.getAllAppointments().stream().filter(a->a.status.equals("Completed")).count();
        long cancelled = HospitalService.getAllAppointments().stream().filter(a->a.status.equals("Cancelled")).count();

        summary.add(makeStatRow("Total", String.valueOf(total),     Theme.ACCENT_BLUE));
        summary.add(makeStatRow("Booked",    String.valueOf(booked),    Theme.ACCENT_TEAL));
        summary.add(makeStatRow("Completed", String.valueOf(completed), Theme.ACCENT_GREEN));
        summary.add(makeStatRow("Cancelled", String.valueOf(cancelled), Theme.ACCENT_ROSE));

        body.add(docReport);
        body.add(deptReport);

        panel.add(body,    BorderLayout.CENTER);
        panel.add(summary, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeStatRow(String label, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel lbl = UIComponents.mutedLabel(label);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 20));
        val.setForeground(color);
        p.add(val, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ─── Ratings ──────────────────────────────────────────────────
    private JPanel buildRatingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(UIComponents.sectionLabel("Doctor Ratings"), BorderLayout.NORTH);

        JPanel rateForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        rateForm.setOpaque(false);

        List<String> docOpts = new ArrayList<>();
        for (Doctor d : HospitalService.getAllDoctors()) docOpts.add("Dr. "+d.name+" ("+d.id+")");
        StyledCombo docCombo = new StyledCombo(docOpts.toArray(new String[0]));
        docCombo.setPreferredSize(new Dimension(260, 38));

        StyledCombo starCombo = new StyledCombo(new String[]{"5 ★★★★★","4 ★★★★","3 ★★★","2 ★★","1 ★"});
        starCombo.setPreferredSize(new Dimension(140, 38));

        StyledButton rateBtn = new StyledButton("Submit Rating", Theme.ACCENT_AMBER);

        JLabel resultLbl = new JLabel(" ");
        resultLbl.setFont(Theme.FONT_SMALL);
        resultLbl.setForeground(Theme.ACCENT_AMBER);

        rateForm.add(UIComponents.mutedLabel("Doctor:"));
        rateForm.add(docCombo);
        rateForm.add(UIComponents.mutedLabel("Rating:"));
        rateForm.add(starCombo);
        rateForm.add(rateBtn);
        rateForm.add(resultLbl);

        // Ratings table
        String[] cols = {"Doctor","Department","Avg Rating","Total Ratings"};
        JTable table = UIComponents.styledTable(cols);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        Runnable reload = () -> {
            model.setRowCount(0);
            for (Doctor d : HospitalService.getAllDoctors())
                model.addRow(new Object[]{
                    "Dr. "+d.name, d.department,
                    d.ratingCount > 0 ? String.format("%.1f ★", d.rating) : "—",
                    d.ratingCount
                });
        };
        reload.run();

        rateBtn.addActionListener(e -> {
            String sel = (String) docCombo.getSelectedItem();
            String docId = sel.replaceAll(".*\\((.*)\\)", "$1");
            int stars = 5 - starCombo.getSelectedIndex();
            HospitalService.addRating(docId, stars);
            reload.run();
            resultLbl.setText("✓ Rated " + stars + " stars");
        });

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(rateForm,                       BorderLayout.NORTH);
        body.add(UIComponents.darkScroll(table), BorderLayout.CENTER);
        
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private String[] buildDeptArray() {
        return HospitalService.getAllDepartments().stream().map(d -> d.name).toArray(String[]::new);
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
}
