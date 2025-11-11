package com.project.core_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotTranslateRequestDTO {
    private String question;
    private boolean execute;
}
