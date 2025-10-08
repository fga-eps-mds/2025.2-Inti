package br.mds.inti.models.pk;

import br.mds.inti.models.Profile;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SharedsPk {
    private Profile profileSharingId;

    private Profile profileSharedId;
}
