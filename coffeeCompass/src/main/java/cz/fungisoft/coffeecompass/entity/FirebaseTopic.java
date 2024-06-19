package cz.fungisoft.coffeecompass.entity;


import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Class to hold info about topics selected by users.
 * Also hold info about list of tokens who subscribed certian topics.
 * 
 * @author Michal V.
 *
 */
@Getter // lombok's @Data cannot be used here as it's generated toString() method causes StackOverflowException for 'tokens' @ManyToMany relation
@Setter
@Entity
@Table(name="firebase_topics", schema="coffeecompass")
public class FirebaseTopic {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    
    @NotNull
    @Column(name="main_topic", nullable=false)
    private String mainTopic; // usually: new_coffeeSite, removed_coffeeSite and so on
    
    @NotNull
    @Column(name="sub_topic", nullable=false)
    private String subTopic; // usually city name
    
    
    // Set of tokens/users subscribed to this Topic
    @ManyToMany(fetch= FetchType.LAZY, mappedBy = "topics") // topics field in DeviceFirebaseToken class
    private Set<DeviceFirebaseToken> tokens = new HashSet<>();
    
    
    /**
     * Default constructor needed for Hibernate
     */
    public FirebaseTopic() {
    }
    
    public FirebaseTopic(String mainTopic, String subTopic) {
        this.mainTopic = mainTopic;
        this.subTopic = subTopic;
    }
}
