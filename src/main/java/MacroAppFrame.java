import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MacroAppFrame extends JFrame implements NativeKeyListener {

    private Map<Integer, List<MacroAction>> macros = new HashMap<>();
    private Robot robot;
    private final String FILE_NAME = "macros.json";

    // NEW: Master Kill-Switch to stop infinite loops safely!
    private AtomicBoolean isPlaying = new AtomicBoolean(false);

    public MacroAppFrame() {
        try {
            robot = new Robot();
            robot.setAutoDelay(20); // Adds a tiny 20ms delay between typing letters so computers don't drop keystrokes
        } catch (AWTException e) {
            System.err.println("Robot Error: " + e.getMessage());
        }

        setTitle("Macro Manager");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JButton createMacroBtn = new JButton("Create New Macro");
        createMacroBtn.addActionListener(e -> openCreateDialog());
        add(createMacroBtn);

        JButton manageBtn = new JButton("Manage Macros");
        manageBtn.addActionListener(e -> openManageDialog());
        add(manageBtn);

        loadMacros();

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException e) {
            System.err.println("Keyboard Hook Error: " + e.getMessage());
        }

        setupSystemTray();
    }

    private void openCreateDialog() {
        CreateMacroDialog dialog = new CreateMacroDialog(this);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            macros.put(dialog.getFinalKeyCode(), dialog.getFinalActions());
            saveMacros();
            JOptionPane.showMessageDialog(this, "Macro saved successfully!");
        }
    }

    private void openManageDialog() {
        ManageMacrosDialog dialog = new ManageMacrosDialog(this, macros);
        dialog.setVisible(true);

        if (dialog.isModified()) {
            saveMacros();
            JOptionPane.showMessageDialog(this, "Macros updated successfully!");
        }
    }

    private void executeMacro(List<MacroAction> actions) {
        isPlaying.set(true); // Lock the engine
        int loopCount = 1;
        int startIndex = 0;

        // Check if the very first action is our special "repeat" command
        if (!actions.isEmpty() && actions.get(0).type.equals("repeat")) {
            // Gson parses numbers as Doubles, so we safely convert it to an int
            loopCount = ((Number) actions.get(0).value).intValue();
            startIndex = 1; // Skip the repeat command so we don't try to "execute" it
        }

        // --- THE MASTER LOOP ---
        // Runs X times. If loopCount is 0, it loops infinitely.
        // It immediately breaks if isPlaying is flipped to false by the kill-switch.
        for (int i = 0; (loopCount == 0 || i < loopCount) && isPlaying.get(); i++) {

            for (int j = startIndex; j < actions.size() && isPlaying.get(); j++) {
                MacroAction action = actions.get(j);

                switch (action.type) {
                    case "text":
                        typeText((String) action.value);
                        break;
                    case "delay":
                        try {
                            Thread.sleep(((Double) action.value).longValue());
                        } catch (InterruptedException e) {
                            System.err.println("Delay Error: " + e.getMessage());
                        }
                        break;
                    case "program":
                        try {
                            new ProcessBuilder((String) action.value).start();
                        } catch (IOException e) {
                            System.err.println("Launch Error: " + e.getMessage());
                        }
                        break;
                }
            }
        }

        isPlaying.set(false); // Unlock the engine when finished
    }

    private void typeText(String text) {
        for (char c : text.toCharArray()) {
            // Check the kill switch after every single letter typed!
            if (!isPlaying.get()) break;

            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (KeyEvent.CHAR_UNDEFINED == keyCode) continue;
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (macros.containsKey(e.getKeyCode())) {

            // EMERGENCY STOP: If a macro is already running, pressing the hotkey kills it!
            if (isPlaying.get()) {
                isPlaying.set(false);
                System.out.println("Macro Stopped by User!");
                return;
            }

            // NEW THREAD: We hand the heavy lifting off to a background thread so the app doesn't freeze
            new Thread(() -> {
                executeMacro(macros.get(e.getKeyCode()));
            }).start();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    private void saveMacros() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            new Gson().toJson(macros, writer);
        } catch (IOException e) {
            System.err.println("Save Error: " + e.getMessage());
        }
    }

    private void loadMacros() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                java.lang.reflect.Type type = new TypeToken<Map<Integer, List<MacroAction>>>(){}.getType();
                macros = new Gson().fromJson(reader, type);
            } catch (IOException e) {
                System.err.println("Load Error: " + e.getMessage());
            }
        }
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon(image, "Macro Manager");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("System Tray Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MacroAppFrame().setVisible(true));
    }
}