package ru.practicum.shareit.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationServiceClass extends PageRequest {
    public PaginationServiceClass(int from, int size) {
        super(Math.floorDiv(from, size), size, Sort.unsorted());
    }

    public static Pageable pagination(int from, int size) {
        return new PaginationServiceClass(from, size);
    }
}
