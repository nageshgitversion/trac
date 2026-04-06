package com.fintrac.portfolio.dto;

import com.fintrac.portfolio.entity.HoldingType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateHoldingRequest {
    @NotNull
    private HoldingType type;

    @NotBlank @Size(max = 200)
    private String name;

    @Size(max = 20)
    private String symbol;

    @DecimalMin("0")
    private BigDecimal units;

    @DecimalMin("0")
    private BigDecimal buyPrice;

    @DecimalMin("0")
    private BigDecimal currentPrice;

    @Size(max = 500)
    private String note;
}
