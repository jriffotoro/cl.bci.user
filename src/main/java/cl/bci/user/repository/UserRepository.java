package cl.bci.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cl.bci.user.model.User;

public interface UserRepository extends CrudRepository<User, String> {
    
    @Override
    List<User> findAll();

    User name(@Param("name") String name);

    Optional<User> email(@Param("email") String email);

}