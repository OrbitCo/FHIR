package com.healthcare.entity;

import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Data
@Table(name = "PROJECT_OUTPUT")
@Where(clause = "IS_DELETED IS NULL OR IS_DELETED = false")
@SQLDelete(sql = "update PROJECT_OUTPUT set is_deleted = true where id = ?")
public class ProjectOutput extends  DefaultEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String outputName;

    @Column(name="output_settings", columnDefinition = "json")
    private byte[] outputSettings;

    @JoinColumn(name = "PROJECT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectDetails projectDetails;

}
