package edu.pjatk.app.project.participant;

import edu.pjatk.app.project.Project;
import edu.pjatk.app.project.Rating;
import edu.pjatk.app.project.task.Task;
import edu.pjatk.app.project.team.Team;
import edu.pjatk.app.timestamp.Timestamp;
import edu.pjatk.app.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ParticipantRole participantRole;
    private boolean pending;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToMany(mappedBy = "participants", cascade = CascadeType.REMOVE)
    private Set<Team> teams = new HashSet<>();

    @OneToMany(mappedBy = "participant", orphanRemoval = true)
    private List<Timestamp> timestamps = new ArrayList<>();

    @OneToMany(mappedBy = "project", orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    public Participant(User user, Project project, Boolean pending, ParticipantRole participantRole){
        this.user = user;
        this.project = project;
        this.pending = pending;
        this.participantRole = participantRole;
    }

}
