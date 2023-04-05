package test;

import com.homo.core.facade.tread.tread.annotation.GetMethod;
import com.homo.core.facade.tread.tread.annotation.SetMethod;
import com.homo.core.utils.rector.Homo;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
public class Material implements Serializable {
    public Integer id;
    public Integer count;


    @GetMethod("material.count")
    public Integer getCount() {
        return count;
    }

    @SetMethod("material.count")
    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Homo<Integer> costCount(Integer cost) {
        this.count -= cost;
        return Homo.result(count);
    }

    public Homo<Integer> addCount(Integer cost) {
        this.count -= cost;
        return Homo.result(count);
    }
}
