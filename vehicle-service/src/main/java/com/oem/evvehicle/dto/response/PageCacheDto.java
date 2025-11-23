package com.oem.evvehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Không serialize null
public class PageCacheDto<T> {
    @Builder.Default
    private List<T> content = Collections.emptyList();
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PageCacheDto<T> from(Page<T> page) {
        List<T> content = Optional.ofNullable(page.getContent()).orElse(Collections.emptyList());
        return PageCacheDto.<T>builder()
                .content(content)
                .number(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(content.isEmpty())
                .build();
    }
}
