package com.solidstategroup.diagnosisview.task;

import com.solidstategroup.diagnosisview.model.codes.Code;
import com.solidstategroup.diagnosisview.service.CodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Async Processor to process codes syncing.
 *
 * Created by Pavlo Maksymchuk.
 */
@Slf4j
@Component
public class CodeProcessor {

    private final CodeService codeService;

    @Autowired
    public CodeProcessor(final CodeService codeService) {
        this.codeService = codeService;
    }

    @Async(value = "asyncExecutor")
    public void processBatch(List<Code> codes, int index) {
        log.info("Starting code batch {} {}", index, codes.size());
        long start = System.currentTimeMillis();

        codeService.batchProcess(codes);

        long stop = System.currentTimeMillis();
        log.info("Finished code batch {}, timing {}", index, (stop - start));
    }
}
