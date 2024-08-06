package ru.practicum.shareit.booking.enums;

import ru.practicum.shareit.exception.BadRequestException;

public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static State isStateValid(String state) {
        try {
            return State.valueOf(state);
        } catch (Exception e) {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
    }
}
