package com.track.track.dto;

public class CodeChefStatsDto {

    private String currentRating;
    private String highestRating;
    private String globalRank;
    private String countryRank;
    private String totalSolved;
    private String contestsAttended;
    private String lastActive;
    private String stars;
    private String division;

    // Last contest info
    private String lastContestName;
    private String lastContestDate;
    private String lastContestRank;
    private String lastContestSolved;
    private String lastContestFinished;

    public CodeChefStatsDto() {}

    public String getCurrentRating()    { return currentRating; }
    public void setCurrentRating(String v)    { this.currentRating = v; }
    public String getHighestRating()    { return highestRating; }
    public void setHighestRating(String v)    { this.highestRating = v; }
    public String getGlobalRank()       { return globalRank; }
    public void setGlobalRank(String v)       { this.globalRank = v; }
    public String getCountryRank()      { return countryRank; }
    public void setCountryRank(String v)      { this.countryRank = v; }
    public String getTotalSolved()      { return totalSolved; }
    public void setTotalSolved(String v)      { this.totalSolved = v; }
    public String getContestsAttended() { return contestsAttended; }
    public void setContestsAttended(String v) { this.contestsAttended = v; }
    public String getLastActive()       { return lastActive; }
    public void setLastActive(String v)       { this.lastActive = v; }
    public String getStars()            { return stars; }
    public void setStars(String v)            { this.stars = v; }
    public String getDivision()         { return division; }
    public void setDivision(String v)         { this.division = v; }

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