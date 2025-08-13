// PermissionVO.java
package com.olivin.app.auth.service;

import lombok.Data;

@Data
public class PermissionVO {
    private String permId;          // PERM_ID (URL 경로)
    private String permName;        // PERM_NAME (메뉴명)
    private String permDescription; // PERM_DESCRIPTION (설명)
    private String icon;            // ICON (아이콘)
    private String parentTo;        // PARENT_TO (부모 경로)
}