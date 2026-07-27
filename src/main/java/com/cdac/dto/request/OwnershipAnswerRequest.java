package com.cdac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class OwnershipAnswerRequest {

    @NotNull(message = "Ownership Question Id is required.")
    private Long ownershipQuestionId;

    @NotBlank(message = "Response cannot be empty.")
    private String responseText;

}