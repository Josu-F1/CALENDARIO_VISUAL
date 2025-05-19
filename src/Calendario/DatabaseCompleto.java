package Calendario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCompleto {

    private String url = "jdbc:mysql://localhost/calendar";
    private String user = "root";
    private String pass = "";
    private Connection connection;

    public DatabaseCompleto() {
        try {
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Registro de usuario (sin encriptar la contraseña, idealmente hashea)
    public boolean addUser(User u) {
        String sql = "INSERT INTO users (user, password) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            int res = ps.executeUpdate();
            return res > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Validar login (devuelve User si ok, sino null)
    public User validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE user = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("ID"), rs.getString("user"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtener eventos filtrados por fecha y user_id
    public ArrayList<Event> getEvents(String date, int userID) {
        ArrayList<Event> events = new ArrayList<>();
        String select = "SELECT * FROM calendar WHERE Date = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setString(1, date);
            ps.setInt(2, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Event ev = new Event(
                        rs.getInt("ID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Date"),
                        rs.getString("Time"),
                        rs.getInt("user_id")
                );
                events.add(ev);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    // Insertar evento con userID
    public void addEvent(Event e, int userID) {
        String sql = "INSERT INTO calendar (Title, Description, Date, Time, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getDate());
            ps.setString(4, e.getTime());
            ps.setInt(5, userID);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean eliminarEvento(int idEvento) {
        String sql = "DELETE FROM calendar WHERE ID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEvento);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean editarEvento(int id, String titulo, String descripcion, String hora) {
        String sql = "UPDATE calendar SET Title = ?, Description = ?, Time = ? WHERE ID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, titulo);
            ps.setString(2, descripcion);
            ps.setString(3, hora);
            ps.setInt(4, id);
            int res = ps.executeUpdate();
            return res > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existeEventoEnMismaHora(String fecha, String hora, int userId, Integer excluirId) {
        String sql = "SELECT COUNT(*) FROM calendar WHERE Date = ? AND Time = ? AND user_id = ?";
        if (excluirId != null) {
            sql += " AND ID != ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fecha);
            ps.setString(2, hora);
            ps.setInt(3, userId);
            if (excluirId != null) {
                ps.setInt(4, excluirId);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Event> obtenerEventosDelUsuario(int userId) {
        List<Event> lista = new ArrayList<>();
        String sql = "SELECT * FROM calendar WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Event e = new Event(
                        rs.getInt("ID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Date"),
                        rs.getString("Time"),
                        rs.getInt("user_id")
                );
                lista.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
