package Calendario;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class RecordatorioThread extends Thread {

    private final DatabaseCompleto db;
    private final User usuario;
    private final JFrame ventana;
    private final List<String> eventosNotificados = new ArrayList<>();

    public RecordatorioThread(DatabaseCompleto db, User usuario, JFrame ventana) {
        this.db = db;
        this.usuario = usuario;
        this.ventana = ventana;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<Event> eventos = db.obtenerEventosDelUsuario(usuario.getID());
                LocalDate hoy = LocalDate.now();
                LocalTime ahora = LocalTime.now().withSecond(0).withNano(0);

                for (Event e : eventos) {
                    LocalDate fechaEvento = LocalDate.parse(e.getDate());

                    if (fechaEvento.isEqual(hoy)) {
                        LocalTime horaEvento = LocalTime.parse(e.getTime());
                        long minutosRestantes = java.time.Duration.between(ahora, horaEvento).toMinutes();

                        // Crear clave única por evento y tipo de recordatorio
                        String claveEvento = e.getID() + "-" + minutosRestantes;

                        if ((minutosRestantes == 3 || minutosRestantes == 0)
                                && !eventosNotificados.contains(claveEvento)) {
                            eventosNotificados.add(claveEvento);
                            mostrarDialogoRecordatorio(e, minutosRestantes);
                        }
                    }
                }

                Thread.sleep(10000); // Espera 1 minuto antes de la siguiente verificación
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

  private final Object lockDialogo = new Object();

private void mostrarDialogoRecordatorio(Event e, long minutosRestantes) {
    new Thread(() -> {
        synchronized (lockDialogo) {
            try {
                String titulo = (minutosRestantes == 0)
                        ? "¡Es hora del evento!"
                        : "Próximo evento en 3 minutos";

                String mensaje = "<html><b>" + titulo + "</b><br>" +
                        "Título: " + e.getTitle() + "<br>" +
                        "Descripción: " + e.getDescription() + "<br>" +
                        "Hora: <b>" + LocalTime.parse(e.getTime()).format(DateTimeFormatter.ofPattern("HH:mm")) + "</b>" +
                        "</html>";

                reproducirSonido();

                // Mostrar el diálogo bloqueante en el hilo de UI
                SwingUtilities.invokeAndWait(() -> {
                    JOptionPane.showMessageDialog(ventana, mensaje, "Recordatorio", JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }).start();
}


    private void reproducirSonido() {
        new Thread(() -> {
            try {
                var audioSrc = getClass().getResourceAsStream("/sonidos/alerta.wav");
                if (audioSrc != null) {
                    var audio = javax.sound.sampled.AudioSystem.getAudioInputStream(
                            new java.io.BufferedInputStream(audioSrc));
                    var clip = javax.sound.sampled.AudioSystem.getClip();
                    clip.open(audio);
                    clip.start();
                } else {
                    System.err.println("No se encontró el archivo de sonido.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
