import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;

public class Pet {
    private JFrame frame;
    private JLabel questionLabel;
    private JTextField answerField;
    private JButton startButton;
    private JButton previousButton;
    private JButton nextButton;
    private JLabel tipLabel;
    private JLabel tipTextLabel;
    private JLabel dogImageLabel;
    private Map<String, String[]> questions;
    private Map<String, String[]> responses;
    private String currentDisease;
    private Map<String, Integer> diseaseQuestionIndices;
    private String[] tips;
    private int tipIndex = 0;

    public Pet() {
        initialize();
    }
    private void suggestDoctor() {
        String city = JOptionPane.showInputDialog(frame, "Enter your city:");
        if (city != null && !city.isEmpty()) {
            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish database connection
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/doctor_directory", "root", "Snehitha11@rafi");

                // Create SQL statement to retrieve doctors in the given city
                String sql = "SELECT doctor_name, rating FROM doctors d JOIN cities c ON d.city_id = c.city_id WHERE c.city_name = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, city);

                // Execute the query
                ResultSet rs = pstmt.executeQuery();

                // Display doctors in a dialog
                StringBuilder doctorList = new StringBuilder();
                while (rs.next()) {
                    String doctorName = rs.getString("doctor_name");
                    double rating = rs.getDouble("rating");
                    doctorList.append(doctorName).append(" (Rating: ").append(rating).append(")\n");
                }
                if (doctorList.length() > 0) {
                    JOptionPane.showMessageDialog(frame, "Doctors in " + city + ":\n" + doctorList.toString(), "Doctors", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "No doctors found in " + city, "No Doctors", JOptionPane.INFORMATION_MESSAGE);
                }

                // Close database resources
                rs.close();
                pstmt.close();
                conn.close();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error retrieving doctors. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initialize() {
        frame = new JFrame("Pet Symptom Analyzer");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JButton suggestDoctorButton = new JButton("Suggest Doctor in my City");
        suggestDoctorButton.setBounds(10, 200, 200, 30);
        suggestDoctorButton.addActionListener(e -> suggestDoctor());
        frame.add(suggestDoctorButton);

        tipTextLabel = new JLabel("");
        tipTextLabel.setBounds(10, 280, 400, 20);
        frame.add(tipTextLabel);

        questions = new HashMap<>();
        questions.put("parvovirus", new String[]{
                "Is your pet experiencing loss of appetite? (yes/no)",
                "Is your pet vomiting frequently? (yes/no)",
                "Is your pet having bloody diarrhea? (yes/no)"
        });
        questions.put("distemper", new String[]{
                "Is your pet experiencing a fever? (yes/no)",
                "Is your pet having a nasal discharge? (yes/no)",
                "Is your pet coughing? (yes/no)",
                "Is your pet lethargic or lacking energy? (yes/no)",
                "Has your pet lost its appetite? (yes/no)",
                "Does your pet have watery eye discharge? (yes/no)",
                "Is your pet experiencing diarrhea with mucus? (yes/no)",
                "Has your pet vomited? (yes/no)",
                "Does your pet have labored breathing? (yes/no)",
                "Is your pet showing a skin rash? (yes/no)"
        });

        questions.put("kennel cough", new String[]{
                "Is your pet experiencing a persistent dry cough? (yes/no)",
                "Is your pet sneezing frequently? (yes/no)",
                "Is your pet having nasal discharge? (yes/no)",
                "Is your pet active and playful? (yes/no)"
        });
        questions.put("rabies", new String[]{
                "Is your pet displaying aggressive behavior? (yes/no)",
                "Is your pet having difficulty swallowing? (yes/no)",
                "Is your pet avoiding water? (yes/no)"
        });
        responses = new HashMap<>();
        for (String disease : questions.keySet()) {
            responses.put(disease, new String[questions.get(disease).length]);
        }

        diseaseQuestionIndices = new HashMap<>();
        for (String disease : questions.keySet()) {
            diseaseQuestionIndices.put(disease, 0);
        }

        startButton = new JButton("Start");
        startButton.setBounds(10, 50, 100, 30);
        startButton.addActionListener(e -> startQuestionnaire());
        frame.add(startButton);

        questionLabel = new JLabel("Welcome to the Pet Symptom Analyzer");
        questionLabel.setBounds(10, 100, 500, 20);
        frame.add(questionLabel);

        answerField = new JTextField();
        answerField.setBounds(10, 130, 200, 20);
        frame.add(answerField);

        previousButton = new JButton("Previous");
        previousButton.setBounds(10, 160, 100, 30);
        previousButton.addActionListener(e -> previousQuestion());
        frame.add(previousButton);

        nextButton = new JButton("Next");
        nextButton.setBounds(120, 160, 100, 30);
        nextButton.addActionListener(e -> nextQuestion());
        frame.add(nextButton);

        tipLabel = new JLabel("Tip for a Healthy Dog/Puppy:");
        tipLabel.setBounds(10, 250, 200, 20);
        frame.add(tipLabel);

        tipTextLabel = new JLabel("");
        tipTextLabel.setBounds(10, 280, 400, 20);
        frame.add(tipTextLabel);

        ImageIcon dogImageIcon = new ImageIcon("dog_image.jpg"); // Replace "dog_image.jpg" with the path to your image file
        Image dogImage = dogImageIcon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
        dogImageIcon = new ImageIcon(dogImage);
        dogImageLabel = new JLabel(dogImageIcon);
        dogImageLabel.setBounds(400, 0, 400, 400);
        frame.add(dogImageLabel);

        frame.getContentPane().setBackground(new Color(173, 216, 230)); // Set background color to light blue
        frame.setVisible(true);

        // Initialize tips
        tips = new String[]{
                "Regular exercise is essential for your dog's health and happiness.",
                "Ensure your dog has a balanced diet with high-quality dog food.",
                "Regular vet check-ups are crucial for preventive care.",
                "Train your dog with patience and positive reinforcement.",
                "Keep your dog hydrated, especially in hot weather."
        };

        // Set up a Timer to update tips every 10 seconds
        Timer timer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTip();
            }
        });
        timer.start();
    }

    private void updateTip() {
        tipIndex = (tipIndex + 1) % tips.length;
        tipTextLabel.setText(tips[tipIndex]);
    }

    private void startQuestionnaire() {
        currentDisease = questions.keySet().iterator().next();
        askQuestion();
        startButton.setEnabled(false);
    }

    private void askQuestion() {
        if (currentDisease != null) {
            String[] currentQuestions = questions.get(currentDisease);
            int currentIndex = diseaseQuestionIndices.get(currentDisease);
            questionLabel.setText(currentQuestions[currentIndex]);
        }
    }

    private void previousQuestion() {
        if (currentDisease != null) {
            int currentIndex = diseaseQuestionIndices.get(currentDisease);
            if (currentIndex > 0) {
                diseaseQuestionIndices.put(currentDisease, currentIndex - 1);
                askQuestion();
            }
        }
    }

    private void nextQuestion() {
        if (currentDisease != null) {
            String[] currentResponses = responses.get(currentDisease);
            int currentIndex = diseaseQuestionIndices.get(currentDisease);
            currentResponses[currentIndex] = answerField.getText().toLowerCase();
            answerField.setText("");

            int lastIndex = questions.get(currentDisease).length - 1;
            if (currentIndex < lastIndex) {
                diseaseQuestionIndices.put(currentDisease, currentIndex + 1);
                askQuestion();
            } else {
                nextDisease();
            }
        }
    }

    private void nextDisease() {
        int currentIndex = new java.util.ArrayList<>(questions.keySet()).indexOf(currentDisease);
        int lastIndex = questions.size() - 1;
        if (currentIndex < lastIndex) {
            currentDisease = questions.keySet().toArray(new String[0])[currentIndex + 1];
            diseaseQuestionIndices.put(currentDisease, 0);
            askQuestion();
        } else {
            submit();
        }
    }

    private void submit() {
        predictCondition();
    }

    private void predictCondition() {
        String mostLikelyDisease = null;
        int maxYesCount = 0;

        for (String disease : responses.keySet()) {
            String[] symptoms = responses.get(disease);
            int yesCount = 0;gitgi
            for (String symptom : symptoms) {
                if ("yes".equalsIgnoreCase(symptom)) {
                    yesCount++;
                }
            }
            if (yesCount > maxYesCount) {
                maxYesCount = yesCount;
                mostLikelyDisease = disease;
            }
        }

        if (mostLikelyDisease != null) {
            String message = "Your pet's symptoms may indicate " + mostLikelyDisease + ". Consult a veterinarian for a proper diagnosis.";
            JOptionPane.showMessageDialog(frame, message, "Condition Prediction", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Based on your responses, your pet seems to be in good health.", "Good Health", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Pet::new);
    }
}
