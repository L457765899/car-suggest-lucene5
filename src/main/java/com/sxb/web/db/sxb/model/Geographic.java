package com.sxb.web.db.sxb.model;

import java.io.Serializable;

public class Geographic implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6641944421106385063L;

	private Integer unid;

    private Integer punid;

    private String pathunid;

    private Integer type;

    private String name;

    private Integer rank;

    private String pathtext;

    private String ename;

    private Long createdate;
    
    private Double longitude;

    private Double latitude;

    public Integer getUnid() {
        return unid;
    }

    public void setUnid(Integer unid) {
        this.unid = unid;
    }

    public Integer getPunid() {
        return punid;
    }

    public void setPunid(Integer punid) {
        this.punid = punid;
    }

    public String getPathunid() {
        return pathunid;
    }

    public void setPathunid(String pathunid) {
        this.pathunid = pathunid == null ? null : pathunid.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getPathtext() {
        return pathtext;
    }

    public void setPathtext(String pathtext) {
        this.pathtext = pathtext == null ? null : pathtext.trim();
    }

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename == null ? null : ename.trim();
    }

    public Long getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Long createdate) {
        this.createdate = createdate;
    }
    
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}