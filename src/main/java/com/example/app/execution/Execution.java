package com.example.app.execution;
import com.example.app.user.User;
import com.example.app.note.Note;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="executions")

public class Execution {
    private @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;

    private String output;
    private String error;
    private LocalDateTime executionTime;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="note_id")
    private Note note;

}
