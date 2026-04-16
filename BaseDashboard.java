package hospital.ui;
import hospital.model.DataStore;
import hospital.model.DataStore.*;

import hospital.service.HospitalService;
import hospital.util.Theme;
import hospital.util.UIComponents.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base dashboard frame with sidebar navigation.
 * All dashboards extend this.
 */
public abstract class BaseDashboard extends JFrame {

    protected User currentUser;
    protected JPanel contentArea;
    private CardLayout contentLayout;
    private Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activeNav = "";

    public BaseDashboard(User user, String title) {
        this.currentUser = user;
        setTitle("MediCare — " + title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        // Top bar
        add(buildTopBar(title), BorderLayout.NORTH);

        // Sidebar + content
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(Theme.BG_DARK);

        JPanel sidebar = buildSidebar();
        bodyPanel.add(sidebar, BorderLayout.WEST);

        contentLayout = new CardLayout();
        contentArea = new JPanel(contentLayout);
        contentArea.setBackground(Theme.BG_DARK);
        contentArea.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        bodyPanel.add(contentArea, BorderLayout.CENTER);

        add(bodyPanel, BorderLayout.CENTER);

        // Subclass adds panels
        buildPanels();

        setVisible(true);
    }

    // ─── Top Bar ─────────────────────────────────────────────────
    private JPanel buildTopBar(String title) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_SIDEBAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));
        bar.setPreferredSize(new Dimension(0, 56));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoPanel.setOpaque(false);
        JLabel logo = new JLabel("✚ MediCare");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Theme.ACCENT_TEAL);
        logoPanel.add(logo);

        // Right — user info + logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel roleTag = new JLabel(currentUser.role.toUpperCase());
        roleTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleTag.setForeground(Theme.ACCENT_BLUE);
        roleTag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_BLUE, 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        JLabel userLabel = new JLabel(currentUser.name);
        userLabel.setFont(Theme.FONT_BODY);
        userLabel.setForeground(Theme.TEXT_PRIMARY);

        OutlineButton logoutBtn = new OutlineButton("Logout", Theme.ACCENT_ROSE);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen();
        });

        rightPanel.add(roleTag);
        rightPanel.add(userLabel);
        rightPanel.add(logoutBtn);

        bar.add(logoPanel, BorderLayout.WEST);
        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    // ─── Sidebar ─────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(24, 0, 24, 0)
        ));
        sidebar.setPreferredSize(new Dimension(210, 0));

        // Avatar
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        avatarPanel.add(buildAvatar());
        sidebar.add(avatarPanel);

        JLabel nameLabel = new JLabel(currentUser.name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setMaximumSize(new Dimension(200, 24));

        JLabel mailLabel = new JLabel(currentUser.email, SwingConstants.CENTER);
        mailLabel.setFont(Theme.FONT_SMALL);
        mailLabel.setForeground(Theme.TEXT_MUTED);
        mailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mailLabel.setMaximumSize(new Dimension(200, 18));

        sidebar.add(nameLabel);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(mailLabel);
        sidebar.add(Box.createVerticalStrut(24));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_SUBTLE);
        sep.setMaximumSize(new Dimension(170, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        // Nav items added by subclass
        for (Map.Entry<String, String> entry : getNavItems().entrySet()) {
            JButton btn = buildNavButton(entry.getKey(), entry.getValue());
            navButtons.put(entry.getKey(), btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JLabel buildAvatar() {
        JLabel av = new JLabel(currentUser.name.substring(0, 1).toUpperCase(), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(64, 156, 255, 60));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.ACCENT_BLUE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, getWidth()-2, getHeight()-2);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        av.setFont(new Font("Segoe UI", Font.BOLD, 22));
        av.setForeground(Theme.ACCENT_BLUE);
        av.setPreferredSize(new Dimension(52, 52));
        return av;
    }

    private JButton buildNavButton(String key, String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = key.equals(activeNav);
                if (active) {
                    g2.setColor(new Color(64, 156, 255, 30));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                    g2.setColor(Theme.ACCENT_BLUE);
                    g2.fillRect(0, 6, 3, getHeight()-12);
                }
                if (getModel().isRollover() && !active) {
                    g2.setColor(Theme.BG_HOVER);
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(active(key) ? Theme.FONT_NAV_BOLD : Theme.FONT_NAV);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(190, 38));
        btn.setPreferredSize(new Dimension(190, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> showPage(key));
        return btn;
    }

    private boolean active(String key) { return key.equals(activeNav); }

    protected void showPage(String key) {
        activeNav = key;
        contentLayout.show(contentArea, key);
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            JButton b = e.getValue();
            b.setForeground(e.getKey().equals(key) ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            b.repaint();
        }
    }

    protected void addPage(String key, JPanel panel) {
        contentArea.add(panel, key);
        if (activeNav.isEmpty()) showPage(key);
    }

    /** Subclass must return ordered nav items: key → display label */
    protected abstract LinkedHashMap<String, String> getNavItems();

    /** Subclass must add all pages via addPage() */
    protected abstract void buildPanels();
}
