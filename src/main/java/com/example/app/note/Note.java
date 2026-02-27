package com.example.app.note;
import com.example.app.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;


@Data
@Table(name="notes")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Note {
    @Id  
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String input;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

}
