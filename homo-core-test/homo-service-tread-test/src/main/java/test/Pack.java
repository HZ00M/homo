package test;

import com.homo.core.facade.tread.tread.TreadMgr;
import com.homo.core.utils.rector.Homo;
import lombok.Data;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.Map;

@Data
public class Pack {
    public Map<Integer, Material> materialMap = new HashMap<>();

    public Material addMater(Material material) {
        materialMap.put(material.id, material);
        return material;
    }

    public Homo<Tuple2<Boolean, String>> addMaterCheck(Material material) {
        if (materialMap.containsKey(material.id)) {
            return Homo.result(Tuples.of(false, "has exist"));
        }
        if (material.getCount() <= 0) {
            return Homo.result(Tuples.of(false, "param wrong"));
        }
        return Homo.result(TreadMgr.checkOk);
    }

    public Material getMater(Integer id) {
        return materialMap.get(id);
    }
}
