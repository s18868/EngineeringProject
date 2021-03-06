package edu.pjatk.app.project;

import edu.pjatk.app.file.File;
import edu.pjatk.app.photo.Photo;
import edu.pjatk.app.photo.PhotoService;
import edu.pjatk.app.project.category.Category;
import edu.pjatk.app.project.category.CategoryService;
import edu.pjatk.app.project.invitation.ProjectInvitation;
import edu.pjatk.app.project.invitation.ProjectInvitationService;
import edu.pjatk.app.project.participant.Participant;
import edu.pjatk.app.project.participant.ParticipantRole;
import edu.pjatk.app.project.participant.ParticipantService;
import edu.pjatk.app.recomendations.RecomendationService;
import edu.pjatk.app.request.ProjectRequest;
import edu.pjatk.app.response.project.*;
import edu.pjatk.app.user.User;
import edu.pjatk.app.user.UserRole;
import edu.pjatk.app.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Lazy;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final PhotoService photoService;
    private final ProjectInvitationService projectInvitationService;
    private final ParticipantService participantService;
    private final RecomendationService recomendationService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, CategoryService categoryService,
                          UserService userService, PhotoService photoService,
                          ProjectInvitationService projectInvitationService,
                          @Lazy ParticipantService participantService,
                          RecomendationService recomendationService) {
        this.projectRepository = projectRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.photoService = photoService;
        this.projectInvitationService = projectInvitationService;
        this.participantService = participantService;
        this.recomendationService = recomendationService;
    }

    public Optional<Project> getProjectObjectById(Long id) {
        return projectRepository.getProjectById(id);
    }

    public Optional<FullProjectResponse> getProjectById(Long id) {

        Optional<Project> projectOptional = projectRepository.getProjectById(id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (projectOptional.isPresent()){
            Project project = projectOptional.get();
            String projectPhoto, authorPhoto, ytLink, gitLink, fbLink, kickLink;
            Set<String> categories = new HashSet<>();

            // project photo
            if (project.getPhoto() != null) {
                projectPhoto = project.getPhoto().getFileName();
            } else {
                projectPhoto = null;
            }

            // author photo
            if (project.getCreator().getProfile().getPhoto() != null) {
                authorPhoto = project.getCreator().getProfile().getPhoto().getFileName();
            } else {
                authorPhoto = null;
            }

            // links
            ytLink = project.getYoutube_link();
            gitLink = project.getGithub_link();
            fbLink = project.getFacebook_link();
            kickLink = project.getKickstarter_link();

            for (Category c : project.getCategories()){
                categories.add(c.getTitle());
            }

            //Return average rating if there is more than one vote
            float averageRating = (project.getRatings().size() > 0 ?
                    project.getRatings().stream().collect(Collectors.summingInt(Rating::getValue)).floatValue()/project.getRatings().size()
                    : 0);
            averageRating = (float) (Math.round(averageRating * Math.pow(10, 2)) / Math.pow(10, 2));
            int numberOfVotes = project.getRatings().size();

            //Return IDs of project members
            Set<Long> participants = new HashSet<>();
            for (Participant p : project.getParticipants()){
                if (!p.isPending()) participants.add(p.getUser().getId());
            }

            return Optional.of(new FullProjectResponse(
                    project.getId(), projectPhoto,
                    project.getProject_name(), project.getProject_introduction(),
                    project.getProject_description(), project.getCreation_date().format(formatter),
                    project.getProject_status().name(), project.getProject_access().name(), categories,
                    ytLink, gitLink, fbLink, kickLink,
                    project.getCreator().getId(), project.getCreator().getUsername(),
                    authorPhoto, averageRating, numberOfVotes, participants
            ));
        }
        else return Optional.empty();
    }

    public Set<MiniProjectResponse> getProjectByName(String project_name) {

        Set<MiniProjectResponse> projectResponses = new HashSet<>();
        Optional<List<Project>> projectList = projectRepository.getProjectsByTitle(project_name);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (projectList.isPresent() && !projectList.get().isEmpty()) {

            String projectPhoto, authorPhoto;

            for (Project p : projectList.get()){
                Set<String> categories = new HashSet<>();

                if (p.getPhoto() != null) {
                    projectPhoto = p.getPhoto().getFileName();
                } else {
                    projectPhoto = null;
                }

                if (p.getCreator().getProfile().getPhoto() != null) {
                    authorPhoto = p.getCreator().getProfile().getPhoto().getFileName();
                } else {
                    authorPhoto = null;
                }

                for (Category c : p.getCategories()){
                    categories.add(c.getTitle());
                }

                projectResponses.add(
                        new MiniProjectResponse(
                                p.getId(), projectPhoto, p.getProject_name(), p.getProject_introduction(),
                                categories, p.getCreation_date().format(formatter), p.getCreator().getId(),
                                p.getCreator().getUsername(), authorPhoto
                        )
                );
            }
            return projectResponses;
        }
        else return Collections.emptySet();
    }

    public Set<MiniProjectResponse> getProjectByNameNoPrivate(String project_name) {
        Set<MiniProjectResponse> projectResponses = new HashSet<>();
        Optional<List<Project>> projectList = projectRepository.getProjectsByTitle(project_name);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (projectList.isPresent() && !projectList.get().isEmpty()) {
            
            String projectPhoto, authorPhoto;
            
            for (Project p : projectList.get()) {
                if(p.getProject_access().toString() != "PRIVATE") {
                    Set<String> categories = new HashSet<>();

                    if (p.getPhoto() != null) {
                        projectPhoto = p.getPhoto().getFileName();
                    } else {
                        projectPhoto = null;
                    }

                    if (p.getCreator().getProfile().getPhoto() != null) {
                        authorPhoto = p.getCreator().getProfile().getPhoto().getFileName();
                    } else {
                        authorPhoto = null;
                    }

                    for (Category c : p.getCategories()){
                        categories.add(c.getTitle());
                    }

                    projectResponses.add(
                            new MiniProjectResponse(
                                    p.getId(), projectPhoto, p.getProject_name(), p.getProject_introduction(),
                                    categories, p.getCreation_date().format(formatter), p.getCreator().getId(),
                                    p.getCreator().getUsername(), authorPhoto
                            )
                    );
                }
            }
            return projectResponses;
        }
        else return Collections.emptySet();
    }
    
    public Set<MiniProjectResponse> getProjectByCategory(String categoryTitle){
        Set<MiniProjectResponse> projectResponses = new HashSet<>();
        Optional<List<Project>> projectList = projectRepository.getByCategory(categoryTitle);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (projectList.isPresent() && !projectList.get().isEmpty()){

            String projectPhoto, authorPhoto;

            for (Project p : projectList.get()){

                Set<String> categories = new HashSet<>();
                if (p.getPhoto() != null) {
                    projectPhoto = p.getPhoto().getFileName();
                } else {
                    projectPhoto = null;
                }

                if (p.getCreator().getProfile().getPhoto() != null) {
                    authorPhoto = p.getCreator().getProfile().getPhoto().getFileName();
                } else {
                    authorPhoto = null;
                }

                for (Category c : p.getCategories()){
                    categories.add(c.getTitle());
                }

                projectResponses.add(
                        new MiniProjectResponse(
                                p.getId(), projectPhoto, p.getProject_name(), p.getProject_introduction(),
                                categories, p.getCreation_date().format(formatter), p.getCreator().getId(),
                                p.getCreator().getUsername(), authorPhoto
                        )
                );
            }
            return projectResponses;
        }
        else return Collections.emptySet();
    }

    public Set<MiniProjectResponse> getProjectsByCreatorId(Long creator_id) {

        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<User> projectCreator = userService.findUserById(creator_id);

        Set<MiniProjectResponse> projectResponses = new HashSet<>();
        Optional<List<Project>> projects = projectRepository.getAllCreatorProjects(creator_id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


        if (projectCreator.isPresent() && projects.isPresent() && !projects.get().isEmpty()){
            String projectPhoto, authorPhoto;

            for (Project p : projects.get()){
                Set<String> categories = new HashSet<>();
                if (p.getPhoto() != null) {
                    projectPhoto = p.getPhoto().getFileName();
                } else {
                    projectPhoto = null;
                }

                if (p.getCreator().getProfile().getPhoto() != null) {
                    authorPhoto = p.getCreator().getProfile().getPhoto().getFileName();
                } else {
                    authorPhoto = null;
                }

                for (Category c : p.getCategories()){
                    categories.add(c.getTitle());
                }

                if (!p.getProject_access().equals(ProjectAccess.PRIVATE) || (loggedUser.isPresent()
                        && loggedUser.get().equals(projectCreator.get()))) {
                    projectResponses.add(
                            new MiniProjectResponse(
                                    p.getId(), projectPhoto, p.getProject_name(), p.getProject_introduction(),
                                    categories, p.getCreation_date().format(formatter), p.getCreator().getId(),
                                    p.getCreator().getUsername(), authorPhoto
                            )
                    );
                }
            }
            return projectResponses;
        }
        else return Collections.emptySet();
    }

    public Set<MiniProjectResponse> getAllProposedProjects(){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Optional<List<Project>> allProjects = projectRepository.getAllProposedProjects(loggedUser.get().getId());

        if (loggedUser.isPresent() && !allProjects.get().isEmpty()){

            Set<MiniProjectResponse> proposedProjects = new HashSet<>();
            Set<String> userCategories = new HashSet<>();
            Set<String> projectCategories = new HashSet<>();
            loggedUser.get().getProfile().getCategories().stream().forEach(category -> userCategories.add(category.getTitle()));

            for (Project p : allProjects.get()){

                p.getCategories().stream().forEach(category -> projectCategories.add(category.getTitle()));

                if (projectCategories.stream().filter(s -> userCategories.contains(s)).collect(Collectors.toSet()).size() > 0){

                    String projectPhoto, authorPhoto;
                    Set<String> categories = new HashSet<>();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                    if (p.getPhoto() != null) {
                        projectPhoto = p.getPhoto().getFileName();
                    } else {
                        projectPhoto = null;
                    }

                    if (p.getCreator().getProfile().getPhoto() != null) {
                        authorPhoto = p.getCreator().getProfile().getPhoto().getFileName();
                    } else {
                        authorPhoto = null;
                    }

                    for (Category c : p.getCategories()){
                        categories.add(c.getTitle());
                    }

                    proposedProjects.add(
                            new MiniProjectResponse(
                                    p.getId(), projectPhoto, p.getProject_name(), p.getProject_introduction(),
                                    categories, p.getCreation_date().format(formatter), p.getCreator().getId(),
                                    p.getCreator().getUsername(), authorPhoto
                            )
                    );

                    projectCategories.clear();
                }
                projectCategories.clear();
            }
            return proposedProjects;
        }
        else return Collections.emptySet();
    }

    public List<FullProjectResponse> getAllNonPrivateProjects(){

        Optional<List<Project>> allProjects = projectRepository.getAllNonPrivateProjects();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<FullProjectResponse> responseList = new ArrayList<>();

        if (allProjects.isPresent() && allProjects.get().size() > 0){

            for (Project project : allProjects.get()){

                String projectPhoto, authorPhoto, ytLink, gitLink, fbLink, kickLink;
                Set<String> categories = new HashSet<>();

                // project photo
                if (project.getPhoto() != null) {
                    projectPhoto = project.getPhoto().getFileName();
                } else {
                    projectPhoto = null;
                }

                // author photo
                if (project.getCreator().getProfile().getPhoto() != null) {
                    authorPhoto = project.getCreator().getProfile().getPhoto().getFileName();
                } else {
                    authorPhoto = null;
                }

                // links
                ytLink = project.getYoutube_link();
                gitLink = project.getGithub_link();
                fbLink = project.getFacebook_link();
                kickLink = project.getKickstarter_link();

                for (Category c : project.getCategories()){
                    categories.add(c.getTitle());
                }

                //Return average rating if there is more than one vote
                float averageRating = (project.getRatings().size() > 0 ?
                        project.getRatings().stream().collect(Collectors.summingInt(Rating::getValue)).floatValue()/project.getRatings().size()
                        : 0);
                averageRating = (float) (Math.round(averageRating * Math.pow(10, 2)) / Math.pow(10, 2));
                int numberOfVotes = project.getRatings().size();

                //Return IDs of project members
                Set<Long> participants = new HashSet<>();
                for (Participant p : project.getParticipants()){
                    if (!p.isPending()) participants.add(p.getUser().getId());
                }

                responseList.add(new FullProjectResponse(
                        project.getId(), projectPhoto,
                        project.getProject_name(), project.getProject_introduction(),
                        project.getProject_description(), project.getCreation_date().format(formatter),
                        project.getProject_status().name(), project.getProject_access().name(), categories,
                        ytLink, gitLink, fbLink, kickLink,
                        project.getCreator().getId(), project.getCreator().getUsername(),
                        authorPhoto, averageRating, numberOfVotes, participants
                ));

            }
            return responseList;
        }
        else return Collections.emptyList();
    }

    //Pagination
    public List<MiniProjectResponse> getProjectsByNameWithPagination(String project_name, int pageNumber, int pageSize) {

        List<MiniProjectResponse> projectResponses = new ArrayList<>();
        Optional<List<Project>> projectList = projectRepository.getProjectsByTitleWithPagination(project_name, pageNumber, pageSize);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (projectList.isPresent() && !projectList.get().isEmpty()) {

            String projectPhoto, authorPhoto;

            for (Project p : projectList.get()){

                Set<String> categories = new HashSet<>();
                if (p.getPhoto() != null) {
                    projectPhoto = p.getPhoto().getFileName();
                } else {
                    projectPhoto = null;
                }

                if (p.getCreator().getProfile().getPhoto() != null) {
                    authorPhoto = p.getCreator().getProfile().getPhoto().getFileName();
                } else {
                    authorPhoto = null;
                }

                for (Category c : p.getCategories()){
                    categories.add(c.getTitle());
                }

                projectResponses.add(
                        new MiniProjectResponse(
                                p.getId(), projectPhoto, p.getProject_name(), p.getProject_introduction(),
                                categories, p.getCreation_date().format(formatter), p.getCreator().getId(),
                                p.getCreator().getUsername(), authorPhoto
                        )
                );
            }
            return projectResponses;
        }
        else return Collections.emptyList();
    }

    public Long getProjectsNumberByTitle(String title) {
        Optional<Long> number = projectRepository.getProjectsNumberByTitle(title);
        return number.isPresent() ? number.get() : 0;
    }

    @Transactional
    public void createProject(ProjectRequest projectRequest, MultipartFile photo){

        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Photo projectPhoto = (photo != null ? photoService.uploadPhoto(photo) : null);

        if (loggedUser.isPresent())
        {
            Set<Category> categories = new HashSet<>();
            for (String category : projectRequest.getCategory()){
                categories.add(categoryService.getCategoryByTitle(category).get());
            }

            Project project = new Project(
                    projectRequest.getTitle(), projectRequest.getIntroduction(), projectRequest.getDescription(),
                    LocalDateTime.now(), categories, ProjectStatus.OPEN, projectRequest.getAccess(),
                    projectRequest.getYoutubeLink(), projectRequest.getFacebookLink(), projectRequest.getGithubLink(),
                    projectRequest.getKickstarterLink(), loggedUser.get(), projectPhoto
            );

            //Add project to repository
            projectRepository.createProject(project);

            //Add project creator as participant with OWNER Role
            project.getParticipants().add(new Participant(loggedUser.get(), project, false, ParticipantRole.OWNER));
            projectRepository.update(project);
        }
    }

    @Transactional
    public void editProject(ProjectRequest projectRequest, MultipartFile photo, Long id){

        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<Project> projectToEdit = projectRepository.getProjectById(id);

        if (loggedUser.isEmpty() || projectToEdit.isEmpty()) { return; }

        Photo projectPhoto;
        if (photo != null) { projectPhoto = photoService.uploadPhoto(photo); }
        else { projectPhoto = projectToEdit.get().getPhoto(); }

        if ( projectToEdit.get().getCreator().getUsername().equals(
                loggedUser.get().getUsername()) || loggedUser.get().getUserRole().equals(UserRole.ADMIN))
        {
            Set<Category> categories = new HashSet<>();
            for (String category : projectRequest.getCategory()){
                categories.add(categoryService.getCategoryByTitle(category).get());
            }

            Project project = new Project(
                    projectRequest.getTitle(), projectRequest.getIntroduction(), projectRequest.getDescription(),
                    LocalDateTime.now(), categories, ProjectStatus.OPEN, projectRequest.getAccess(),
                    projectRequest.getYoutubeLink(), projectRequest.getFacebookLink(), projectRequest.getGithubLink(),
                    projectRequest.getKickstarterLink(), loggedUser.get(), projectPhoto
            );
            project.setId(id);
            projectRepository.update(project);
        }
    }


    //Rating section
    @Transactional
    public void rateProject(Long id, int ratingValue){

        Optional<Project> project = projectRepository.getProjectById(id);
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        if (project.isPresent() && loggedUser.isPresent()){

            Rating newRate = new Rating(ratingValue, loggedUser.get().getProfile(), project.get());

            Optional<Rating> oldRate = project.get().getRatings().stream().filter(
                    rating -> rating.getProfile().equals(loggedUser.get().getProfile())).findFirst();

            //Check if user already rated this project
            if (oldRate.isPresent()){
                for (int i=0; i < project.get().getRatings().size(); i++){
                    if (project.get().getRatings().get(i).equals(oldRate.get())){
                        project.get().getRatings().get(i).setValue(ratingValue);
                    }
                }
            } else project.get().getRatings().add(newRate);

            projectRepository.update(project.get());
        }
    }

    public int getMyRating(Long projectId){
        Optional<Project> projectOptional = projectRepository.getProjectById(projectId);
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        if (loggedUser.isPresent() && projectOptional.isPresent()){
            Project project = projectOptional.get();
            Optional<Rating> rate = project.getRatings().stream().filter(
                    rating -> rating.getProfile().equals(loggedUser.get().getProfile())).findFirst();
            if (rate.isPresent()) return rate.get().getValue();
            else return 0;
        }
        else return 0;
    }

    public List<FullProjectResponse> getTopRatedProjects(){

        List<FullProjectResponse> allProjects = getAllNonPrivateProjects();
        if (allProjects.isEmpty()) return Collections.emptyList();

        allProjects.sort(Comparator.comparing(FullProjectResponse::getNumberOfVotes).reversed());

        List<FullProjectResponse> top10 = new ArrayList<>(allProjects.stream().limit(10).toList());

        top10.sort(Comparator.comparing(FullProjectResponse::getAverageRating).reversed());

        return top10;

    }

    public List<FullProjectResponse> getRandomRecommendedProjects(){

        Optional<List<Project>> allProjects = projectRepository.getRandomRecommendedProjects(5);
        if (allProjects.isEmpty()) return Collections.emptyList();

        List<Project> randomProjects = allProjects.get();
        List<FullProjectResponse> randomResponse = new ArrayList<>();

        for (Project project: randomProjects) {
            //Return average rating if there is more than one vote
            float averageRating = (project.getRatings().size() > 0 ?
                    ((Integer) project.getRatings().stream().mapToInt(Rating::getValue).sum()).floatValue()/project.getRatings().size()
                    : 0);
            averageRating = (float) (Math.round(averageRating * Math.pow(10, 2)) / Math.pow(10, 2));
            int numberOfVotes = project.getRatings().size();

            //Return IDs of project members
            Set<Long> participants = new HashSet<>();
            for (Participant p : project.getParticipants()){
                if (!p.isPending()) participants.add(p.getUser().getId());
            }

            Set<String> categories = new HashSet<>();
            if (!project.getCategories().isEmpty()) {
                for (Category c : project.getCategories()){
                    categories.add(c.getTitle());
                }
            }

            String projectPhoto = "";
            if (project.getPhoto() != null) {
                projectPhoto = project.getPhoto().getFileName();
            }

            String userPhoto = "";
            if (project.getCreator().getProfile().getPhoto() != null) {
                userPhoto = project.getCreator().getProfile().getPhoto().getFileName();
            }

            FullProjectResponse projectResponse = new FullProjectResponse(
                    project.getId(), projectPhoto, project.getProject_name(), project.getProject_introduction(),
                    project.getProject_description(), project.getCreation_date().toString(), project.getProject_status().toString(),
                    project.getProject_access().toString(),categories, project.getYoutube_link(), project.getGithub_link(),
                    project.getFacebook_link(), project.getKickstarter_link(), project.getCreator().getId(),
                    project.getCreator().getUsername(), userPhoto,
                    averageRating, numberOfVotes, participants
            );
            randomResponse.add(projectResponse);
        }
        return randomResponse;
    }
    public List<FullProjectResponse> getRecommendedProjects(Long userId){
        Optional<User> userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty()) { return Collections.emptyList(); }
        User user = userOptional.get();

        List<Long> allProjectsIds = recomendationService.monthlyRecomendationIds(user).stream().limit(5).toList();
        List<Project> projects = new ArrayList<>(Collections.emptyList());
        for (Long id: allProjectsIds) {
            Optional<Project> optionalProject = projectRepository.getProjectById(id);
            if (optionalProject.isPresent()) {
                projects.add(optionalProject.get());
            }
        }

        List<FullProjectResponse> fullProjectResponses= new ArrayList<>();

        for (Project project: projects) {
            //Return average rating if there is more than one vote
            float averageRating = (project.getRatings().size() > 0 ?
                    ((Integer) project.getRatings().stream().mapToInt(Rating::getValue).sum()).floatValue()/project.getRatings().size()
                    : 0);
            averageRating = (float) (Math.round(averageRating * Math.pow(10, 2)) / Math.pow(10, 2));
            int numberOfVotes = project.getRatings().size();

            //Return IDs of project members
            Set<Long> participants = new HashSet<>();
            for (Participant p : project.getParticipants()){
                if (!p.isPending()) participants.add(p.getUser().getId());
            }

            Set<String> categories = new HashSet<>();
            if (!project.getCategories().isEmpty()) {
                for (Category c : project.getCategories()){
                    categories.add(c.getTitle());
                }
            }

            String projectPhoto = "";
            if (project.getPhoto() != null) {
                projectPhoto = project.getPhoto().getFileName();
            }

            String userPhoto = "";
            if (project.getCreator().getProfile().getPhoto() != null) {
                userPhoto = project.getCreator().getProfile().getPhoto().getFileName();
            }

            FullProjectResponse projectResponse = new FullProjectResponse(
                    project.getId(), projectPhoto, project.getProject_name(), project.getProject_introduction(),
                    project.getProject_description(), project.getCreation_date().toString(), project.getProject_status().toString(),
                    project.getProject_access().toString(),categories, project.getYoutube_link(), project.getGithub_link(),
                    project.getFacebook_link(), project.getKickstarter_link(), project.getCreator().getId(),
                    project.getCreator().getUsername(), userPhoto,
                    averageRating, numberOfVotes, participants
            );
            fullProjectResponses.add(projectResponse);
        }
        return fullProjectResponses;
    }




    //Project files section
    public Set<FileResponse> getProjectFiles(Long projectId){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Optional<Project> project = projectRepository.getProjectById(projectId);
        if (project.isEmpty()) return Collections.emptySet();

        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<Participant> participant = participantService.getParticipantByUserAndProject(
                loggedUser.get().getId(), project.get().getId());

        if (participant.isPresent()){

            Set<FileResponse> fileResponses = new HashSet<>();
            String profilePhoto;

            for (File f : project.get().getFiles()){
                if (f.getUser().getProfile().getPhoto() != null) {
                    profilePhoto = f.getUser().getProfile().getPhoto().getFileName();
                } else {
                    profilePhoto = null;
                }

                fileResponses.add(new FileResponse(f.getId(), f.getName(), f.getUrl(), f.getUser().getId(),
                        f.getUser().getUsername(), profilePhoto, f.getSize(),
                        f.getUploadDate().format(formatter), f.getIsLocked() ? "LOCKED" : "UNLOCKED"));
            }
            return fileResponses;
        }
        else return Collections.emptySet();
    }


    //Project roles section
    @Transactional
    public boolean promoteMember(Long userId, Long projectId){

        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Optional<Participant> loggedParticipant = participantService.getParticipantByUserAndProject(loggedUser.get().getId(), projectId);
        Optional<Project> project = projectRepository.getProjectById(projectId);
        Optional<Participant> participant = participantService.getParticipantByUserAndProject(userId, projectId);

        if (loggedUser.isPresent() && loggedParticipant.isPresent() && participant.isPresent() &&
                project.isPresent() && participant.get().getParticipantRole().equals(ParticipantRole.PARTICIPANT) &&
                project.get().getCreator().equals(loggedUser.get())){
            participant.get().setParticipantRole(ParticipantRole.MODERATOR);
            participantService.updateParticipant(participant.get());
            return true;
        }
        else return false;
    }

    @Transactional
    public boolean degradeMember(Long userId, Long projectId){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Optional<Participant> loggedParticipant = participantService.getParticipantByUserAndProject(loggedUser.get().getId(), projectId);
        Optional<Project> project = projectRepository.getProjectById(projectId);
        Optional<Participant> participant = participantService.getParticipantByUserAndProject(userId, projectId);

        if (loggedUser.isPresent() && loggedParticipant.isPresent() && project.isPresent() &&
                participant.isPresent() && participant.get().getParticipantRole().equals(ParticipantRole.MODERATOR) &&
                project.get().getCreator().equals(loggedUser.get())){
            participant.get().setParticipantRole(ParticipantRole.PARTICIPANT);
            participantService.updateParticipant(participant.get());
            return true;
        }
        else return false;
    }

    @Transactional
    public boolean kickMember(Long userId, Long projectId){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Optional<Participant> loggedParticipant = participantService.getParticipantByUserAndProject(loggedUser.get().getId(), projectId);
        Optional<Project> project = projectRepository.getProjectById(projectId);
        Optional<Participant> participant = participantService.getParticipantByUserAndProject(userId, projectId);

        if (loggedUser.isPresent() && loggedParticipant.isPresent() && project.isPresent() &&
                participant.isPresent() && !loggedUser.get().getId().equals(userId) &&
                !loggedParticipant.get().getParticipantRole().equals(ParticipantRole.PARTICIPANT) &&
                !project.get().getCreator().getId().equals(userId)){
            if (loggedParticipant.get().getParticipantRole().equals(ParticipantRole.OWNER)) {
                participantService.removeParticipant(participant.get());
                return true;
            }
            else if (loggedParticipant.get().getParticipantRole().equals(ParticipantRole.MODERATOR) && participant.get().getParticipantRole().equals(ParticipantRole.PARTICIPANT)) {
                participantService.removeParticipant(participant.get());
                return true;
            }
            else return false;
        }
        else return false;
    }


    //Membership and invitations section
    public Set<ProjectJoinRequestResponse> getAllPendingRequests(){
        //Requests to join 'protected' project
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<List<Participant>> participants = participantService.getAllPending(loggedUser.get().getId());

        if (participants.isPresent() && participants.get().size() > 0){
            String profilePhoto;
            Set<ProjectJoinRequestResponse> pending = new HashSet<>();

            for (Participant p : participants.get()){
                if (p.getUser().getProfile().getPhoto() != null) {
                    profilePhoto = p.getUser().getProfile().getPhoto().getFileName();
                } else {
                    profilePhoto = null;
                }

                pending.add(new ProjectJoinRequestResponse(
                        p.getId(), p.getUser().getId(), p.getUser().getUsername(),
                        profilePhoto, p.getProject().getId(),
                        p.getProject().getProject_name()
                ));
            }
            return pending;
        }
        else return Collections.emptySet();
    }

    public Set<InvitationResponse> getAllInvitations(){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<List<ProjectInvitation>> invitations = projectInvitationService.getAllInvitationsByUserId(loggedUser.get().getId());
        Set<InvitationResponse> invitationResponses = new HashSet<>();

        if (invitations.isPresent() && invitations.get().size() > 0){
            String projectPhoto;
            for (ProjectInvitation invitation : invitations.get()){
                if (invitation.getProject().getPhoto() != null) {
                    projectPhoto = invitation.getProject().getPhoto().getFileName();
                } else {
                    projectPhoto = null;
                }

                invitationResponses.add(
                        new InvitationResponse(invitation.getId(), invitation.getProject().getId(),
                                projectPhoto, invitation.getProject().getProject_name())
                );
            }
            return invitationResponses;
        }
        else return Collections.emptySet();
    }

    public Set<MiniProjectResponse> getAllProjectsWhereUserJoined(){
        //Includes only members with pending = false
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<List<Participant>> participantOf = participantService.getAllWhereUserJoined(loggedUser.get().getId());

        if (participantOf.isPresent() && participantOf.get().size() > 0){

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            Set<MiniProjectResponse> projectResponses = new HashSet<>();
            String projectPhoto, authorPhoto;

            for (Participant participant : participantOf.get()){
                Set<String> categories = new HashSet<>();

                if (participant.getProject().getPhoto() != null) {
                    projectPhoto = participant.getProject().getPhoto().getFileName();
                } else {
                    projectPhoto = null;
                }

                if (participant.getProject().getCreator().getProfile().getPhoto() != null) {
                    authorPhoto = participant.getProject().getCreator().getProfile().getPhoto().getFileName();
                } else {
                    authorPhoto = null;
                }

                for (Category c : participant.getProject().getCategories()){
                    categories.add(c.getTitle());
                }

                projectResponses.add(
                        new MiniProjectResponse(
                                participant.getProject().getId(), projectPhoto, participant.getProject().getProject_name(),
                                participant.getProject().getProject_introduction(), categories,
                                participant.getProject().getCreation_date().format(formatter),
                                participant.getProject().getCreator().getId(),
                                participant.getProject().getCreator().getUsername(), authorPhoto
                        )
                );
            }
            return projectResponses;
        }
        else return Collections.emptySet();
    }

    public Set<MemberResponse> getProjectMembers(Long projectId){

        Optional<Project> project = projectRepository.getProjectById(projectId);

        if (project.isPresent()){

            String profilePhoto;
            Set<MemberResponse> members = new HashSet<>();

            for (Participant participant : project.get().getParticipants()){
                if (!participant.isPending()){
                    if (participant.getUser().getProfile().getPhoto() != null) {
                        profilePhoto = participant.getUser().getProfile().getPhoto().getFileName();
                    } else {
                        profilePhoto = null;
                    }

                    members.add(new MemberResponse(participant.getUser().getId(), participant.getUser().getUsername(),
                            profilePhoto, participant.getParticipantRole().toString())
                    );
                }
            }
            return members;
        }
        else return Collections.emptySet();
    }

    @Transactional
    public boolean inviteToProject(Long projectId, Long userId){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Optional<User> userToInvite = userService.findUserById(userId);
        Optional<Project> project = projectRepository.getProjectById(projectId);
        Optional<ProjectInvitation> invitation = projectInvitationService
                .getInvitationByUserIdAndProjectId(userId, projectId);

        boolean userIsProjectModerator = !new HashSet<>(project.get()
                .getParticipants()).stream().filter(
                participant -> loggedUser.get().getParticipants().contains(participant) && 
                        participant.getParticipantRole().equals(ParticipantRole.MODERATOR)
        ).collect(Collectors.toSet()).isEmpty();

        boolean userToInviteIsNotAlreadyProjectParticipant = new HashSet<>(project.get()
                .getParticipants()).stream().filter(
                participant -> userToInvite.get().getParticipants().contains(participant))
                .collect(Collectors.toSet()).isEmpty();


        if ( userToInvite.isPresent() && project.isPresent() && invitation.isEmpty() &&
                (project.get().getCreator().equals(loggedUser.get()) || userIsProjectModerator)
                && userToInviteIsNotAlreadyProjectParticipant){
            projectInvitationService.addProjectInvitation(
                    new ProjectInvitation(project.get(), userToInvite.get())
            );
            return true;
        }
        else return false;
    }

    @Transactional
    public boolean acceptInvitation(Long invitationId){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<ProjectInvitation> invitation = projectInvitationService.getInvitationById(invitationId);

        if (invitation.isPresent() && invitation.get().getReceiver().equals(loggedUser.get())){
            Optional<Project> project = projectRepository.getProjectById(invitation.get().getProject().getId());
            project.get().getParticipants().add(new Participant(loggedUser.get(), project.get(), false, ParticipantRole.PARTICIPANT));
            projectRepository.update(project.get());
            projectInvitationService.removeProjectInvitation(invitation.get());
            return true;
        }
        else return false;
    }

    @Transactional
    public boolean rejectInvitation(Long invitationId){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<ProjectInvitation> invitation = projectInvitationService.getInvitationById(invitationId);

        if (invitation.isPresent() && invitation.get().getReceiver().equals(loggedUser.get())){
            projectInvitationService.removeProjectInvitation(invitation.get());
            return true;
        }
        else return false;
    }

    @Transactional
    public boolean joinProject(Long id){

        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Optional<Project> project = projectRepository.getProjectById(id);

        Set<Participant> participants = new HashSet<>(project.get().getParticipants());
        participants.retainAll(loggedUser.get().getParticipants());
        //Check if user is not already in project
        if (loggedUser.isPresent() && participants.size() == 0){

            //If project is public
            if (project.get().getProject_access().equals(ProjectAccess.PUBLIC)){
                project.get().getParticipants().add(
                        new Participant(loggedUser.get(), project.get(), false, ParticipantRole.PARTICIPANT)
                );
            }
            else {
                //If project is protected
                project.get().getParticipants().add(
                        new Participant(loggedUser.get(), project.get(), true, ParticipantRole.PARTICIPANT)
                );
            }
            projectRepository.update(project.get());
            return true;
        }
        else {
            return false;
        }
    }

    @Transactional
    public boolean leaveProject(Long id){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Optional<Project> project = projectRepository.getProjectById(id);

        Optional<Participant> participant = participantService.getParticipantByUserAndProject(
                loggedUser.get().getId(), project.get().getId()
        );

        if (participant.isPresent()){
            project.get().getParticipants().remove(participant.get());
            projectRepository.update(project.get());
            return true;
        }
        else return false;
    }

    @Transactional
    public void acceptPending(Long pendingId){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<Participant> participant = participantService.getById(pendingId);

        if(participant.isPresent() && participant.get().getProject().getCreator().equals(loggedUser.get())){
            participant.get().setPending(false);
            participantService.updateParticipant(participant.get());
        }
    }

    @Transactional
    public void rejectPending(Long pendingId){
        Optional<User> loggedUser = userService.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Optional<Participant> participant = participantService.getById(pendingId);

        if(participant.isPresent() && participant.get().getProject().getCreator().equals(loggedUser.get())){
            participantService.removeParticipant(participant.get());
        }
    }

    public void deleteProject(Long id) {
        projectRepository.deleteProject(id);
    }
}
