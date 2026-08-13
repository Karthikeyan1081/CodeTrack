package com.track.track.dto;

public class CodeforcesStatsDto {

    private String currentRating;
    private String highestRating;
    private String globalRank;
    private String totalSolved;
    private String contestsAttended;
    private String lastActive;

    // Last contest info
    private String lastContestName;
    private String lastContestDate;
    private String lastContestRank;
    private String lastContestSolved;
    private String lastContestFinished;

    public CodeforcesStatsDto() {}

    public String getCurrentRating()    { return currentRating; }
    public void setCurrentRating(String v)    { this.currentRating = v; }
    public String getHighestRating()    { return highestRating; }
    public void setHighestRating(String v)    { this.highestRating = v; }
    public String getGlobalRank()       { return globalRank; }
    public void setGlobalRank(String v)       { this.globalRank = v; }
    public String getTotalSolved()      { return totalSolved; }
    public void setTotalSolved(String v)      { this.totalSolved = v; }
    public String getContestsAttended() { return contestsAttended; }
    public void setContestsAttended(String v) { this.contestsAttended = v; }
    public String getLastActive()       { return lastActive; }
    public void setLastActive(String v)       { this.lastActive = v; }

    public String getLastContestName()     { return lastContestName; }
    public void setLastContestName(String v)     { this.lastContestName = v; }
    public String getLastContestDate()     { return lastContestDate; }
    public void setLastContestDate(String v)     { this.lastContestDate = v; }
    public String getLastContestRank()     { return lastContestRank; }
    public void setLastContestRank(String v)     { this.lastContestRank = v; }
    public String getLastContestSolved()   { return lastContestSolved; }
    public void setLastContestSolved(String v)   { this.lastContestSolved = v; }
    public String getLastContestFinished() { return lastContestFinished; }
    public void setLastContestFinished(String v) { this.lastContestFinished = v; }
}