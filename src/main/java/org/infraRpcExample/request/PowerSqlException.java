package org.infraRpcExample.request;

import lombok.Data;

/**
 * @author Lingyu Zhang
 * Time: 2024/6/11 11:53 上午
 **/
@Data
public class PowerSqlException {

    private Integer code;

    private String message;
}
