import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.*;

/**
 * CastilloVentas - Sistema de Ventas e Inventario
 * ─────────────────────────────────────────────────
 * Versión 2.0 — Interfaz Mejorada con Funcionalidades Extendidas
 *
 * Compilar : javac -encoding UTF-8 CastilloVentas.java
 * Ejecutar : java CastilloVentas
 *
 * Credenciales por defecto:
 *   admin    / admin123  (Administrador)
 *   vendedor / 1234      (Vendedor)
 */
public class CastilloVentas {

    // ══════════════════════════════════════════════════════════════════
    //  CONSTANTES DE DISEÑO  — Paleta de colores y fuentes
    // ══════════════════════════════════════════════════════════════════
    static final Color
        C_SIDEBAR    = new Color(0x1A237E),   // Azul oscuro sidebar
        C_SIDEBAR_H  = new Color(0x283593),   // Hover sidebar
        C_ACCENT     = new Color(0x3F51B5),   // Acento principal (indigo)
        C_ACCENT2    = new Color(0x5C6BC0),   // Acento secundario
        C_SUCCESS    = new Color(0x2E7D32),   // Verde éxito
        C_DANGER     = new Color(0xC62828),   // Rojo peligro
        C_WARNING    = new Color(0xE65100),   // Naranja advertencia
        C_FONDO      = new Color(0xF0F2F5),   // Fondo general
        C_CARD       = new Color(0xFFFFFF),   // Fondo tarjetas
        C_TEXT       = new Color(0x212121),   // Texto principal
        C_TEXT_LIGHT = new Color(0x757575),   // Texto secundario
        C_BORDER     = new Color(0xE0E0E0);   // Bordes

    static final Font
        F_TITLE  = new Font("Segoe UI", Font.BOLD, 26),
        F_HEADER = new Font("Segoe UI", Font.BOLD, 15),
        F_BODY   = new Font("Segoe UI", Font.PLAIN, 14),
        F_SMALL  = new Font("Segoe UI", Font.PLAIN, 12),
        F_BOLD   = new Font("Segoe UI", Font.BOLD, 14),
        F_MONO   = new Font("Consolas", Font.PLAIN, 13);

