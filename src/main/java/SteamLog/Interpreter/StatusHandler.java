package SteamLog.Interpreter;

import SteamLog.App;
import SteamLog.Utils.AppConfig;
import SteamLog.Status.SteamStatus;

import java.util.List;

public class StatusHandler {
    private SteamStatus previousStatus;

    private int errCount = 0;

    private boolean isValid(SteamStatus status) {
        if ( status == null ) {
            //DEBUG
            App.logger.trace("Couldn't refresh status.");
            errCount++;
            return false;
        }
        else if ( !status.isProfileVisible() ) {
            App.logger.error("Requested user is invisible to the used api key.");
            errCount++;
            return false;
        }

        errCount = 0;
        return true;
    }


    public void process(SteamStatus newStatus) {
        int oldErrCount = errCount;

        if ( !isValid(newStatus) ) return;

        //DEBUG
        App.logger.trace(newStatus.toString());

        //Log when reaching new slowdown level.
        if (errCount > oldErrCount) {
            List<Integer> errorCountThresholds = AppConfig.ERROR_COUNT_THRESHOLDS;
            for (int i = 0; i < errorCountThresholds.size(); i++) {
                int threshold = errorCountThresholds.get(i);
                if (errCount >= threshold && oldErrCount < threshold) {
                    App.logger.debug(
                            "Error threshold of " + threshold + " reached."
                                + " Entering slowdown, new refresh delay: " + AppConfig.REFRESH_TIMES.get(i));
                }
            }
        }

        //if it's the first status logged in session
        // || status has changed
        // || it's the first refresh after an error chain
        if ( previousStatus == null || !previousStatus.equals(newStatus) || errCount < oldErrCount ) {
            App.logger.info(newStatus.csv());

            if (errCount < oldErrCount)
                App.logger.debug("");
        }

        previousStatus = newStatus;
    }

    /**
     * @return Time in millis to wait until the next ping should be sent.
     */
    public int getRefreshTime() {
        int i;
        for (i = 0; i < AppConfig.ERROR_COUNT_THRESHOLDS.size(); i++) {
            int threshold = AppConfig.ERROR_COUNT_THRESHOLDS.get(i);

            if (errCount < threshold) return AppConfig.REFRESH_TIMES.get(i);
        }

        //Return the immediately following item rather than the last
        // to avoid jumping over items if the user puts too many into the list
        return AppConfig.REFRESH_TIMES.get(AppConfig.REFRESH_TIMES.get(i));
    }



}
