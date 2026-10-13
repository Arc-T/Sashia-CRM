package ir.crm.identity.team.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TeamCreateRequest(
        @NotBlank @Size(max = 255) String name,
        Long leader,
        @PositiveOrZero @Digits(integer = 15, fraction = 2) BigDecimal monthlyTarget,
        @Size(max = 2000) String description) {}
