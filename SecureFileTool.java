import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecureFileTool extends JFrame implements ActionListener

{
    JLabel titleLabel, fileLabel, passwordLabel, statusLabel;
    JTextField fileField;
    JPasswordField passwordField;
    JButton browseButton, encryptButton, decryptButton, resetButton, exitButton;

    public SecureFileTool() {
        setTitle("Secure File Encryption and Decryption Tool");
        setSize(700, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        titleLabel = new JLabel("Secure File Encryption and Decryption Tool");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBounds(120, 20, 500, 30);
        add(titleLabel);

        fileLabel = new JLabel("Select File:");
        fileLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        fileLabel.setBounds(50, 90, 100, 25);
        add(fileLabel);

        fileField = new JTextField();
        fileField.setBounds(150, 90, 360, 30);
        add(fileField);

        browseButton = new JButton("Browse");
        browseButton.setBounds(530, 90, 100, 30);
        browseButton.addActionListener(this);
        add(browseButton);

        passwordLabel = new JLabel("Enter Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setBounds(50, 150, 120, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(180, 150, 250, 30);
        add(passwordField);

        encryptButton = new JButton("Encrypt");
        encryptButton.setBounds(80, 230, 110, 40);
        encryptButton.addActionListener(this);
        add(encryptButton);

        decryptButton = new JButton("Decrypt");
        decryptButton.setBounds(210, 230, 110, 40);
        decryptButton.addActionListener(this);
        add(decryptButton);

        resetButton = new JButton("Reset");
        resetButton.setBounds(340, 230, 110, 40);
        resetButton.addActionListener(this);
        add(resetButton);

        exitButton = new JButton("Exit");
        exitButton.setBounds(470, 230, 110, 40);
        exitButton.addActionListener(this);
        add(exitButton);

        statusLabel = new JLabel("Status: Waiting for user action");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        statusLabel.setBounds(50, 320, 600, 25);
        add(statusLabel);

        getContentPane().setBackground(new Color(240, 248, 255));
        setVisible(true);
    }

    public static SecretKeySpec getSecretKey(String password) throws Exception {
        byte[] key = password.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }

    public static void encryptFile(File inputFile, String password) throws Exception {
        SecretKeySpec secretKey = getSecretKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] fileData = Files.readAllBytes(inputFile.toPath());
        byte[] encryptedData = cipher.doFinal(fileData);

        File outputFile = new File(inputFile.getAbsolutePath() + ".enc");
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(encryptedData);
        fos.close();

        boolean deleted = inputFile.delete();
        if (!deleted) {
            throw new Exception("Encrypted file created, but original file could not be deleted.");
        }
    }

    public static void decryptFile(File inputFile, String password) throws Exception {
        SecretKeySpec secretKey = getSecretKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] encryptedData = Files.readAllBytes(inputFile.toPath());
        byte[] decryptedData = cipher.doFinal(encryptedData);

        String outputPath = inputFile.getAbsolutePath();
        if (outputPath.endsWith(".enc")) {
            outputPath = outputPath.substring(0, outputPath.length() - 4);
        } else {
            outputPath = outputPath + "_decrypted";
        }

        File outputFile = new File(outputPath);
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(decryptedData);
        fos.close();

        boolean deleted = inputFile.delete();
        if (!deleted) {
            throw new Exception("Decrypted file created, but encrypted file could not be deleted.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == browseButton) {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                fileField.setText(selectedFile.getAbsolutePath());
                statusLabel.setText("Status: File selected successfully");
            }
        }

        if (e.getSource() == encryptButton) {
            String filePath = fileField.getText();
            String password = new String(passwordField.getPassword());

            if (filePath.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Status: Please select a file and enter password");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "After encryption, the original file will be deleted.\nDo you want to continue?",
                    "Confirm Encryption",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                statusLabel.setText("Status: Encryption cancelled");
                return;
            }

            try {
                File inputFile = new File(filePath);
                encryptFile(inputFile, password);
                statusLabel.setText("Status: File encrypted successfully and original deleted");
                JOptionPane.showMessageDialog(this,
                        "Encryption completed.\nEncrypted file saved as:\n" + inputFile.getAbsolutePath() + ".enc\n\nOriginal file deleted.");
            } catch (Exception ex) {
                statusLabel.setText("Status: Encryption failed");
                JOptionPane.showMessageDialog(this,
                        "Encryption failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        if (e.getSource() == decryptButton) {
            String filePath = fileField.getText();
            String password = new String(passwordField.getPassword());

            if (filePath.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Status: Please select a file and enter password");
                return;
            }

            try {
                File inputFile = new File(filePath);
                decryptFile(inputFile, password);
                statusLabel.setText("Status: File decrypted successfully and .enc file deleted");
                JOptionPane.showMessageDialog(this,
                        "Decryption completed.\nOriginal file restored.\nEncrypted .enc file deleted.");
            } catch (Exception ex) {
                statusLabel.setText("Status: Decryption failed");
                JOptionPane.showMessageDialog(this,
                        "Decryption failed.\nPossible reason: wrong password or invalid file.\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        if (e.getSource() == resetButton) {
            fileField.setText("");
            passwordField.setText("");
            statusLabel.setText("Status: Fields cleared");
        }

        if (e.getSource() == exitButton) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new SecureFileTool();
    }
}