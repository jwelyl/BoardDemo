package com.example.boarddemo.domain.dto;

import com.example.boarddemo.domain.entity.Board;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDto {
    private Long id;
    private String author;
    private String title;
    private String content;
    private String filePath;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public Board toEntity() {
        return Board.builder().id(id).author(author).title(title).content(content).filePath(filePath).build();
    }
}
