import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// ── Model ────────────────────────────────────────────────────────────────────

enum Category {
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚗"),
    BILLS("Bills", "💡"),
    SHOPPING("Shopping", "🛍"),
    HEALTH("Health", "💊"),
    OTHERS("Others", "📦");

    final String label;
    final String icon;

    Category(String label, String icon) {
        this.label = label;
        this.icon  = icon;
    }

    @Override public String toString() { return icon + "  " + label; }
}

record Expense(Category category, double amount, LocalDate date) {
    static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    String formattedAmount() { return String.format("₹%.2f", amount); }
    String formattedDate()   { return date.format(FMT); }
}

// ── Main Application ─────────────────────────────────────────────────────────

public class ExpenseTracker extends JFrame {

    // ── palette ──────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(15,  17,  23);
    private static final Color SURFACE   = new Color(24,  27,  36);
    private static final Color CARD      = new Color(31,  36,  48);
    private static final Color ACCENT    = new Color(99, 179, 237);
    private static final Color ACCENT2   = new Color(154, 230, 180);
    private static final Color DANGER    = new Color(252, 129, 129);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(113, 128, 150);
    private static final Color BORDER    = new Color(45,  55,  72);

    // ── state ─────────────────────────────────────────────────────────────────
    private final List<Expense> expenses = new ArrayList<>();

    // ── input widgets ─────────────────────────────────────────────────────────
    private JComboBox<Category> cbCategory;
    private JTextField          tfAmount;
    private JTextField          tfDate;
    private JComboBox<String>   cbFilter;

    // ── table ─────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            expenseTable;

    // ── summary labels ────────────────────────────────────────────────────────
    private JLabel lblTotal;
    private JLabel lblCount;

    // ─────────────────────────────────────────────────────────────────────────

