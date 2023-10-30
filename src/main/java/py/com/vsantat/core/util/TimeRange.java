package py.com.vsantat.core.util;


import java.time.LocalDateTime;

public class TimeRange {

    private LocalDateTime start;
    private LocalDateTime end;

    private PeriodInterval interval;

    public TimeRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
        this.interval = PeriodInterval.UNDEFINED;
    }

    public TimeRange(LocalDateTime start, LocalDateTime end, PeriodInterval interva) {
        this.start = start;
        this.end = end;
        this.interval = interva;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public PeriodInterval getInterval() {
        return interval;
    }

    public void setInterval(PeriodInterval interval) {
        this.interval = interval;
    }

}