public class MacroAction {
    // Encapsulation: Storing the type of action and its value [cite: 200, 201]
    public String type; // e.g., "text", "click", "delay", "program"
    public Object value; // e.g., "Hello World", or 1000 (milliseconds)

    // Constructor to create a new action [cite: 202]
    public MacroAction(String type, Object value) {
        this.type = type;
        this.value = value;
    }
}