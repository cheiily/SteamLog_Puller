package SteamLog.Utils;

import java.io.*;
import java.time.*;

public class Util {
    public final static String unobtained = "UNOBTAINED";

    //thank you, SO user
    public static String convertStreamToString(InputStream is) {
        StringBuilder sb = new StringBuilder();

        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static LocalDateTime parseEpoch(String epoch) {
        try {
            return LocalDateTime.ofEpochSecond(Long.parseLong(epoch), 0, ZonedDateTime.now().getOffset());
        } catch (NumberFormatException exception) {
            return LocalDateTime.ofEpochSecond(0, 0, ZonedDateTime.now().getOffset());
        }
    }

}
