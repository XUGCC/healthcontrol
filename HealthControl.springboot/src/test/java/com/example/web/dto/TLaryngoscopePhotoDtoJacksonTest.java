package com.example.web.dto;

import com.example.web.dto.query.TLaryngoscopePhotoPagedInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TLaryngoscopePhotoDtoJacksonTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Test
    void shouldDeserializeSpaceSeparatedCheckTime() throws Exception {
        String json = "{\"CheckTime\":\"2026-04-16 17:25:00\"}";

        TLaryngoscopePhotoDto dto = objectMapper.readValue(json, TLaryngoscopePhotoDto.class);

        assertThat(dto.getCheckTime()).isEqualTo(LocalDateTime.of(2026, 4, 16, 17, 25, 0));
    }

    @Test
    void shouldDeserializeMinutePrecisionCheckTime() throws Exception {
        String json = "{\"CheckTime\":\"2026-04-16 17:25\"}";

        TLaryngoscopePhotoDto dto = objectMapper.readValue(json, TLaryngoscopePhotoDto.class);

        assertThat(dto.getCheckTime()).isEqualTo(LocalDateTime.of(2026, 4, 16, 17, 25, 0));
    }

    @Test
    void shouldDeserializeIsoCheckTimeWithoutBreakingCompatibility() throws Exception {
        String json = "{\"CheckTime\":\"2026-04-16T17:25:00\"}";

        TLaryngoscopePhotoDto dto = objectMapper.readValue(json, TLaryngoscopePhotoDto.class);

        assertThat(dto.getCheckTime()).isEqualTo(LocalDateTime.of(2026, 4, 16, 17, 25, 0));
    }

    @Test
    void shouldSerializeCheckTimeAsStandardSpaceSeparatedString() throws Exception {
        TLaryngoscopePhotoDto dto = new TLaryngoscopePhotoDto();
        dto.setCheckTime(LocalDateTime.of(2026, 4, 16, 17, 25, 0));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"CheckTime\":\"2026-04-16 17:25:00\"");
    }

    @Test
    void shouldDeserializePagedInputRanges() throws Exception {
        String json = "{\"UpdateTimeRange\":[\"2026-04-12 10:00:00\",\"2026-04-12T12:00:00\"]}";

        TLaryngoscopePhotoPagedInput input = objectMapper.readValue(json, TLaryngoscopePhotoPagedInput.class);

        assertThat(input.getUpdateTimeRange())
                .containsExactly(
                        LocalDateTime.of(2026, 4, 12, 10, 0, 0),
                        LocalDateTime.of(2026, 4, 12, 12, 0, 0));
    }
}
