package SteamLog.Status;

import SteamLog.Utils.Util;
import SteamLog.Status.Enums.PersonaStatus;
import SteamLog.Status.Enums.VisibilityState;

import java.time.LocalDateTime;
import java.util.Objects;

public record SteamStatus(
    String userId,
    String profileName,
    PersonaStatus personaStatus,
    String gameID,
    String gameName,
    VisibilityState visibilityState,
    LocalDateTime lastLogOff,
    LocalDateTime timeObtained
) {
    public SteamStatus(String userId, String profileName, PersonaStatus personaStatus, String gameID, String gameName, VisibilityState visibilityState, LocalDateTime lastLogOff) {
        this(userId, profileName, personaStatus, gameID, gameName, visibilityState, lastLogOff, LocalDateTime.now());
    }

    public SteamStatus(String userId, String profileName, PersonaStatus personaStatus, VisibilityState visibilityState, LocalDateTime lastLogOff) {
        this(userId, profileName, personaStatus, Util.unobtained, Util.unobtained, visibilityState, lastLogOff, LocalDateTime.now());
    }

    public boolean isProfileVisible() { return visibilityState == VisibilityState.VISIBLE; }

    public boolean isPlaying() {
        return personaStatus == PersonaStatus.ONLINE
                && gameID != null
                && !gameID.equals(Util.unobtained);
    }

    public SteamStatus withGame(String gameID, String gameName) {
        return new SteamStatus(this.userId, this.profileName, this.personaStatus, gameID, gameName, this.visibilityState, this.lastLogOff, this.timeObtained);
    }


    @Override
    public String toString() {
        return ""
                + timeObtained + ','
                + userId + ','
                + profileName + ','
                + visibilityState + ','
                + personaStatus + ','
                + gameID + ','
                + gameName + ','
                + lastLogOff;
    }

    public String csv() {
        return ""
                + timeObtained + ','
                + userId + ','
                + profileName + ','
                + visibilityState.getVal() + ','
                + personaStatus.getVal() + ','
                + gameID + ','
                + gameName + ','
                + lastLogOff;
    }

    /**
     * @implNote   Does not compare retrieval time
     * @param o   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SteamStatus that = (SteamStatus) o;
        return Objects.equals(profileName, that.profileName)
                && personaStatus == that.personaStatus
                && Objects.equals(gameID, that.gameID)
                && Objects.equals(gameName, that.gameName)
                && visibilityState == that.visibilityState
                && Objects.equals(lastLogOff, that.lastLogOff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileName, personaStatus, gameID, gameName, visibilityState, lastLogOff, timeObtained);
    }
}
