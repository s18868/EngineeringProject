package edu.pjatk.app.photo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Optional;

@Repository
public class PhotoRepository {

    private final EntityManager entityManager;

    @Autowired
    public PhotoRepository(EntityManager entityManager){
        this.entityManager = entityManager;
    }


    public void save(Photo photo){
        entityManager.persist(photo);
    }

    public Optional<Photo> findById(Long id){
        Optional photo;
        try {
            photo = Optional.of(
                    entityManager.createQuery(
                                    "SELECT photo FROM Photo photo WHERE photo.id = :id", Photo.class)
                            .setParameter("id", id).getSingleResult()
            );
        } catch (NoResultException noResultException){
            photo = Optional.empty();
        }
        return photo;
    }

    public Optional<Photo> findByUrl(String url){
        Optional photo;
        try {
            photo = Optional.of(
                    entityManager.createQuery(
                                    "SELECT photo FROM Photo photo WHERE photo.url = :url", Photo.class)
                            .setParameter("url", url).getSingleResult()
            );
        } catch (NoResultException noResultException){
            photo = Optional.empty();
        }
        return photo;
    }

}
