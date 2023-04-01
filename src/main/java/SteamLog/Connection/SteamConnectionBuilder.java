package SteamLog.Connection;

import SteamLog.App;
import SteamLog.Utils.Util;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SteamConnectionBuilder {
    private static final String STATS_QUERY_CORE = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/";
    private static final String STATS_QUERY_LINK = "&steamids=";
    private static final String VANITY_QUERY_CORE = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/";
    private static final String VANITY_QUERY_LINK = "&vanityurl=";
    private static final String API_KEY_LINK = "?key=";
    private final String apiKey;
    private String userID;
    private String vanityID;
    private String request;

    //----METHODS

    public record VanityArgs(String apiKey, String vid) {}
    public record UIDArgs(String apiKey, String uid) {}

    public SteamConnectionBuilder(UIDArgs args) {
        this.apiKey = args.apiKey();
        this.userID = args.uid();
    }

    public SteamConnectionBuilder(VanityArgs args) {
        this.apiKey = args.apiKey();
        this.vanityID = args.vid();
        recoverUID(args.vid());
        buildRequest();
    }

    private void buildRequest() {
        request = STATS_QUERY_CORE + API_KEY_LINK + apiKey + STATS_QUERY_LINK + userID;
    }

    private String recoverUID(String vanityUrl) {
        try {
            URL uidUrl = new URL(VANITY_QUERY_CORE + API_KEY_LINK + apiKey + VANITY_QUERY_LINK + vanityUrl);
            HttpURLConnection uidcon = (HttpURLConnection) uidUrl.openConnection();
            uidcon.setRequestMethod("GET");
            if (uidcon.getResponseCode() != 200) return null;

            String response = Util.convertStreamToString(uidcon.getInputStream());
            JSONObject inter = new JSONObject(response);
            JSONObject proper = (JSONObject) inter.get("response");
            userID = proper.optString("steamid");
        } catch (Exception ex) {
            App.logger.trace(ex.getMessage());
            return null;
        }
        return userID;
    }

    //package private - only for use with handler
    String getUID() {
        return userID;
    }
    String getVID() {
        return vanityID;
    }

    //"It should be noted that a URLConnection instance does not establish the actual network connection on creation. This will happen only when calling URLConnection.connect()."
    public ConBuilderState checkState() {
        if (userID == null) return ConBuilderState.NO_UID;
        try {
            new URL(request).openConnection();
        } catch (MalformedURLException ex) {
            return ConBuilderState.BAD_URL;
        } catch (IOException ex) {
            return ConBuilderState.BAD_CONNECTION;
        }
        return ConBuilderState.READY;
    }

    HttpURLConnection build() {
        try {
            return (HttpURLConnection) (new URL(request)).openConnection();
        } catch (Exception any) {
            return null;
        }
    }

    public String getRequestString() {
        return request;
    }


}
