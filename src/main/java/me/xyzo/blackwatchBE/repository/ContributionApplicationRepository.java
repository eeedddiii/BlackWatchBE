package me.xyzo.blackwatchBE.repository;

import me.xyzo.blackwatchBE.domain.ContributionApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContributionApplicationRepository extends JpaRepository<ContributionApplication, String> {
}