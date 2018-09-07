package hoohoot.synapse.adapter.exceptions;

public class ConfigurationException extends Exception {
    public ConfigurationException() {
        super("Unable to load configuration.");
    }
}
