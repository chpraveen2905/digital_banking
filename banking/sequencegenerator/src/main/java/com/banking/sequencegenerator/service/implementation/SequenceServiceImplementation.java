package com.banking.sequencegenerator.service.implementation;

import com.banking.sequencegenerator.model.Sequence;
import com.banking.sequencegenerator.repository.SequenceRepository;
import com.banking.sequencegenerator.service.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceServiceImplementation implements SequenceService {

    private final SequenceRepository sequenceRepository;

    @Override
    public Sequence create() {
        log.info("creating a account number");
        return sequenceRepository.findById(1L)
                .map(sequence -> {
                    sequence.setAccountNumber(sequence.getAccountNumber() + 1);
                    return sequenceRepository.save(sequence);
                }).orElseGet(() -> sequenceRepository.save(Sequence.builder().accountNumber(1L).build()));
    }
}
