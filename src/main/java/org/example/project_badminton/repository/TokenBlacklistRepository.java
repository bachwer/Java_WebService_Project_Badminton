package org.example.project_badminton.repository;

import org.example.project_badminton.entity.TokenBlacklist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface TokenBlacklistRepository extends CrudRepository<TokenBlacklist, String> {

}
