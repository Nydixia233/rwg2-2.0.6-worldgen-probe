package rwg2.util;
public class ContinentalNoise {
public static float unpackVolcanoX(long c){return Float.intBitsToFloat((int)(c>>>32));}
public static float unpackVolcanoY(long c){return Float.intBitsToFloat((int)c);}}