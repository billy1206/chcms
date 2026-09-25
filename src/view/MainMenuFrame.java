package view;

import controller.AppointmentController;
import controller.DoctorController;
import controller.PatientController;
import controller.ReportController;
import controller.TreatmentController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.Clinic;

/**
 * Main window. Holds one CardLayout with every screen and a navigation sidebar.
 * The frame knows about controllers only - never about repositories or files.
 */
public class MainMenuFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Clinic clinic;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    private final PatientPanel patientPanel;
    private final DoctorPanel doctorPanel;
    private final AppointmentPanel appointmentPanel;
    private final TreatmentPanel treatmentPanel;
    private final ReportsPanel reportsPanel;

    public MainMenuFrame(Clinic clinic) {
        this.clinic = clinic;

        PatientController patientController = new PatientController(clinic);
        DoctorController doctorController = new DoctorController(clinic);
        AppointmentController appointmentController = new AppointmentController(clinic);
        TreatmentController treatmentController = new TreatmentController(clinic);
        ReportController reportController = new ReportController(clinic);

        patientPanel = new PatientPanel(patientController);
        doctorPanel = new DoctorPanel(doctorController);
        appointmentPanel = new AppointmentPanel(appointmentController, patientController, doctorController);
        treatmentPanel = new TreatmentPanel(treatmentController, appointmentController);
        reportsPanel = new ReportsPanel(reportController, patientController, doctorController);

        setTitle("Community Health Clinic Management System - " + clinic.getName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1180, 700);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        content.add(buildHome(), "HOME");
        content.add(patientPanel, "PATIENTS");
        content.add(doctorPanel, "DOCTORS");
        content.add(appointmentPanel, "APPOINTMENTS");
        content.add(treatmentPanel, "TREATMENTS");
        content.add(reportsPanel, "REPORTS");
        add(content, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Community Health Clinic Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(clinic.getName() + "  |  " + clinic.getAddress()
                + "  |  " + clinic.getPhone());
        subtitle.setForeground(new Color(219, 234, 244));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(8, 1, 0, 6));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        sidebar.setBackground(UIHelper.LIGHT);
        sidebar.setPreferredSize(new Dimension(190, 0));

        sidebar.add(navButton("Main Menu", "HOME"));
        sidebar.add(navButton("Register Patient", "PATIENTS"));
        sidebar.add(navButton("Register Doctor", "DOCTORS"));
        sidebar.add(navButton("Appointments", "APPOINTMENTS"));
        sidebar.add(navButton("Treatments", "TREATMENTS"));
        sidebar.add(navButton("Reports", "REPORTS"));

        JButton save = UIHelper.button("Save all data");
        save.addActionListener(e -> saveAll(true));
        sidebar.add(save);

        JButton exit = UIHelper.button("Save & Exit");
        exit.addActionListener(e -> exitApplication());
        sidebar.add(exit);
        return sidebar;
    }

    private JButton navButton(String text, String card) {
        JButton button = UIHelper.button(text);
        button.addActionListener(e -> showCard(card));
        return button;
    }

    /** Every screen refreshes on entry so it always reflects current model state. */
    private void showCard(String card) {
        switch (card) {
            case "PATIENTS" -> patientPanel.reload();
            case "DOCTORS" -> doctorPanel.reload();
            case "APPOINTMENTS" -> appointmentPanel.reload();
            case "TREATMENTS" -> treatmentPanel.reload();
            case "REPORTS" -> reportsPanel.reload();
            default -> { }
        }
        cards.show(content, card);
    }

    private JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout());
        home.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("<html><div style='text-align:center;'>"
                + "<h1>Welcome</h1>"
                + "<p style='font-size:13px;'>Use the menu on the left to register patients and "
                + "doctors, book appointments,<br>record treatments and generate reports.</p>"
                + "<p style='font-size:12px;color:#555;'>All records are saved to plain text files "
                + "in the <b>data</b> folder and reloaded on start-up.</p>"
                + "</div></html>", SwingConstants.CENTER);
        home.add(welcome, BorderLayout.CENTER);
        return home;
    }

    private void saveAll(boolean announce) {
        try {
            clinic.saveAll();
            if (announce) {
                UIHelper.info(this, "All records saved to the data folder.");
            }
        } catch (IOException ex) {
            UIHelper.error(this, "Saving failed: " + ex.getMessage());
        }
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Save all data and exit?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            saveAll(false);
            dispose();
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
