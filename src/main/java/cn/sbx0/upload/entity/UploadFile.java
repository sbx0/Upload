package cn.sbx0.upload.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * 上传文件实体类
 */
@Entity
@Table(name = "FILES")
public class UploadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false, length = 100)
    String originalName; // 原始文件名
    @Column(nullable = false, length = 50)
    String name; // 唯一文件名
    @Column(nullable = false, length = 10)
    String ext; // 后缀名
    @Column(nullable = false, length = 10)
    String type; // 文件类型
    @Column(nullable = false, length = 50)
    String md5; // md5值
    @Column(nullable = false)
    Long size; // 文件大小
    @Column(nullable = false)
    Date time; // 上传日期

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "UploadFile{" +
                "id=" + id +
                ", originalName='" + originalName + '\'' +
                ", name='" + name + '\'' +
                ", ext='" + ext + '\'' +
                ", type='" + type + '\'' +
                ", md5='" + md5 + '\'' +
                ", size=" + size +
                ", time=" + time +
                '}';
    }
}
