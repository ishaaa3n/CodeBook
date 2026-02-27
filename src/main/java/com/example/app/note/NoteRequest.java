package com.example.app.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class NoteRequest {
    private String title;
    private String content;
    private String language;
    private String input;

}
