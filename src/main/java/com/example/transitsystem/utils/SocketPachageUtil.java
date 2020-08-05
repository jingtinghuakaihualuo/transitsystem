package com.example.transitsystem.utils;

public class SocketPachageUtil {

    public final static int HEADLENGTH = 4;
    /**
     * 组包
     * @param str
     * @return
     */
    public static byte[] builderSendBytes(String str) {
        byte[] bytes = str.getBytes();
        int len = bytes.length;
        byte[] headByte = int2Bytes(len, true);
        byte[] newBytes = new byte[len + 4];
        System.arraycopy(headByte,0, newBytes, 0 , 4);
        System.arraycopy(bytes,0, newBytes,4,len);
        return newBytes;
    }

    public static byte[] int2Bytes(int i, boolean reverse) {
        int v = i;
        if(reverse == true){
            v  = Integer.reverseBytes(i);
        }
        return new byte[] { (byte)v, (byte) (v>>>8), (byte) (v>>>16), (byte) (v>>>24) };
    }

    public static int bytes2Int(byte[] i, boolean reverse){
        int result = (((int)i[3]) & 0xFF) << 24 | (((int)i[2]) & 0xFF) << 16 | (((int)i[1]) & 0xFF) << 8 | (((int)i[0]) & 0xFF);
        if(reverse == true){
            result  = Integer.reverseBytes(result);
        }
        return result;
    }

}
