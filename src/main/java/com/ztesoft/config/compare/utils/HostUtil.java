package com.ztesoft.config.compare.utils;

import ch.ethz.ssh2.Connection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ztesoft.config.compare.dto.HostInfoDto;
import com.ztesoft.config.compare.entity.HostDetail;
import com.ztesoft.config.compare.entity.HostInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * 服务器工具类，密码加密，服务器信息转换
 */
public class HostUtil {
    private static final String KEY = "iwhalecloud";
    private static final String SALT = "00000000";
    private static byte[] iv = SALT.getBytes();

    private HostUtil() {

    }

    /**
     * 将host转换为hostDto
     * @param hostInfo
     * @return
     */
    public static HostInfoDto transEntity2Dto(HostInfo hostInfo) {
        if(hostInfo == null){
            return null;
        }
        HostInfoDto hostInfoDto = new HostInfoDto();
        BeanUtils.copyProperties(hostInfo, hostInfoDto);
        hostInfoDto.setPassword(HostUtil.decryptDES(hostInfo.getPassword()));
        String additionValue = hostInfo.getAdditionValue();
        if (!StringUtils.isEmpty(additionValue)) {
            List<HostDetail> hostDetails = JSONObject.parseArray(additionValue, HostDetail.class);
            hostInfoDto.setHostDetailList(hostDetails);
        }
        return hostInfoDto;
    }

    public static HostInfo transDto2Entity(HostInfoDto hostInfoDto) {
        HostInfo hostInfo = new HostInfo();
        BeanUtils.copyProperties(hostInfoDto, hostInfo);
        hostInfo.setPassword(encryptDES(hostInfoDto.getPassword()));
        List<HostDetail> hostDetails = hostInfoDto.getHostDetailList();
        hostInfo.setAdditionValue(JSON.toJSONString(hostDetails));
        return hostInfo;
    }
    /**
     * 服务器详情list转map
     *
     * @param hostDetails
     * @return
     */
    public static Map<String, Object> hostDetailList2Map(List<HostDetail> hostDetails) {
        Map<String, Object> map = new HashMap<>();
        for (HostDetail hostDetail : hostDetails) {
            map.put(hostDetail.getKey(), hostDetail.getValue());
        }
        return map;
    }

    /**
     * 服务器信息转map，固定信息加上可选信息
     *
     * @param hostInfo
     * @return
     */
    public static Map<String, String> hostInfo2Map(HostInfo hostInfo) {
        Map<String, String> map = new HashMap<>();
        map.put("hostIp", hostInfo.getHostIp());
        map.put("user", hostInfo.getUser());
        map.put("port", String.valueOf(hostInfo.getPort()));
        map.put("password", decryptDES(hostInfo.getPassword()));
        map.put("hostId", String.valueOf(hostInfo.getHostId()));
        String additionValue = hostInfo.getAdditionValue();
        if (StringUtils.isEmpty(additionValue)) {
            return map;
        }
        List<HostDetail> hostDetails = JSONObject.parseArray(additionValue, HostDetail.class);
        for (HostDetail hostDetail : hostDetails) {
            map.put(hostDetail.getKey(), hostDetail.getValue());
        }
        return map;
    }

    /**
     * 服务器信息转详情的list
     *
     * @param hostInfo
     * @return
     */
    public static List<HostDetail> hostInfo2HostDetailList(HostInfo hostInfo) {
        List<HostDetail> hostDetails = hostProp2HostDetails(hostInfo);
        String additionValue = hostInfo.getAdditionValue();
        if (StringUtils.isEmpty(additionValue) || "null".equals(additionValue)) {
            return hostDetails;
        }
        List<HostDetail> tempList = JSONObject.parseArray(additionValue, HostDetail.class);
        hostDetails.addAll(tempList);
        return hostDetails;
    }


    /**
     * map转服务器详情list
     *
     * @param param
     * @param hostId
     * @return
     */
    public static List<HostDetail> map2HostDetailList(Map<String, String> param, Long hostId) {
        List<HostDetail> hostDetails = new ArrayList<>();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            HostDetail hostDetail = new HostDetail();
            hostDetail.setHostId(hostId);
            hostDetail.setKey(mapKey);
            hostDetail.setValue(mapValue);
            hostDetails.add(hostDetail);
        }
        return hostDetails;
    }

    /**
     * 验证host是否能成功登录
     *
     * @param param
     * @return
     */
    public static boolean checkHost(Map<String, String> param) {
        String hostIp = param.get("hostIp");
        String user = param.get("user");
        String password = param.get("password");
        String port = param.get("port");
        Connection connection = null;
        if (port == null) {
            connection = new Connection(hostIp);
        } else {
            connection = new Connection(hostIp, Integer.parseInt(port));
        }
        try {
            connection.connect();
            return connection.authenticateWithPassword(user, password);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            connection.close();
        }
    }

    /**
     * 验证host是否能成功登录
     *
     * @param hostInfo
     * @return
     */
    public static boolean checkHost(HostInfo hostInfo) {
        String hostIp = hostInfo.getHostIp();
        String user = hostInfo.getUser();
        String password = hostInfo.getPassword();
        Integer port = hostInfo.getPort();
        Connection connection = null;
        if (port == null) {
            connection = new Connection(hostIp);
        } else {
            connection = new Connection(hostIp, port);
        }
        try {
            connection.connect();
            return connection.authenticateWithPassword(user, password);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            connection.close();
        }
    }

    /**
     * DES加密密码
     *
     * @param encryptString
     * @return
     */
    public static String encryptDES(String encryptString) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        DESKeySpec keySpec;
        byte[] encryptedData = null;
        try {
            keySpec = new DESKeySpec(KEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            encryptedData = cipher.doFinal(encryptString.getBytes());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encryptedData);
    }


    /**
     * DES解密
     *
     * @param decryptString
     * @return
     */
    public static String decryptDES(String decryptString) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] byteMi = decoder.decode(decryptString);
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        try {
            DESKeySpec keySpec = new DESKeySpec(KEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            // 真正开始解密操作
            String result = new String(cipher.doFinal(byteMi));
//            加密后的密码
//            System.out.println("加密后的密码： " + result);
            return result;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptString;
    }

    /**
     * 服务器基本信息转服务器详情list
     *
     * @param hostInfo
     * @return
     */
    public static List<HostDetail> hostProp2HostDetails(HostInfo hostInfo) {
        List<HostDetail> hostDetails = new ArrayList<>(4);
        HostDetail hostDetail1 = new HostDetail();
        hostDetail1.setKey("hostIp");
        hostDetail1.setValue(hostInfo.getHostIp());
        hostDetails.add(hostDetail1);
        HostDetail hostDetail2 = new HostDetail();
        hostDetail2.setKey("port");
        hostDetail2.setValue(String.valueOf(hostInfo.getPort()));
        hostDetails.add(hostDetail2);
        HostDetail hostDetail3 = new HostDetail();
        hostDetail3.setKey("user");
        hostDetail3.setValue(hostInfo.getUser());
        hostDetails.add(hostDetail3);
        HostDetail hostDetail4 = new HostDetail();
        hostDetail4.setKey("password");
        hostDetail4.setValue(decryptDES(hostInfo.getPassword()));
        hostDetails.add(hostDetail4);
        return hostDetails;
    }

}
