package com.imooc.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DataDictionaryBO {

    private String id;
    private String typeCode;
    private String typeName;
    private String itemKey;
    private String itemValue;
    private Integer sort;
    private String icon;
    private Boolean enable;

}
