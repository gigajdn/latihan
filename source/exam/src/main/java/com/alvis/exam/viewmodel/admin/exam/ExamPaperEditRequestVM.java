package com.alvis.exam.viewmodel.admin.exam;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class ExamPaperEditRequestVM {
    private Integer id;
    @NotNull
    private Integer level;
    @NotNull
    private Integer subjectId;
    @NotNull
    private Integer paperType;
    @NotBlank
    private String name;
    @NotNull
    private Integer suggestTime;

    private List<String> limitDateTime;

    @NotNull
    @Valid
    private List<ExamPaperTitleItemVM> titleItems;

    private String score;

}
