package main.java.com.ecm.library.repository;

import com.ecm.library.entity.RetentionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RetentionPolicyRepository extends JpaRepository<RetentionPolicy, UUID> {

    Optional<RetentionPolicy> findByPolicyCode(String policyCode);
}
