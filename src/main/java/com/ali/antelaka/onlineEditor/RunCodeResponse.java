package com.ali.antelaka.onlineEditor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeResponse {
    private String message ;
    private Integer Code ;
    private Integer numberOfRequestsPerDay ;
    private Integer maxRunsPerDay ;
}
