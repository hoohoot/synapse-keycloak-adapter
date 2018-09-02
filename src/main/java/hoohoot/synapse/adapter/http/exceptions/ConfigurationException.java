package hoohoot.synapse.adapter.http.exceptions;

import java.util.function.Supplier;

public class ConfigurationException extends Exception {
    public ConfigurationException() {
        super("Unable to load configuration.");
    }
}
