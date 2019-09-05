package com.esisba2019.finalproject2019.service.repository;

import com.esisba2019.finalproject2019.model.ConfirmationToken;
import org.springframework.data.repository.CrudRepository;

public interface ConfirmationTokenRepository extends CrudRepository<ConfirmationToken, String> {
	ConfirmationToken findByConfirmationToken(String confirmationToken);
}
