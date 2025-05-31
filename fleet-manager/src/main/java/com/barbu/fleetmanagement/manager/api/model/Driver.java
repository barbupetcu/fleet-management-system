package com.barbu.fleetmanagement.manager.api.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record Driver(Long id,
                     @NotEmpty String firstName,
                     @NotEmpty String lastName,
                     @NotEmpty String identificationNumber,
                     @NotNull LocalDate birthDate) {

}
