package dev.demon.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PotionEffectInfo {
    private int amplifer;
    private int id;
    private int duration;

    public PotionEffectInfo(int amplifer, int id, int duration) {
        this.amplifer = amplifer;
        this.id = id;
        this.duration = duration;
    }

}
