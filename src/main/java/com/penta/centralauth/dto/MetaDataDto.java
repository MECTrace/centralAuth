package com.penta.centralauth.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MetaDataDto {

    private String dataId;

    private LocalDateTime timestamp;

    private String fileType;

    private Integer dataType;

    private Integer securityLevel;

    private Integer dataPriority;

    private Integer availabilityPolicy;

    private String dataSignature;

    private String cert;

    private String directory;

    private String linkedEdge;

    private Long dataSize;

}
