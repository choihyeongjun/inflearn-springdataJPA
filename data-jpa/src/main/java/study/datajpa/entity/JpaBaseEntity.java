package study.datajpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter@Setter
public class JpaBaseEntity {
    @Column(updatable = false)//변경방지
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist//불러오기전에 호출
    public void prePersist()
    {
        LocalDateTime now=LocalDateTime.now();
        createdDate=now;
        updatedDate=now;
    }
    @PreUpdate//업데이트되기전에 호출
    public void preUpdate(){
        updatedDate=LocalDateTime.now();
    }
}
