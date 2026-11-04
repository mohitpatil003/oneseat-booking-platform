package com.oneseat.booking;

import java.time.Instant;
import java.util.UUID;

public record Event(UUID id, String name, Instant startsAt) {
}
