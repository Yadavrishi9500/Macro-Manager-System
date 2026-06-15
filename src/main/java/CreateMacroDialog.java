import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateMacroDialog extends JDialog {

    private int finalKeyCode;
    private List<MacroAction> finalActions = new ArrayList<>();
    private boolean isSaved = false;

    public CreateMacroDialog(JFrame parent) {
        super(parent, "Create New Macro", true);
        setSize(400, 300); // Made slightly taller to fit the new row
        setLayout(new GridLayout(5, 1)); // Upgraded to 5 rows
        setLocationRelativeTo(parent);

        // 1. Key Selection Area
        JPanel keyPanel = new JPanel();
        keyPanel.add(new JLabel("Trigger Key:"));
        String[] keys = {
                "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
        };
        JComboBox<String> keyBox = new JComboBox<>(keys);
        keyPanel.add(keyBox);
        add(keyPanel);

        // 2. Action Type Area
        JPanel typePanel = new JPanel();
        typePanel.add(new JLabel("What should this macro do?"));
        String[] actionTypes = {"Type Text", "Open Application"};
        JComboBox<String> typeBox = new JComboBox<>(actionTypes);
        typePanel.add(typeBox);
        add(typePanel);

        // 3. Action Input Area
        JPanel actionPanel = new JPanel();
        actionPanel.add(new JLabel("Text OR File Path:"));
        JTextField textField = new JTextField(20);
        actionPanel.add(textField);
        add(actionPanel);

        // 4. NEW: Repeat Count Area
        JPanel repeatPanel = new JPanel();
        repeatPanel.add(new JLabel("Repeat Count (0 = Infinite):"));
        // Spinner goes from 1 to 9999. 0 is our secret code for "Infinite Loop"
        JSpinner repeatSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
        repeatPanel.add(repeatSpinner);
        add(repeatPanel);

        // 5. Save Button
        JButton saveButton = new JButton("Save Macro");
        saveButton.addActionListener(e -> {

            String selectedKey = (String) keyBox.getSelectedItem();

            switch (selectedKey) {
                case "F1": finalKeyCode = NativeKeyEvent.VC_F1; break;
                case "F2": finalKeyCode = NativeKeyEvent.VC_F2; break;
                case "F3": finalKeyCode = NativeKeyEvent.VC_F3; break;
                case "F4": finalKeyCode = NativeKeyEvent.VC_F4; break;
                case "F5": finalKeyCode = NativeKeyEvent.VC_F5; break;
                case "F6": finalKeyCode = NativeKeyEvent.VC_F6; break;
                case "F7": finalKeyCode = NativeKeyEvent.VC_F7; break;
                case "F8": finalKeyCode = NativeKeyEvent.VC_F8; break;
                case "F9": finalKeyCode = NativeKeyEvent.VC_F9; break;
                case "F10": finalKeyCode = NativeKeyEvent.VC_F10; break;
                case "F11": finalKeyCode = NativeKeyEvent.VC_F11; break;
                case "F12": finalKeyCode = NativeKeyEvent.VC_F12; break;
                case "1": finalKeyCode = NativeKeyEvent.VC_1; break;
                case "2": finalKeyCode = NativeKeyEvent.VC_2; break;
                case "3": finalKeyCode = NativeKeyEvent.VC_3; break;
                case "4": finalKeyCode = NativeKeyEvent.VC_4; break;
                case "5": finalKeyCode = NativeKeyEvent.VC_5; break;
                case "6": finalKeyCode = NativeKeyEvent.VC_6; break;
                case "7": finalKeyCode = NativeKeyEvent.VC_7; break;
                case "8": finalKeyCode = NativeKeyEvent.VC_8; break;
                case "9": finalKeyCode = NativeKeyEvent.VC_9; break;
                case "0": finalKeyCode = NativeKeyEvent.VC_0; break;
            }

            // Grab the repeat number
            int repeatCount = (int) repeatSpinner.getValue();

            // We inject a special "repeat" action at the very start of our list so the engine knows how many loops to do!
            finalActions.add(new MacroAction("repeat", repeatCount));

            String actionType = typeBox.getSelectedIndex() == 0 ? "text" : "program";
            finalActions.add(new MacroAction(actionType, textField.getText()));

            isSaved = true;
            setVisible(false);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        add(buttonPanel);
    }

    public int getFinalKeyCode() { return finalKeyCode; }
    public List<MacroAction> getFinalActions() { return finalActions; }
    public boolean isSaved() { return isSaved; }
}