    static final DecimalFormat COP;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        COP = new DecimalFormat("$ #,##0", s);
    }

    static final File ARCH = new File("castilloventas.dat");

    // ══════════════════════════════════════════════════════════════════
    //  ESTADO GLOBAL
    // ══════════════════════════════════════════════════════════════════
    static HashMap<String, ArrayList<Object[]>> db = new HashMap<>();
    static JFrame ventana;
    static JPanel centro;
    static JPanel sidebarPanel;
    static String usuario, rol;
    static JButton btnActivo = null;

    // ══════════════════════════════════════════════════════════════════
    //  UTILITARIOS
    // ══════════════════════════════════════════════════════════════════
    static String cop(long v) { return COP.format(v) + " COP"; }

    /** SHA-256 de una cadena (para almacenar contraseñas) */
    static String sha256(String txt) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] h = d.digest(txt.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { return txt; }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PERSISTENCIA
    // ══════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    static void cargar() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ARCH))) {
            db = (HashMap<String, ArrayList<Object[]>>) in.readObject();
            migrarContrasenas();
        } catch (Exception e) { semilla(); guardar(); }
    }

    static void guardar() {
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(ARCH))) {
            o.writeObject(db);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /** Migra contraseñas en texto plano a SHA-256 */
    static void migrarContrasenas() {
        if (!db.containsKey("usuarios")) return;
        for (Object[] u : db.get("usuarios")) {
            String pass = (String) u[1];
            if (pass.length() != 64 || !pass.matches("[0-9a-f]+")) {
                u[1] = sha256(pass);
            }
        }
        guardar();
    }

    static void semilla() {
        db.put("productos", new ArrayList<>(List.of(
            new Object[]{1, "Camiseta Básica",    "Ropa",        35000L,  40},
            new Object[]{2, "Jean Clásico",       "Ropa",       120000L,  25},
            new Object[]{3, "Gorra Deportiva",    "Accesorios",  28000L,   3},
            new Object[]{4, "Tenis Urbanos",      "Calzado",    210000L,  15},
            new Object[]{5, "Morral Escolar",     "Accesorios",  85000L,  10},
            new Object[]{6, "Chaqueta de Cuero",  "Ropa",       350000L,   8},
            new Object[]{7, "Cinturón de Cuero",  "Accesorios",  45000L,  20},
            new Object[]{8, "Botas de Montaña",   "Calzado",    280000L,  12})));

        db.put("clientes", new ArrayList<>(List.of(
            new Object[]{1, "Juan Pérez",      "1098765432", "3101234567", "juan@email.com"},
            new Object[]{2, "María Gómez",     "52123456",   "3209876543", "maria@email.com"},
            new Object[]{3, "Carlos Rivera",   "80456789",   "3151122334", "carlos@email.com"},
            new Object[]{4, "Ana Martínez",    "1095123456", "3002233445", "ana@email.com"})));

        db.put("usuarios", new ArrayList<>(List.of(
            new Object[]{"admin",    sha256("admin123"), "Administrador", "Activo"},
            new Object[]{"vendedor", sha256("1234"),     "Vendedor",      "Activo"})));

        db.put("categorias", new ArrayList<>(List.of(
            new Object[]{"Ropa"}, new Object[]{"Calzado"}, new Object[]{"Accesorios"})));

        ArrayList<Object[]> v = new ArrayList<>();
        Random r = new Random(7);
        for (int i = 0; i < 14; i++) {
            long t = (r.nextInt(30) + 5) * 10000L;
            int desc = r.nextInt(4) * 5;
            v.add(new Object[]{
                i + 1,
                LocalDateTime.now().minusDays(i * 12L)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Cliente general",
                t,
                "Venta de ejemplo " + (i + 1),
                desc
            });
        }
        db.put("ventas", v);
    }

    static int sigId(List<Object[]> d) {
        int m = 0;
        for (Object[] r : d) if (r[0] instanceof Integer) m = Math.max(m, (Integer) r[0]);
        return m + 1;
    }

    // ══════════════════════════════════════════════════════════════════
    //  COMPONENTES DE ESTILO
    // ══════════════════════════════════════════════════════════════════
    static JButton btn(String txt, Color bg) {
        JButton b = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? bg.darker() :
                             getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        b.setFont(F_BOLD);
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 20, 10, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, 38));
        return b;
    }

    static JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8F9FA));
                else c.setBackground(new Color(0xC5CAE9));
                return c;
            }
        };
        t.setRowHeight(34);
        t.setFont(F_BODY);
        t.setSelectionBackground(new Color(0xC5CAE9));
        t.setSelectionForeground(C_TEXT);
        t.setGridColor(new Color(0xEEEEEE));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setShowVerticalLines(false);

        JTableHeader h = t.getTableHeader();
        h.setBackground(C_ACCENT);
        h.setForeground(Color.WHITE);
        h.setFont(F_BOLD);
        h.setPreferredSize(new Dimension(0, 40));
        h.setReorderingAllowed(false);
        return t;
    }

    static JPanel base(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(C_FONDO);
        p.setBorder(new EmptyBorder(25, 25, 20, 25));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel l = new JLabel(titulo);
        l.setFont(F_TITLE);
        l.setForeground(C_TEXT);
        headerPanel.add(l, BorderLayout.NORTH);

        if (subtitulo != null) {
            JLabel sub = new JLabel(subtitulo);
            sub.setFont(F_SMALL);
            sub.setForeground(C_TEXT_LIGHT);
            sub.setBorder(new EmptyBorder(2, 0, 0, 0));
            headerPanel.add(sub, BorderLayout.CENTER);
        }

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        sep.setBorder(new EmptyBorder(8, 0, 0, 0));
        headerPanel.add(sep, BorderLayout.SOUTH);

        p.add(headerPanel, BorderLayout.NORTH);
        return p;
    }

    static JPanel tarjeta(String titulo, String valor, Color bg, String icono) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(getWidth() - 60, -20, 100, 100);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel iconLabel = new JLabel(icono);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setForeground(new Color(255, 255, 255, 180));

        JPanel txtPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        txtPanel.setOpaque(false);

        JLabel valLabel = new JLabel(valor);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        valLabel.setForeground(Color.WHITE);

        JLabel titLabel = new JLabel(titulo.toUpperCase());
        titLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titLabel.setForeground(new Color(255, 255, 255, 200));

        txtPanel.add(valLabel);
        txtPanel.add(titLabel);

        p.add(iconLabel, BorderLayout.WEST);
        p.add(txtPanel, BorderLayout.CENTER);
        return p;
    }

    static JTextField campo(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(F_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        tf.setBackground(Color.WHITE);
        tf.setForeground(C_TEXT);
        return tf;
    }

    static JPanel campoConLabel(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(F_BOLD);
        l.setForeground(C_TEXT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOGIN
    // ══════════════════════════════════════════════════════════════════
    static void login() {
        JFrame f = new JFrame("CastilloVentas v2.0 — Acceso al Sistema");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);

        JPanel izq = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, C_SIDEBAR, 0, getHeight(), new Color(0x311B92));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(-40, -40, 200, 200);
                g2.fillOval(getWidth() - 100, getHeight() - 100, 200, 200);
                g2.dispose();
            }
        };

        JPanel logoBox = new JPanel();
        logoBox.setOpaque(false);
        logoBox.setLayout(new BoxLayout(logoBox, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel("🏪");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("<html><center><b style='font-size:28px'>Castillo<br>Ventas</b></center></html>");
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sistema de Ventas e Inventario");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 180));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ver = new JLabel("v2.0");
        ver.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ver.setForeground(new Color(0xB0BEC5));
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoBox.add(iconLabel);
        logoBox.add(Box.createVerticalStrut(10));
        logoBox.add(titulo);
        logoBox.add(Box.createVerticalStrut(6));
        logoBox.add(sub);
        logoBox.add(Box.createVerticalStrut(8));
        logoBox.add(ver);
        izq.add(logoBox);

        JPanel der = new JPanel();
        der.setLayout(new BoxLayout(der, BoxLayout.Y_AXIS));
        der.setBackground(Color.WHITE);
        der.setBorder(new EmptyBorder(50, 45, 50, 45));

        JLabel hdr = new JLabel("Iniciar Sesión");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 26));
        hdr.setForeground(C_SIDEBAR);
        hdr.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("Ingresa tus credenciales para continuar");
        desc.setFont(F_SMALL);
        desc.setForeground(C_TEXT_LIGHT);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField uField = campo("");
        uField.setAlignmentX(Component.LEFT_ALIGNMENT);
        uField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPasswordField pField = new JPasswordField();
        pField.setFont(F_BODY);
        pField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        pField.setAlignmentX(Component.LEFT_ALIGNMENT);
        pField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton okBtn = btn("  INGRESAR  →", C_ACCENT);
        okBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        okBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        okBtn.setPreferredSize(new Dimension(300, 45));

        JLabel msg = new JLabel(" ");
        msg.setFont(F_SMALL);
        msg.setForeground(C_DANGER);
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);

        ActionListener doLogin = e -> {
            String u = uField.getText().trim();
            String p = sha256(new String(pField.getPassword()));
            for (Object[] r : db.get("usuarios")) {
                if (r[0].equals(u) && r[1].equals(p) && r[3].equals("Activo")) {
                    usuario = (String) r[0];
                    rol = (String) r[2];
                    f.dispose();
                    abrir();
                    return;
                }
            }
            msg.setText("⚠ Usuario o contraseña incorrectos");
            pField.setText("");
            pField.requestFocus();
        };

        okBtn.addActionListener(doLogin);
        pField.addActionListener(doLogin);

        der.add(hdr);
        der.add(Box.createVerticalStrut(4));
        der.add(desc);
        der.add(Box.createVerticalStrut(30));
        der.add(new JLabel("Usuario (admin / vendedor)"));
        der.add(Box.createVerticalStrut(5));
        der.add(uField);
        der.add(Box.createVerticalStrut(14));
        der.add(new JLabel("Contraseña"));
        der.add(Box.createVerticalStrut(5));
        der.add(pField);
        der.add(Box.createVerticalStrut(20));
        der.add(okBtn);
        der.add(Box.createVerticalStrut(10));
        der.add(msg);

        f.setLayout(new GridLayout(1, 2));
        f.add(izq);
        f.add(der);
        f.setSize(820, 480);
        f.setLocationRelativeTo(null);
        f.setVisible(true);

        SwingUtilities.invokeLater(uField::requestFocus);
    }

    // ══════════════════════════════════════════════════════════════════
    //  VENTANA PRINCIPAL
    // ══════════════════════════════════════════════════════════════════
    static void abrir() {
        ventana = new JFrame("CastilloVentas v2.0 — Sistema de Ventas");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        sidebarPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, C_SIDEBAR, 0, getHeight(), new Color(0x1A1A5E));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(230, 0));

        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(24, 16, 16, 16));
        logoPanel.setMaximumSize(new Dimension(230, 100));

        JLabel sIcon = new JLabel("🏪  CastilloVentas");
        sIcon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 17));
        sIcon.setForeground(Color.WHITE);
        sIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sUser = new JLabel("   " + usuario + "  •  " + rol);
        sUser.setFont(F_SMALL);
        sUser.setForeground(new Color(0xB0BEC5));
        sUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(sIcon);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(sUser);
        sidebarPanel.add(logoPanel);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 40));
        sep.setMaximumSize(new Dimension(230, 2));
        sidebarPanel.add(sep);
        sidebarPanel.add(Box.createVerticalStrut(8));

        record MenuItem(String emoji, String label) {}
        List<MenuItem> items = new ArrayList<>(List.of(
            new MenuItem("📊", "Inicio"),
            new MenuItem("📦", "Productos"),
            new MenuItem("👥", "Clientes"),
            new MenuItem("🛒", "Nueva Venta"),
            new MenuItem("📈", "Reportes")
        ));
        if ("Administrador".equals(rol)) items.add(new MenuItem("🔧", "Usuarios"));
        items.add(new MenuItem("🚪", "Salir"));

        for (MenuItem item : items) {
            JButton b = new JButton(item.emoji + "  " + item.label) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = (btnActivo == this)
                        ? C_ACCENT
                        : getModel().isRollover() ? C_SIDEBAR_H : new Color(0,0,0,0);
                    if (btnActivo == this || getModel().isRollover()) {
                        g2.setColor(bg);
                        g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
                    }
                    if (btnActivo == this) {
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            b.setForeground(new Color(0xCFD8DC));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setBorder(new EmptyBorder(11, 20, 11, 10));
            b.setMaximumSize(new Dimension(230, 48));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));

            b.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { b.setForeground(Color.WHITE); b.repaint(); }
                @Override public void mouseExited(MouseEvent e) {
                    b.setForeground(btnActivo == b ? Color.WHITE : new Color(0xCFD8DC));
                    b.repaint();
                }
            });

            b.addActionListener(e -> {
                if (item.label.equals("Salir")) {
                    int c = JOptionPane.showConfirmDialog(ventana, "¿Deseas cerrar sesión?", "Salir", JOptionPane.YES_NO_OPTION);
                    if (c != JOptionPane.YES_OPTION) return;
                    ventana.dispose();
                    login();
                } else {
                    if (btnActivo != null) {
                        btnActivo.setForeground(new Color(0xCFD8DC));
                        btnActivo.repaint();
                    }
                    btnActivo = b;
                    b.setForeground(Color.WHITE);
                    b.repaint();
                    ir(item.label);
                }
            });

            sidebarPanel.add(b);
        }
        sidebarPanel.add(Box.createVerticalGlue());

        JLabel pie = new JLabel("  v2.0 — Sistema de Ventas");
        pie.setFont(F_SMALL);
        pie.setForeground(new Color(255, 255, 255, 60));
        pie.setBorder(new EmptyBorder(10, 16, 14, 0));
        sidebarPanel.add(pie);

        centro = new JPanel(new BorderLayout());
        centro.setBackground(C_FONDO);

        ventana.add(sidebarPanel, BorderLayout.WEST);
        ventana.add(centro, BorderLayout.CENTER);
        ventana.setSize(1280, 760);
        ventana.setLocationRelativeTo(null);
        ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        ir("Inicio");
        for (Component c : sidebarPanel.getComponents()) {
            if (c instanceof JButton b2 && b2.getText().contains("Inicio")) {
                btnActivo = b2;
                b2.setForeground(Color.WHITE);
                b2.repaint();
                break;
            }
        }
    }

    static void ir(String m) {
        centro.removeAll();
        JPanel panel = switch (m) {
            case "Productos"   -> panelProductos();
            case "Clientes"    -> panelClientes();
            case "Usuarios"    -> panelUsuarios();
            case "Nueva Venta" -> venta();
            case "Reportes"    -> reportes();
            default            -> inicio();
        };
        centro.add(panel, BorderLayout.CENTER);
        centro.revalidate();
        centro.repaint();
    }

    // ══════════════════════════════════════════════════════════════════
    //  DASHBOARD DE INICIO
    // ══════════════════════════════════════════════════════════════════
    static JPanel inicio() {
        JPanel p = base("Panel de Control", "Bienvenido, " + usuario + " · " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", new Locale("es"))));

        String hoy = LocalDate.now().toString(), mes = hoy.substring(0, 7);
        long vh = 0, vm = 0, tot = 0;
        int nv = 0;
        for (Object[] v : db.get("ventas")) {
            String fec = (String) v[1]; long t = (Long) v[3]; tot += t; nv++;
            if (fec.startsWith(hoy)) vh += t;
            if (fec.startsWith(mes)) vm += t;
        }
        long bajo = db.get("productos").stream().filter(r -> (Integer) r[4] <= 5).count();

        JPanel kpiPanel = new JPanel(new GridLayout(2, 4, 14, 14));
        kpiPanel.setOpaque(false);
        kpiPanel.setPreferredSize(new Dimension(0, 220));

        kpiPanel.add(tarjeta("Ventas de Hoy",     cop(vh),             new Color(0x1565C0), "💰"));
        kpiPanel.add(tarjeta("Ventas del Mes",     cop(vm),             new Color(0xB71C1C), "📅"));
        kpiPanel.add(tarjeta("Total Acumulado",    cop(tot),            new Color(0x2E7D32), "📈"));
        kpiPanel.add(tarjeta("Ticket Promedio",    cop(nv==0?0:tot/nv), new Color(0x4527A0), "🎫"));
        kpiPanel.add(tarjeta("Productos",          "" + db.get("productos").size(), new Color(0xE65100), "📦"));
        kpiPanel.add(tarjeta("Clientes",           "" + db.get("clientes").size(),  new Color(0x00695C), "👥"));
        kpiPanel.add(tarjeta("Stock Bajo ≤5",      "" + bajo,           new Color(0xC62828), "⚠"));
        kpiPanel.add(tarjeta("N° de Ventas",       "" + nv,             new Color(0x6A1B9A), "🛒"));

        JPanel graf = new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                long[] totals = new long[6];
                String[] etiquetas = new String[6];
                long maxVal = 1;
                YearMonth ahora = YearMonth.now();

                for (int i = 0; i < 6; i++) {
                    YearMonth ym = ahora.minusMonths(5 - i);
                    etiquetas[i] = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es"));
                    for (Object[] v : db.get("ventas"))
                        if (((String) v[1]).startsWith(ym.toString())) totals[i] += (Long) v[3];
                    maxVal = Math.max(maxVal, totals[i]);
                }

                int W = getWidth(), H = getHeight();
                int padL = 70, padR = 20, padT = 50, padB = 50;
                int chartW = W - padL - padR;
                int chartH = H - padT - padB;
                int barW = Math.max(1, chartW / 6);

                g.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g.setColor(C_TEXT);
                g.drawString("📊  Ventas Comparativas — Últimos 6 Meses", padL, 30);

                g.setFont(F_SMALL);
                for (int i = 0; i <= 4; i++) {
                    int y = padT + chartH - (chartH * i / 4);
                    g.setColor(new Color(0xEEEEEE));
                    g.drawLine(padL, y, W - padR, y);
                    g.setColor(C_TEXT_LIGHT);
                    g.drawString(COP.format(maxVal * i / 4), 5, y + 4);
                }

                for (int i = 0; i < 6; i++) {
                    int alt = (int) ((double) chartH * totals[i] / maxVal);
                    int x = padL + i * barW + barW / 6;
                    int bw = barW * 2 / 3;
                    int y = padT + chartH - alt;

                    g.setColor(new Color(0, 0, 0, 20));
                    g.fillRoundRect(x + 3, y + 3, bw, alt, 8, 8);

                    Color c1 = i == 5 ? new Color(0x43A047) : C_ACCENT;
                    Color c2 = i == 5 ? new Color(0x81C784) : C_ACCENT2;
                    GradientPaint gp = new GradientPaint(x, y, c1, x, y + alt, c2);
                    g.setPaint(gp);
                    g.fillRoundRect(x, y, bw, alt, 8, 8);

                    if (totals[i] > 0) {
                        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        g.setColor(C_TEXT);
                        String val = "$" + (totals[i] / 1000) + "K";
                        g.drawString(val, x + bw / 2 - g.getFontMetrics().stringWidth(val) / 2, y - 5);
                    }

                    g.setFont(F_BOLD);
                    g.setColor(i == 5 ? new Color(0x2E7D32) : C_TEXT);
                    int ew = g.getFontMetrics().stringWidth(etiquetas[i]);
                    g.drawString(etiquetas[i], x + bw / 2 - ew / 2, H - padB + 20);
                }
            }
        };
        graf.setBackground(Color.WHITE);
        graf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(5, 5, 5, 5)));

        JPanel alertas = new JPanel(new BorderLayout(0, 8));
        alertas.setBackground(Color.WHITE);
        alertas.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFFCDD2), 1, true),
            new EmptyBorder(12, 14, 12, 14)));

        JLabel tAlert = new JLabel("⚠  Alertas de Stock Bajo (≤ 5 unidades)");
        tAlert.setFont(F_BOLD);
        tAlert.setForeground(C_DANGER);
        alertas.add(tAlert, BorderLayout.NORTH);

        JPanel listaAlerta = new JPanel();
        listaAlerta.setLayout(new BoxLayout(listaAlerta, BoxLayout.Y_AXIS));
        listaAlerta.setOpaque(false);

        List<Object[]> bajoStock = db.get("productos").stream()
            .filter(r -> (Integer) r[4] <= 5).toList();

        if (bajoStock.isEmpty()) {
            JLabel ok = new JLabel("  ✅  Todos los productos tienen stock suficiente");
            ok.setFont(F_BODY);
            ok.setForeground(C_SUCCESS);
            listaAlerta.add(ok);
        } else {
            for (Object[] pr : bajoStock) {
                JLabel item = new JLabel(String.format("  • %s  ─  Stock: %d  ─  %s",
                    pr[1], pr[4], pr[2]));
                item.setFont(F_BODY);
                item.setForeground(C_TEXT);
                listaAlerta.add(item);
            }
        }
        alertas.add(listaAlerta, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setOpaque(false);
        bottom.add(graf);
        bottom.add(alertas);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(kpiPanel, BorderLayout.NORTH);
        content.add(bottom, BorderLayout.CENTER);

        p.add(content, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  CRUD PRODUCTOS
    // ══════════════════════════════════════════════════════════════════
    static JPanel panelProductos() {
        JPanel p = base("Gestión de Productos", "Administra tu catálogo de productos e inventario");
        List<Object[]> d = db.get("productos");

        String[] cols = {"ID", "Nombre", "Categoría", "Precio (COP)", "Stock"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = tabla(m);

        List<String> cats = new ArrayList<>();
        cats.add("Todas");
        if (db.containsKey("categorias")) {
            for (Object[] cat : db.get("categorias")) cats.add((String) cat[0]);
        }

        JTextField bus = campo("");
        bus.setPreferredSize(new Dimension(220, 36));

        JComboBox<String> filtroCat = new JComboBox<>(cats.toArray(new String[0]));
        filtroCat.setFont(F_BODY);
        filtroCat.setPreferredSize(new Dimension(150, 36));

        List<Object[]> vis = new ArrayList<>();

        Runnable refrescar = () -> {
            m.setRowCount(0);
            vis.clear();
            String q = bus.getText().toLowerCase();
            String catSel = (String) filtroCat.getSelectedItem();
            for (Object[] r : d) {
                if (!Arrays.toString(r).toLowerCase().contains(q)) continue;
                if (!"Todas".equals(catSel) && !r[2].equals(catSel)) continue;
                vis.add(r);
                m.addRow(new Object[]{r[0], r[1], r[2], cop((Long) r[3]), r[4]});
            }
        };

        Consumer<Object[]> formulario = fila -> {
            JDialog dlg = new JDialog(ventana, fila == null ? "Nuevo Producto" : "Editar Producto", true);
            dlg.setLayout(new BorderLayout());
            dlg.setSize(420, 380);
            dlg.setLocationRelativeTo(ventana);

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
            form.setBorder(new EmptyBorder(20, 24, 10, 24));
            form.setBackground(Color.WHITE);

            JTextField nombre = campo("");
            nombre.setText(fila == null ? "" : (String) fila[1]);

            String[] catsArr = cats.stream().filter(c -> !c.equals("Todas")).toArray(String[]::new);
            JComboBox<String> catCombo = new JComboBox<>(catsArr);
            catCombo.setFont(F_BODY);
            if (fila != null) catCombo.setSelectedItem(fila[2]);

            JTextField precio = campo("");
            precio.setText(fila == null ? "" : String.valueOf(fila[3]));

            JTextField stock = campo("");
            stock.setText(fila == null ? "" : String.valueOf(fila[4]));

            form.add(campoConLabel("Nombre del producto", nombre));
            form.add(campoConLabel("Categoría", catCombo));
            form.add(campoConLabel("Precio (COP, sin puntos)", precio));
            form.add(campoConLabel("Stock inicial", stock));

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
            btnPanel.setBackground(new Color(0xF5F5F5));
            btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
            JButton cancelar = btn("Cancelar", C_TEXT_LIGHT);
            JButton guardar2 = btn("Guardar", C_ACCENT);
            btnPanel.add(cancelar);
            btnPanel.add(guardar2);

            cancelar.addActionListener(ev -> dlg.dispose());
            guardar2.addActionListener(ev -> {
                try {
                    String nom = nombre.getText().trim();
                    String cat = (String) catCombo.getSelectedItem();
                    long pr = Long.parseLong(precio.getText().trim());
                    int st = Integer.parseInt(stock.getText().trim());
                    if (nom.isEmpty()) throw new Exception("El nombre no puede estar vacío.");
                    if (pr < 0 || st < 0) throw new Exception("Precio y stock no pueden ser negativos.");

                    Object[] nuevo = {fila == null ? sigId(d) : fila[0], nom, cat, pr, st};
                    if (fila == null) d.add(nuevo);
                    else d.set(d.indexOf(fila), nuevo);
                    guardar();
                    refrescar.run();
                    dlg.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "El precio y stock deben ser números enteros sin puntos.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dlg.add(form, BorderLayout.CENTER);
            dlg.add(btnPanel, BorderLayout.SOUTH);
            dlg.setVisible(true);
        };

        JButton btnNuevo    = btn("+ Nuevo Producto", new Color(0x2E7D32));
        JButton btnEditar   = btn("✏ Editar",         C_ACCENT);
        JButton btnEliminar = btn("🗑 Eliminar",       C_DANGER);

        btnNuevo.addActionListener(e -> formulario.accept(null));
        btnEditar.addActionListener(e -> {
            int i = t.getSelectedRow();
            if (i < 0) { JOptionPane.showMessageDialog(ventana, "Selecciona un producto.", "Aviso", JOptionPane.INFORMATION_MESSAGE); return; }
            formulario.accept(vis.get(i));
        });
        btnEliminar.addActionListener(e -> {
            int i = t.getSelectedRow();
            if (i < 0) { JOptionPane.showMessageDialog(ventana, "Selecciona un producto.", "Aviso", JOptionPane.INFORMATION_MESSAGE); return; }
            int c = JOptionPane.showConfirmDialog(ventana, "¿Eliminar \"" + vis.get(i)[1] + "\"?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c == JOptionPane.YES_OPTION) { d.remove(vis.get(i)); guardar(); refrescar.run(); }
        });

        bus.addKeyListener(new KeyAdapter() { @Override public void keyReleased(KeyEvent e) { refrescar.run(); } });
        filtroCat.addActionListener(e -> refrescar.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.setOpaque(false);
        top.add(new JLabel("🔍"));
        top.add(bus);
        top.add(new JLabel("Categoría:"));
        top.add(filtroCat);
        top.add(Box.createHorizontalStrut(10));
        top.add(btnNuevo);
        top.add(btnEditar);
        top.add(btnEliminar);

        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(new LineBorder(C_BORDER));

        JPanel c = new JPanel(new BorderLayout(0, 10));
        c.setOpaque(false);
        c.add(top, BorderLayout.NORTH);
        c.add(scroll, BorderLayout.CENTER);

        p.add(c, BorderLayout.CENTER);
        refrescar.run();
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  CRUD CLIENTES
    // ══════════════════════════════════════════════════════════════════
    static JPanel panelClientes() {
        JPanel p = base("Gestión de Clientes", "Administra tu base de clientes");
        List<Object[]> d = db.get("clientes");
        String[] cols = {"ID", "Nombre", "Documento", "Teléfono", "Correo"};

        for (Object[] cli : d) if (cli.length < 5) {
            Object[] nuevo = Arrays.copyOf(cli, 5);
            nuevo[4] = "";
            d.set(d.indexOf(cli), nuevo);
        }

        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = tabla(m);
        JTextField bus = campo("");
        bus.setPreferredSize(new Dimension(250, 36));

        List<Object[]> vis = new ArrayList<>();
        Runnable refrescar = () -> {
            m.setRowCount(0); vis.clear();
            String q = bus.getText().toLowerCase();
            for (Object[] r : d) {
                if (!Arrays.toString(r).toLowerCase().contains(q)) continue;
                vis.add(r);
                m.addRow(r);
            }
        };

        Consumer<Object[]> formulario = fila -> {
            JTextField nombre = campo(""); nombre.setText(fila == null ? "" : (String) fila[1]);
            JTextField doc    = campo(""); doc.setText(fila == null ? "" : (String) fila[2]);
            JTextField tel    = campo(""); tel.setText(fila == null ? "" : (String) fila[3]);
            JTextField email  = campo(""); email.setText(fila == null ? "" : (String) fila[4]);

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            form.setBackground(Color.WHITE);
            form.add(campoConLabel("Nombre completo", nombre));
            form.add(campoConLabel("Número de documento", doc));
            form.add(campoConLabel("Teléfono", tel));
            form.add(campoConLabel("Correo electrónico", email));

            int res = JOptionPane.showConfirmDialog(ventana, form, fila == null ? "Nuevo Cliente" : "Editar Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            try {
                String nom = nombre.getText().trim(), dc = doc.getText().trim();
                String tl = tel.getText().trim(), em = email.getText().trim();
                if (nom.isEmpty() || dc.isEmpty()) throw new Exception();
                Object[] nuevo = {fila == null ? sigId(d) : fila[0], nom, dc, tl, em};
                if (fila == null) d.add(nuevo);
                else d.set(d.indexOf(fila), nuevo);
                guardar(); refrescar.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ventana, "Nombre y documento son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        JButton btnNuevo    = btn("+ Nuevo Cliente", new Color(0x2E7D32));
        JButton btnEditar   = btn("✏ Editar",         C_ACCENT);
        JButton btnEliminar = btn("🗑 Eliminar",        C_DANGER);

        btnNuevo.addActionListener(e -> formulario.accept(null));
        btnEditar.addActionListener(e -> { int i = t.getSelectedRow(); if (i < 0) JOptionPane.showMessageDialog(ventana, "Selecciona un cliente."); else formulario.accept(vis.get(i)); });
        btnEliminar.addActionListener(e -> {
            int i = t.getSelectedRow();
            if (i < 0) { JOptionPane.showMessageDialog(ventana, "Selecciona un cliente."); return; }
            if (JOptionPane.showConfirmDialog(ventana, "¿Eliminar a \"" + vis.get(i)[1] + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                d.remove(vis.get(i)); guardar(); refrescar.run();
            }
        });

        bus.addKeyListener(new KeyAdapter() { @Override public void keyReleased(KeyEvent e) { refrescar.run(); } });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.setOpaque(false);
        top.add(new JLabel("🔍")); top.add(bus); top.add(btnNuevo); top.add(btnEditar); top.add(btnEliminar);

        JPanel c = new JPanel(new BorderLayout(0, 10)); c.setOpaque(false);
        c.add(top, BorderLayout.NORTH);
        c.add(new JScrollPane(t), BorderLayout.CENTER);
        p.add(c, BorderLayout.CENTER);
        refrescar.run();
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  CRUD USUARIOS
    // ══════════════════════════════════════════════════════════════════
    static JPanel panelUsuarios() {
        JPanel p = base("Gestión de Usuarios", "Solo el administrador puede gestionar usuarios");
        List<Object[]> d = db.get("usuarios");
        String[] cols = {"Usuario", "Contraseña (hash)", "Rol", "Estado"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = tabla(m);

        Runnable refrescar = () -> {
            m.setRowCount(0);
            for (Object[] r : d)
                m.addRow(new Object[]{r[0], "●●●●●●●●", r[2], r[3]});
        };

        Consumer<Object[]> formulario = fila -> {
            JTextField user   = campo(""); user.setText(fila == null ? "" : (String) fila[0]);
            JPasswordField pass = new JPasswordField();
            pass.setFont(F_BODY);
            pass.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));

            String[] roles = {"Administrador", "Vendedor"};
            JComboBox<String> rolCombo = new JComboBox<>(roles);
            rolCombo.setFont(F_BODY);
            if (fila != null) rolCombo.setSelectedItem(fila[2]);

            String[] estados = {"Activo", "Inactivo"};
            JComboBox<String> estCombo = new JComboBox<>(estados);
            estCombo.setFont(F_BODY);
            if (fila != null) estCombo.setSelectedItem(fila[3]);

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            form.setBackground(Color.WHITE);
            form.add(campoConLabel("Nombre de usuario", user));
            form.add(campoConLabel("Nueva contraseña" + (fila != null ? " (dejar vacío = sin cambio)" : ""), pass));
            form.add(campoConLabel("Rol", rolCombo));
            form.add(campoConLabel("Estado", estCombo));

            int res = JOptionPane.showConfirmDialog(ventana, form, fila == null ? "Nuevo Usuario" : "Editar Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            String u = user.getText().trim();
            String pw = new String(pass.getPassword()).trim();
            if (u.isEmpty()) { JOptionPane.showMessageDialog(ventana, "El usuario es obligatorio."); return; }
            if (fila == null && pw.isEmpty()) { JOptionPane.showMessageDialog(ventana, "La contraseña es obligatoria para nuevos usuarios."); return; }

            String hashPw = pw.isEmpty() ? (String) fila[1] : sha256(pw);
            Object[] nuevo = {u, hashPw, rolCombo.getSelectedItem(), estCombo.getSelectedItem()};
            if (fila == null) d.add(nuevo);
            else d.set(d.indexOf(fila), nuevo);
            guardar(); refrescar.run();
        };

        JButton btnNuevo    = btn("+ Nuevo Usuario", new Color(0x2E7D32));
        JButton btnEditar   = btn("✏ Editar",         C_ACCENT);
        JButton btnEliminar = btn("🗑 Eliminar",        C_DANGER);

        btnNuevo.addActionListener(e -> formulario.accept(null));
        btnEditar.addActionListener(e -> { int i = t.getSelectedRow(); if (i < 0) JOptionPane.showMessageDialog(ventana, "Selecciona un usuario."); else formulario.accept(d.get(i)); });
        btnEliminar.addActionListener(e -> {
            int i = t.getSelectedRow();
            if (i < 0) { JOptionPane.showMessageDialog(ventana, "Selecciona un usuario."); return; }
            if (d.get(i)[0].equals(usuario)) { JOptionPane.showMessageDialog(ventana, "No puedes eliminarte a ti mismo."); return; }
            if (JOptionPane.showConfirmDialog(ventana, "¿Eliminar usuario \"" + d.get(i)[0] + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                d.remove(i); guardar(); refrescar.run();
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.setOpaque(false);
        top.add(btnNuevo); top.add(btnEditar); top.add(btnEliminar);

        JPanel c = new JPanel(new BorderLayout(0, 10)); c.setOpaque(false);
        c.add(top, BorderLayout.NORTH);
        c.add(new JScrollPane(t), BorderLayout.CENTER);
        p.add(c, BorderLayout.CENTER);
        refrescar.run();
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  NUEVA VENTA CON DESCUENTOS
    // ══════════════════════════════════════════════════════════════════
    static JPanel venta() {
        JPanel p = base("Registrar Nueva Venta", "Selecciona productos y aplica descuentos");
        List<Object[]> prods = db.get("productos");
        List<Object[]> carro = new ArrayList<>();

        JComboBox<String> cliCombo = new JComboBox<>();
        cliCombo.addItem("Cliente general");
        cliCombo.setFont(F_BODY);
        for (Object[] c : db.get("clientes")) cliCombo.addItem(c[1] + " (" + c[2] + ")");

        JComboBox<String> proCombo = new JComboBox<>();
        proCombo.setFont(F_BODY);
        for (Object[] r : prods) proCombo.addItem(r[1] + "  —  " + cop((Long) r[3]) + "  [stock: " + r[4] + "]");

        JSpinner cantSpin = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        ((JSpinner.DefaultEditor) cantSpin.getEditor()).getTextField().setFont(F_BODY);

        Integer[] descuentos = {0, 5, 10, 15, 20, 25, 30};
        JComboBox<Integer> descCombo = new JComboBox<>(descuentos);
        descCombo.setFont(F_BODY);
        descCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setText(v + "%");
                return this;
            }
        });

        DefaultTableModel m = new DefaultTableModel(
            new String[]{"Producto", "Cant.", "P. Unitario", "Desc. %", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaCarrito = tabla(m);

        JLabel lblSub   = new JLabel("Subtotal: $0");   lblSub.setFont(F_BODY);
        JLabel lblDesc  = new JLabel("Descuento: $0");  lblDesc.setFont(F_BODY); lblDesc.setForeground(C_SUCCESS);
        JLabel lblIva   = new JLabel("IVA (19%): $0");  lblIva.setFont(F_BODY);  lblIva.setForeground(C_WARNING);
        JLabel lblTotal = new JLabel("TOTAL: $0");      lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20)); lblTotal.setForeground(C_SUCCESS);

        long[] totalFinal = {0};

        Runnable calcular = () -> {
            m.setRowCount(0);
            long subRaw = 0, descTotal = 0;
            for (Object[] item : carro) {
                long precio = (Long) item[2];
                int cant = (Integer) item[1];
                int desc = (Integer) item[3];
                long sub = precio * cant;
                long descAmt = sub * desc / 100;
                long subConDesc = sub - descAmt;
                subRaw += sub;
                descTotal += descAmt;
                m.addRow(new Object[]{item[0], cant, cop(precio), desc + "%", cop(subConDesc)});
            }
            long subConDesc = subRaw - descTotal;
            long iva = Math.round(subConDesc * 0.19);
            totalFinal[0] = subConDesc + iva;

            lblSub.setText("Subtotal bruto: " + cop(subRaw));
            lblDesc.setText("Descuento total: -" + cop(descTotal));
            lblIva.setText("IVA (19%): +" + cop(iva));
            lblTotal.setText("TOTAL: " + cop(totalFinal[0]));
        };

        JButton btnAdd    = btn("+ Agregar al Carrito", C_ACCENT);
        JButton btnQuitar = btn("✕ Quitar", C_DANGER);
        JButton btnCobrar = btn("💰 COBRAR VENTA", C_SUCCESS);

        btnAdd.addActionListener(e -> {
            if (prods.isEmpty()) return;
            int idxProd = proCombo.getSelectedIndex();
            Object[] pr = prods.get(idxProd);
            int q = (Integer) cantSpin.getValue();
            int desc = (Integer) descCombo.getSelectedItem();

            Object[] ex = null;
            for (Object[] r : carro) if (r[0].equals(pr[1])) { ex = r; break; }
            int ya = ex == null ? 0 : (Integer) ex[1];

            if (ya + q > (Integer) pr[4]) {
                JOptionPane.showMessageDialog(ventana, "Stock insuficiente. Disponible: " + pr[4], "Sin stock", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ex == null) carro.add(new Object[]{pr[1], q, pr[3], desc});
            else { ex[1] = ya + q; ex[3] = desc; }
            calcular.run();
        });

        btnQuitar.addActionListener(e -> {
            int i = tablaCarrito.getSelectedRow();
            if (i >= 0) { carro.remove(i); calcular.run(); }
        });

        btnCobrar.addActionListener(e -> {
            if (carro.isEmpty()) { JOptionPane.showMessageDialog(ventana, "El carrito está vacío.", "Aviso", JOptionPane.INFORMATION_MESSAGE); return; }
            StringBuilder det = new StringBuilder();
            for (Object[] item : carro) {
                for (Object[] pr : prods)
                    if (pr[1].equals(item[0])) pr[4] = (Integer) pr[4] - (Integer) item[1];
                det.append(item[1]).append("x ").append(item[0]).append("; ");
            }
            int descGlobal = carro.stream().mapToInt(r -> (Integer) r[3]).max().orElse(0);
            String cliente = (String) cliCombo.getSelectedItem();
            db.get("ventas").add(new Object[]{
                sigId(db.get("ventas")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                cliente,
                totalFinal[0],
                det.toString(),
                descGlobal
            });
            guardar();
            JOptionPane.showMessageDialog(ventana,
                "✅  Venta registrada exitosamente\n\nTotal cobrado: " + cop(totalFinal[0]) + "\nCliente: " + cliente,
                "Venta Completada", JOptionPane.INFORMATION_MESSAGE);
            ir("Nueva Venta");
        });

        JPanel selPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        selPanel.setOpaque(false);
        selPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER), new EmptyBorder(10, 10, 10, 10)));

        selPanel.add(new JLabel("Cliente:")); selPanel.add(cliCombo);
        selPanel.add(new JLabel("  Producto:")); selPanel.add(proCombo);
        selPanel.add(new JLabel("  Cant.:")); selPanel.add(cantSpin);
        selPanel.add(new JLabel("  Desc.:")); selPanel.add(descCombo);
        selPanel.add(Box.createHorizontalStrut(8)); selPanel.add(btnAdd);

        JPanel totalesPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        totalesPanel.setOpaque(false);
        totalesPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        totalesPanel.add(lblSub);
        totalesPanel.add(lblDesc);
        totalesPanel.add(lblIva);
        totalesPanel.add(lblTotal);

        JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        accionesPanel.setOpaque(false);
        accionesPanel.add(btnQuitar);
        accionesPanel.add(btnCobrar);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.add(accionesPanel, BorderLayout.WEST);
        bottomPanel.add(totalesPanel, BorderLayout.EAST);

        JPanel ventaContent = new JPanel(new BorderLayout(0, 10));
        ventaContent.setOpaque(false);
        ventaContent.add(selPanel, BorderLayout.NORTH);
        ventaContent.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        ventaContent.add(bottomPanel, BorderLayout.SOUTH);

        p.add(ventaContent, BorderLayout.CENTER);
        calcular.run();
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  REPORTES
    // ══════════════════════════════════════════════════════════════════
    static JPanel reportes() {
        JPanel p = base("Reporte de Ventas", "Historial completo de transacciones");
        List<Object[]> ventas = db.get("ventas");

        JTextField desde = campo(""); desde.setPreferredSize(new Dimension(120, 34));
        desde.setToolTipText("yyyy-MM-dd");
        JTextField hasta = campo(""); hasta.setPreferredSize(new Dimension(120, 34));
        hasta.setToolTipText("yyyy-MM-dd");
        JTextField busCliente = campo(""); busCliente.setPreferredSize(new Dimension(160, 34));

        String[] cols = {"N°", "Fecha", "Cliente", "Desc. %", "Total", "Detalle"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = tabla(m);
        t.getColumnModel().getColumn(5).setPreferredWidth(300);

        long[] sumaTotal = {0};
        List<Object[]> visFiltradas = new ArrayList<>();

        Runnable refrescar = () -> {
            m.setRowCount(0);
            visFiltradas.clear();
            sumaTotal[0] = 0;
            String desdeStr = desde.getText().trim();
            String hastaStr = hasta.getText().trim();
            String cli = busCliente.getText().toLowerCase();

            for (int i = ventas.size() - 1; i >= 0; i--) {
                Object[] v = ventas.get(i);
                String fec = (String) v[1];
                String cliente = String.valueOf(v[2]).toLowerCase();

                if (!desdeStr.isEmpty() && fec.compareTo(desdeStr) < 0) continue;
                if (!hastaStr.isEmpty() && fec.compareTo(hastaStr + "z") > 0) continue;
                if (!cli.isEmpty() && !cliente.contains(cli)) continue;

                visFiltradas.add(v);
                sumaTotal[0] += (Long) v[3];
                int desc = v.length > 5 && v[5] instanceof Integer ? (Integer) v[5] : 0;
                m.addRow(new Object[]{v[0], v[1], v[2], desc + "%", cop((Long) v[3]), v[4]});
            }
        };

        JLabel lblResumen = new JLabel();
        lblResumen.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblResumen.setForeground(C_ACCENT);

        Runnable actualizar = () -> {
            refrescar.run();
            lblResumen.setText("  Ventas mostradas: " + visFiltradas.size() + "   |   Total: " + cop(sumaTotal[0]));
        };

        JButton btnFiltrar = btn("🔍 Filtrar", C_ACCENT);
        JButton btnLimpiar = btn("✕ Limpiar", C_TEXT_LIGHT);
        JButton btnCSV     = btn("📥 Exportar CSV", new Color(0x00897B));

        btnFiltrar.addActionListener(e -> actualizar.run());
        btnLimpiar.addActionListener(e -> { desde.setText(""); hasta.setText(""); busCliente.setText(""); actualizar.run(); });

        btnCSV.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("reporte_ventas_" + LocalDate.now() + ".csv"));
            if (fc.showSaveDialog(ventana) != JFileChooser.APPROVE_OPTION) return;
            try {
                StringBuilder sb = new StringBuilder("N°;Fecha;Cliente;Descuento%;Total COP;Detalle\n");
                for (Object[] v : visFiltradas) {
                    int desc = v.length > 5 && v[5] instanceof Integer ? (Integer) v[5] : 0;
                    sb.append(v[0]).append(';').append(v[1]).append(';')
                      .append(v[2]).append(';').append(desc).append("%;")
                      .append(v[3]).append(';').append(v[4]).append('\n');
                }
                Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(ventana, "✅ Reporte exportado correctamente.", "Exportado", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ventana, "No se pudo guardar el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Desde:")); filtros.add(desde);
        filtros.add(new JLabel("Hasta:")); filtros.add(hasta);
        filtros.add(new JLabel("Cliente:")); filtros.add(busCliente);
        filtros.add(btnFiltrar); filtros.add(btnLimpiar);
        filtros.add(Box.createHorizontalStrut(16)); filtros.add(lblResumen);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        acciones.setOpaque(false);
        acciones.add(btnCSV);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(filtros, BorderLayout.NORTH);
        topPanel.add(acciones, BorderLayout.SOUTH);

        JPanel c = new JPanel(new BorderLayout(0, 10)); c.setOpaque(false);
        c.add(topPanel, BorderLayout.NORTH);
        c.add(new JScrollPane(t), BorderLayout.CENTER);

        p.add(c, BorderLayout.CENTER);
        actualizar.run();
        return p;
    }

    // ══════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        UIManager.put("Label.font", F_BODY);
        UIManager.put("Button.font", F_BOLD);
        UIManager.put("TextField.font", F_BODY);
        UIManager.put("ComboBox.font", F_BODY);
        UIManager.put("Table.font", F_BODY);
        UIManager.put("TableHeader.font", F_BOLD);
        UIManager.put("OptionPane.messageFont", F_BODY);
        UIManager.put("OptionPane.buttonFont", F_BOLD);

        cargar();
        SwingUtilities.invokeLater(CastilloVentas::login);
    }
}
