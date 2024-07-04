package com.homo.core.utils.origin.version;

import com.homo.core.utils.origin.NumberUtil;
import com.homo.core.utils.origin.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * 根据国家规定,版本号必须符合格式  <主版本号>.<次版本号>.<修订号>
 * 版本号. 可以对版本进行比较.
 * 版本号可以是单纯的数据:456, 或以点号分隔的数字字符串:1.0.10
 * 把版本号拆分为数字数组.按每一个数组序号进行对比
 */
public class Version implements Comparable<Version> {
  /**
   * 各版本子号对应的数组序号
   *
   */
  static final int MAJOR = 0;
  static final int MINOR = 1;
  static final int PATCH = 2;

  //支持的版本后缀.以及对应的权限
  //后缀信息来源网上. 如果有自己的版本,需要自己定义
  private static final String PRE_STRING = "pre";
  private static final String ALPHA_STRING = "alpha";
  private static final String BETA_STRING = "beta";
  private static final String RC_STRING = "rc";
  private static final String RELEASE_STRING = "release";

  //各版本后缀的权重.
  /**
   * 没有后缀.或后缀无法识别,则权重为0.
   */
  private static final int UNKNOWN = 0;
  private static final int PRE_ALPHA = 1;
  private static final int ALPHA = 2;
  private static final int BETA = 3;
  private static final int RC = 4;


  /**
   * 初始化时的版本号字符串
   */
  private final String rawVersionString;

  /**
   * rawString拆分后的数字list
   */
  private final List<Integer> subVersions = new ArrayList<>();
  /*
   * 符合版本号格式的.去掉首尾空格的版本号
   */
  private final String versionString;
  /**
   * 版本号的后缀. 例如Beta, RC什么的
   */
  private String versionSuffix = "";
  /**
   * versionString是否是有效的版本号
   */
  private final boolean validVersion;
  /**
   * 使用版本号字符串初始化
   */
  public Version(@NotNull String versionString) {
    this(versionString, false);
  }

  /**
   * 初始化版本号.
   * 使用 . 拆分成数组后. 把整数填写到subVersion中. 如果碰到第一个非数字.则把后面的都截取为versionSuffix
   * @param version 版本号原始字符串.
   * @param throwExceptionIfVersionIllegal 如果为true,则版本号不正确就会抛出异常
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public Version(@NotNull String version, boolean throwExceptionIfVersionIllegal) {

    rawVersionString= version;
    boolean isEmpty= StringUtil.isEmpty(rawVersionString);
    if (throwExceptionIfVersionIllegal) {
      if (isEmpty) {
        throw new NullPointerException("版本号不能为空");
      }
    }
    //去掉所有空格, 如果版本号前带字母V.去掉V
    String tempVersion=isEmpty? "":rawVersionString.replaceAll("\\s", "").toLowerCase();
    if(tempVersion.startsWith("v")){
      tempVersion=tempVersion.substring(1);
    }
    versionString = tempVersion;

    validVersion = NumberUtil.startsNumeric(versionString);
    if (throwExceptionIfVersionIllegal) {
      if(!validVersion){
        throw new IllegalArgumentException("参数versionString不是有一个效的版本号");
      }
    }
    if (validVersion) {
      String[] versionTokens = versionString.split("\\.");
      boolean suffixFound = false;
      StringBuilder suffixSb = null;

      for (String versionToken : versionTokens) {
        if (suffixFound) {
            suffixSb.append(".");
            suffixSb.append(versionToken);
        } else if (NumberUtil.isDigits(versionToken)) {
          subVersions.add(NumberUtil.safeParseInt(versionToken));
        } else {
          for (int i = 0; i < versionToken.length(); i++) {
            if (!Character.isDigit(versionToken.charAt(i))) {
              suffixSb = new StringBuilder();
              if (i > 0) {
                subVersions.add(NumberUtil.safeParseInt(versionToken.substring(0, i)));
                suffixSb.append(versionToken.substring(i));
              } else {
                suffixSb.append(versionToken);
              }
              suffixFound = true;
              break;
            }
          }
        }
      }
      if (suffixSb != null) {
        versionSuffix  = suffixSb.toString();
      }
    }else{
      versionSuffix=versionString;
    }
  }
  /**
   *
   * @param otherVersion
   * @param compareSuffix  如果版本数字比较相等.是否再进行版本后缀比较
   * @return
   */
  public int compareTo(@NotNull Version otherVersion,boolean compareSuffix) {
    int c =compareSubVersion(subVersions, otherVersion.subVersions);
    if (c==0 && compareSuffix) {
      return compareSuffix(versionSuffix, otherVersion.versionSuffix) ;
    }
    return c;
  }
  public int compareTo(@NotNull String otherVersionString,boolean compareSuffix) {
    return compareTo(new Version(otherVersionString),compareSuffix);
  }
  @Override
  public int compareTo(@NotNull Version otherVersion) {
    return compareTo(otherVersion,true);
  }
  public int compareTo(@NotNull String otherVersionString) {
    return compareTo(otherVersionString,true);
  }

  public boolean isHigher(@NotNull Version otherVersion,boolean compareSuffix){
    return compareTo(otherVersion,compareSuffix)>0;
  }
  public boolean isHigher(@NotNull String otherVersionString,boolean compareSuffix){
    return compareTo(otherVersionString,compareSuffix)>0;
  }
  public boolean isHigher(@NotNull Version otherVersion){
    return compareTo(otherVersion)>0;
  }
  public boolean isHigher(@NotNull String otherVersionString){
    return compareTo(otherVersionString)>0;
  }

