package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author liton
 * Created on 5/15/22 11:39 AM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DocumentSequence extends SuperEntity<Long> {
    @Column(nullable = false)
    private Integer documentId; // from common Constant

    @Column(nullable = false, length = 50)
    private String DocumentName;

    @Column(nullable = false)
    private Integer maxSequence; //00001

    @Column(nullable = false)
    private Integer sequenceLength;

    @Column(nullable = false, length = 10)
    private String prefix;

    @Column(length = 10)
    private String postfix;

    private String description;
}
