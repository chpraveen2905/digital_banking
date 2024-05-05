package com.banking.sequencegenerator.repository;

import com.banking.sequencegenerator.model.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SequenceRepository extends JpaRepository<Sequence, Long> {
    @Query("SELECT Count(s) from Sequence s")
    int countAll();

    Sequence findFirstOrderBySequenceIdDesc();
}