    public ExpenseTracker() {
        super("Expense Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 580);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(null);
        applyGlobalTheme();

        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),     BorderLayout.NORTH);
        add(buildSidebar(),    BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFooter(),     BorderLayout.SOUTH);
    }

    // ── theme ─────────────────────────────────────────────────────────────────

    private void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        getContentPane().setBackground(BG);
        UIManager.put("Table.background",            SURFACE);
        UIManager.put("Table.foreground",            TEXT);
        UIManager.put("Table.gridColor",             BORDER);
        UIManager.put("Table.selectionBackground",   ACCENT.darker());
        UIManager.put("Table.selectionForeground",   Color.WHITE);
        UIManager.put("TableHeader.background",      CARD);
        UIManager.put("TableHeader.foreground",      ACCENT);
        UIManager.put("ScrollPane.background",       SURFACE);
        UIManager.put("Viewport.background",         SURFACE);
        UIManager.put("ComboBox.background",         CARD);
        UIManager.put("ComboBox.foreground",         TEXT);
        UIManager.put("TextField.background",        CARD);
        UIManager.put("TextField.foreground",        TEXT);
        UIManager.put("TextField.caretForeground",   ACCENT);
    }

    // ── header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SURFACE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel title = new JLabel("💸  Expense Tracker");
        title.setFont(new Font("Monospaced", Font.BOLD, 20));
        title.setForeground(TEXT);

        JLabel sub = new JLabel("Track every rupee.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);
        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── sidebar (inputs) ──────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SURFACE);
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, BORDER),
            new EmptyBorder(20, 20, 20, 20)
        ));
        sidebar.setPreferredSize(new Dimension(240, 0));

        // ─ category ─
        cbCategory = new JComboBox<>(Category.values());
        styleCombo(cbCategory);

        // ─ amount ─
        tfAmount = new JTextField();
        styleField(tfAmount, "0.00");

        // ─ date ─
        tfDate = new JTextField(LocalDate.now().toString());
        styleField(tfDate, "YYYY-MM-DD");

        // ─ filter ─
        String[] filterOptions = buildFilterOptions();
        cbFilter = new JComboBox<>(filterOptions);
        styleCombo(cbFilter);
        cbFilter.addActionListener(e -> refreshTable());

        // ─ buttons ─
        JButton btnAdd    = buildButton("＋  Add Expense",    ACCENT,  Color.BLACK);
        JButton btnDelete = buildButton("✕  Delete Selected", DANGER,  Color.BLACK);
        JButton btnClear  = buildButton("↺  Clear All",       MUTED,   Color.BLACK);

        btnAdd   .addActionListener(e -> addExpense());
        btnDelete.addActionListener(e -> deleteSelected());
        btnClear .addActionListener(e -> clearAll());

        // ─ layout ─
        sidebar.add(sectionLabel("CATEGORY"));
        sidebar.add(cbCategory);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(sectionLabel("AMOUNT (₹)"));
        sidebar.add(tfAmount);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(sectionLabel("DATE"));
        sidebar.add(tfDate);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(btnAdd);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnDelete);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnClear);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(sectionLabel("FILTER BY CATEGORY"));
        sidebar.add(cbFilter);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    // ── table panel ───────────────────────────────────────────────────────────

    private JPanel buildTablePanel() {
        tableModel = new DefaultTableModel(
            new String[]{"#", "Category", "Amount", "Date"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        expenseTable = new JTable(tableModel);
        expenseTable.setRowHeight(34);
        expenseTable.setShowVerticalLines(false);
        expenseTable.setIntercellSpacing(new Dimension(0, 1));
        expenseTable.setFont(new Font("Monospaced", Font.PLAIN, 13));
        expenseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        expenseTable.getTableHeader().setReorderingAllowed(false);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // column widths
        expenseTable.getColumnModel().getColumn(0).setMaxWidth(40);
        expenseTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        expenseTable.getColumnModel().getColumn(3).setPreferredWidth(110);

        // alternating row renderer
        expenseTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setOpaque(true);
                if (sel) {
                    setBackground(ACCENT.darker().darker());
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? SURFACE : CARD);
                    setForeground(col == 2 ? ACCENT2 : TEXT);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (col == 0) setHorizontalAlignment(CENTER);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(expenseTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 12, 0, 12));
        p.add(scroll);
        return p;
    }

    // ── footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SURFACE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER),
            new EmptyBorder(10, 24, 10, 24)
        ));

        lblCount = new JLabel("0 transactions");
        lblCount.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCount.setForeground(MUTED);

        lblTotal = new JLabel("Total: ₹0.00");
        lblTotal.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblTotal.setForeground(ACCENT2);

        p.add(lblCount, BorderLayout.WEST);
        p.add(lblTotal, BorderLayout.EAST);
        return p;
    }

    // ── actions ───────────────────────────────────────────────────────────────

    private void addExpense() {
        Category category = (Category) cbCategory.getSelectedItem();
        String   amtText  = tfAmount.getText().trim();
        String   dateText = tfDate.getText().trim();

        if (amtText.isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Amount must be a positive number.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText, Expense.FMT);
        } catch (DateTimeParseException ex) {
            showError("Date must be in YYYY-MM-DD format.");
            return;
        }

        expenses.add(new Expense(category, amount, date));
        tfAmount.setText("");
        refreshTable();
    }

    private void deleteSelected() {
        int viewRow = expenseTable.getSelectedRow();
        if (viewRow == -1) {
            showError("Please select a row to delete.");
            return;
        }
        // #-column holds the original 1-based index
        int originalIndex = (int) tableModel.getValueAt(viewRow, 0) - 1;
        expenses.remove(originalIndex);
        refreshTable();
    }

    private void clearAll() {
        if (expenses.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(
            this, "Delete all expenses?", "Clear All",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            expenses.clear();
            refreshTable();
        }
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    private void refreshTable() {
        tableModel.setRowCount(0);

        String filterLabel = (String) cbFilter.getSelectedItem();
        List<Expense> filtered = "All Categories".equals(filterLabel)
            ? expenses
            : expenses.stream()
                .filter(e -> e.category().label.equals(filterLabel))
                .collect(Collectors.toList());

        for (int i = 0; i < filtered.size(); i++) {
            Expense exp = filtered.get(i);
            tableModel.addRow(new Object[]{
                expenses.indexOf(exp) + 1,   // real index for delete
                exp.category().toString(),
                exp.formattedAmount(),
                exp.formattedDate()
            });
        }

        double total = filtered.stream().mapToDouble(Expense::amount).sum();
        lblTotal.setText(String.format("Total: ₹%.2f", total));
        lblCount.setText(filtered.size() + " transaction" + (filtered.size() != 1 ? "s" : ""));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String[] buildFilterOptions() {
        List<String> opts = new ArrayList<>();
        opts.add("All Categories");
        for (Category c : Category.values()) opts.add(c.label);
        return opts.toArray(new String[0]);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(MUTED);
        lbl.setBorder(new EmptyBorder(0, 2, 4, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleField(JTextField tf, String placeholder) {
        tf.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tf.setBackground(CARD);
        tf.setForeground(TEXT);
        tf.setCaretColor(ACCENT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(LEFT_ALIGNMENT);
    }

    private <T> void styleCombo(JComboBox<T> cb) {
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBackground(CARD);
        cb.setForeground(TEXT);
        cb.setBorder(new LineBorder(BORDER, 1, true));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cb.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton buildButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? bg.darker()  :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    // ── entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpenseTracker().setVisible(true));
    }
}
