package test;


import com.homo.core.facade.tread.tread.enums.ExecRet;
import com.homo.core.utils.rector.Homo;

//@EntityType(typeName = "Tread")
//@CacheTime(saveTime = 1000, value = 1000 * 6)
public interface ITreadHandler {
    void reset(Integer a, Integer b, Integer c, Integer d);

    void addGun(Gun gun);

    void addMaterial(Material material);

    Homo<Integer> getGunLevel(Integer gunId);

    Homo<Integer> getGunDamage(Integer gunId);

    Homo<Integer> getMaterCount(Integer materId);

    Homo<ExecRet> upLevelGun(Integer gunId, Integer level, Integer materialId, Integer cost, Integer damage);

//    public Integer getA();
//
//    public Homo<String> setA(Integer a);
//
//    public Homo<Integer> getB();
//
//    public Integer setB(Integer b);
//
//    public Homo<Integer> getC();
//
//    public Homo<Integer> setC(Integer c);
//
//    public Integer getD();
//
//    public String setD(Integer d);
//
//    public Integer getE();
//
//    public Homo<String> setE(Integer e);
//
//    public Integer getF();
//
//    public void setF(Integer f);
//
//    public Integer getG();
//
//    public void setG(Integer g);
//
//    public Long getX();
//
//    public void setX(Long x);
//
//    public Boolean getY() ;
//
//    public void setY(Boolean y);
//
//    public String getZ();
//
//    public void setZ(String z);
}
