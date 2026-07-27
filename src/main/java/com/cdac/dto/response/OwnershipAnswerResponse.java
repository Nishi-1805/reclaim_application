package com.cdac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipAnswerResponse {

    private Long ownershipQuestionId;

    private String questionText;

    private String responseText;

}