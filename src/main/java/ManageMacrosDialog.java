import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Inheritance: Extends JDialog for our popup window
public class ManageMacrosDialog extends JDialog {

    private Map<Integer, List<MacroAction>> macros;
    private DefaultListModel<String> listModel;
    private JList<String> macroList;
    private List<Integer> keyCodes; // Keeps track of which key belongs to which list item
    private boolean wasModified = false;

    public ManageMacrosDialog(JFrame parent, Map<Integer , List<MacroAction>> currentMacros) {
        super(parent, "Manage Macros", true);
        this.macros = currentMacros;

        setSize(350, 250);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        listModel = new DefaultListModel<>();
        keyCodes = new ArrayList<>();

        // 1. Read the data and populate the list
        for (Map.Entry<Integer, List<MacroAction>> entry : macros.entrySet()) {
            int key = entry.getKey();
            List<MacroAction> actions = entry.getValue();

            // Convert the raw keycode into a readable name (e.g., "F8")
            String keyName = NativeKeyEvent.getKeyText(key);

            // Grab a quick preview of what the macro does
            String preview = actions.isEmpty() ? "Empty" : actions.get(0).type + ": " + actions.get(0).value;

            listModel.addElement("Key " + keyName + "  ->  " + preview);
            keyCodes.add(key);
        }

        // 2. Create the visual list
        macroList = new JList<>(listModel);
        add(new JScrollPane(macroList), BorderLayout.CENTER);

        // 3. Create the Delete Button
        JButton deleteBtn = new JButton("Delete Selected Macro");
        deleteBtn.addActionListener(e -> {
            int index = macroList.getSelectedIndex();
            if (index != -1) { // If an item is actually selected
                int keyToRemove = keyCodes.get(index);
                macros.remove(keyToRemove); // Delete from data

                // Delete from visual list
                listModel.remove(index);
                keyCodes.remove(index);
                wasModified = true;
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(deleteBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Tells the main app if we deleted anything so it knows to save the file
    public boolean isModified() {
        return wasModified;
    }
}