  public boolean isLower(@NotNull Version otherVersion,boolean compareSuffix){
    return compareTo(otherVersion,compareSuffix)<0;
  }
  public boolean isLower(@NotNull String otherVersionString,boolean compareSuffix){
    return compareTo(otherVersionString,compareSuffix)<0;
  }
  public boolean isLower(@NotNull Version otherVersion){
    return compareTo(otherVersion)<0;
  }
  public boolean isLower(@NotNull String otherVersionString){
    return compareTo(otherVersionString)<0;
  }

  public boolean isEquals(@NotNull Version otherVersion,boolean compareSuffix){
    return compareTo(otherVersion,compareSuffix)==0;
  }
  public boolean isEquals(@NotNull String otherVersionString,boolean compareSuffix){
    return compareTo(otherVersionString,compareSuffix)==0;
  }
  public boolean isEquals(@NotNull Version otherVersion){
    return compareTo(otherVersion)==0;
  }
  public boolean isEquals(@NotNull String otherVersionString){
    return compareTo(otherVersionString)==0;
  }

  /**
   * 主版本号.默认0
   */
  public int getMajor() {
    return subVersions.size() > MAJOR ? subVersions.get(MAJOR) : 0;
  }

  /**
   * 次版本号.默认0
   */
  public int getMinor() {
    return subVersions.size() > MINOR ? subVersions.get(MINOR) : 0;
  }

  /**
   * 修定版本号.默认0
   */
  public int getPatch() {
    return subVersions.size() > PATCH ? subVersions.get(PATCH) : 0;
  }

  /**
   * 版本号列表
   */
   public List<Integer> getSubVersions() {
    return subVersions;
  }

  /**
   * 版本后缀
   */
   public String getVersionSuffix() {
    return versionSuffix;
  }

  /**
   * 初始化时原始字符串
   */
  public String getRawVersionString() {
    return rawVersionString;
  }

  /**
   * 格式化后的版本号字符串
   * @return
   */
  public String getVersionString() {
    return versionString;
  }

  /**
   * 是否有成功获取到有效的版本信息
   * @return
   */
  public boolean isValidVersion() {
    return validVersion;
  }


  /**
   * 按版本号的subVersion列表,一个一个整数来判断
   * @param versA
   * @param versB
   * @return
   */
  static int compareSubVersion(@NotNull final List<Integer> versA,@NotNull final List<Integer> versB) {
    final int versASize = versA.size();
    final int versBSize = versB.size();
    final int maxSize = Math.max(versASize, versBSize);
    for (int i = 0; i < maxSize; i++) {
      if ((i < versASize ? versA.get(i) : 0) > (i < versBSize ? versB.get(i) : 0)) {
        return 1;
      } else if ((i < versASize ? versA.get(i) : 0) < (i < versBSize ? versB.get(i) : 0)) {
        return -1;
      }
    }
    return 0;

  }

  /**
   * 当版本号一致时.则判断后缀
   * @param suffixA
   * @param suffixB
   * @param ifEqualsComparator 如果两个后缀的权重相等. 传了此比较器,则使用此比较器进行对比.否则使用字符串对比器对比
   * @return
   */
  private static int compareSuffix(@Nullable final String suffixA, @Nullable final String suffixB, @Nullable final Comparator<String> ifEqualsComparator) {
    final int weightA = getSuffixWeight(suffixA);
    final int weightB = getSuffixWeight(suffixB);
    int c=Integer.compare(weightA,weightB);
    if(c==0){
      if(ifEqualsComparator!=null){
        c= ifEqualsComparator.compare(suffixA,suffixB);
      }else {
        //如果后缀权重相等. 那么不知道这两个版本号对比的规则是什么?
        //简单的返回字符串对比.
        c=suffixA.compareTo(suffixB);
      }
    }
    if(c>0){
      return 1;
    }else if(c<0){
      return -1;
    }else {
      return 0;
    }
  }
  private static int compareSuffix(final String suffixA, final String suffixB) {
     return compareSuffix(suffixA,suffixB,null);
  }
  /**
   * 根据后缀判断权重
   * @param suffix
   * @return
   */
  private static int getSuffixWeight(@Nullable String suffix) {
    if (StringUtil.isNotEmpty(suffix)) {
        suffix = suffix.toLowerCase();
        if (suffix.contains(RC_STRING) || suffix.contains(RELEASE_STRING)){
          return RC;
        }
        if (suffix.contains(BETA_STRING)){
          return BETA;
        }
        if (suffix.contains(ALPHA_STRING)) {
          if (suffix.substring(0, suffix.indexOf(ALPHA_STRING)).contains(PRE_STRING)) {
            return PRE_ALPHA;
          } else {
            return ALPHA;
          }
      }
    }
    return UNKNOWN;
  }

  public static int compare(@NotNull String versionString,@NotNull String otherVersionString,boolean compareSuffix) {
    return new Version(versionString).compareTo(otherVersionString,compareSuffix);
  }

  public static int compare(@NotNull String versionString,@NotNull String otherVersionString) {
    return compare(versionString,otherVersionString,true);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Version version = (Version) o;
    return validVersion == version.validVersion &&
            Objects.equals(rawVersionString, version.rawVersionString) &&
            Objects.equals(subVersions, version.subVersions) &&
            Objects.equals(versionString, version.versionString) &&
            Objects.equals(versionSuffix, version.versionSuffix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawVersionString, subVersions, versionString, versionSuffix, validVersion);
  }
}
