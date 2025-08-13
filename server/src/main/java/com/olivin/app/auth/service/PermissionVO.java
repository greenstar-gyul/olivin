// PermissionVO.java
package com.olivin.app.auth.service;

import lombok.Data;

@Data
public class PermissionVO {
    private String permId;          // PERM_ID (VARCHAR2)
    private String permName;        // PERM_NAME
    private String permDescription; // PERM_DESCRIPTION
    private String icon;            // ICON
    private String parentTo;        // PARENT_TO
}