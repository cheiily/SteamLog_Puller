package SteamLog.Connection;

/**
 * READY - self-explanatory
 * NO_UID - the builder has failed to obtain the user id and will not function properly
 * BAD_URL - the builder has failed to build the URL, most likely due to bad config args
 * BAD_CONNECTION - the builder has failed to open the test connection, indicating a network error
 */
public enum ConBuilderState {
    READY(""),
    NO_UID("Failed to obtain UID from vanity url."),
    BAD_URL("Malformed request URL, likely due to bad configuration arguments."),
    BAD_CONNECTION("Failed to create the test connection.");

    public final String errMessage;
    ConBuilderState(String msg) {
        errMessage = msg;
    }
}
