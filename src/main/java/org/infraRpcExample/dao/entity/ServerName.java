 /* power by sekingme */

package org.infraRpcExample.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * server name
 */
@Deprecated
@TableName("netty_server_name")
public class ServerName {

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private String name;

    private String addrList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @TableField(exist = false)
    private boolean systemDefault;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddrList() {
        return addrList;
    }

    public void setAddrList(String addrList) {
        this.addrList = addrList;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public boolean getSystemDefault() {
        return systemDefault;
    }

    public void setSystemDefault(boolean systemDefault) {
        this.systemDefault = systemDefault;
    }

    @Override
    public String toString() {
        return "ServerName{"
                + "id= " + id
                + ", name= " + name
                + ", createTime= " + createTime
                + ", updateTime= " + updateTime
                + ", systemDefault= " + systemDefault
                + "}";
    }

}
