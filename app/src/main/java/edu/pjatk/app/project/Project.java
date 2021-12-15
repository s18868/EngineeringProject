package edu.pjatk.app.project;

import edu.pjatk.app.file.File;
import edu.pjatk.app.task.Task;
import edu.pjatk.app.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`project`")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String project_name;
    private String project_description;
    private LocalDateTime creation_date;
    private String project_category;
    private String project_status;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_creator")
    private User creator;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")   //project_participant
    private List<User> participants;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")  //project_file
    private List<File> file;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")  //project_task
    private List<Task> task;


    public Project(String project_name, LocalDateTime creation_date,
                    String project_category, String project_status, User creator){
        this.project_name = project_name;
        this.creation_date = creation_date;
        this.project_category = project_category;
        this.project_status = project_status;
        this.creator = creator;
    }

    public Project(String project_name, String project_description, LocalDateTime creation_date,
                   String project_category, String project_status, User creator){
        this.project_name = project_name;
        this.project_description = project_description;
        this.creation_date = creation_date;
        this.project_category = project_category;
        this.project_status = project_status;
        this.creator = creator;
    }
}