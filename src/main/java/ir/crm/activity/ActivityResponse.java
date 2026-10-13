package ir.crm.activity;

import java.time.Instant;

/** kind: create | call | email | note | status (matches the client's timeline icons). */
public record ActivityResponse(String id, String kind, String text, String user, Instant at) {}
