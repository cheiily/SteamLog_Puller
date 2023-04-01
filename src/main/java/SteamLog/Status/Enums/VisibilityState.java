package SteamLog.Status.Enums;

public enum VisibilityState {
    INVISIBLE(1),
    VISIBLE(3);

    private final int val;

    private VisibilityState(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static VisibilityState fromInt(int i) {
        for (var v : VisibilityState.values())
            if (v.val == i) return v;
        return null;
    }
}
