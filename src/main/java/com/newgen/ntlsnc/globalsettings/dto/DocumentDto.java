package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author sagor
 * @date ১৭/৫/২২
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {

    private Long id;

    @NotNull(message = "Table Name field is required.")
    private String refTable;

    @NotNull(message = "File Path field is required.")
    private String filePath;

    @NotNull(message = "Id field is required.")
    private Long refId;

    @NotNull(message = "File Name field is required.")
    private String fileName;

    @NotNull(message = "File Type field is required.")
    private String fileType;
}
