package com.quize.quizproject.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "quiz")  // The quiz table name
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Integer quizId;

    @Column(name = "title")
    private String title;

    // Many-to-many relationship with the Question entity
    @ManyToMany
    @JoinTable(
            name = "quiz_question",  // Name of the join table
            joinColumns = @JoinColumn(name = "quiz_id"),  // Foreign key column for Quiz
            inverseJoinColumns = @JoinColumn(name = "question_id")  // Foreign key column for Question
    )
    private List<Question> questionList;

    // Getters and setters
    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }
}

