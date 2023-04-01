package SteamLog.Utils;

import SteamLog.Exceptions.SteamLogConfigException;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AppConfig {
    private static final Path _configPath = Path.of("config.ini");

    /**
     * API key to use. Necessary for the app to run at all.<br/>
     * Can be obtained from <a href="https://steamcommunity.com/dev/apikey">https://steamcommunity.com/dev/apikey</a>
     */
    public static String API_KEY;

    /**
     * List of Steam user IDs to track. Use this or VANITY_IDS_TO_TRACK, or both.
     * In case this list is empty and no vanity id could be resolved, the app will terminate with an error.
     */
    public static List<String> UIDS_TO_TRACK;

    /**
     * List of Steam Vanity IDs to track. The app will automatically resolve them for UIDs.
     * Can be used instead of, or in conjunction with UIDS_TO_TRACK.
     */
    public static List<String> VANITY_IDS_TO_TRACK;



    /**
     * Filepath to where the session records will be stored.<br/>
     * The application will only log when there occurs a status change or when a graceful shutdown occurs.<br/>
     * If the path is empty or such a file cannot be created/accessed, the app will not run.
     */
    public static Path SESSION_LOG_PATH;

    /**
     * Filepath to where the occurring errors & other info messages will be noted down.<br/>
     * If there is no path, or it can't be parsed properly - no errors will be logged.
     */
    public static Path DEBUG_LOG_PATH;

    /**
     * Error count thresholds indicating after how many errors should the application enter slowdown.<br/>
     * If this list is empty a default table will be used instead.
     */
    public static List<Integer> ERROR_COUNT_THRESHOLDS;

    /**
     * A list of refresh delay values in milliseconds indicating how long should the application wait
     * before issuing another API query.<br/>
     * Closely connected with ERROR_COUNT_THRESHOLDS -
     * if this list is of shorter or equal length than the other one (or empty),
     * a default threshold table will be used instead.<br/>
     * Lowering any of the values will result in using up more of the daily API call pool but increase the resolution.<br/>
     * Increasing any of the values will result in using up less of the daily API call pool but decrease the resolution.<br/>
     */
    public static List<Integer> REFRESH_TIMES;



    private static class Default {
        public static final List<Integer> ERROR_COUNT_THRESHOLDS =
                Arrays.asList(
                        5,
                        15,
                        50
                );
        public static final List<Integer> REFRESH_TIMES =
                Arrays.asList(
                        5_000,
                        30_000,
                        120_000,
                        600_000
                );
    }



    public static void load() throws IOException, SteamLogConfigException {
        org.ini4j.Config iniConfig = new org.ini4j.Config();
        iniConfig.setMultiOption(true);
        Ini ini = new Ini();
        ini.setConfig(iniConfig);
        ini.load(new File(_configPath.toString()));

        Profile.Section tracking,
                        uids,
                        vanity,

                        logging,

                        thresholds,
                        refresh;

        tracking = ini.get("tracking");
        logging = ini.get("logging");
        thresholds = ini.get("behavior.thresholds");
        refresh = ini.get("behavior.refresh");

        if ( tracking == null )
            throw SteamLogConfigException.section("tracking");
        if ( logging == null )
            throw SteamLogConfigException.section("logging");

        uids = ini.get("tracking.uids");
        vanity = ini.get("tracking.vanity");



        //tracking
        String apiKey = tracking.get("api");
        if (apiKey == null)
            throw SteamLogConfigException.key("api");
        API_KEY = apiKey;

        boolean uidUnrdy = uids == null || uids.isEmpty();
        boolean vanUnrdy = vanity == null || vanity.isEmpty();

        if ( uidUnrdy && vanUnrdy ) {
            throw SteamLogConfigException.other("\"uids\" and \"vanity\" can't both be missing or empty.");
        }

        List<String> uidList = ( uids == null || uids.getAll("uid") == null )
                ? List.of() : Collections.unmodifiableList(uids.getAll("uid"));
        List<String> vidList = ( vanity == null || vanity.getAll("vid") == null )
                ? List.of() : Collections.unmodifiableList(vanity.getAll("vid"));

        UIDS_TO_TRACK = uidList;
        VANITY_IDS_TO_TRACK = vidList;


        //logging
        String session = logging.get("sessions");
        if (session == null)
            throw SteamLogConfigException.key("sessions");

        String err = logging.get("debug");

        SESSION_LOG_PATH = Path.of(session);
        if (err != null) DEBUG_LOG_PATH = Path.of(err);


        //behavior
        int thrSize = thresholds == null ? -1 : thresholds.getAll("thr").size();
        int refSize = refresh == null ? -1 : refresh.getAll("ref").size();

        if ( thrSize == -1 && refSize == -1 ) {
            ERROR_COUNT_THRESHOLDS = Default.ERROR_COUNT_THRESHOLDS;
            REFRESH_TIMES = Default.REFRESH_TIMES;

            return;
        }

        if ( thrSize == 0 ) ERROR_COUNT_THRESHOLDS = Default.ERROR_COUNT_THRESHOLDS;
        else if ( thrSize > 0 ) {
            ERROR_COUNT_THRESHOLDS = thresholds.getAll("thr").stream()
                    .map(Integer::parseInt)
                    .toList();
        }

        if ( refSize == 0 ) REFRESH_TIMES = Default.REFRESH_TIMES;
        else if ( refSize > 0 ) {
            if ( refSize < thrSize + 1 ) {
                ERROR_COUNT_THRESHOLDS = Default.ERROR_COUNT_THRESHOLDS;
                REFRESH_TIMES = Default.REFRESH_TIMES;

                throw SteamLogConfigException.behavior();
            } else
                REFRESH_TIMES = refresh.getAll("ref").stream()
                        .map(Integer::parseInt)
                        .map(elem -> elem * 1000)
                        .toList();
        }

    }
}
