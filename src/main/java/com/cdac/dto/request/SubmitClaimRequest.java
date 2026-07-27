package com.cdac.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class SubmitClaimRequest {

    @NotNull(message = "Item Match Id is required.")
    private Long itemMatchId;

    @Valid
    @NotEmpty(message = "Ownership responses are required.")
    private List<OwnershipAnswerRequest> ownershipAnswers;

}