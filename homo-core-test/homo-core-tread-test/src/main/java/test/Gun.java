package test;


import com.homo.core.facade.tread.tread.annotation.GetMethod;
import com.homo.core.facade.tread.tread.annotation.SetMethod;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
public class Gun implements Serializable {
    public int id;
    public int level;
    public int damage;
    public int count;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @GetMethod("gun.level")
    public int getLevel() {
        return level;
    }

    @SetMethod("gun.level")
    public void setLevel(int level) {
        this.level = level;
    }

    @GetMethod("gun.damage")
    public int getDamage() {
        return damage;
    }

    @SetMethod("gun.damage")
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @GetMethod("gun.count")
    public int getCount() {
        return count;
    }

    @SetMethod("gun.count")
    public void setCount(int count) {
        this.count = count;
    }

    public void upLevel(int level) {
        this.level += level;
    }

    public void upDamage(int damage) {
        this.damage += damage;
    }

    public boolean canUp(int level) {
        return level < 5;
    }

}
