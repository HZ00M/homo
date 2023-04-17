package test;

import com.homo.core.tread.tread.intTread.IntTreadMgr;
import com.homo.core.utils.fun.Func2Ex;
import com.homo.core.utils.fun.Func3Ex;
import com.homo.core.utils.rector.Homo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Log4j2
@Component
public class TreadServiceImpl  implements TreadService{
 

    @Autowired
    IntTreadMgr treadMgr;

    @PostConstruct
    public void init() {
        treadMgr.registerSetFun("E", (addTarget, opValue) -> {
            TreadHandler treadHandler = (TreadHandler) addTarget;
            treadHandler.setE(opValue);
            return treadHandler.e;
        });
        treadMgr.registerGetFun("E", (addTarget) -> {
            TreadHandler treadHandler = (TreadHandler) addTarget;
            treadHandler.getE();
            return treadHandler.e;
        });
        treadMgr.registerPromiseSetFun("F", (addTarget, opValue) -> {
            TreadHandler treadHandler = (TreadHandler) addTarget;
            int i = 2 / 0;
            treadHandler.setF(opValue);

            return Homo.result(treadHandler.f);
        });
        treadMgr.registerPromiseGetFun("F", addTarget -> {
            TreadHandler treadHandler = (TreadHandler) addTarget;
            return Homo.result(treadHandler.f);
        });
        treadMgr.registerSetFun("G", (addTarget, integer) -> {
            TreadHandler treadHandler = (TreadHandler) addTarget;
            treadHandler.setA(integer);
            return treadHandler.g;
        });
        treadMgr.registerPromiseGetFun("G", addTarget -> {
            TreadHandler treadHandler = (TreadHandler) addTarget;
            int i = 2 / 0;
            return Homo.result(treadHandler.g);
        });

        treadMgr.registerGetObjFun(TreadHandler.class, new Func2Ex<TreadHandler, Object, Object>() {
            public Object apply(TreadHandler treadHandler, Object o) throws Exception {
                log.info("registerGetObjFun call mgrObj {} id {}",treadHandler,o);
                Integer id = (Integer) o;
                EquipInfoModel equipInfoModel = treadHandler.equipMap.get(id);
                return equipInfoModel;
            }
        }, ResourceType.EQUIP_NUM);
        treadMgr.registerCreateObjFun(TreadHandler.class, new Func2Ex<TreadHandler, Object, Object>() {
            @Override
            public Object apply(TreadHandler treadHandler, Object o) throws Exception {
                log.info("registerCreateObjFun call mgrObj {} id {}",treadHandler,o);
                Integer id = (Integer) o;
                EquipInfoModel equipInfoModel = new EquipInfoModel(id,1,1,1);
                return equipInfoModel;
            }
        }, ResourceType.EQUIP_NUM);
        treadMgr.registerSetObjFun(TreadHandler.class, new Func3Ex<TreadHandler, Object, Object, Object>() {

            @Override
            public Object apply(TreadHandler treadHandler, Object o, Object o2) throws Exception {
                log.info("registerSetObjFun call mgrObj {} id {}",treadHandler,o);
                Integer id= (Integer) o;
                EquipInfoModel equipInfoModel = (EquipInfoModel) o2;
                return treadHandler.equipMap.put(id,equipInfoModel);
            }
        }, ResourceType.EQUIP_NUM);
    }
}
