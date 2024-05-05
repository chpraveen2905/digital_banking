package com.banking.sequencegenerator.controller;

import com.banking.sequencegenerator.model.Sequence;
import com.banking.sequencegenerator.service.SequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sequence")
public class SequenceController {
    private final SequenceService sequenceService;

    @PostMapping
    public Sequence generateAccountNumber(){
        return sequenceService.create();
    }
}
