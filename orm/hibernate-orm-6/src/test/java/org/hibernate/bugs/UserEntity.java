package org.hibernate.bugs;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Collection;
import java.util.LinkedList;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "user")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 20)
    private Collection<FederatedIdentityEntity> federatedIdentities = new LinkedList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Collection<FederatedIdentityEntity> getFederatedIdentities() {
        if (federatedIdentities == null) {
            federatedIdentities = new LinkedList<>();
        }
        return federatedIdentities;
    }

    public void setFederatedIdentities(Collection<FederatedIdentityEntity> federatedIdentities) {
        this.federatedIdentities = federatedIdentities;
    }

}
