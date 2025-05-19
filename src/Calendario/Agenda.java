/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Calendario;

import com.toedter.calendar.JCalendar;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.sound.sampled.*;


public class Agenda extends javax.swing.JFrame {

    private User loggedUser;
    private DatabaseCompleto db;
    private DefaultTableModel model;
    

    public Agenda(User user) {
        this.loggedUser = user;
        initComponents();
        db = new DatabaseCompleto();
        setTitle("Agenda de Eventos - Usuario: " + user.getUsername());

        // Cargar modelo con ID oculto
        model = new DefaultTableModel(new Object[]{"ID", "Título", "Descripción", "Hora"}, 0);
        jTblEventos.setModel(model);

        // Ocultar la columna de ID
        jTblEventos.getColumnModel().getColumn(0).setMinWidth(0);
        jTblEventos.getColumnModel().getColumn(0).setMaxWidth(0);
        jTblEventos.getColumnModel().getColumn(0).setWidth(0);

        jCalendar1.addPropertyChangeListener("calendar", e -> cargarEventosDelDia());
        cargarEventosDelDia();
        
         RecordatorioThread recordatorio = new RecordatorioThread(db, loggedUser, this);
    recordatorio.start();
    }

    // Método para mostrar el formulario de nuevo evento
    private void mostrarFormularioEvento() {
        boolean eventoValido = false;

        // Variables que persisten entre intentos
        String tituloPrevio = "";
        String descripcionPrevia = "";
        Date horaPrevia = new Date();

        while (!eventoValido) {
            JTextField titulo = new JTextField(tituloPrevio);
            JTextField descripcion = new JTextField(descripcionPrevia);
            SpinnerDateModel modeloSpinner = new SpinnerDateModel();
            JSpinner horaSpinner = new JSpinner(modeloSpinner);
            horaSpinner.setEditor(new JSpinner.DateEditor(horaSpinner, "HH:mm"));
            horaSpinner.setValue(horaPrevia);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Título:"));
            panel.add(titulo);
            panel.add(new JLabel("Descripción:"));
            panel.add(descripcion);
            panel.add(new JLabel("Hora (HH:mm):"));
            panel.add(horaSpinner);

            int res = JOptionPane.showConfirmDialog(this, panel, "Nuevo Evento", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                String nuevoTitulo = titulo.getText().trim();
                String nuevaDescripcion = descripcion.getText().trim();

                // Guardar los valores temporalmente
                tituloPrevio = nuevoTitulo;
                descripcionPrevia = nuevaDescripcion;
                horaPrevia = (Date) horaSpinner.getValue();

                // Obtener fecha y hora
                SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdfFecha.format(jCalendar1.getDate());

                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
                String horaStr = sdfHora.format(horaPrevia);

                // Validar si ya hay evento a esa hora
                if (db.existeEventoEnMismaHora(fecha, horaStr, loggedUser.getID(), null)) {
                    JOptionPane.showMessageDialog(this, "Ya tienes un evento a esa hora. Elige otra hora.", "Conflicto", JOptionPane.WARNING_MESSAGE);
                    continue; // volver a mostrar el formulario con los datos previos
                }

                // Si no hay conflicto, guardar
                Event nuevo = new Event(nuevoTitulo, nuevaDescripcion, fecha, horaStr, loggedUser.getID());
                db.addEvent(nuevo, loggedUser.getID());
                cargarEventosDelDia();
                eventoValido = true;
            } else {
                // Si el usuario presiona "Cancelar"
                eventoValido = true;
            }
        }
    }

    // Método para cargar eventos del día seleccionado
    private void cargarEventosDelDia() {
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(jCalendar1.getDate());
        ArrayList<Event> eventos = db.getEvents(fecha, loggedUser.getID());

        for (Event e : eventos) {
            model.addRow(new Object[]{e.getID(), e.getTitle(), e.getDescription(), e.getTime()});

        }
    }

