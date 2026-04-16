package hospital.ui;
import hospital.model.DataStore;
import hospital.model.DataStore.*;

import hospital.service.HospitalService;
import hospital.service.HospitalService;
import hospital.util.Theme;
import hospital.util.UIComponents;
import hospital.util.UIComponents.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginScreen extends JFrame {

    private JPanel cardContainer;
    private CardLayout cardLayout;
    private JPanel loginPanel, registerPanel;

    public LoginScreen() {
        setTitle("MediCare Hospital System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setUndecorated(false);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.BG_DARK);
        add(mainPanel, BorderLayout.CENTER);

        JPanel outerCard = new JPanel(new BorderLayout());
        outerCard.setBackground(Theme.BG_CARD);
        outerCard.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        outerCard.setPreferredSize(new Dimension(820, 520));

        // Left branding panel
        JPanel brandPanel = createBrandPanel();
        outerCard.add(brandPanel, BorderLayout.WEST);

        // Right form panel (card layout)
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(Theme.BG_CARD);
        cardContainer.setPreferredSize(new Dimension(420, 520));

        loginPanel = buildLoginPanel();
        registerPanel = buildRegisterPanel();

        cardContainer.add(loginPanel, "login");
        cardContainer.add(registerPanel, "register");
        cardLayout.show(cardContainer, "login");

        outerCard.add(cardContainer, BorderLayout.CENTER);
        mainPanel.add(outerCard);

        setVisible(true);
    }

    // ─── Brand Panel ─────────────────────────────────────────────
    private JPanel createBrandPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(14, 40, 80),
                    getWidth(), getHeight(), new Color(10, 20, 50)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // decorative circle top right
                g2.setColor(new Color(64, 156, 255, 20));
                g2.fillOval(220, -60, 220, 220);
                g2.setColor(new Color(32, 210, 175, 15));
                g2.fillOval(-40, 280, 200, 200);

                // grid dots
                g2.setColor(new Color(64, 156, 255, 25));
                for (int x = 20; x < getWidth(); x += 24)
                    for (int y = 20; y < getHeight(); y += 24)
                        g2.fillOval(x-1, y-1, 2, 2);

                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(380, 520));
        panel.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        // Logo icon
        JLabel icon = new JLabel("✚");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 52));
        icon.setForeground(Theme.ACCENT_TEAL);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("MediCare");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Hospital Management System");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(Box.createVerticalStrut(10));
        inner.add(title);
        inner.add(Box.createVerticalStrut(6));
        inner.add(subtitle);
        inner.add(Box.createVerticalStrut(36));

        // Features list
        String[] features = {"🔐  Secure Role-based Access",
                             "📅  Smart Appointment Booking",
                             "🏥  Multi-department Support",
                             "💊  Digital Prescriptions",
                             "📊  Real-time Analytics"};
        for (String f : features) {
            JLabel fl = new JLabel(f);
            fl.setFont(Theme.FONT_SMALL);
            fl.setForeground(new Color(160, 190, 230));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            inner.add(fl);
            inner.add(Box.createVerticalStrut(8));
        }

        panel.add(inner);
        return panel;
    }

    // ─── Login Panel ─────────────────────────────────────────────
    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("Welcome back");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emailLbl = UIComponents.fieldLabel("Email address");
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        StyledField emailField = new StyledField("email@hospital.com");
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLbl = UIComponents.fieldLabel("Password");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        StyledPasswordField passField = new StyledPasswordField();
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Role selector
        JLabel roleLbl = UIComponents.fieldLabel("Login as");
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        StyledCombo roleCombo = new StyledCombo(new String[]{"Patient", "Doctor", "Admin"});
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(Theme.FONT_SMALL);
        errLabel.setForeground(Theme.ACCENT_ROSE);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton loginBtn = new StyledButton("Sign In →", Theme.ACCENT_BLUE);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switchRow.setOpaque(false);
        switchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel switchLbl = UIComponents.mutedLabel("New patient? ");
        JButton switchBtn = makeTextButton("Create account");
        switchRow.add(switchLbl);
        switchRow.add(switchBtn);

        // Hint panel
        JPanel hintPanel = buildHintPanel();
        hintPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(header);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(28));
        form.add(emailLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(emailField);
        form.add(Box.createVerticalStrut(14));
        form.add(passLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(passField);
        form.add(Box.createVerticalStrut(14));
        form.add(roleLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(roleCombo);
        form.add(Box.createVerticalStrut(6));
        form.add(errLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(14));
        form.add(switchRow);
        form.add(Box.createVerticalStrut(14));
        form.add(hintPanel);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());
            String role  = roleCombo.getSelectedItem().toString().toLowerCase();

            User user = HospitalService.login(email, pass);
            if (user == null) {
                errLabel.setText("Invalid email or password.");
                return;
            }
            if (!user.role.equals(role)) {
                errLabel.setText("Role mismatch. You are registered as: " + user.role);
                return;
            }
            errLabel.setText(" ");
            try {
                openDashboard(user);
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                errLabel.setText("System Error: " + ex.getMessage());
            }
        });

        switchBtn.addActionListener(e -> cardLayout.show(cardContainer, "register"));
        panel.add(form);
        return panel;
    }

    private JPanel buildHintPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel hint = UIComponents.mutedLabel("Demo credentials:");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel h1 = UIComponents.mutedLabel("Patient:  raj@email.com / pass123");
        h1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel h2 = UIComponents.mutedLabel("Doctor:   arjun@hospital.com / doctor123");
        h2.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel h3 = UIComponents.mutedLabel("Admin:    admin@hospital.com / admin123");
        h3.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(hint); p.add(Box.createVerticalStrut(4));
        p.add(h1); p.add(h2); p.add(h3);
        return p;
    }

    // ─── Register Panel ──────────────────────────────────────────
    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("Create account");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Patient registration");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledField nameField  = new StyledField("Full name");
        StyledField emailField = new StyledField("Email address");
        StyledPasswordField passField = new StyledPasswordField();
        StyledField phoneField = new StyledField("Phone number");

        for (JComponent c : new JComponent[]{nameField, emailField, passField, phoneField}) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(Theme.FONT_SMALL);
        errLabel.setForeground(Theme.ACCENT_ROSE);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton regBtn = new StyledButton("Register →", Theme.ACCENT_TEAL);
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switchRow.setOpaque(false);
        switchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel switchLbl = UIComponents.mutedLabel("Have an account? ");
        JButton backBtn  = makeTextButton("Sign in");
        switchRow.add(switchLbl); switchRow.add(backBtn);

        String[] labels = {"Full name", "Email address", "Password", "Phone number"};
        JComponent[] fields = {nameField, emailField, passField, phoneField};

        form.add(header);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(20));

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = UIComponents.fieldLabel(labels[i]);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(lbl);
            form.add(Box.createVerticalStrut(5));
            form.add(fields[i]);
            form.add(Box.createVerticalStrut(10));
        }

        form.add(errLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(regBtn);
        form.add(Box.createVerticalStrut(14));
        form.add(switchRow);

        regBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());
            String phone = phoneField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
                errLabel.setText("All fields are required."); return;
            }
            if (!email.contains("@")) {
                errLabel.setText("Enter a valid email."); return;
            }
            if (HospitalService.emailExists(email)) {
                errLabel.setText("Email already registered."); return;
            }
            boolean success = HospitalService.registerPatient(name, email, pass, phone);
            if (!success) {
                errLabel.setText("Database error: Could not register patient.");
                return;
            }
            JOptionPane.showMessageDialog(this,
                "Account created! You can now sign in.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cardContainer, "login");
        });

        backBtn.addActionListener(e -> cardLayout.show(cardContainer, "login"));
        panel.add(form);
        return panel;
    }

    private JButton makeTextButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Theme.ACCENT_BLUE);
        btn.setBackground(null);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void openDashboard(User user) {
        switch (user.role) {
            case "patient": new PatientDashboard(user); break;
            case "doctor":  new DoctorDashboard(user);  break;
            case "admin":   new AdminDashboard(user);   break;
        }
    }
}
