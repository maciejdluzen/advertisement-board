package com.adsb.models;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "advertisements")
public class Advertisement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotBlank
    @Column(name = "mytitle")
    private String title;
    
    @Column(updatable = false)
    private Timestamp createdAt;

    @Column(insertable = false)
    private Timestamp modifiedAt;
    
    @Version
    private long version;
    
    public Advertisement() {
        
    }

    public Advertisement(String title) {
        this.title = title;
    }
    
    public Advertisement(Long id, long version) {
        this.id = id;
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }
    
    public Timestamp getCreatedAt() {
        if (createdAt != null) {
            return new Timestamp(createdAt.getTime());
        }
        return null;
    }
    
    protected void setCreatedAt(Timestamp timestamp) {
        this.createdAt = timestamp;
    }
    
    public Timestamp getModifiedAt() {
        if (modifiedAt != null) {
            return new Timestamp(modifiedAt.getTime());
        }
        return null;
    }
    
    public long getVersion() {
        return version;
    }
    
    @PrePersist
    protected void onPersist() {
        setCreatedAt(now());
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = now();
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    protected static Timestamp now() {
        return new Timestamp((new Date()).getTime());
    }

    public void setVersion(long version) {
        this.version = version;
    }

}