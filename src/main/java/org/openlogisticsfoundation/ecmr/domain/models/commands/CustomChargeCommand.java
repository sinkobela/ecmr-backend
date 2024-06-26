package org.openlogisticsfoundation.ecmr.domain.models.commands;

import org.openlogisticsfoundation.ecmr.api.model.areas.seventeen.PayerType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CustomChargeCommand {
    @Min(0)
    @Max(99999)
    private Float value;
    @Size(min = 2, max = 512)
    private String currency;
    private PayerType payer;
}
