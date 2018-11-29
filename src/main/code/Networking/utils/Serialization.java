package main.code.Networking.utils;

import java.io.*;
import java.nio.ByteBuffer;

public final class Serialization {
    public static byte[] shortToByte(short number){
        return new byte[] { (byte)((number & 0xFF00) >> 8),(byte)(number & 0x00FF)};
    }
    public static short byteToShort(byte[] data) {
        return (short) (((data[0] << 8)) | ((data[1] & 0x00FF)));
    }
    public static void main(String[] args){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter something:");
        try {
            String input = br.readLine();
            short number = Short.parseShort(input);
            byte[] numberBytes = shortToByte(number);
            number = byteToShort(numberBytes);
            System.out.println(number);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static byte[] doubleToByte(double x){
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(x);
        return bytes;
    }
    public static double bytesToDouble(byte[] bytes){
        return ByteBuffer.wrap(bytes).getDouble();
    }
    public static byte[] concat(short size,byte[]... args) {
        byte[] toReturn = new byte[size];
        short i = 0;
        for(byte[] arr:args){
            for(byte b : arr){
                toReturn[i] = b;
                i++;
            }
        }
        return toReturn;
    }
    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
    public static int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
