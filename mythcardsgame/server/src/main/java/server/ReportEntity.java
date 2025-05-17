package server;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a user report.
 */
@Entity
@Table(name = "reports")
public class ReportEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private UserEntity target;

    @Column(name = "reason", length = 500, nullable = false)
    private String reason;

    @Column(name = "reported_at", nullable = false, updatable = false)
    private Instant reportedAt = Instant.now();

    // ---------------- Constructors ----------------

    protected ReportEntity() {
        // for JPA
    }

    public ReportEntity(UserEntity reporter,
                        UserEntity target,
                        String reason) {
        this.reporter   = reporter;
        this.target     = target;
        this.reason     = reason;
        this.reportedAt = Instant.now();
    }

    // ---------------- Getters & Setters ----------------

    public UUID getId() {
        return id;
    }

    public UserEntity getReporter() {
        return reporter;
    }

    public void setReporter(UserEntity reporter) {
        this.reporter = reporter;
    }

    public UserEntity getTarget() {
        return target;
    }

    public void setTarget(UserEntity target) {
        this.target = target;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }
}