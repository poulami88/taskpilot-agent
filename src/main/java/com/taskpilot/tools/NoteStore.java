package com.taskpilot.tools;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/** Simple in-memory note storage shared by the note tools. */
@Component
public class NoteStore {

    public record Note(int id, Instant createdAt, String text) {
    }

    private final List<Note> notes = new CopyOnWriteArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Clock clock;

    public NoteStore(Clock clock) {
        this.clock = clock;
    }

    public Note add(String text) {
        Note note = new Note(nextId.getAndIncrement(), clock.instant(), text);
        notes.add(note);
        return note;
    }

    public List<Note> all() {
        return List.copyOf(notes);
    }
}
