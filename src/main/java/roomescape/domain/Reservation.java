package roomescape.domain;

import java.time.LocalDate;

public class Reservation {
    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final TimeSlot time;
    private final Theme theme;

    public Reservation(final Long id, final Member member, final LocalDate date, final TimeSlot time, final Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(final Member member, final LocalDate date, final TimeSlot time, final Theme theme) {
        this(null, member, date, time, theme);
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getId() {
        return id;
    }

    public Reservation with(final Long id) {
        return new Reservation(id, member, date, time, theme);
    }
}
