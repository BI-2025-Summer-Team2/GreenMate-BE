package kr.bi.greenmate.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agreement {

    @NotNull(message = "termId 필수")
    @Positive(message = "termId 양수")
    private Long termId;

    @NotNull(message = "agreed 필수")
    private Boolean agreed;

}
