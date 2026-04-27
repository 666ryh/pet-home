package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role_menu")
public class RoleMenu {
    @TableId
    private Integer roleId;
    
    private Integer menuId;
    
    private LocalDateTime createdTime;
} 