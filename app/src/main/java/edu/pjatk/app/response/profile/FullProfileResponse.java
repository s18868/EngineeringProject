package edu.pjatk.app.response.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class FullProfileResponse {

    private String username;
    private String name;
    private String surname;
    private String bio;
    private String profile_photo;

    private Set<String> categories;

}
