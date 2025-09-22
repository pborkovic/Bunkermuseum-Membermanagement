package com.bunkermuseum.membermanagement;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "task", indexes = {
    @Index(name = "idx_task_creation_date", columnList = "creation_date"),
    @Index(name = "idx_task_due_date", columnList = "due_date")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Task {

    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @Column(name = "creation_date", nullable = false)
    @CreationTimestamp
    private Instant creationDate;

    @Column(name = "last_modified_date")
    @UpdateTimestamp
    private Instant lastModifiedDate;

    @Column(name = "due_date")
    @Nullable
    private LocalDate dueDate;

    protected Task() {
        // Default constructor for JPA
    }

    public Task(String description) {
        setDescription(description);
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.description = description;

    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public @Nullable Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@Nullable LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Task task = (Task) obj;
        return getId() != null && getId().equals(task.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
