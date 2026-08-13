package com.track.track.dto;

public class GitHubStatsDto {

    private String username;
    private String name;
    private String bio;
    private String avatarUrl;
    private int publicRepos;
    private int followers;
    private int following;
    private int totalStars;
    private int totalForks;
    private int totalCommits;
    private String topLanguage;
    private String profileUrl;
    private String location;
    private String company;
    private String blog;
    private String createdAt;

    public GitHubStatsDto() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public int getPublicRepos() { return publicRepos; }
    public void setPublicRepos(int publicRepos) { this.publicRepos = publicRepos; }
    public int getFollowers() { return followers; }
    public void setFollowers(int followers) { this.followers = followers; }
    public int getFollowing() { return following; }
    public void setFollowing(int following) { this.following = following; }
    public int getTotalStars() { return totalStars; }
    public void setTotalStars(int totalStars) { this.totalStars = totalStars; }
    public int getTotalForks() { return totalForks; }
    public void setTotalForks(int totalForks) { this.totalForks = totalForks; }
    public int getTotalCommits() { return totalCommits; }
    public void setTotalCommits(int totalCommits) { this.totalCommits = totalCommits; }
    public String getTopLanguage() { return topLanguage; }
    public void setTopLanguage(String topLanguage) { this.topLanguage = topLanguage; }
    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getBlog() { return blog; }
    public void setBlog(String blog) { this.blog = blog; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}