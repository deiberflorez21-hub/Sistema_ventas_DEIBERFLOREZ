import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.function.*;

/** CastilloVentas - Sistema de ventas (Java Swing, pesos COP).
 *  Compilar: javac CastilloVentas.java   |   Ejecutar: java CastilloVentas
 *  Acceso: admin / admin123  (Administrador)  o  vendedor / 1234 (Vendedor) */
public class CastilloVentas {
    static final Color OSCURO = new Color(0x263238), AZUL = new Color(0x1E88E5), FONDO = new Color(0xECEFF1);
    static final Font F = new Font("Segoe UI", Font.PLAIN, 14), FB = new Font("Segoe UI", Font.BOLD, 14);
    static final File ARCH = new File("castilloventas.dat");
    static final DecimalFormat COP;
    static { DecimalFormatSymbols s = new DecimalFormatSymbols(); s.setGroupingSeparator('.'); s.setDecimalSeparator(',');
             COP = new DecimalFormat("$ #,##0", s); }
    static HashMap<String, ArrayList<Object[]>> db = new HashMap<>();
    static JFrame ventana; static JPanel centro; static String usuario, rol;

    static String cop(long v) { return COP.format(v) + " COP"; }

    // ---------- Datos ----------
    @SuppressWarnings("unchecked")
    static void cargar() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ARCH))) {
            db = (HashMap<String, ArrayList<Object[]>>) in.readObject();
        } catch (Exception e) { semilla(); guardar(); }
    }
    static void guardar() {
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(ARCH))) { o.writeObject(db); } catch (IOException e) { e.printStackTrace(); }
    }
    static void semilla() {
        db.put("productos", new ArrayList<>(List.of(
            new Object[]{1, "Camiseta Básica", "Ropa", 35000L, 40}, new Object[]{2, "Jean Clásico", "Ropa", 120000L, 25},
            new Object[]{3, "Gorra Deportiva", "Accesorios", 28000L, 3}, new Object[]{4, "Tenis Urbanos", "Calzado", 210000L, 15},
            new Object[]{5, "Morral Escolar", "Accesorios", 85000L, 10})));
        db.put("clientes", new ArrayList<>(List.of(
            new Object[]{1, "Juan Pérez", "1098765432", "3101234567"}, new Object[]{2, "María Gómez", "52123456", "3209876543"})));
        db.put("usuarios", new ArrayList<>(List.of(
            new Object[]{"admin", "admin123", "Administrador", "Activo"}, new Object[]{"vendedor", "1234", "Vendedor", "Activo"})));
        ArrayList<Object[]> v = new ArrayList<>(); Random r = new Random(7);
        for (int i = 0; i < 14; i++) {
            long t = (r.nextInt(30) + 5) * 10000L;
            v.add(new Object[]{i + 1, LocalDateTime.now().minusDays(i * 12L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Cliente general", t, "Venta de ejemplo"});
        }
        db.put("ventas", v);
    }
    static int sigId(List<Object[]> d) { int m = 0; for (Object[] r : d) m = Math.max(m, (Integer) r[0]); return m + 1; }

    // ---------- Componentes de estilo ----------
    static JButton btn(String txt, Color c) {
        JButton b = new JButton(txt); b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setBackground(c); b.setForeground(Color.WHITE); b.setFont(FB); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 18, 9, 18)); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
    }
    static JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m); t.setRowHeight(30); t.setFont(F); t.setSelectionBackground(new Color(0xBBDEFB));
        t.setSelectionForeground(Color.BLACK); t.setGridColor(new Color(0xE0E0E0));
        JTableHeader h = t.getTableHeader(); h.setBackground(OSCURO); h.setForeground(Color.WHITE); h.setFont(FB);
        h.setPreferredSize(new Dimension(0, 34)); return t;
    }
    static JPanel base(String titulo) {
        JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBackground(FONDO); p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel l = new JLabel(titulo); l.setFont(new Font("Segoe UI", Font.BOLD, 24)); l.setForeground(OSCURO); p.add(l, BorderLayout.NORTH); return p;
    }
    static JPanel tarjeta(String tit, String val, int color) {
        JPanel p = new JPanel(new GridLayout(2, 1)); p.setBackground(new Color(color)); p.setBorder(new EmptyBorder(15, 18, 15, 18));
        JLabel a = new JLabel(val), b = new JLabel(tit);
        a.setFont(new Font("Segoe UI", Font.BOLD, 20)); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        a.setForeground(Color.WHITE); b.setForeground(new Color(255, 255, 255, 210)); p.add(a); p.add(b); return p;
    }

    // ---------- Login ----------
    static void login() {
        JFrame f = new JFrame("CastilloVentas - Acceso"); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel izq = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) { ((Graphics2D) g).setPaint(new GradientPaint(0, 0, AZUL, 0, getHeight(), OSCURO));
                g.fillRect(0, 0, getWidth(), getHeight()); }
        };
        JLabel t = new JLabel("<html><center>Castillo<b>Ventas</b><br><span style='font-size:12px'>Sistema de Ventas e Inventario</span></center></html>");
        t.setForeground(Color.WHITE); t.setFont(new Font("Segoe UI", Font.PLAIN, 30)); izq.add(t);
        JPanel der = new JPanel(new GridLayout(0, 1, 6, 6)); der.setBackground(Color.WHITE); der.setBorder(new EmptyBorder(40, 40, 40, 40));
        JLabel h = new JLabel("INICIAR SESIÓN"); h.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JTextField u = new JTextField(); JPasswordField p = new JPasswordField(); u.setFont(F); p.setFont(F);
        JButton ok = btn("INGRESAR", AZUL); JLabel msg = new JLabel(" "); msg.setForeground(Color.RED);
        der.add(h); der.add(new JLabel("Usuario")); der.add(u); der.add(new JLabel("Contraseña")); der.add(p); der.add(ok); der.add(msg);
        ActionListener a = e -> {
            for (Object[] r : db.get("usuarios"))
                if (r[0].equals(u.getText().trim()) && r[1].equals(new String(p.getPassword())) && r[3].equals("Activo")) {
                    usuario = (String) r[0]; rol = (String) r[2]; f.dispose(); abrir(); return;
                }
            msg.setText("Usuario o contraseña incorrectos");
        };
        ok.addActionListener(a); p.addActionListener(a);
        f.setLayout(new GridLayout(1, 2)); f.add(izq); f.add(der); f.setSize(780, 420); f.setLocationRelativeTo(null); f.setVisible(true);
    }

    // ---------- Ventana principal ----------
    static void abrir() {
        ventana = new JFrame("CastilloVentas - Sistema de Ventas"); ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel lado = new JPanel(); lado.setLayout(new BoxLayout(lado, BoxLayout.Y_AXIS)); lado.setBackground(OSCURO);
        lado.setPreferredSize(new Dimension(220, 0));
        JLabel logo = new JLabel("  CastilloVentas"); logo.setFont(new Font("Segoe UI", Font.BOLD, 20)); logo.setForeground(Color.WHITE);
        logo.setBorder(new EmptyBorder(22, 10, 4, 0)); lado.add(logo);
        JLabel us = new JLabel("   " + usuario + " (" + rol + ")"); us.setForeground(new Color(0x90A4AE)); us.setBorder(new EmptyBorder(0, 10, 20, 0)); lado.add(us);
        List<String> menu = new ArrayList<>(List.of("Inicio", "Productos", "Clientes", "Nueva Venta", "Reportes"));
        if (rol.equals("Administrador")) menu.add("Usuarios"); menu.add("Salir");
        for (String m : menu) {
            JButton b = new JButton("   " + m); b.setUI(new javax.swing.plaf.basic.BasicButtonUI()); b.setFont(F);
            b.setBackground(OSCURO); b.setForeground(Color.WHITE); b.setHorizontalAlignment(SwingConstants.LEFT); b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(13, 20, 13, 10)); b.setMaximumSize(new Dimension(220, 46)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { b.setBackground(AZUL); }
                public void mouseExited(MouseEvent e) { b.setBackground(OSCURO); } });
            b.addActionListener(e -> { if (m.equals("Salir")) { ventana.dispose(); login(); } else ir(m); });
            lado.add(b);
        }
        centro = new JPanel(new BorderLayout());
        ventana.add(lado, BorderLayout.WEST); ventana.add(centro); ir("Inicio");
        ventana.setSize(1200, 720); ventana.setLocationRelativeTo(null); ventana.setVisible(true);
    }
    static void ir(String m) {
        centro.removeAll();
        centro.add(switch (m) {
            case "Productos" -> crud("Gestión de Productos", "productos", new String[]{"ID", "Nombre", "Categoría", "Precio (COP)", "Stock"},
                new Object[]{0, "", "", 0L, 0}, true);
            case "Clientes" -> crud("Gestión de Clientes", "clientes", new String[]{"ID", "Nombre", "Documento", "Teléfono"},
                new Object[]{0, "", "", ""}, true);
            case "Usuarios" -> crud("Gestión de Usuarios", "usuarios", new String[]{"Usuario", "Contraseña", "Rol (Administrador/Vendedor)", "Estado (Activo/Inactivo)"},
                new Object[]{"", "", "", ""}, false);
            case "Nueva Venta" -> venta();
            case "Reportes" -> reportes();
            default -> inicio();
        });
        centro.revalidate(); centro.repaint();
    }

    // ---------- Panel principal ----------
    static JPanel inicio() {
        JPanel p = base("Panel de Control - Resumen de Datos");
        String hoy = LocalDate.now().toString(), mes = hoy.substring(0, 7); long vh = 0, vm = 0, tot = 0;
        for (Object[] v : db.get("ventas")) { String f = (String) v[1]; long t = (Long) v[3]; tot += t; if (f.startsWith(hoy)) vh += t; if (f.startsWith(mes)) vm += t; }
        long bajo = db.get("productos").stream().filter(r -> (Integer) r[4] <= 5).count(), n = db.get("ventas").size();
        JPanel c = new JPanel(new GridLayout(2, 4, 15, 15)); c.setOpaque(false); c.setPreferredSize(new Dimension(0, 210));
        c.add(tarjeta("VENTAS DE HOY", cop(vh), 0x29B6F6)); c.add(tarjeta("VENTAS DEL MES", cop(vm), 0xEF5350));
        c.add(tarjeta("TOTAL VENDIDO", cop(tot), 0x66BB6A)); c.add(tarjeta("TICKET PROMEDIO", cop(n == 0 ? 0 : tot / n), 0x5C6BC0));
        c.add(tarjeta("PRODUCTOS", "" + db.get("productos").size(), 0xFFA726)); c.add(tarjeta("CLIENTES", "" + db.get("clientes").size(), 0x26A69A));
        c.add(tarjeta("STOCK BAJO (≤5)", "" + bajo, 0xE53935)); c.add(tarjeta("NÚMERO DE VENTAS", "" + n, 0x8E24AA));
        JPanel graf = new JPanel() {
            protected void paintComponent(Graphics g0) {
                super.paintComponent(g0); Graphics2D g = (Graphics2D) g0; g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                long[] t = new long[6]; String[] et = new String[6]; long max = 1; YearMonth ahora = YearMonth.now();
                for (int i = 0; i < 6; i++) { YearMonth ym = ahora.minusMonths(5 - i);
                    et[i] = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es"));
                    for (Object[] v : db.get("ventas")) if (((String) v[1]).startsWith(ym.toString())) t[i] += (Long) v[3];
                    max = Math.max(max, t[i]); }
                g.setFont(FB); g.setColor(OSCURO); g.drawString("COMPARATIVA DE VENTAS - ÚLTIMOS 6 MESES", 15, 25);
                int w = getWidth(), h = getHeight(), bw = (w - 60) / 6;
                for (int i = 0; i < 6; i++) { int alt = (int) ((h - 100) * t[i] / max), x = 30 + i * bw;
                    g.setColor(i == 5 ? new Color(0x43A047) : AZUL); g.fillRoundRect(x + 12, h - 35 - alt, bw - 24, alt, 8, 8);
                    g.setColor(OSCURO); g.setFont(F); g.drawString(et[i], x + bw / 2 - 10, h - 15); g.drawString(COP.format(t[i]), x + 12, h - 40 - alt); }
            }
        };
        graf.setBackground(Color.WHITE); graf.setBorder(new LineBorder(new Color(0xCFD8DC)));
        JPanel cont = new JPanel(new BorderLayout(0, 15)); cont.setOpaque(false); cont.add(c, BorderLayout.NORTH); cont.add(graf);
        p.add(cont); return p;
    }

    // ---------- CRUD genérico ----------
    static JPanel crud(String titulo, String key, String[] cols, Object[] tipo, boolean autoId) {
        JPanel p = base(titulo); List<Object[]> d = db.get(key), vis = new ArrayList<>();
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = tabla(m); JTextField bus = new JTextField(18); bus.setFont(F);
        Runnable ref = () -> { m.setRowCount(0); vis.clear(); String q = bus.getText().toLowerCase();
            for (Object[] r : d) { if (!Arrays.toString(r).toLowerCase().contains(q)) continue; vis.add(r); Object[] v = r.clone();
                for (int i = 0; i < v.length; i++) if (cols[i].startsWith("Precio")) v[i] = cop((Long) v[i]); m.addRow(v); } };
        Consumer<Object[]> form = fila -> {
            JTextField[] tf = new JTextField[cols.length]; JPanel f = new JPanel(new GridLayout(0, 2, 8, 8));
            for (int i = 0; i < cols.length; i++) { if (autoId && i == 0) continue; f.add(new JLabel(cols[i]));
                tf[i] = new JTextField(fila == null ? "" : String.valueOf(fila[i]), 16); f.add(tf[i]); }
            if (JOptionPane.showConfirmDialog(ventana, f, fila == null ? "Nuevo registro" : "Editar registro", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            try { Object[] n = new Object[cols.length];
                for (int i = 0; i < cols.length; i++) { if (autoId && i == 0) { n[0] = fila == null ? sigId(d) : fila[0]; continue; }
                    String s = tf[i].getText().trim(); if (s.isEmpty()) throw new Exception();
                    n[i] = tipo[i] instanceof Long ? (Object) Long.parseLong(s) : tipo[i] instanceof Integer ? (Object) Integer.parseInt(s) : s; }
                if (fila == null) d.add(n); else d.set(d.indexOf(fila), n);
                guardar(); ref.run();
            } catch (Exception e) { JOptionPane.showMessageDialog(ventana, "Datos inválidos. Complete todo y use números sin puntos."); }
        };
        JButton nuevo = btn("+ Nuevo", new Color(0x43A047)), edit = btn("Editar", AZUL), del = btn("Eliminar", new Color(0xE53935));
        nuevo.addActionListener(e -> form.accept(null));
        edit.addActionListener(e -> { int i = t.getSelectedRow(); if (i < 0) JOptionPane.showMessageDialog(ventana, "Seleccione una fila."); else form.accept(vis.get(i)); });
        del.addActionListener(e -> { int i = t.getSelectedRow(); if (i < 0) { JOptionPane.showMessageDialog(ventana, "Seleccione una fila."); return; }
            if (JOptionPane.showConfirmDialog(ventana, "¿Eliminar el registro seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                d.remove(vis.get(i)); guardar(); ref.run(); } });
        bus.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { ref.run(); } });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); top.setOpaque(false);
        top.add(new JLabel("Buscar:")); top.add(bus); top.add(nuevo); top.add(edit); top.add(del);
        JPanel c = new JPanel(new BorderLayout(0, 10)); c.setOpaque(false); c.add(top, BorderLayout.NORTH); c.add(new JScrollPane(t));
        p.add(c); ref.run(); return p;
    }

    // ---------- Nueva venta ----------
    static JPanel venta() {
        JPanel p = base("Registrar Nueva Venta"); List<Object[]> prods = db.get("productos"), carro = new ArrayList<>();
        JComboBox<String> cli = new JComboBox<>(), pro = new JComboBox<>(); cli.addItem("Cliente general");
        for (Object[] c : db.get("clientes")) cli.addItem((String) c[1]);
        for (Object[] r : prods) pro.addItem(r[1] + "  -  " + cop((Long) r[3]) + "  (stock: " + r[4] + ")");
        JSpinner cant = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        DefaultTableModel m = new DefaultTableModel(new String[]{"Producto", "Cant.", "Precio unit.", "Subtotal"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = tabla(m); JLabel tot = new JLabel(); tot.setFont(new Font("Segoe UI", Font.PLAIN, 16)); long[] total = {0};
        Runnable calc = () -> { m.setRowCount(0); long s = 0;
            for (Object[] r : carro) { long st = (Long) r[2] * (Integer) r[1]; s += st; m.addRow(new Object[]{r[0], r[1], cop((Long) r[2]), cop(st)}); }
            long iva = Math.round(s * 0.19); total[0] = s + iva;
            tot.setText("<html>Subtotal: " + cop(s) + "<br>IVA (19%): " + cop(iva) + "<br><b style='font-size:20px;color:#2E7D32'>TOTAL: " + cop(total[0]) + "</b></html>"); };
        JButton add = btn("Agregar", AZUL), quitar = btn("Quitar", new Color(0xFB8C00)), cobrar = btn("COBRAR VENTA", new Color(0x43A047));
        add.addActionListener(e -> { if (prods.isEmpty()) return; Object[] pr = prods.get(pro.getSelectedIndex()); int q = (Integer) cant.getValue();
            Object[] ex = null; for (Object[] r : carro) if (r[0].equals(pr[1])) ex = r;
            int ya = ex == null ? 0 : (Integer) ex[1];
            if (ya + q > (Integer) pr[4]) { JOptionPane.showMessageDialog(ventana, "Stock insuficiente. Disponible: " + pr[4]); return; }
            if (ex == null) carro.add(new Object[]{pr[1], q, pr[3]}); else ex[1] = ya + q; calc.run(); });
        quitar.addActionListener(e -> { int i = t.getSelectedRow(); if (i >= 0) { carro.remove(i); calc.run(); } });
        cobrar.addActionListener(e -> { if (carro.isEmpty()) { JOptionPane.showMessageDialog(ventana, "El carrito está vacío."); return; }
            StringBuilder det = new StringBuilder();
            for (Object[] r : carro) { for (Object[] pr : prods) if (pr[1].equals(r[0])) pr[4] = (Integer) pr[4] - (Integer) r[1];
                det.append(r[1]).append("x ").append(r[0]).append("; "); }
            db.get("ventas").add(new Object[]{sigId(db.get("ventas")), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                cli.getSelectedItem(), total[0], det.toString()});
            guardar(); JOptionPane.showMessageDialog(ventana, "¡Venta registrada!\nTotal cobrado: " + cop(total[0])); ir("Nueva Venta"); });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); top.setOpaque(false);
        top.add(new JLabel("Cliente:")); top.add(cli); top.add(new JLabel("Producto:")); top.add(pro); top.add(new JLabel("Cant.:")); top.add(cant); top.add(add);
        JPanel pie = new JPanel(new BorderLayout()); pie.setOpaque(false); JPanel bs = new JPanel(new FlowLayout(FlowLayout.LEFT)); bs.setOpaque(false);
        bs.add(quitar); bs.add(cobrar); pie.add(bs, BorderLayout.WEST); pie.add(tot, BorderLayout.EAST);
        JPanel c = new JPanel(new BorderLayout(0, 10)); c.setOpaque(false); c.add(top, BorderLayout.NORTH); c.add(new JScrollPane(t)); c.add(pie, BorderLayout.SOUTH);
        p.add(c); calc.run(); return p;
    }

    // ---------- Reportes ----------
    static JPanel reportes() {
        JPanel p = base("Reporte de Ventas"); List<Object[]> v = db.get("ventas");
        DefaultTableModel m = new DefaultTableModel(new String[]{"N°", "Fecha", "Cliente", "Total", "Detalle"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        long suma = 0; for (int i = v.size() - 1; i >= 0; i--) { Object[] r = v.get(i); suma += (Long) r[3]; m.addRow(new Object[]{r[0], r[1], r[2], cop((Long) r[3]), r[4]}); }
        JTable t = tabla(m); t.getColumnModel().getColumn(4).setPreferredWidth(350);
        JLabel l = new JLabel("Total acumulado: " + cop(suma) + "   |   Ventas: " + v.size()); l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JButton exp = btn("Exportar a CSV", AZUL);
        exp.addActionListener(e -> { JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File("reporte_ventas.csv"));
            if (fc.showSaveDialog(ventana) != JFileChooser.APPROVE_OPTION) return;
            try { StringBuilder sb = new StringBuilder("N;Fecha;Cliente;Total COP;Detalle\n");
                for (Object[] r : v) sb.append(r[0]).append(';').append(r[1]).append(';').append(r[2]).append(';').append(r[3]).append(';').append(r[4]).append('\n');
                Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes("UTF-8")); JOptionPane.showMessageDialog(ventana, "Reporte exportado.");
            } catch (IOException ex) { JOptionPane.showMessageDialog(ventana, "No se pudo guardar el archivo."); } });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5)); top.setOpaque(false); top.add(l); top.add(exp);
        JPanel c = new JPanel(new BorderLayout(0, 10)); c.setOpaque(false); c.add(top, BorderLayout.NORTH); c.add(new JScrollPane(t));
        p.add(c); return p;
    }

    public static void main(String[] a) { cargar(); SwingUtilities.invokeLater(CastilloVentas::login); }
}
