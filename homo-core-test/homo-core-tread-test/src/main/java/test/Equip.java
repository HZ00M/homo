package test;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Equip {
    public Map<Integer, Gun> gunMap = new HashMap<>();

    public Gun addGun(Gun gun) {
        gunMap.put(gun.getId(), gun);
        return gun;
    }

    public boolean checkCanAdd(Gun gun) {
        return gunMap.containsKey(gun.getId());
    }

    public Gun getGun(Integer gunId) {
        return gunMap.get(gunId);
    }
}
