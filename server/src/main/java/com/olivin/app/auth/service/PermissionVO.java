// PermissionVO.java
package com.olivin.app.auth.service;

import lombok.Data;

@Data
public class PermissionVO {
    private Long permId;            // PERM_ID
    private String permName;        // PERM_NAME
    private String permDescription; // PERM_DESCRIPTION
}