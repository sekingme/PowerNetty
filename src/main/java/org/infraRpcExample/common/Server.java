 /* power by sekingme */
package org.infraRpcExample.common;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * server
 */
@Deprecated
public class Server {

    /**
     * id
     */
    private int id;

    /**
     * host
     */
    private String host;

    /**
     * port
     */
    private int port;

    /**
     * master directory in zookeeper
     */
    private String zkDirectory;

    /**
     * resource info: CPU and memory
     */
    private String resInfo;

    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * laster heart beat time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastHeartbeatTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getZkDirectory() {
        return zkDirectory;
    }

    public void setZkDirectory(String zkDirectory) {
        this.zkDirectory = zkDirectory;
    }

    public Date getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public void setLastHeartbeatTime(Date lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    public String getResInfo() {
        return resInfo;
    }

    public void setResInfo(String resInfo) {
        this.resInfo = resInfo;
    }

    @Override
    public String toString() {
        return "MasterServer{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", zkDirectory='" + zkDirectory + '\'' +
                ", resInfo='" + resInfo + '\'' +
                ", createTime=" + createTime +
                ", lastHeartbeatTime=" + lastHeartbeatTime +
                '}';
    }
}
