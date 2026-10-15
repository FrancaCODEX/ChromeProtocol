package br.com.chromeprotocol;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Career {
    @Id public UUID id;
    @Version public long version;
    public UUID archivedCareerId;
    @Lob @Column(nullable = false) public String state;
    protected Career() {}
    public Career(UUID id, String state) { this.id = id; this.state = state; }
}
