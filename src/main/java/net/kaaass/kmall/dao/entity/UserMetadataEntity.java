package net.kaaass.kmall.dao.entity;

import lombok.Data;
import net.kaaass.kmall.util.Constants;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "user_metadata")
public class UserMetadataEntity {
    @Id
    @GenericGenerator(name = Constants.ID_GENERATOR, strategy = Constants.UUID)
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    private String id;

    @Column(name = "uid")
    private String uid;

    @Column(name = "meta_key")
    private String key;

    @Column(name = "meta_value",
            columnDefinition = "TEXT DEFAULT NULL")
    private String value;

    @Column(name = "last_update_time",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Generated(GenerationTime.ALWAYS)
    private Timestamp lastUpdateTime;
}