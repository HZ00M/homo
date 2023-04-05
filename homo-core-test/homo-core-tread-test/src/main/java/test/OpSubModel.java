package test;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OpSubModel {
    Integer id;
    Integer num;
    Long level;
    boolean lock;
    String desc;
}