    private void editarEventoSeleccionado() {
        int fila = jTblEventos.getSelectedRow();
        if (fila >= 0) {
            int idEvento = Integer.parseInt(model.getValueAt(fila, 0).toString());
            String tituloActual = model.getValueAt(fila, 1).toString();
            String descripcionActual = model.getValueAt(fila, 2).toString();
            String horaActual = model.getValueAt(fila, 3).toString();

            JTextField titulo = new JTextField(tituloActual);
            JTextField descripcion = new JTextField(descripcionActual);
            JSpinner horaSpinner = new JSpinner(new SpinnerDateModel());
            horaSpinner.setEditor(new JSpinner.DateEditor(horaSpinner, "HH:mm"));

            try {
                Date hora = new SimpleDateFormat("HH:mm:ss").parse(horaActual);
                horaSpinner.setValue(hora);
            } catch (Exception e) {
                horaSpinner.setValue(new Date());
            }

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Título:"));
            panel.add(titulo);
            panel.add(new JLabel("Descripción:"));
            panel.add(descripcion);
            panel.add(new JLabel("Hora (HH:mm):"));
            panel.add(horaSpinner);

            int res = JOptionPane.showConfirmDialog(this, panel, "Editar Evento", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                String nuevoTitulo = titulo.getText();
                String nuevaDescripcion = descripcion.getText();
                String nuevaHora = new SimpleDateFormat("HH:mm").format((Date) horaSpinner.getValue());

                // Obtener la fecha del evento actualmente mostrado en el calendario
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(jCalendar1.getDate());

                // Verificar si ya hay otro evento a la misma hora (excluyendo el actual)
                if (db.existeEventoEnMismaHora(fecha, nuevaHora, loggedUser.getID(), idEvento)) {
                    JOptionPane.showMessageDialog(this, "Ya tienes un evento a esa hora. Elige otra hora.", "Conflicto", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean actualizado = db.editarEvento(idEvento, nuevoTitulo, nuevaDescripcion, nuevaHora);
                if (actualizado) {
                    JOptionPane.showMessageDialog(this, "Evento actualizado correctamente.");
                    cargarEventosDelDia();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar el evento.");
                }
            }

        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un evento para editar.");
        }
    }

    private void eliminarEventoSeleccionado() {
        int filaSeleccionada = jTblEventos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String idStr = model.getValueAt(filaSeleccionada, 0).toString();
            int idEvento = Integer.parseInt(idStr); // conversión segura

            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Deseas eliminar este evento?", "Confirmación", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean eliminado = db.eliminarEvento(idEvento);
                if (eliminado) {
                    JOptionPane.showMessageDialog(this, "Evento eliminado exitosamente.");
                    cargarEventosDelDia();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar el evento.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un evento para eliminar.");
        }
    }



    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCalendar1 = new com.toedter.calendar.JCalendar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTblEventos = new javax.swing.JTable();
        jBntAgregarEvento = new javax.swing.JButton();
        jBtnEliminar = new javax.swing.JButton();
        jBtnEditar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTblEventos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        jScrollPane1.setViewportView(jTblEventos);

        jBntAgregarEvento.setText("Agregar Evento");
        jBntAgregarEvento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBntAgregarEventoActionPerformed(evt);
            }
        });

        jBtnEliminar.setText("Eliminar Evento");
        jBtnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEliminarActionPerformed(evt);
            }
        });

        jBtnEditar.setText("Editar Evento");
        jBtnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEditarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCalendar1, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBntAgregarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jBtnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addComponent(jBtnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jCalendar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBtnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBntAgregarEvento, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(35, 35, 35))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBntAgregarEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBntAgregarEventoActionPerformed
        mostrarFormularioEvento();
    }//GEN-LAST:event_jBntAgregarEventoActionPerformed

    private void jBtnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnEditarActionPerformed
        editarEventoSeleccionado();
    }//GEN-LAST:event_jBtnEditarActionPerformed

    private void jBtnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnEliminarActionPerformed
        eliminarEventoSeleccionado();
    }//GEN-LAST:event_jBtnEliminarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Agenda.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Agenda.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Agenda.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Agenda.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
//                User demoUser = new User(1, "demo", "123");
//            new Agenda(demoUser).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBntAgregarEvento;
    private javax.swing.JButton jBtnEditar;
    private javax.swing.JButton jBtnEliminar;
    private com.toedter.calendar.JCalendar jCalendar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTblEventos;
    // End of variables declaration//GEN-END:variables
}
