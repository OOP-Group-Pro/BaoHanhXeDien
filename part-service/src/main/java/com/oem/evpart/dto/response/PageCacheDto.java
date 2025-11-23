package com.oem.evpart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Getter
@Service
@NoArgsConstructor
@AllArgsConstructor
public class PageCacheDto<T> implements Serializable {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;

    // constructor, getter/setter
}
