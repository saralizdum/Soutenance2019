package com.esisba2019.finalproject2019.service.repository;

import com.esisba2019.finalproject2019.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("userRepository")
public interface UserRepository extends CrudRepository<User, String> {
	User findByEmailIdIgnoreCase(String emailId);
}
