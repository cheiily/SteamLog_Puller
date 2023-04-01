package SteamLog.Status.Enums;

public enum PersonaStatus {
    OFFLINE(0),
    ONLINE(1),
    BUSY(2),
    AWAY(3),
    LTT(4),                //looking to trade
    LTP(5),                 //looking to play

    UNOBTAINED(-1);

    private final int val;

    private PersonaStatus(int v) {
        val = v;
    }

    public int getVal() {
        return val;
    }

    public static PersonaStatus fromInt(int i) {
        for (var v : PersonaStatus.values()) {
            if (v.val == i) return v;
        }
        return null;
    }
}
