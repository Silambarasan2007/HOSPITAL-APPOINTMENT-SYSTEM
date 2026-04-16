package hospital.util;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class UIComponents {

    // ─── Styled Button ──────────────────────────────────────────
    public static class StyledButton extends JButton {
        private Color bgColor;
        private Color hoverColor;
        private boolean hovered = false;

        public StyledButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            this.hoverColor = bg.brighter();
            setFont(Theme.FONT_BTN);
            setForeground(Theme.TEXT_ON_ACCENT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? hoverColor : bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 24, d.height + 10);
        }
    }

    // ─── Outlined Button ────────────────────────────────────────
    public static class OutlineButton extends JButton {
        private Color borderColor;
        private boolean hovered = false;

        public OutlineButton(String text, Color border) {
            super(text);
            this.borderColor = border;
            setFont(Theme.FONT_BTN);
            setForeground(border);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hovered) {
                g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            }
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight()-2, 10, 10));
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 24, d.height + 10);
        }
    }

    // ─── Styled Text Field ──────────────────────────────────────
    public static class StyledField extends JTextField {
        public StyledField(String placeholder) {
            setFont(Theme.FONT_BODY);
            setForeground(Theme.TEXT_PRIMARY);
            setBackground(Theme.BG_INPUT);
            setCaretColor(Theme.ACCENT_BLUE);
            setBorder(Theme.inputBorder());
            setPreferredSize(new Dimension(200, 40));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { setBorder(Theme.activeBorder()); }
                public void focusLost(FocusEvent e)   { setBorder(Theme.inputBorder()); }
            });
        }
    }

    // ─── Styled Password Field ──────────────────────────────────
    public static class StyledPasswordField extends JPasswordField {
        public StyledPasswordField() {
            setFont(Theme.FONT_BODY);
            setForeground(Theme.TEXT_PRIMARY);
            setBackground(Theme.BG_INPUT);
            setCaretColor(Theme.ACCENT_BLUE);
            setBorder(Theme.inputBorder());
            setPreferredSize(new Dimension(200, 40));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { setBorder(Theme.activeBorder()); }
                public void focusLost(FocusEvent e)   { setBorder(Theme.inputBorder()); }
            });
        }
    }

    // ─── Styled ComboBox ────────────────────────────────────────
    public static class StyledCombo extends JComboBox<String> {
        public StyledCombo(String[] items) {
            super(items);
            setFont(Theme.FONT_BODY);
            setForeground(Theme.TEXT_PRIMARY);
            setBackground(Theme.BG_INPUT);
            setBorder(Theme.inputBorder());
            setPreferredSize(new Dimension(200, 40));
            setRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean selected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, selected, cellHasFocus);
                    setBackground(selected ? Theme.BG_HOVER : Theme.BG_INPUT);
                    setForeground(Theme.TEXT_PRIMARY);
                    setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                    return this;
                }
            });
        }
    }

    // ─── Card Panel ─────────────────────────────────────────────
    public static class CardPanel extends JPanel {
        public CardPanel() {
            setBackground(Theme.BG_CARD);
            setBorder(Theme.cardBorder());
            setOpaque(true);
        }
    }

    // ─── Section Header ─────────────────────────────────────────
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SUBTITLE);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_LABEL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        return lbl;
    }

    // ─── Status Badge ───────────────────────────────────────────
    public static class StatusBadge extends JLabel {
        public StatusBadge(String status) {
            super(" " + status.toUpperCase() + " ");
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setForeground(Color.WHITE);
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = Theme.statusColor(getText().trim().toLowerCase());
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth()-1, getHeight()-1, 8, 8));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // ─── Styled Table ───────────────────────────────────────────
    public static JTable styledTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(Theme.BG_CARD);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(42);
        table.setGridColor(Theme.BORDER_SUBTLE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(Theme.BG_HOVER);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.BG_SIDEBAR);
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setFont(Theme.FONT_LABEL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE));
        header.setReorderingAllowed(false);

        return table;
    }

    // ─── Scrollable dark pane ────────────────────────────────────
    public static JScrollPane darkScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(Theme.BG_DARK);
        sp.getViewport().setBackground(Theme.BG_DARK);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setBackground(Theme.BG_CARD);
        sp.getHorizontalScrollBar().setBackground(Theme.BG_CARD);
        return sp;
    }

    // ─── Stat Card ──────────────────────────────────────────────
    public static JPanel statCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            )
        ));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLabel.setForeground(accent);

        JLabel lblLabel = new JLabel(label.toUpperCase());
        lblLabel.setFont(Theme.FONT_SMALL);
        lblLabel.setForeground(Theme.TEXT_SECONDARY);

        card.add(valLabel, BorderLayout.CENTER);
        card.add(lblLabel, BorderLayout.SOUTH);
        return card;
    }

    // ─── Divider ────────────────────────────────────────────────
    public static JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_SUBTLE);
        sep.setBackground(Theme.BORDER_SUBTLE);
        return sep;
    }

    // ─── Form row helper ────────────────────────────────────────
    public static JPanel formRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel lbl = fieldLabel(labelText);
        lbl.setPreferredSize(new Dimension(120, 40));
        field.setPreferredSize(new Dimension(300, 40));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }
}
