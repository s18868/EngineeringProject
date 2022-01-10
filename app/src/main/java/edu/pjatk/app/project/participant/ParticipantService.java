package edu.pjatk.app.project.participant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository){
        this.participantRepository = participantRepository;
    }

    public void updateParticipant(Participant participant){
        participantRepository.update(participant);
    }

    public void removeParticipant(Participant participant){
        participantRepository.remove(participant);
    }

    public Optional<Participant> getById(Long id){
        return participantRepository.getById(id);
    }

    public Optional<Participant> getParticipantByUserAndProject(Long userid, Long projectId){
        return participantRepository.getByUserAndProjectIds(userid, projectId);
    }

    public Optional<List<Participant>> getAllWhereUserIsMember(Long userId){
        return participantRepository.getAllWhereUserIsMemberByUserId(userId);
    }

    public Optional<List<Participant>> getAllPending(Long userId){
        return participantRepository.getAllPending(userId);
    }

}
