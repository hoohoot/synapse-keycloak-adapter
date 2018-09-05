package hoohoot.synapse.adapter.http.exceptions;

public class ConfigurationException extends Exception {
    public ConfigurationException() {
        super("Unable to load configuration.");
    }
}
