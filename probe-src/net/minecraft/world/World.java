package net.minecraft.world;
import java.util.*; import net.minecraft.block.Block; import net.minecraft.init.Blocks;
public class World {
public final Map<String,Block> blocks=new HashMap<>();
public int invalidWrites, solidOverwrites, stoneOverwrites;
public String key(int x,int y,int z){return x+","+y+","+z;}
public Block getBlock(int x,int y,int z){return blocks.getOrDefault(key(x,y,z),Blocks.air);}
public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
public boolean setBlock(int x,int y,int z,Block b,int m,int f){
 if(y<0||y>255){invalidWrites++;return false;}
 Block old=getBlock(x,y,z);
 if(old!=Blocks.air&&old!=Blocks.log&&old!=Blocks.leaves&&b==Blocks.log)solidOverwrites++;
 if(old==Blocks.stone&&b==Blocks.log)stoneOverwrites++;
 blocks.put(key(x,y,z),b); return old!=b;
}
public int getHeightValue(int x,int z){for(int y=255;y>=0;y--)if(!isAirBlock(x,y,z))return y+1;return 0;}
}