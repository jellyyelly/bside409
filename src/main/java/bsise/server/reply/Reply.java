package bsise.server.reply;

import bsise.server.common.BaseTimeEntity;
import bsise.server.letter.Letter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reply")
public class Reply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reply_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "letter_id", nullable = false, unique = true)
    private Letter letter;

    @Column(name = "message_f")
    private String messageForF;

    @Column(name = "message_t")
    private String messageForT;

    @Builder
    public Reply(Letter letter, String messageForF, String messageForT) {
        this.letter = letter;
        this.messageForF = messageForF;
        this.messageForT = messageForT;
    }
}