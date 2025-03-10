package dev.luisoliveira.roteiro.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProcessStatus {
    private String processId;
    private String currentStage;
    private int progressPercentage;
    private LocalDateTime startTime;
    private LocalDateTime lastUpdated;
    private boolean completed;
    private String resultPath;
}