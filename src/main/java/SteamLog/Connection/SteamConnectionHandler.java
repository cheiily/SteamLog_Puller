package SteamLog.Connection;

import SteamLog.App;
import SteamLog.Status.SteamStatus;
import SteamLog.Status.Enums.PersonaStatus;
import SteamLog.Status.Enums.VisibilityState;
import SteamLog.Utils.AppConfig;
import SteamLog.Utils.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;

public class SteamConnectionHandler {
    private SteamConnectionBuilder connectionBuilder;

    /**
     * As per HttpUrlConnection documentation - each "connection" object represents a single query.
     * @return boolean representing the success of initialization,
     */
    public SteamStatus requestData() {
        ConBuilderState st;
        if ( (st = connectionBuilder.checkState()) != ConBuilderState.READY ) {
            App.logger.error("Connection builder for " + connectionBuilder.getUID() + " unready with message: " + st.errMessage
                    + " Appending new builder.");

            if ( st == ConBuilderState.NO_UID )
                connectionBuilder = new SteamConnectionBuilder(
                        new SteamConnectionBuilder.VanityArgs(AppConfig.API_KEY, connectionBuilder.getVID())
                );
            else connectionBuilder = new SteamConnectionBuilder(
                    new SteamConnectionBuilder.UIDArgs(AppConfig.API_KEY, connectionBuilder.getUID())
                );

            return null;
        }

        JSONObject jsonObject;
        try {
            String ss = Util.convertStreamToString(connectionBuilder.build().getInputStream());
            JSONObject inter = (JSONObject) (new JSONObject(ss).get("response"));

            jsonObject = (JSONObject) (inter.getJSONArray("players").get(0));
        } catch (JSONException jsonEx) {
            App.logger.error("Failed to parse response to JSON.");
            return null;
        } catch (IOException exception) {
            App.logger.error("Failed to obtain a response from Steam. Check the network connection.");
            return null;
        }

        String userID =         connectionBuilder.getUID();
        String profileName =    jsonObject.optString("personaname", Util.unobtained);
        PersonaStatus personaStatus = PersonaStatus.fromInt(
                                jsonObject.optInt("personastate", -1));
        String gameId =         jsonObject.optString("gameid", Util.unobtained);
        String gameName =       jsonObject.optString("gameextrainfo", Util.unobtained);
        VisibilityState visibilityState = VisibilityState.fromInt(
                                jsonObject.optInt("communityvisibilitystate", 1));
        LocalDateTime lastlogoff = Util.parseEpoch(
                                jsonObject.optString("lastlogoff", "0"));

        return new SteamStatus(userID, profileName, personaStatus, gameId, gameName, visibilityState, lastlogoff);
    }

    public SteamConnectionHandler(SteamConnectionBuilder builder) {
        connectionBuilder = builder;
    }


    @Override
    public String toString() {
        return "SteamConnectionHandler{" +
                "connectionBuilder=" + connectionBuilder +
                '}';
    }

}
