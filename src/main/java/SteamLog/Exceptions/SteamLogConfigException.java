package SteamLog.Exceptions;

public class SteamLogConfigException extends SteamLogException {
    public SteamLogConfigException(String s) {
        super(s);
    }

    public static SteamLogConfigException section(String sectionName) {
        return new SteamLogConfigException("Config file invalid. Missing section: " + sectionName);
    }

    public static SteamLogConfigException key(String key) {
        return new SteamLogConfigException("Config file invalid. Missing key: " + key);
    }

    public static SteamLogConfigException other(String message) {
        return new SteamLogConfigException("Config file invalid. " + message);
    }

    public static SteamLogConfigException behavior() {
        return new SteamLogConfigException("Behavior misconfiguration. Consult the guide on how to properly configure behavior.");
    }

}
