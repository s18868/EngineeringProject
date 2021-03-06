package edu.pjatk.app.response.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class FullProjectResponse {

    private Long projectId;
    private String projectPhoto;
    private String title;
    private String introduction;
    private String description;
    private String creationDate;
    private String status;
    private String access;
    private Set<String> categories;

    private String youtubeLink;
    private String githubLink;
    private String facebookLink;
    private String kickstarterLink;

    private Long authorId;
    private String authorUsername;
    private String authorPhoto;

    private float averageRating;
    private int numberOfVotes;

    private Set<Long> participants;
}
