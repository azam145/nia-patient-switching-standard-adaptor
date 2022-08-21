package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patient_attachment_log", schema = "public")
public class PatientAttachmentLog {

    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "mid")
    private String mid;
    @Column(name = "parent_mid")
    private String parentMid;
    @Column(name = "filename")
    private String filename;
    @Column(name = "content_type")
    private String contentType;
    @Column(name = "compressed")
    private boolean compressed;
    @Column(name = "large_attachment")
    private boolean largeAttachment;
    @Column(name = "base64")
    private boolean base64;
    @Column(name = "skeleton")
    private boolean skeleton;
    @Column(name = "uploaded")
    private boolean uploaded;
    @Column(name = "length_num")
    private Integer lengthNum;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_migration_req_id")
    private PatientMigrationRequest patientMigrationRequest;
    @Column(name = "order_num")
    private Integer orderNum;
    @Column(name = "deleted")
    private Boolean deleted;
}
