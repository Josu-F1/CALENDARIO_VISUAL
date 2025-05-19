package Calendario;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private DatabaseCompleto db;

    public LoginFrame() {
        setTitle("Login / Registro");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        db = new DatabaseCompleto();

        add(new JLabel("Usuario:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginBtn = new JButton("Iniciar Sesión");
        loginBtn.addActionListener(e -> login());
        add(loginBtn);

        JButton registerBtn = new JButton("Registrar");
        registerBtn.addActionListener(e -> register());
        add(registerBtn);

        setVisible(true);
    }

   private void login() {
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());

    User u = db.validateLogin(username, password);
    if (u != null) {
        JOptionPane.showMessageDialog(this, "Login exitoso");

        Agenda agenda = new Agenda(u); // <- aquí ahora sí pasas el User correcto
        agenda.setVisible(true);
        this.dispose();
    } else {
        JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos");
    }
}


    private void register() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar usuario y contraseña.");
            return;
        }

        User u = new User();
        u.setUsername(user);
        u.setPassword(pass);  // Aquí idealmente hacer hash

        boolean created = db.addUser(u);
        if (created) {
            JOptionPane.showMessageDialog(this, "Usuario registrado correctamente. Ahora puede iniciar sesión.");
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar. Usuario puede ya existir.");
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
